"""유틸리티 함수 및 LLM 설정 (Utility Functions and LLM Configuration)
- LLM 초기화 및 구조화된 출력 설정
- 시간 관련 유틸리티
- 데이터베이스 시뮬레이션 함수
"""

import os
from datetime import datetime, timedelta

import httpx
from langchain_anthropic import ChatAnthropic
from langgraph.checkpoint.memory import MemorySaver

from agent.schemas import (
    BehaviorAnalysis,
    EffectivenessAnalysis,
    InterventionDecision,
    InterventionState,
    MissionData,
    MissionNotificationResult,
    NudgeMessage,
    StrategyAdjustment,
)

# =============================================================================
# LLM 설정 (LLM Configuration)
# =============================================================================

# LLM 초기화
llm = ChatAnthropic(model="claude-sonnet-4-5")

# 구조화된 출력을 위한 LLM들
behavior_analyzer = llm.with_structured_output(BehaviorAnalysis)
intervention_decider = llm.with_structured_output(InterventionDecision)
nudge_generator = llm.with_structured_output(NudgeMessage)
effectiveness_analyzer = llm.with_structured_output(EffectivenessAnalysis)
strategy_adjuster = llm.with_structured_output(StrategyAdjustment)

# Checkpointer (상태 영속성)
checkpointer = MemorySaver()


# =============================================================================
# Spring 서버 연동 설정 (Spring Server Integration)
# =============================================================================

SPRING_SERVER_URL = os.getenv("SPRING_SERVER_URL", "http://52.78.96.102:8080")
SECURITY_INTERNAL_API_KEY = os.getenv("SECURITY_INTERNAL_API_KEY")


# =============================================================================
# 시간 유틸리티 함수 (Time Utility Functions)
# =============================================================================


def get_current_timestamp() -> str:
    """현재 시간을 ISO 포맷으로 반환"""
    return datetime.now().isoformat()


def schedule_evaluation(intervention_time: str, delay_minutes: int) -> str:
    """평가 스케줄 시간 계산"""
    intervention_dt = datetime.fromisoformat(intervention_time)
    evaluation_dt = intervention_dt + timedelta(minutes=delay_minutes)
    return evaluation_dt.isoformat()


def get_time_slot_from_timestamp(timestamp_str: str) -> str:
    """타임스탬프에서 time_slot 계산

    Args:
        timestamp_str: ISO 8601 타임스탬프 (예: "2025-01-03T23:45:00")

    Returns:
        time_slot: "morning", "afternoon", "evening", "night"
    """
    dt = datetime.fromisoformat(timestamp_str)
    hour = dt.hour

    if 6 <= hour < 12:
        return "morning"
    elif 12 <= hour < 18:
        return "afternoon"
    elif 18 <= hour < 22:
        return "evening"
    else:
        return "night"


def truncate_message(message: str, max_length: int = 100) -> str:
    """메시지를 최대 길이로 잘라냄

    Args:
        message: 원본 메시지
        max_length: 최대 길이 (기본값: 100자)

    Returns:
        잘라낸 메시지 (한글 기준)
    """
    if len(message) <= max_length:
        return message

    # 100자로 자르되, 마침표나 느낌표가 있으면 그 앞에서 자름
    truncated = message[:max_length]

    # 문장 부호 찾기 (뒤에서부터)
    for i in range(len(truncated) - 1, max(0, len(truncated) - 20), -1):
        if truncated[i] in [".", "!", "?", "。", "!", "?"]:
            return truncated[: i + 1]

    # 문장 부호가 없으면 그냥 100자에서 자르고 '...' 추가 (단, 97자까지만)
    return message[:97] + "..."


# =============================================================================
# 데이터베이스 시뮬레이션 함수 (Database Simulation Functions)
# =============================================================================


def simulate_behavior_log(user_id: int) -> dict:
    """실제 환경에서는 app_usage_logs 테이블에서 가져옴
    MVP에서는 시뮬레이션
    """
    return {
        "app_name": "YouTube Shorts",
        "duration_seconds": 1200,  # 20분
        "session_count": 1,
        "usage_timestamp": datetime.now().isoformat(),  # ISO 8601 전체 타임스탬프
        "recent_app_switches": 2,
    }


