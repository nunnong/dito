"""유틸리티 함수 및 LLM 설정 (Utility Functions and LLM Configuration)
- LLM 초기화 및 구조화된 출력 설정
- 시간 관련 유틸리티
- 데이터베이스 시뮬레이션 함수
"""

from datetime import datetime, timedelta

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


def save_intervention_to_db(state: InterventionState) -> int:
    """실제 환경에서는 missions 테이블에 저장
    MVP에서는 시뮬레이션하여 ID 반환
    """
    print(f"[DB] Saving intervention for user {state['user_id']}")
    print(f"     Type: {state['intervention_type']}")
    print(f"     Message: {state['nudge_message']}")
    return 12345  # 시뮬레이션 intervention_id
