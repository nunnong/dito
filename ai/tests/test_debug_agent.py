# =============================================================================
# LangGraph SDK Tests (Real API Call)
# =============================================================================
from langgraph_sdk import get_sync_client  # or get_client for async


def test_debug_agent_via_langgraph_sdk():
    """LangGraph SDK를 사용한 debug 에이전트 테스트 (실제 서버 필요)"""
    # LangGraph 서버가 localhost:2024에서 실행 중이어야 함
    client = get_sync_client(url="http://localhost:2024")

    input_data = {
        "user_id": 29,
        "mission_info": {
            "mission_type": "REST",
            "duration_seconds": 5,
            "coin_reward": 15,
            "target_app": "youtube",
            "stat_change_self_care": 20,
            "stat_change_focus": 20,
            "stat_change_sleep": 20,
            "prompt": "AI 개입 완료 테스트",
        },
        "nudge_message": "잠시 휴식하고 강의 영상을 보는 건 어때요? (인스타그램 1시간 탐색 감지)",
    }

    print("\n=== Debug Agent SDK Test 시작 ===")
    print(f"Input: {input_data}")

    for chunk in client.runs.stream(
        None,  # Threadless run
        "debug",  # assistant_id - langgraph.json에 정의된 이름
        input=input_data,
        stream_mode="updates",
    ):
        print(f"\nReceiving new event of type: {chunk.event}...")
        print(f"Data: {chunk.data}")

    print("\n=== Debug Agent SDK Test 완료 ===")
