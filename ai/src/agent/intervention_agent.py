"""실시간 개입 에이전트 (Intervention Agent)
- 행동 패턴 분석
- 개입 필요성 판단
- 넛지 메시지 생성 및 발송
"""

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
)
from agent.schemas import InterventionState
from agent.utils import (
    behavior_analyzer,
    get_current_timestamp,
    get_time_slot_from_timestamp,
    intervention_decider,
    nudge_generator,
    save_intervention_to_db,
    schedule_evaluation,
    simulate_behavior_log,
)

# =============================================================================
# Intervention Agent Nodes
# =============================================================================

def analyze_behavior(state: InterventionState) -> dict:
    """1단계: 행동 패턴 분석
    사용자의 앱 사용 로그를 분석하여 패턴 파악
    """
    print("\n[1/4] 행동 패턴 분석 중...")

    # Validate behavior_log is present
    if "behavior_log" not in state or state.get("behavior_log") is None:
        raise ValueError("behavior_log is required for analyze_behavior")

    behavior_log = state["behavior_log"]

    # usage_timestamp에서 time_slot 계산
    time_slot = get_time_slot_from_timestamp(behavior_log['usage_timestamp'])

    # LLM을 사용한 행동 패턴 분석
    analysis_prompt = get_behavior_analysis_prompt(behavior_log, time_slot)

    analysis = behavior_analyzer.invoke([
        SystemMessage(content=SYSTEM_MSG_BEHAVIOR_ANALYZER),
        HumanMessage(content=analysis_prompt)
    ])

    print(f"     패턴 유형: {analysis.pattern_type}")
    print(f"     트리거 이벤트: {analysis.trigger_event}")
    print(f"     심각도: {analysis.severity_score}")

    return {
        "behavior_pattern": analysis.summary,
        "pattern_type": analysis.pattern_type,
        "trigger_event": analysis.trigger_event,
        "severity_score": analysis.severity_score,
        "key_indicators": analysis.key_indicators,
        "timestamp": get_current_timestamp()
    }


def decide_intervention(state: InterventionState) -> Command[Literal["generate_nudge", "__end__"]]:
    """2단계: 개입 필요성 판단
    분석된 패턴을 바탕으로 개입 여부와 유형 결정
    """
    print("\n[2/4] 개입 필요성 판단 중...")

    decision_prompt = get_intervention_decision_prompt(
        state['behavior_pattern'],
        state['trigger_event']
    )

    decision = intervention_decider.invoke([
        SystemMessage(content=SYSTEM_MSG_INTERVENTION_DECIDER),
        HumanMessage(content=decision_prompt)
    ])
    
    print(f"     개입 필요: {decision.intervention_needed}")
    print(f"     개입 유형: {decision.intervention_type}")
    print(f"     긴급도: {decision.urgency_level}")
    
    # 개입이 필요한 경우 넛지 생성으로, 아니면 종료
    if decision.intervention_needed:
        return Command(
            update={
                "intervention_needed": True,
                "intervention_type": decision.intervention_type,
                "urgency_level": decision.urgency_level
            },
            goto="generate_nudge"
        )
    else:
        return Command(
            update={"intervention_needed": False},
            goto=END
        )


def generate_nudge(state: InterventionState) -> dict:
    """3단계: 넛지 메시지 생성
    개입 유형에 맞는 맞춤형 넛지 메시지 생성
    """
    print("\n[3/4] 넛지 메시지 생성 중...")

    nudge_prompt = get_nudge_generation_prompt(
        state['behavior_pattern'],
        state['intervention_type'],
        state['urgency_level']
    )

    nudge = nudge_generator.invoke([
        SystemMessage(content=SYSTEM_MSG_NUDGE_GENERATOR),
        HumanMessage(content=nudge_prompt)
    ])
    
    print(f"     메시지: {nudge.message}")
    
    return {
        "nudge_message": nudge.message,
        "nudge_type": nudge.nudge_type
    }


def send_intervention(state: InterventionState) -> dict:
    """4단계: 개입 실행 및 평가 스케줄링
    넛지를 전송하고, N분 후 평가를 스케줄링
    """
    print("\n[4/4] 개입 실행 및 평가 스케줄링 중...")
    
    # 현재 시간
    intervention_time = get_current_timestamp()
    
    # DB에 저장 (실제로는 missions 테이블에)
    intervention_id = save_intervention_to_db(state)
    
    # 평가 지연 시간 결정 (개입 유형에 따라)
    # 중재 이론: 행동효과 지속시간 30-45분
    if state["urgency_level"] == "high":
        delay_minutes = 30
    elif state["urgency_level"] == "medium":
        delay_minutes = 45
    else:
        delay_minutes = 60
    
    # 평가 스케줄 시간 계산
    eval_scheduled_time = schedule_evaluation(intervention_time, delay_minutes)
    
    print(f"     개입 ID: {intervention_id}")
    print(f"     개입 시간: {intervention_time}")
    print(f"     평가 예정 시간: {eval_scheduled_time} (약 {delay_minutes}분 후)")
    print(f"     ✅ 넛지 전송 완료: '{state['nudge_message']}'")
    
    # 실제 환경에서는 여기서 푸시 알림 전송
    # send_push_notification(state["user_id"], state["nudge_message"])
    
    # 실제 환경에서는 여기서 평가 작업을 스케줄링
    # schedule_evaluation_job(intervention_id, eval_scheduled_time)
    
    return {
        "intervention_time": intervention_time,
        "intervention_id": intervention_id,
        "evaluation_scheduled_time": eval_scheduled_time,
        "evaluation_delay_minutes": delay_minutes
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
    print("="*60)
    print("디토 개입 에이전트 테스트 시작")
    print("="*60)
    
    # 초기 상태
    initial_state = {
        "user_id": user_id,
        "behavior_log": simulate_behavior_log(user_id),
    }
    
    # 그래프 실행
    config = {"configurable": {"thread_id": f"user_{user_id}"}}
    result = intervention_agent.invoke(initial_state, config)
    
    print("\n" + "="*60)
    print("실행 결과")
    print("="*60)
    print(f"개입 필요: {result.get('intervention_needed', False)}")
    
    if result.get('intervention_needed'):
        print(f"개입 유형: {result['intervention_type']}")
        print(f"넛지 메시지: {result['nudge_message']}")
        print(f"개입 시간: {result['intervention_time']}")
        print(f"평가 예정: {result['evaluation_scheduled_time']}")
    
    print("="*60)
    
    return result


if __name__ == "__main__":
    # 테스트 실행
    result = test_intervention_agent(user_id=1)
