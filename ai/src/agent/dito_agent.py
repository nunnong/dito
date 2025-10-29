"""
디토(Dito) 디지털 디톡스 에이전트 MVP
- 실시간 행동 모니터링 및 개입
- 지연된 효과 평가 및 전략 조정
- LangGraph 1.0+ 사용
"""

from typing import TypedDict, Literal, Annotated, Optional, NotRequired
from datetime import datetime, timedelta
import operator
from pydantic import BaseModel, Field

from langgraph.graph import StateGraph, START, END
from langgraph.checkpoint.memory import MemorySaver
from langgraph.types import Command

from langchain_anthropic import ChatAnthropic
from langchain_core.messages import SystemMessage, HumanMessage

# =============================================================================
# 상태 정의 (State Definitions)
# =============================================================================

class InterventionState(TypedDict):
    """실시간 개입 에이전트의 상태"""
    # 필수 입력 필드 (초기 상태에서 제공되어야 함)
    user_id: int
    session_id: str
    behavior_log: dict  # app_usage_logs 데이터

    # Optional - 워크플로우 중 생성되는 필드
    timestamp: NotRequired[str]
    behavior_pattern: NotRequired[str]  # LLM 분석 결과
    trigger_event: NotRequired[str]  # 트리거된 이벤트 타입

    # Optional - 조건부 필드 (Command 분기 결정에 사용)
    intervention_needed: NotRequired[bool]
    intervention_type: NotRequired[str]  # "short-form-overuse", "bedtime-usage", "focus-break" 등
    urgency_level: NotRequired[str]  # "low", "medium", "high"

    # Optional - 넛지 생성
    nudge_message: NotRequired[str]
    nudge_type: NotRequired[str]  # "rest-suggestion", "sleep-reminder", "focus-mode" 등

    # Optional - 개입 실행
    intervention_time: NotRequired[str]
    intervention_id: NotRequired[int]  # missions 테이블 ID

    # Optional - 평가 스케줄링
    evaluation_scheduled_time: NotRequired[str]
    evaluation_delay_minutes: NotRequired[int]


class EvaluationState(TypedDict):
    """지연된 평가 에이전트의 상태"""
    # 필수 입력 필드 (초기 상태에서 제공되어야 함)
    intervention_id: int
    user_id: int
    intervention_type: str

    # Optional - 효과 측정 (워크플로우 중 생성)
    pre_intervention_usage: NotRequired[dict]
    post_intervention_usage: NotRequired[dict]
    effectiveness_score: NotRequired[float]  # 0.0 ~ 1.0
    behavior_change_detected: NotRequired[bool]

    # Optional - 전략 조정 (Command 분기 결정에 사용)
    adjustment_needed: NotRequired[bool]
    adjustment_reason: NotRequired[str]
    new_strategy: NotRequired[str]
    

# =============================================================================
# LLM 구조화 출력 스키마 (Structured Output Schemas)
# =============================================================================

class BehaviorAnalysis(BaseModel):
    """행동 패턴 분석 결과"""
    pattern_type: Literal["normal", "concerning", "critical"] = Field(
        description="행동 패턴 유형"
    )
    trigger_event: str = Field(
        description="감지된 트리거 이벤트 (예: '숏폼 20분 연속', '취침 30분 전 사용')"
    )
    summary: str = Field(
        description="행동 패턴 요약"
    )


class InterventionDecision(BaseModel):
    """개입 필요성 판단 결과"""
    intervention_needed: bool = Field(
        description="개입이 필요한가?"
    )
    intervention_type: Literal[
        "short-form-overuse",
        "bedtime-usage", 
        "focus-break",
        "app-switching",
        "none"
    ] = Field(
        description="개입 유형"
    )
    urgency_level: Literal["low", "medium", "high"] = Field(
        description="긴급도"
    )
    reasoning: str = Field(
        description="판단 근거"
    )


