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
        if truncated[i] in ['.', '!', '?', '。', '!', '?']:
            return truncated[:i+1]

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
    """Send FCM notification request to Spring server

    Returns:
        intervention_id: String ID if successful, None if failed
    """
    # 환경 변수 유효성 검증
    if not SECURITY_INTERNAL_API_KEY:
        print("❌ SECURITY_INTERNAL_API_KEY environment variable is not set")
        print("   Please check your .env file or environment configuration")
        return None

    intervention_id = f"INT_{int(datetime.now().timestamp() * 1000)}"

    payload = {
        "user_id": state["user_id"],  # personalId (문자열)
        "message": state["nudge_message"],
        "intervention_id": intervention_id,
        "intervention_needed": state["intervention_needed"],
        "intervention_type": state.get("intervention_type", "none"),
        "type": "intervention"
    }

    headers = {
        "X-API-Key": SECURITY_INTERNAL_API_KEY,
        "Content-Type": "application/json"
    }

    try:
        with httpx.Client(timeout=10.0) as client:
            response = client.post(
                f"{SPRING_SERVER_URL}/fcm/send",
                json=payload,
                headers=headers
            )
            response.raise_for_status()
            result = response.json()

            if result.get("success"):
                print(f"✅ FCM notification sent: {intervention_id}")
                return intervention_id
            else:
                print(f"❌ FCM failed: {result.get('error')}")
                return None

    except httpx.HTTPError as e:
        print(f"❌ HTTP error: {e}")
        return None
