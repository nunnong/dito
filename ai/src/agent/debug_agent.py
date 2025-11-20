"""디버그/알림 에이전트 (Debug/Notification Agent).

사용자 또는 Spring 서버로부터 받은 REST 요청을 통해 미션과 FCM 알림을 직접 생성합니다.

주요 기능:
- REST 요청 데이터로부터 미션 생성 (Spring 백엔드 API 호출)
- 넛지 메시지 생성 및 FCM 알림 발송
- 행동 분석 없이 바로 미션/알림 생성 (테스트 및 수동 트리거 용도)

워크플로우: mission_node → message_node
"""

from langgraph.graph import END, START, StateGraph

from agent.schemas import DebugState, MissionData
from agent.utils import (
    create_mission,
    send_fcm_with_mission,
)


def make_mission_data(state: DebugState) -> MissionData:
    """Mission data 생성."""
    mission_data = state["mission_info"].copy()

    # mission_info에 mission_text가 없으면 nudge_message 사용
    if "mission_text" not in mission_data:
        mission_data["mission_text"] = state["nudge_message"]

    mission_data["user_id"] = state["user_id"]

    return MissionData(**mission_data)


def mission_node(state: DebugState) -> dict:
    """1단계: 미션 생성.

    역할:
    1. LLM이 상황에 따라 적절한 mission_type (REST/MEDITATION)과 duration_seconds 결정
    2. Spring 백엔드 /api/mission API 호출하여 실제 미션 생성
    3. 생성된 mission_id를 state에 저장
    """
    # 필수 필드 검증
    required_fields = ["user_id", "mission_info", "nudge_message"]
    missing_fields = [
        field
        for field in required_fields
        if field not in state or state.get(field) is None
    ]
    if missing_fields:
        raise ValueError(
            f"Missing required fields in state: {', '.join(missing_fields)}"
        )

    mission_data = make_mission_data(state)

    # API 호출하여 미션 생성
    mission_id = create_mission(mission_data)

    # 생성된 미션 정보를 state에 저장
    return {
        "mission_id": int(mission_id) if mission_id else None,
    }


def message_node(state: DebugState) -> dict:
    """2단계: 넛지 메시지 생성 및 FCM 전송.

    역할:
    - FCM 알림 전송
    """
    # FCM 전송
    fcm_sent = send_fcm_with_mission(
        state["user_id"], str(state["mission_id"]), state["nudge_message"]
    )

    # 결과 처리: nudge_message와 fcm_sent를 모두 반환
    return {"fcm_sent": fcm_sent}


# =============================================================================
# Intervention Agent Graph 구성
# =============================================================================


def build_debug_agent() -> StateGraph:
    """디버그/알림 에이전트 그래프 구성.

    REST 요청으로부터 받은 데이터를 사용하여 미션과 FCM 알림을 생성합니다.

    워크플로우:
    1. mission_node: Spring 백엔드 API를 호출하여 미션 생성
    2. message_node: 넛지 메시지 생성 및 FCM 알림 전송

    필수 입력 필드 (InterventionState):
    - user_id: 사용자 personalId (문자열)
    - behavior_log: 앱 사용 로그 (app_name 포함)
    - behavior_pattern: 행동 패턴 설명 (문자열)
    - trigger_event: 트리거 이벤트 (문자열)
    - severity_score: 심각도 점수 (0-10)
    - coin_reward: 코인 보상 (선택, 기본값 10)

    사용 예시:
    ```python
    POST /runs
    {
      "assistant_id": "notification",
      "input": {
        "user_id": "user123",
        "behavior_log": {"app_name": "YouTube Shorts", "usage_timestamp": "2025-01-13T23:45:00"},
        "behavior_pattern": "30분 연속 숏폼 시청 중",
        "trigger_event": "숏폼 20분 연속",
        "severity_score": 8,
        "coin_reward": 10
      }
    }
    ```
    """
    workflow = StateGraph(DebugState)

    # 노드 추가
    workflow.add_node("mission_node", mission_node)
    workflow.add_node("message_node", message_node)

    # 엣지 추가
    workflow.add_edge(START, "mission_node")
    workflow.add_edge("mission_node", "message_node")
    workflow.add_edge("message_node", END)

    # 컴파일 (LangGraph Server가 자동으로 checkpointer 관리)
    return workflow.compile()


# 그래프 생성
debug_agent = build_debug_agent()