def simulate_post_intervention_usage(user_id: int, intervention_id: int) -> dict:
    """실제 환경에서는 intervention 후의 app_usage_logs를 조회
    MVP에서는 시뮬레이션
    """
    return {
        "user_id": user_id,
        "intervention_id": intervention_id,
        "duration_after_intervention": 300,  # 5분 (개선됨)
        "behavior_changed": True,
    }


# =============================================================================
# 미션 및 알림 함수 (Mission and Notification Functions)
# =============================================================================


def get_db_user_id(personal_id: str) -> int | None:
    """personalId로 DB user_id 조회

    Args:
        personal_id: 사용자 personalId (로그인 ID)

    Returns:
        DB user_id (int) if successful, None if failed
    """
    if not SECURITY_INTERNAL_API_KEY:
        print("❌ SECURITY_INTERNAL_API_KEY environment variable is not set")
        return None

    print(f"     🔍 DB user_id 조회 중... (personalId={personal_id})")

    headers = {
        "X-API-Key": SECURITY_INTERNAL_API_KEY,
        "Content-Type": "application/json",
    }

    try:
        with httpx.Client(timeout=10.0) as client:
            response = client.get(
                f"{SPRING_SERVER_URL}/api/user/{personal_id}",
                headers=headers,
            )
            response.raise_for_status()
            user_data = response.json()

            db_user_id = (
                user_data.get("data", {}).get("profile", {}).get("userId")
            )
            if not db_user_id:
                print("     ❌ DB user_id 조회 실패: 응답에 userId 없음")
                return None

            print(f"     ✅ DB user_id 조회 완료: {db_user_id}")
            return db_user_id

    except httpx.HTTPError as e:
        print(f"     ❌ DB user_id 조회 실패: {e}")
        if hasattr(e, "response") and e.response:
            print(f"        응답 코드: {e.response.status_code}")
            try:
                error_detail = e.response.json()
                print(f"        오류 상세: {error_detail}")
            except:
                print(f"        오류 텍스트: {e.response.text[:200]}")
        return None


def create_mission(mission_data: MissionData) -> str | None:
    """미션 생성 API 호출

    Args:
        mission_data: 미션 생성 데이터 (Pydantic model)

    Returns:
        mission_id (str) if successful, None if failed
    """
    if not SECURITY_INTERNAL_API_KEY:
        print("❌ SECURITY_INTERNAL_API_KEY environment variable is not set")
        return None

    print("     📝 미션 생성 중...")

    headers = {
        "X-API-Key": SECURITY_INTERNAL_API_KEY,
        "Content-Type": "application/json",
    }

    # Pydantic model을 dict로 변환
    mission_payload = mission_data.model_dump()

    try:
        with httpx.Client(timeout=10.0) as client:
            response = client.post(
                f"{SPRING_SERVER_URL}/api/mission",
                json=mission_payload,
                headers=headers,
            )
            response.raise_for_status()
            result = response.json()

            mission_id = result.get("data", {}).get("missionId")

            if mission_id:
                print(f"     ✅ 미션 생성 완료: ID={mission_id}")
                return str(mission_id)
            else:
                print("     ⚠️ 미션 생성 응답에 missionId 없음")
                return None

    except httpx.HTTPError as e:
        print(f"     ❌ 미션 생성 실패: {e}")
        if hasattr(e, "response") and e.response:
            print(f"        응답 코드: {e.response.status_code}")
            try:
                error_detail = e.response.json()
                print(f"        오류 상세: {error_detail}")
            except:
                print(f"        오류 텍스트: {e.response.text[:200]}")
        return None


