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
    """3단계: 넛지 메시지 생성 - Always executed

    역할:
    LLM이 상황에 따라 적절한 넛지를 생성:
    1. nudge_type (REST/MEDITATION) 선택
    2. duration_seconds 결정
    3. 공감적인 메시지 생성
    """
    print("\n[3/4] 넛지 메시지 생성 중...")

    if state.get("intervention_needed", False):
        intervention_type = state.get("intervention_type", "none")
        urgency_level = state.get("urgency_level", "medium")

        print(f"     개입 유형: {intervention_type}, 긴급도: {urgency_level}")

        # LLM이 nudge_type과 duration_seconds를 결정하도록 프롬프트 작성
        nudge_prompt = f"""
사용자에게 전달할 넛지 메시지를 생성하세요.

상황:
- 행동 패턴: {state["behavior_pattern"]}
- 개입 유형: {state["intervention_type"]}
- 긴급도: {state["urgency_level"]}

당신의 역할:
1. **넛지 타입 선택**: REST (휴식) 또는 MEDITATION (명상) 중 하나
2. **미션 시간 결정**: 10초~900초 (적절한 시간 선택)
3. **공감적 메시지 생성**: 최대 100자 이내

선택 가이드:
- **REST 추천**: 숏폼 과다 사용, 앱 전환 과다 → 디지털 디톡스 휴식
  - short-form-overuse: 600초(10분)
  - app-switching: 600초(10분)

- **MEDITATION 추천**: 취침 전 사용, 집중 후 휴식 → 명상/마음챙김
  - bedtime-usage: 300초(5분)
  - focus-break: 300초(5분)

긴급도별 시간 조정:
- high: 10~30초 (긴급, 짧은 개입)
- medium: 300~600초 (일반, 5~10분)
- low: 600~900초 (여유, 10~15분)

메시지 프레임: "[인식] → [제안] → [보상]"
예시: "30분째 시청 중이에요 → 10분 휴식 어때요? → 성공 시 +10 코인!"

요구사항:
1. 최대 100자 이내
2. 친근하고 공감적인 톤
3. +10 코인 보상 언급
"""

        # LLM 호출 - NudgeMessage 구조화 출력 (message, nudge_type, duration_seconds)
        try:
            nudge = nudge_generator.invoke(
                [
                    SystemMessage(content=SYSTEM_MSG_NUDGE_GENERATOR),
                    HumanMessage(content=nudge_prompt),
                ]
            )
        except Exception as e:
            print(f"⚠️ LLM 호출 실패, 기본값 사용: {e}")
            # 실패 시 기본 메시지 생성
            from agent.schemas import NudgeMessage
            nudge = NudgeMessage(
                message="잠시 휴식이 필요해요. 성공 시 +10 코인!",
                nudge_type="REST",
                duration_seconds=300
            )

    else:
        # 개입 불필요 시 상태 메시지 생성
        print(f"     개입 불필요 - 상태 메시지 생성")

        nudge_prompt = get_status_nudge_prompt(
            state["behavior_pattern"],
            state.get("pattern_type", "normal"),
            state.get("trigger_event", "none"),
            state.get("severity_score", 0)
        )

        try:
            # 상태 메시지용 LLM 호출 - LLM이 모든 필드 생성
            nudge = nudge_generator.invoke(
                [
                    SystemMessage(content=SYSTEM_MSG_NUDGE_GENERATOR),
                    HumanMessage(content=nudge_prompt),
                ]
            )
        except Exception as e:
            print(f"⚠️ 상태 메시지 생성 실패: {e}")
            from agent.schemas import NudgeMessage
            nudge = NudgeMessage(
                message="잘하고 있어요! 건강한 디지털 습관을 유지하세요.",
                nudge_type="REST",
                duration_seconds=1  # 스키마 제약상 양수
            )

    # 메시지 길이 검증 및 자르기 (최대 100자)
    truncated_message = truncate_message(nudge.message, max_length=100)

    if len(nudge.message) > 100:
        print(f"     원본 메시지 ({len(nudge.message)}자): {nudge.message}")
        print(f"     잘라낸 메시지 ({len(truncated_message)}자): {truncated_message}")
    else:
        print(f"     메시지 ({len(nudge.message)}자): {nudge.message}")

    print(f"     LLM 선택 넛지 타입: {nudge.nudge_type}")
    print(f"     LLM 선택 지속 시간: {nudge.duration_seconds}초 ({nudge.duration_seconds//60}분 {nudge.duration_seconds%60}초)")

    # LLM이 결정한 값을 그대로 반환
    return {
        "nudge_message": truncated_message,
        "nudge_type": nudge.nudge_type,
        "duration_seconds": nudge.duration_seconds
    }


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
        # intervention_type에 따라 평가 지연 시간 결정
        intervention_type = state.get("intervention_type", "none")
        delay_map = {
            "short-form-overuse": 60,  # 숏폼 과다: 60분 후 평가 (긴 행동 패턴)
            "bedtime-usage": 30,        # 취침 전 사용: 30분 후 평가 (짧은 개입)
            "focus-break": 45,          # 집중 후 휴식: 45분 후 평가 (중간)
            "app-switching": 60,        # 앱 전환 과다: 60분 후 평가 (긴 행동 패턴)
            "none": 45                  # 기본값: 45분
        }
        delay_minutes = delay_map.get(intervention_type, 45)

        eval_scheduled_time = schedule_evaluation(intervention_time, delay_minutes)
        print(f"     평가 예정: {eval_scheduled_time} ({delay_minutes}분 후, 개입 유형: {intervention_type})")
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
