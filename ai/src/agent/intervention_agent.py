"""FCM 테스트용 개입 에이전트 (Intervention Agent - Test Version).

- 미션 생성 및 FCM 전송만 수행
- 무조건 mission_id 생성
"""

from datetime import datetime

from langgraph.graph import END, START, StateGraph

from agent.schemas import InterventionState
from agent.utils import (
    get_current_timestamp,
    send_fcm_notification,
    simulate_behavior_log,
)

# =============================================================================
# Intervention Agent Node (단일 노드)
# =============================================================================


def send_intervention(state: InterventionState) -> dict:
    """FCM 테스트용 개입 실행 노드.

    - 고정된 넛지 메시지로 미션 생성
    - FCM 알림 전송
    - mission_id 무조건 생성
    """
    print("\n[FCM 테스트] 미션 생성 및 FCM 전송 중...")

    intervention_time = get_current_timestamp()

    import random

    test_state = {}
    if random.randint(0, 1) == 1:
        # State에 테스트용 고정값 설정
        test_state = {
            **state,
            "intervention_needed": True,  # 무조건 True
            "intervention_type": "short-form-overuse",  # 고정 타입
            "nudge_message": "잠시 휴식이 필요해요. 10분 휴식하고 +10 코인 받으세요!",
            "nudge_type": "REST",  # REST 또는 MEDITATION
            "duration_seconds": 10,  # 10분
            "behavior_pattern": "테스트용 행동 패턴",
            "pattern_type": "overuse",
            "trigger_event": "test-trigger",
            "severity_score": 7,
        }
    else:
        # State에 테스트용 고정값 설정
        test_state = {
            **state,
            "intervention_needed": True,  # 무조건 True
            "intervention_type": "short-form-overuse",  # 고정 타입
            "nudge_message": "잠시 휴식이 필요해요. 10분 휴식하고 +10 코인 받으세요!",
            "nudge_type": "MEDITATION",  # REST 또는 MEDITATION
            "duration_seconds": 10,  # 10분
            "behavior_pattern": "테스트용 행동 패턴",
            "pattern_type": "overuse",
            "trigger_event": "test-trigger",
            "severity_score": 7,
        }

    print(f"     메시지: {test_state['nudge_message']}")
    print(f"     타입: {test_state['nudge_type']}")
    print(f"     시간: {test_state['duration_seconds']}초")

    # FCM 전송 (미션 생성 포함)
    intervention_id_str = send_fcm_notification(test_state)

    if intervention_id_str is None:
        intervention_id_str = f"INT_FAILED_{int(datetime.now().timestamp())}"
        print("⚠️ FCM send failed, using fallback ID")
    else:
        print(f"     ✅ mission_id: {intervention_id_str}")
        print(f"     ✅ FCM 알림 전송 완료")

    intervention_id = hash(intervention_id_str) % (10**8)

    return {
        "intervention_time": intervention_time,
        "intervention_id": intervention_id,
        "intervention_id_str": intervention_id_str,
        "intervention_needed": True,
        "intervention_type": "short-form-overuse",
        "nudge_message": test_state["nudge_message"],
        "nudge_type": test_state["nudge_type"],
        "duration_seconds": test_state["duration_seconds"],
    }


# =============================================================================
# Intervention Agent Graph 구성 (단일 노드)
# =============================================================================


def build_intervention_agent() -> StateGraph:
    """FCM 테스트용 개입 에이전트 그래프 구성 (단일 노드)."""
    workflow = StateGraph(InterventionState)

    # 단일 노드만 추가
    workflow.add_node("send_intervention", send_intervention)

    # 단순 엣지: START → send_intervention → END
    workflow.add_edge(START, "send_intervention")
    workflow.add_edge("send_intervention", END)

    # 컴파일 (LangGraph Server가 자동으로 checkpointer 관리)
    return workflow.compile()


# 그래프 생성
intervention_agent = build_intervention_agent()

print("FCM 테스트용 Intervention Agent 그래프 구성 완료 (단일 노드)")


# =============================================================================
# 테스트 실행 함수
# =============================================================================


def test_intervention_agent(user_id: int = 1):
    """FCM 테스트용 개입 에이전트 테스트."""
    print("=" * 60)
    print("FCM 테스트용 디토 개입 에이전트 시작")
    print("=" * 60)

    # 초기 상태 (behavior_log는 simulate로 생성)
    initial_state = {
        "user_id": user_id,
        "behavior_log": simulate_behavior_log(user_id),
    }

    # 그래프 실행
    config = {"configurable": {"thread_id": f"test_user_{user_id}"}}
    result = intervention_agent.invoke(initial_state, config)

    print("\n" + "=" * 60)
    print("실행 결과")
    print("=" * 60)
    print(f"개입 필요: {result.get('intervention_needed', False)}")
    print(f"개입 유형: {result.get('intervention_type', 'N/A')}")
    print(f"넛지 메시지: {result.get('nudge_message', 'N/A')}")
    print(f"개입 시간: {result.get('intervention_time', 'N/A')}")
    print(f"mission_id: {result.get('intervention_id_str', 'N/A')}")
    print("=" * 60)

    return result


if __name__ == "__main__":
    # 테스트 실행
    result = test_intervention_agent(user_id=1)
