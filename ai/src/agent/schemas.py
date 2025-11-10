"""데이터 구조 정의 (Data Schema Definitions)
- TypedDict 상태 정의
- Pydantic 모델 (LLM 구조화 출력용)
- 미션 및 알림 데이터 모델
"""

from dataclasses import dataclass
from typing import Literal, NotRequired, TypedDict

from pydantic import BaseModel, Field

# =============================================================================
# 상태 정의 (State Definitions)
# =============================================================================


class InterventionState(TypedDict):
    """실시간 개입 에이전트의 상태"""

    # 필수 입력 필드 (초기 상태에서 제공되어야 함)
    user_id: str  # DB user ID (스프링에서 변환후에 전달함)
    behavior_log: dict  # app_usage_logs 데이터

    # Optional - 워크플로우 중 생성되는 필드
    timestamp: NotRequired[str]
    behavior_pattern: NotRequired[str]  # LLM 분석 결과
    pattern_type: NotRequired[str]  # "normal", "concerning", "critical"
    trigger_event: NotRequired[str]  # 트리거된 이벤트 타입
    severity_score: NotRequired[int]  # 심각도 점수 (0-10)
    key_indicators: NotRequired[list[str]]  # 주요 지표들

    # Optional - 조건부 필드 (Command 분기 결정에 사용)
    intervention_needed: NotRequired[bool]
    intervention_type: NotRequired[
        str
    ]  # "short-form-overuse", "bedtime-usage", "focus-break" 등
    urgency_level: NotRequired[str]  # "low", "medium", "high"

    # Optional - 넛지 생성
    nudge_message: NotRequired[str]
    nudge_type: NotRequired[str]  # "REST" or "MEDITATION"
    duration_seconds: NotRequired[int]  # 미션 실행 시간 (초)

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
    user_id: str  # personalId (로그인 ID)
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
    severity_score: int = Field(ge=0, le=10, description="심각도 점수 (0-10)")
    key_indicators: list[str] = Field(description="주요 지표들")
    summary: str = Field(default="", description="행동 패턴 요약")


class InterventionDecision(BaseModel):
    """개입 필요성 판단 결과"""

    intervention_needed: bool = Field(description="개입이 필요한가?")
    intervention_type: Literal[
        "short-form-overuse", "bedtime-usage", "focus-break", "app-switching", "none"
    ] = Field(description="개입 유형")
    urgency_level: Literal["low", "medium", "high"] = Field(description="긴급도")
    reasoning: str = Field(description="판단 근거")


class NudgeMessage(BaseModel):
    """넛지 메시지"""

    message: str = Field(description="사용자에게 전달할 메시지")
    nudge_type: Literal["REST", "MEDITATION"] = Field(
        description="넛지 유형 (REST: 휴식, MEDITATION: 명상)"
    )
    duration_seconds: int = Field(gt=0, lt=60, description="미션 실행 시간 (초)")


class EffectivenessAnalysis(BaseModel):
    """효과성 분석 결과"""

    effectiveness_score: float = Field(
        ge=0.0, le=1.0, description="효과 점수 (0.0 ~ 1.0)"
    )
    behavior_change_detected: bool = Field(description="행동 변화가 감지되었는가?")
    summary: str = Field(description="효과 분석 요약")


class StrategyAdjustment(BaseModel):
    """전략 조정 결정"""

    adjustment_needed: bool = Field(description="전략 조정이 필요한가?")
    adjustment_reason: str = Field(description="조정이 필요한 이유")
    new_strategy: str | None = Field(default=None, description="새로운 전략 (필요시)")


# =============================================================================
# 미션 및 알림 데이터 모델 (Mission and Notification Models)
# =============================================================================


class MissionData(BaseModel):
    """미션 생성 페이로드"""

    user_id: int = Field(description="DB user ID (not personalId)")
    mission_type: str = Field(description="미션 타입 (REST or MEDITATION)")
    mission_text: str = Field(description="미션 메시지")
    coin_reward: int = Field(default=10, description="코인 보상")
    duration_seconds: int = Field(description="미션 실행 시간 (초)")
    target_app: str = Field(default="All Apps", description="대상 앱")
    stat_change_self_care: int = Field(default=1, description="자기관리 스탯 변화")
    stat_change_focus: int = Field(default=1, description="집중력 스탯 변화")
    stat_change_sleep: int = Field(default=1, description="수면 스탯 변화")
    prompt: str = Field(default="AI Intervention", description="프롬프트")


@dataclass
class MissionNotificationResult:
    """미션 생성 및 FCM 알림 전송 결과"""

    success: bool  # 전체 작업 성공 여부
    mission_id: str | None  # 생성된 미션 ID
    fcm_sent: bool  # FCM 전송 성공 여부
    db_user_id: int | None  # DB user ID
    error_stage: (
        str | None
    )  # 실패 단계: "user_lookup" | "mission_create" | "fcm_send" | None
