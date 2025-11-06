"""실시간 개입 에이전트 (Intervention Agent)
- 행동 패턴 분석
- 개입 필요성 판단
- 넛지 메시지 생성 및 발송.
"""

from datetime import datetime
from typing import Literal

from langchain_core.messages import HumanMessage, SystemMessage
from langgraph.graph import END, START, StateGraph
from langgraph.types import Command

from agent.prompts import (
    SYSTEM_MSG_BEHAVIOR_ANALYZER,
    SYSTEM_MSG_INTERVENTION_DECIDER,
    SYSTEM_MSG_NUDGE_GENERATOR,
    get_behavior_analysis_prompt,
    get_intervention_decision_prompt,
    get_nudge_generation_prompt,
    get_status_nudge_prompt,
)
from agent.schemas import InterventionState
from agent.utils import (
    behavior_analyzer,
    get_current_timestamp,
    get_time_slot_from_timestamp,
    intervention_decider,
    nudge_generator,
    schedule_evaluation,
    send_fcm_notification,
    simulate_behavior_log,
    truncate_message,
)

# =============================================================================
# Intervention Agent Nodes
# =============================================================================


def analyze_behavior(state: InterventionState) -> dict:
    """1단계: 행동 패턴 분석
    사용자의 앱 사용 로그를 분석하여 패턴 파악.
    """
    print("\n[1/4] 행동 패턴 분석 중...")

    # Validate behavior_log is present
    if "behavior_log" not in state or state.get("behavior_log") is None:
        raise ValueError("behavior_log is required for analyze_behavior")

    behavior_log = state["behavior_log"]

    # usage_timestamp에서 time_slot 계산
    time_slot = get_time_slot_from_timestamp(behavior_log["usage_timestamp"])

    # LLM을 사용한 행동 패턴 분석
    analysis_prompt = get_behavior_analysis_prompt(behavior_log, time_slot)

    analysis = behavior_analyzer.invoke(
        [
            SystemMessage(content=SYSTEM_MSG_BEHAVIOR_ANALYZER),
            HumanMessage(content=analysis_prompt),
        ]
    )

    print(f"     패턴 유형: {analysis.pattern_type}")
    print(f"     트리거 이벤트: {analysis.trigger_event}")
    print(f"     심각도: {analysis.severity_score}")

    return {
        "behavior_pattern": analysis.summary,
        "pattern_type": analysis.pattern_type,
        "trigger_event": analysis.trigger_event,
        "severity_score": analysis.severity_score,
        "key_indicators": analysis.key_indicators,
        "timestamp": get_current_timestamp(),
    }


def decide_intervention(state: InterventionState) -> Command[Literal["generate_nudge"]]:
    """2단계: 개입 필요성 판단 - ALWAYS route to generate_nudge"""
    print("\n[2/4] 개입 필요성 판단 중...")

    decision_prompt = get_intervention_decision_prompt(
        state["behavior_pattern"], state["trigger_event"]
    )

    decision = intervention_decider.invoke(
        [
            SystemMessage(content=SYSTEM_MSG_INTERVENTION_DECIDER),
            HumanMessage(content=decision_prompt),
        ]
    )

    print(f"     개입 필요: {decision.intervention_needed}")
    print(f"     개입 유형: {decision.intervention_type}")
    print(f"     긴급도: {decision.urgency_level}")

    # 항상 generate_nudge로 라우팅 (조건 제거)
    return Command(
        update={
            "intervention_needed": decision.intervention_needed,
            "intervention_type": decision.intervention_type,
            "urgency_level": decision.urgency_level,
        },
        goto="generate_nudge",
    )


def generate_nudge(state: InterventionState) -> dict:
    """3단계: 넛지 메시지 생성 - Always executed"""
    print("\n[3/4] 넛지 메시지 생성 중...")

    # intervention_needed에 따라 다른 프롬프트 사용
    if state.get("intervention_needed", False):
        nudge_prompt = get_nudge_generation_prompt(
            state["behavior_pattern"],
            state["intervention_type"],
            state["urgency_level"],
        )
    else:
        # 개입 불필요 시 이유를 포함한 상태 메시지 생성
        nudge_prompt = get_status_nudge_prompt(
            state["behavior_pattern"],
            state.get("pattern_type", "normal"),
            state.get("trigger_event", "none"),
            state.get("severity_score", 0)
        )

    nudge = nudge_generator.invoke(
        [
            SystemMessage(content=SYSTEM_MSG_NUDGE_GENERATOR),
            HumanMessage(content=nudge_prompt),
        ]
    )

    # 메시지 길이 검증 및 자르기 (최대 100자)
    truncated_message = truncate_message(nudge.message, max_length=100)

    if len(nudge.message) > 100:
        print(f"     원본 메시지 ({len(nudge.message)}자): {nudge.message}")
        print(f"     잘라낸 메시지 ({len(truncated_message)}자): {truncated_message}")
    else:
        print(f"     메시지 ({len(nudge.message)}자): {nudge.message}")

    return {"nudge_message": truncated_message, "nudge_type": nudge.nudge_type}


