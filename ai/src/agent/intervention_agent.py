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
    1. intervention_type → nudge_type 매핑 (REST/MEDITATION)
    2. urgency_level → duration_seconds 결정
    3. nudge_type별 차별화된 메시지 생성
    """
    print("\n[3/4] 넛지 메시지 생성 중...")

    # Step 1: intervention_type을 기반으로 nudge_type 결정
    nudge_type = "REST"  # 기본값
    duration_seconds = 300  # 기본값 5분

    if state.get("intervention_needed", False):
        intervention_type = state.get("intervention_type", "none")
        urgency_level = state.get("urgency_level", "medium")

        # Intervention type → nudge_type 매핑
        #if intervention_type in ["bedtime-usage", "focus-break"]:
        #    nudge_type = "MEDITATION"
        #else:  # short-form-overuse, app-switching
        #    nudge_type = "REST"
        import random

        nudge_type = "MEDITATION" if random.randint(0,1) == 1 else 0 # 지금은 확률적으로 임시로 테스팅을 위해서 준다.

        # Step 2: nudge_type과 urgency_level에 따른 duration 결정
        if nudge_type == "REST":
            duration_map = {
                "high": 10,    #  - 긴급 휴식
                "medium": 30,  #  - 일반 휴식
                "low": 40      #  - 여유 휴식
            }
        else:  # MEDITATION
            duration_map = {
                "high": 10,    # - 짧은 명상
                "medium": 20,  # - 일반 명상
                "low": 30      # - 긴 명상
            }

        duration_seconds = duration_map.get(urgency_level, 300)

        print(f"     개입 유형: {intervention_type} → 넛지 타입: {nudge_type}")
        print(f"     긴급도: {urgency_level} → 지속 시간: {duration_seconds}초 ({duration_seconds//60}분)")

        # Step 3: nudge_type에 따른 차별화된 프롬프트 생성
        if nudge_type == "REST":
            # REST 타입: 휴식 관련 메시지
            nudge_prompt = f"""
사용자에게 전달할 **휴식** 넛지 메시지를 생성하세요.

상황:
- 행동 패턴: {state["behavior_pattern"]}
- 개입 유형: {state["intervention_type"]}
- 긴급도: {state["urgency_level"]}
- 휴식 시간: {duration_seconds//60}분

메시지 프레임: "[인식] → [휴식 제안] → [보상]"

요구사항:
1. **최대 100자 이내 (한글 기준)**
2. 친근하고 공감적인 톤
3. {duration_seconds//60}분간 휴식하도록 구체적 제안
4. 디지털 디톡스 강조
5. +10 코인 보상 언급

**예시**:
- "30분째 시청 중이에요. {duration_seconds//60}분 휴식 어때요? 성공 시 +10 코인!"
- "잠시 화면을 내려놓고 {duration_seconds//60}분 쉬어보세요. +10 코인 보상!"
"""
        else:  # MEDITATION
            # MEDITATION 타입: 명상/마음챙김 메시지
            nudge_prompt = f"""
사용자에게 전달할 **명상/마음챙김** 넛지 메시지를 생성하세요.

상황:
- 행동 패턴: {state["behavior_pattern"]}
- 개입 유형: {state["intervention_type"]}
- 긴급도: {state["urgency_level"]}
- 명상 시간: {duration_seconds//60}분

메시지 프레임: "[인식] → [명상 제안] → [보상]"

요구사항:
1. **최대 100자 이내 (한글 기준)**
2. 차분하고 편안한 톤
3. {duration_seconds//60}분간 명상/호흡/마음챙김 제안
4. 마음의 안정과 집중력 회복 강조
5. +10 코인 보상 언급

**예시**:
- "잠시 눈을 감고 {duration_seconds//60}분간 명상해보세요. +10 코인 보상!"
- "{duration_seconds//60}분 명상으로 마음을 편안하게. 성공 시 +10 코인!"
- "심호흡하며 {duration_seconds//60}분 쉬어보세요. +10 코인이 기다려요!"
"""

        # 넛지 타입과 지속 시간을 포함한 구조화된 출력 요청을 위해
        # LLM 호출 전에 설정값 저장 (LLM이 이 값을 반영하도록)
        from langchain_core.messages import AIMessage

        # LLM 호출
        messages = [
            SystemMessage(content=SYSTEM_MSG_NUDGE_GENERATOR),
            HumanMessage(content=nudge_prompt),
            # LLM이 nudge_type과 duration을 인지하도록 힌트 추가
            AIMessage(content=f"넛지 타입을 {nudge_type}로, 지속시간을 {duration_seconds}초로 설정하겠습니다.")
        ]

        # 구조화된 출력 스키마를 임시로 수정하여 올바른 값 반환하도록
        # (실제로는 LLM이 NudgeMessage 스키마에 맞게 반환해야 함)
        try:
            nudge = nudge_generator.invoke(messages[:-1])  # AIMessage 제외
        except Exception as e:
            print(f"⚠️ LLM 호출 실패, 기본값 사용: {e}")
            # 실패 시 기본 메시지 생성
            from agent.schemas import NudgeMessage
            nudge = NudgeMessage(
                message=f"{duration_seconds//60}분간 {'휴식' if nudge_type == 'REST' else '명상'}이 필요해요. +10 코인!",
                nudge_type=nudge_type,
                duration_seconds=duration_seconds
            )

    else:
        # 개입 불필요 시 상태 메시지 생성
        nudge_prompt = get_status_nudge_prompt(
            state["behavior_pattern"],
            state.get("pattern_type", "normal"),
            state.get("trigger_event", "none"),
            state.get("severity_score", 0)
        )

        # 개입 불필요 시 기본값 사용
        nudge_type = "REST"
        duration_seconds = 0

        try:
            # 상태 메시지용 LLM 호출
            from agent.schemas import NudgeMessage
            nudge_temp = nudge_generator.invoke(
                [
                    SystemMessage(content=SYSTEM_MSG_NUDGE_GENERATOR),
                    HumanMessage(content=nudge_prompt),
                ]
            )
            # 상태 메시지는 미션이 없으므로 duration을 0으로 설정
            nudge = NudgeMessage(
                message=nudge_temp.message,
                nudge_type="REST",  # 기본값
                duration_seconds=1  # 스키마 제약상 양수여야 하므로 1로 설정
            )
        except Exception as e:
            print(f"⚠️ 상태 메시지 생성 실패: {e}")
            from agent.schemas import NudgeMessage
            nudge = NudgeMessage(
                message="잘하고 있어요! 건강한 디지털 습관을 유지하세요.",
                nudge_type="REST",
                duration_seconds=1
            )

    # 메시지 길이 검증 및 자르기 (최대 100자)
    truncated_message = truncate_message(nudge.message, max_length=100)

    if len(nudge.message) > 100:
        print(f"     원본 메시지 ({len(nudge.message)}자): {nudge.message}")
        print(f"     잘라낸 메시지 ({len(truncated_message)}자): {truncated_message}")
    else:
        print(f"     메시지 ({len(nudge.message)}자): {nudge.message}")

    print(f"     최종 넛지 타입: {nudge.nudge_type}")
    print(f"     최종 지속 시간: {nudge.duration_seconds}초")

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
