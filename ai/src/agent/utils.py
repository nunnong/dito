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


def send_fcm_notification(state: InterventionState) -> str | None:
    """Send FCM notification request to Spring server (FCM 테스트용 - 무조건 미션 생성)

    역할:
    1. personalId로 DB user_id 조회 (/api/user/{personalId})
    2. DB user_id로 미션 생성 API 호출 (/api/mission) - 무조건 실행
    3. mission_id 획득
    4. 간소화된 FCM 형식으로 전송 (/api/fcm/send)
       - 백엔드가 mission_id로부터 자동으로 미션 데이터 조회 및 enrichment
       - AI는 user_id, title, message, mission_id만 전달

    Returns:
        mission_id: String ID if successful, None if failed
    """
    # 환경 변수 유효성 검증
    if not SECURITY_INTERNAL_API_KEY:
        print("❌ SECURITY_INTERNAL_API_KEY environment variable is not set")
        print("   Please check your .env file or environment configuration")
        return None

    # Step 0: personalId로 DB user_id 조회
    personal_id = state["user_id"]  # 입력으로 받은 personalId
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
            )  # DB의 실제 user ID
            if not db_user_id:
                print("     ❌ DB user_id 조회 실패: 응답에 id 없음")
                return None

            print(f"     ✅ DB user_id 조회 완료: {db_user_id}")

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

    mission_id = None

    # Step 1: 미션 생성 (무조건 실행 - FCM 테스트용)
    print("     📝 미션 생성 중... (무조건 실행)")

    # behavior_log에서 target_app 추출
    target_app = "All Apps"  # 기본값
    if "behavior_log" in state and state["behavior_log"]:
        target_app = state["behavior_log"].get("app_name", "All Apps")

    # 미션 생성 API 페이로드 (DB user_id 사용)
    mission_payload = {
        "user_id": db_user_id,  # DB의 실제 user ID
        "mission_type": state.get("nudge_type", "REST"),  # LLM이 선택한 타입
        "mission_text": state["nudge_message"],
        "coin_reward": 10,
        "duration_seconds": state.get("duration_seconds", 300),  # LLM이 선택한 시간
        "target_app": target_app,  # behavior_log에서 추출
        "stat_change_self_care": 1,
        "stat_change_focus": 1,
        "stat_change_sleep": 1,
        "prompt": "AI Intervention",
    }

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
            else:
                print("     ⚠️ 미션 생성 응답에 mission_id 없음")

    except httpx.HTTPError as e:
        print(f"     ❌ 미션 생성 실패: {e}")
        # 미션 생성 실패해도 FCM은 전송 (상태 메시지로)

    # Step 2: FCM 전송 (간소화된 형식, personalId 사용)
    print("     📱 FCM 알림 전송 중...")

    # FCM 페이로드 구성 (백엔드가 mission_id로부터 자동 enrichment)
    # FCM은 personalId를 사용 (디바이스 토큰 조회용)
    fcm_payload = {
        "user_id": personal_id,  # FCM은 personalId 사용
        "title": "디토",
        "message": state["nudge_message"],
    }

    # mission_id가 있으면 추가 (백엔드가 Mission 테이블에서 나머지 정보 조회)
    if mission_id is not None:
        fcm_payload["mission_id"] = mission_id

    try:
        with httpx.Client(timeout=10.0) as client:
            response = client.post(
                f"{SPRING_SERVER_URL}/api/fcm/send",  # 새로운 엔드포인트
                json=fcm_payload,
                headers=headers,
            )
            response.raise_for_status()
            result = response.json()

            if result.get("success"):
                if mission_id:
                    print(f"     ✅ FCM 전송 완료: mission_id={mission_id}")
                    return str(mission_id)
                else:
                    print("     ⚠️ FCM 전송 성공했으나 mission_id 없음")
                    return None
            else:
                print(f"     ❌ FCM 전송 실패: {result.get('error')}")
                return None

    except httpx.HTTPError as e:
        print(f"     ❌ FCM HTTP 오류: {e}")
        # 디버깅을 위한 상세 정보 출력
        if hasattr(e, "response") and e.response:
            print(f"        응답 코드: {e.response.status_code}")
            try:
                error_detail = e.response.json()
                print(f"        오류 상세: {error_detail}")
            except:
                print(f"        오류 텍스트: {e.response.text[:200]}")
        return None