def send_intervention(state: InterventionState) -> dict:
    """4단계: 개입 실행 - ALWAYS send FCM"""
    print("\n[4/4] 개입 실행 및 평가 스케줄링 중...")

    intervention_time = get_current_timestamp()

    # 실제 Spring 서버 호출
    intervention_id_str = send_fcm_notification(state)

    if intervention_id_str is None:
        intervention_id_str = f"INT_FAILED_{int(datetime.now().timestamp())}"
        print("⚠️ FCM send failed, using fallback ID")

    intervention_id = hash(intervention_id_str) % (10**8)

    # 평가 스케줄은 intervention_needed=true일 때만
    delay_minutes = None
    eval_scheduled_time = None

    if state.get("intervention_needed", False):
        if state["urgency_level"] == "high":
            delay_minutes = 30
        elif state["urgency_level"] == "medium":
            delay_minutes = 45
        else:
            delay_minutes = 60

        eval_scheduled_time = schedule_evaluation(intervention_time, delay_minutes)
        print(f"     평가 예정: {eval_scheduled_time} ({delay_minutes}분 후)")
    else:
        print(f"     평가 스케줄 안 함 (개입 불필요)")

    print(f"     개입 ID: {intervention_id_str}")
    print(f"     ✅ FCM 알림 전송 완료")

    return {
        "intervention_time": intervention_time,
        "intervention_id": intervention_id,
        "intervention_id_str": intervention_id_str,
        "evaluation_scheduled_time": eval_scheduled_time,
        "evaluation_delay_minutes": delay_minutes,
    }


# =============================================================================
# Intervention Agent Graph 구성
# =============================================================================


def build_intervention_agent() -> StateGraph:
    """실시간 개입 에이전트 그래프 구성"""
    workflow = StateGraph(InterventionState)

    # 노드 추가
    workflow.add_node("analyze_behavior", analyze_behavior)
    workflow.add_node("decide_intervention", decide_intervention)
    workflow.add_node("generate_nudge", generate_nudge)
    workflow.add_node("send_intervention", send_intervention)

    # 엣지 추가
    workflow.add_edge(START, "analyze_behavior")
    workflow.add_edge("analyze_behavior", "decide_intervention")
    # decide_intervention에서 조건부 라우팅 (Command 사용)
    workflow.add_edge("generate_nudge", "send_intervention")
    workflow.add_edge("send_intervention", END)

    # 컴파일 (LangGraph Server가 자동으로 checkpointer 관리)
    return workflow.compile()


# 그래프 생성
intervention_agent = build_intervention_agent()

print("Intervention Agent 그래프 구성 완료")


# =============================================================================
# 테스트 실행 함수
# =============================================================================


def test_intervention_agent(user_id: int = 1):
    """개입 에이전트 테스트"""
    print("=" * 60)
    print("디토 개입 에이전트 테스트 시작")
    print("=" * 60)

    # 초기 상태
    initial_state = {
        "user_id": user_id,
        "behavior_log": simulate_behavior_log(user_id),
    }

    # 그래프 실행
    config = {"configurable": {"thread_id": f"user_{user_id}"}}
    result = intervention_agent.invoke(initial_state, config)

    print("\n" + "=" * 60)
    print("실행 결과")
    print("=" * 60)
    print(f"개입 필요: {result.get('intervention_needed', False)}")

    if result.get("intervention_needed"):
        print(f"개입 유형: {result['intervention_type']}")
        print(f"넛지 메시지: {result['nudge_message']}")
        print(f"개입 시간: {result['intervention_time']}")
        print(f"평가 예정: {result['evaluation_scheduled_time']}")

    print("=" * 60)

    return result


if __name__ == "__main__":
    # 테스트 실행
    result = test_intervention_agent(user_id=1)