def send_fcm_with_mission(
    personal_id: str, mission_id: str, message: str
) -> bool:
    """FCM 알림 전송 (미션 ID 포함)

    Args:
        personal_id: 사용자 personalId (디바이스 토큰 조회용)
        mission_id: 미션 ID (백엔드가 Mission 테이블에서 enrichment)
        message: 알림 메시지

    Returns:
        True if successful, False if failed
    """
    if not SECURITY_INTERNAL_API_KEY:
        print("❌ SECURITY_INTERNAL_API_KEY environment variable is not set")
        return False

    print("     📱 FCM 알림 전송 중...")

    headers = {
        "X-API-Key": SECURITY_INTERNAL_API_KEY,
        "Content-Type": "application/json",
    }

    fcm_payload = {
        "user_id": personal_id,
        "title": "디토",
        "message": message,
        "mission_id": mission_id,
    }

    try:
        with httpx.Client(timeout=10.0) as client:
            response = client.post(
                f"{SPRING_SERVER_URL}/api/fcm/send",
                json=fcm_payload,
                headers=headers,
            )
            response.raise_for_status()
            result = response.json()

            if result.get("success"):
                print(f"     ✅ FCM 전송 완료: mission_id={mission_id}")
                return True
            else:
                print(f"     ❌ FCM 전송 실패: {result.get('error')}")
                return False

    except httpx.HTTPError as e:
        print(f"     ❌ FCM HTTP 오류: {e}")
        if hasattr(e, "response") and e.response:
            print(f"        응답 코드: {e.response.status_code}")
            try:
                error_detail = e.response.json()
                print(f"        오류 상세: {error_detail}")
            except:
                print(f"        오류 텍스트: {e.response.text[:200]}")
        return False


def create_and_notify_mission(state: InterventionState) -> MissionNotificationResult:
    """미션 생성 및 FCM 알림 전송 (Orchestrator)

    역할:
    1. personalId로 DB user_id 조회
    2. 미션 생성
    3. FCM 알림 전송

    각 단계별로 에러 처리 및 결과 추적

    Args:
        state: Intervention state containing user_id, nudge_message, etc.

    Returns:
        MissionNotificationResult with detailed success/failure info
    """
    # Step 1: User ID lookup
    personal_id = state["user_id"]
    db_user_id = get_db_user_id(personal_id)

    if db_user_id is None:
        return MissionNotificationResult(
            success=False,
            mission_id=None,
            fcm_sent=False,
            db_user_id=None,
            error_stage="user_lookup",
        )

    # Step 2: Mission creation
    target_app = "All Apps"
    if "behavior_log" in state and state["behavior_log"]:
        target_app = state["behavior_log"].get("app_name", "All Apps")

    mission_data = MissionData(
        user_id=db_user_id,
        mission_type=state.get("nudge_type", "REST"),
        mission_text=state["nudge_message"],
        coin_reward=10,
        duration_seconds=state.get("duration_seconds", 300),
        target_app=target_app,
        stat_change_self_care=1,
        stat_change_focus=1,
        stat_change_sleep=1,
        prompt="AI Intervention",
    )

    mission_id = create_mission(mission_data)

    if mission_id is None:
        return MissionNotificationResult(
            success=False,
            mission_id=None,
            fcm_sent=False,
            db_user_id=db_user_id,
            error_stage="mission_create",
        )

    # Step 3: FCM send
    fcm_sent = send_fcm_with_mission(personal_id, mission_id, state["nudge_message"])

    if not fcm_sent:
        # Mission created but FCM failed - partial success
        return MissionNotificationResult(
            success=False,
            mission_id=mission_id,
            fcm_sent=False,
            db_user_id=db_user_id,
            error_stage="fcm_send",
        )

    # Full success
    return MissionNotificationResult(
        success=True,
        mission_id=mission_id,
        fcm_sent=True,
        db_user_id=db_user_id,
        error_stage=None,
    )


def send_fcm_notification(state: InterventionState) -> str | None:
    """Send FCM notification request to Spring server (DEPRECATED - 하위 호환성용)

    DEPRECATED: create_and_notify_mission() 사용을 권장합니다.
    이 함수는 하위 호환성을 위해 유지되며, 내부적으로 create_and_notify_mission()을 호출합니다.

    Returns:
        mission_id: String ID if successful, None if failed
    """
    result = create_and_notify_mission(state)
    return result.mission_id