class NudgeMessage(BaseModel):
    """넛지 메시지"""
    message: str = Field(
        description="사용자에게 전달할 메시지"
    )
    nudge_type: str = Field(
        description="넛지 유형 (예: 'rest-suggestion', 'sleep-reminder')"
    )


class EffectivenessAnalysis(BaseModel):
    """효과성 분석 결과"""
    effectiveness_score: float = Field(
        ge=0.0, le=1.0,
        description="효과 점수 (0.0 ~ 1.0)"
    )
    behavior_change_detected: bool = Field(
        description="행동 변화가 감지되었는가?"
    )
    summary: str = Field(
        description="효과 분석 요약"
    )


class StrategyAdjustment(BaseModel):
    """전략 조정 결정"""
    adjustment_needed: bool = Field(
        description="전략 조정이 필요한가?"
    )
    adjustment_reason: str = Field(
        description="조정이 필요한 이유"
    )
    new_strategy: Optional[str] = Field(
        default=None,
        description="새로운 전략 (필요시)"
    )


# =============================================================================
# 설정 (Configuration)
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

# 중재 이론 가이드라인 (중재_이론 파일 기반)
INTERVENTION_GUIDELINES = """
## 개입 타이밍 가이드라인
- 하루 개입 빈도: 2~3회 (최대 4회 이하)
- 개입 간 최소 간격: 2~3시간 이상
- 단일 알림 노출: 1~2분 내
- 행동효과 지속시간: 30-45분 내외

## 트리거 이벤트별 개입 메시지
1. 연속 30분 집중 사용 → 휴식 제안 (Flow Interrupt)
2. 앱 전환 10회 이상/5분 → 집중모드 추천
3. 동일 앱 3회 반복실행/5분 → 자각 유도 메시지
4. 숏폼 20분 연속 → 중단 권장 + 대체활동 제안
5. 취침 30분 전 사용 → 수면권유 메시지

## 메시지 프레임
"[인식] → [제안] → [보상]"
예) "30분째 시청 중이에요 → 5분 휴식 어때요? → 성공 시 +5 코인 보상"
"""


# =============================================================================
# 유틸리티 함수 (Utility Functions)
# =============================================================================

def get_current_timestamp() -> str:
    """현재 시간을 ISO 포맷으로 반환"""
    return datetime.now().isoformat()


def schedule_evaluation(intervention_time: str, delay_minutes: int) -> str:
    """평가 스케줄 시간 계산"""
    intervention_dt = datetime.fromisoformat(intervention_time)
    evaluation_dt = intervention_dt + timedelta(minutes=delay_minutes)
    return evaluation_dt.isoformat()


def simulate_behavior_log(user_id: int) -> dict:
    """
    실제 환경에서는 app_usage_logs 테이블에서 가져옴
    MVP에서는 시뮬레이션
    """
    return {
        "user_id": user_id,
        "app_name": "YouTube Shorts",
        "duration_seconds": 1200,  # 20분
        "session_count": 1,
        "time_slot": "night",
        "usage_date": datetime.now().date().isoformat(),
        "recent_app_switches": 2,
    }


def simulate_post_intervention_usage(user_id: int, intervention_id: int) -> dict:
    """
    실제 환경에서는 intervention 후의 app_usage_logs를 조회
    MVP에서는 시뮬레이션
    """
    return {
        "user_id": user_id,
        "intervention_id": intervention_id,
        "duration_after_intervention": 300,  # 5분 (개선됨)
        "behavior_changed": True,
    }


def save_intervention_to_db(state: InterventionState) -> int:
    """
    실제 환경에서는 missions 테이블에 저장
    MVP에서는 시뮬레이션하여 ID 반환
    """
    print(f"[DB] Saving intervention for user {state['user_id']}")
    print(f"     Type: {state['intervention_type']}")
    print(f"     Message: {state['nudge_message']}")
    return 12345  # 시뮬레이션 intervention_id


print("Dito Agent 모듈 로드 완료")
