"""데이터 구조 정의 (Data Schema Definitions)
- TypedDict 상태 정의
- Pydantic 모델 (LLM 구조화 출력용)
- 미션 및 알림 데이터 모델
"""

from dataclasses import dataclass
from typing import List, Literal, NotRequired, TypedDict

from pydantic import BaseModel, Field


# =============================================================================
# 상태 정의 (State Definitions)
# =============================================================================
class YoutubeState(TypedDict):
    """유튜브 에이전트 상태"""

    title: str
    channel: str

    video_type: NotRequired[str]
    keywords: NotRequired[List[str]]


class InterventionState(TypedDict):
    """실시간 개입 에이전트의 상태"""

    # 필수 입력 필드 (초기 상태에서 제공되어야 함)
    user_id: str  # 사용자 personalId (문자열)
    behavior_log: dict  # app_usage_logs 데이터 (app_metadata 포함 가능)

    # Optional - 워크플로우 중 생성되는 필드
    video_type: NotRequired[str]  # 유튜브 영상 타입 (youtube_analyze_node에서 생성)
    keywords: NotRequired[
        list[str]
    ]  # 유튜브 영상 키워드 (youtube_analyze_node에서 생성)

    behavior_pattern: NotRequired[str]  # LLM 분석 결과
    trigger_event: NotRequired[str]  # 트리거된 이벤트 타입
    severity_score: NotRequired[int]  # 심각도 점수 (0-10)
    key_indicators: NotRequired[list[str]]  # 주요 지표들

    # Optional - 조건부 필드 (Command 분기 결정에 사용)
    intervention_reason: NotRequired[str]  # 개입 이유
    intervention_needed: NotRequired[bool]

    mission_id: NotRequired[int]  # missions 테이블 ID
    mission_type: NotRequired[str]  # "REST", "MEDITATION" 추가할 예정
    duration_seconds: NotRequired[int]  # 미션 실행 시간 (초)
    coin_reward: NotRequired[int]

    nudge_message: NotRequired[str]
    fcm_sent: NotRequired[bool]  # FCM 전송 성공 여부


class EvaluationState(TypedDict):
    """미션 평가 에이전트의 상태"""

    # 필수 입력 필드 (백엔드로부터 제공)
    user_id: int  # DB user ID
    mission_id: int
    behavior_logs: list[dict]  # BehaviorLog 목록

    # Optional - 미션 정보 (API 조회 결과)
    mission_info: NotRequired[dict]  # GET /api/mission/{id} 응답
    mission_type: NotRequired[str]  # mission_info에서 추출
    target_app: NotRequired[str]

    # Optional - 평가 결과
    evaluation_result: NotRequired[str]  # "SUCCESS" | "FAILURE"
    feedback: NotRequired[str]  # 평가 추론 내용

    # Optional - 효과 측정
    effectiveness_score: NotRequired[float]  # 0.0 ~ 1.0
    behavior_change_detected: NotRequired[bool]

    # Optional - 전략 조정 (Command 분기 결정에 사용)
    adjustment_needed: NotRequired[bool]
    adjustment_reason: NotRequired[str]
    new_strategy: NotRequired[str]

    # Optional - FCM 전송
    fcm_sent: NotRequired[bool]


# =============================================================================
# LLM 구조화 출력 스키마 (Structured Output Schemas)
# =============================================================================


class BehaviorAnalysis(BaseModel):
    """행동 패턴 분석 결과"""

    trigger_event: str = Field(
        description="감지된 트리거 이벤트 (예: '숏폼 20분 연속', '취침 30분 전 사용')"
    )
    severity_score: int = Field(ge=0, le=10, description="심각도 점수 (0-10)")
    key_indicators: list[str] = Field(description="주요 지표들")
    summary: str = Field(default="", description="행동 패턴 요약")


class InterventionDecision(BaseModel):
    """개입 필요성 판단 결과"""

    intervention_needed: bool = Field(description="개입이 필요한가?")
    reasoning: list[str] = Field(
        description="판단 근거 3가지를 짧은 문장으로", max_length=3
    )


class Mission(BaseModel):
    """미션 생성 결과"""

    mission_type: Literal["REST", "MEDITATION"] = Field(description="개입 유형")
    duration_seconds: int = Field(
        gt=10, le=300, description="미션 실행 시간 (10초-5분, 300초 포함)"
    )


class NudgeMessage(BaseModel):
    """넛지 메시지"""

    message: str = Field(description="사용자에게 전달할 메시지")


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
    error_stage: str | None  # 실패 단계: "mission_create" | "fcm_send" | None


class VideoType(BaseModel):
    """Video type and content keywords"""

    video_type: Literal[
        "EDUCATIONAL",
        "ENTERTAINMENT",
        "NEWS_INFO",
        "VLOG",
        "SHORT_FORM",
        "GAMING",
        "MUSIC",
        "REVIEW",
        "UNKNOWN",
    ] = Field(..., description="The primary format of the video (single choice)")

    keywords: List[
        Literal[
            "HISTORY",
            "SCIENCE",
            "TECH",
            "FOOD",
            "TRAVEL",
            "HEALTH",
            "FITNESS",
            "BEAUTY",
            "FASHION",
            "IDOL",
            "SPORTS",
            "POLITICS",
            "ECONOMY",
            "ART",
            "MOVIE",
            "MUSIC",
            "GAME",
            "ANIMAL",
            "EDUCATION",
            "DAILY",
            "LIFESTYLE",
            "OTHER",
        ]
    ] = Field(
        ...,
        min_items=1,
        description="The main topics or subjects covered in the video (multiple selections allowed)",
    )


class DebugState(TypedDict):
    """debug 용 상태"""

    user_id: int  # 사용자 db_id
    mission_info: dict
    nudge_message: str
    mission_id: NotRequired[int]
    fcm_sent: NotRequired[bool]  # FCM 전송 성공 여부
