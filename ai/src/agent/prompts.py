"""프롬프트 템플릿 및 설정 (Prompt Templates and Configuration)
- 중재 이론 가이드라인
- 시스템 메시지 상수
- 프롬프트 생성 함수
- 효과성 평가 임계값
"""


# =============================================================================
# 중재 이론 가이드라인 (Intervention Guidelines)
# =============================================================================

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
# 시스템 메시지 상수 (System Message Constants)
# =============================================================================

# SYSTEM_MSG_BEHAVIOR_ANALYZER = "당신은 디지털 웰빙 전문가입니다."
# SYSTEM_MSG_INTERVENTION_DECIDER = "당신은 디지털 웰빙 개입 전문가입니다."
SYSTEM_MSG_NUDGE_GENERATOR = "당신은 디지털 웰빙 코치입니다."
SYSTEM_MSG_EFFECTIVENESS_ANALYZER = "당신은 디지털 웰빙 효과성 평가 전문가입니다."
SYSTEM_MSG_STRATEGY_ADJUSTER = "당신은 디지털 웰빙 전략 최적화 전문가입니다."


# =============================================================================
# 효과성 평가 임계값 (Effectiveness Thresholds)
# =============================================================================

EFFECTIVENESS_THRESHOLD_HIGH = 0.7  # 0.7 이상: 효과적, 조정 불필요
EFFECTIVENESS_THRESHOLD_LOW = 0.4  # 0.4 미만: 비효과적, 전략 조정 필요


# =============================================================================
# 프롬프트 템플릿 함수 (Prompt Template Functions)
# =============================================================================


def get_behavior_analysis_prompt(
    behavior_log: dict, time_slot: str, video: dict = None
) -> str:
    """행동 패턴 분석 프롬프트 생성

    Args:
        behavior_log: 앱 사용 로그 데이터
        time_slot: 시간대 ("morning", "afternoon", "evening", "night")
        video: 유튜브 영상 정보 (video_type, keywords 포함, optional)

    Returns:
        분석 프롬프트 문자열
    """
    base_prompt = f"""
당신은 디지털 사용패턴 분석 전문가입니다.
사용자의 앱 사용 데이터를 분석하여 패턴을 파악하세요.

사용 데이터:
- 앱: {behavior_log.get("app_name", "Unknown")}
- 사용 시간: {behavior_log.get("duration_seconds", 0)}초
- 사용 시점: {behavior_log.get("usage_timestamp", "Unknown")}
- 시간대: {time_slot}
- 최근 앱 전환 횟수: {behavior_log.get("recent_app_switches", 0)}회
"""

    # 유튜브 영상 정보가 있으면 추가
    if video and video.get("video_type"):
        keywords_str = ", ".join(video.get("keywords", []))
        base_prompt += f"""
시청 영상 정보:
- 영상 타입: {video.get("video_type", "UNKNOWN")}
- 콘텐츠 키워드: {keywords_str}

**영상 타입별 분석 가이드라인**:
- SHORT_FORM: 클릭베이트성 숏폼 영상 → 심각도 +2~3 (중독성 높음)
- ENTERTAINMENT: 엔터테인먼트 콘텐츠 → 장시간 시청 시 심각도 +1~2
- EDUCATIONAL: 교육 콘텐츠 → 심각도 -1~2 (학습 목적 고려)
- VLOG, NEWS_INFO: 정보성 콘텐츠 → 적정 시청 시 심각도 유지
"""

    base_prompt += """
패턴 유형을 판단하고, 트리거 이벤트를 명확히 식별하세요.
심각도 점수(0-10)와 주요 지표들을 포함하세요.
"""

    return base_prompt


def get_intervention_decision_prompt(behavior_pattern: str, trigger_event: str) -> str:
    """개입 필요성 판단 프롬프트 생성

    Args:
        behavior_pattern: 분석된 행동 패턴
        trigger_event: 감지된 트리거 이벤트
        severity_score: 심각도 (0~10)

    Returns:
        개입 판단 프롬프트 문자열
    """
    return f"""
당신은 디지털 웰빙 개입 전문가입니다.
사용자의 행동 패턴을 분석한 결과입니다:

행동 패턴: {behavior_pattern}
트리거 이벤트: {trigger_event}

중재 이론 가이드라인:
{INTERVENTION_GUIDELINES}

개입이 필요한지 판단하시오.
"""


def get_mission_generation_prompt(
    behavior_pattern: str, trigger_event: str, severity_score: int
) -> str:
    """미션 생성 프롬프트

    Args:
        behavior_pattern: 행동 패턴
        trigger_event: 트리거 이벤트
        severity_score: 심각도 점수 (0-10)

    Returns:
        미션 생성 프롬프트 문자열
    """
    return f"""
사용자의 행동 패턴을 기반으로 적절한 미션을 생성하세요.

**상황 분석**:
- 행동 패턴: {behavior_pattern}
- 트리거 이벤트: {trigger_event}
- 심각도: {severity_score}/10

**미션 타입 선택 기준**:
1. **REST** (휴식):
   - 심각도 1-5

2. **MEDITATION** (명상):
   - 심각도가 높은 경우 (6-10)

**미션 시간 설정**:
- 낮은 심각도 (0-4): 60-120초
- 중간 심각도 (5-7): 120-180초
- 높은 심각도 (8-10): 180-300초

적절한 mission_type과 duration_seconds를 선택하세요.
"""


def get_nudge_generation_prompt(
    behavior_pattern: str, intervention_type: str, urgency_level: str
) -> str:
    """넛지 메시지 생성 프롬프트

    Args:
        behavior_pattern: 행동 패턴
        intervention_type: 개입 유형
        urgency_level: 긴급도

    Returns:
        넛지 생성 프롬프트 문자열
    """
    return f"""
사용자에게 전달할 넛지 메시지를 생성하세요.

상황:
- 행동 패턴: {behavior_pattern}
- 개입 유형: {intervention_type}
- 긴급도: {urgency_level}

메시지 프레임: "[인식] → [제안] → [보상]"

요구사항:
1. **최대 100자 이내 (한글 기준)**
2. 친근하고 공감적인 톤
3. 구체적인 행동 제안
4. 긍정적 보상 (코인 등) 제시
5. 1-2문장으로 간결하게

**예시** (100자 이내):
- "30분째 시청 중이에요. 5분 휴식 어때요? 성공 시 +5 코인 보상!"
- "숏폼 20분 연속 시청! 잠깐 쉬고 +10 코인 받아가세요."
"""


def get_effectiveness_analysis_prompt(
    intervention_type: str, pre_usage: dict, post_usage: dict
) -> str:
    """효과성 분석 프롬프트 생성

    Args:
        intervention_type: 개입 유형
        pre_usage: 개입 전 사용 데이터
        post_usage: 개입 후 사용 데이터

    Returns:
        효과성 분석 프롬프트 문자열
    """
    return f"""
개입의 효과성을 분석하세요.

개입 유형: {intervention_type}

개입 전 사용 패턴:
- 사용 시간: {pre_usage["duration_seconds"]}초
- 앱 전환: {pre_usage["app_switches"]}회
- 세션 수: {pre_usage["sessions"]}회

개입 후 사용 패턴:
- 사용 시간: {post_usage["duration_after_intervention"]}초

평가 기준:
1. 사용 시간 감소 정도
2. 행동 변화 여부
3. 전반적인 개선도

효과 점수(0.0~1.0)를 계산하고, 행동 변화가 감지되었는지 판단하세요.
"""


def get_adjustment_decision_prompt(
    intervention_type: str, effectiveness_score: float, behavior_change_detected: bool
) -> str:
    """전략 조정 판단 프롬프트 생성

    Args:
        intervention_type: 개입 유형
        effectiveness_score: 효과 점수
        behavior_change_detected: 행동 변화 감지 여부

    Returns:
        전략 조정 판단 프롬프트 문자열
    """
    return f"""
개입의 효과성을 바탕으로 전략 조정이 필요한지 판단하세요.

개입 유형: {intervention_type}
효과 점수: {effectiveness_score:.2f}
행동 변화: {behavior_change_detected}

판단 기준:
- {EFFECTIVENESS_THRESHOLD_HIGH} 이상: 효과적, 조정 불필요
- {EFFECTIVENESS_THRESHOLD_LOW} ~ {EFFECTIVENESS_THRESHOLD_HIGH}: 보통, 미세 조정 고려
- {EFFECTIVENESS_THRESHOLD_LOW} 미만: 비효과적, 전략 변경 필요

전략 조정이 필요한지 판단하고, 필요하다면 새로운 전략을 제안하세요.
"""


def get_status_nudge_prompt(
    behavior_pattern: str, pattern_type: str, trigger_event: str, severity_score: int
) -> str:
    """Generate prompt for status message when intervention NOT needed

    Args:
        behavior_pattern: 행동 패턴 분석 결과
        pattern_type: 패턴 유형 (normal, concerning, critical)
        trigger_event: 트리거 이벤트
        severity_score: 심각도 점수 (0-10)

    Returns:
        LLM 프롬프트 (상태 메시지 생성용)
    """
    return f"""
사용자의 현재 행동 패턴이 정상 범위 내에 있어 개입이 필요하지 않습니다:

**분석 결과**:
- 패턴 유형: {pattern_type}
- 트리거 이벤트: {trigger_event}
- 심각도 점수: {severity_score}/10
- 패턴 상세: {behavior_pattern}

사용자에게 **개입이 필요하지 않은 구체적인 이유**와 함께 현재 상태가 양호함을 알리는 메시지를 생성하세요.

**요구사항**:
1. 최대 100자 이내 (한글 기준)
2. 개입이 필요하지 않은 **이유를 명확히 포함** (예: 사용 시간이 짧음, 정상 범위 내, 적절한 휴식 등)
3. 친근하고 격려하는 톤
4. 현재 디지털 습관이 건강함을 강조
5. 계속 유지할 것을 권장

**좋은 예시** (이유 포함):
- "사용 시간 3분으로 적절해요. 이대로 유지하세요!"
- "짧은 사용과 적절한 휴식 중! 건강한 습관입니다."
- "앱 전환이 적고 집중적이에요. 잘하고 있습니다!"
- "정상 범위 내 사용 중입니다. 계속 유지해주세요!"

**나쁜 예시** (이유 없음):
- "현재 앱 사용이 적절한 수준입니다." (왜 적절한지 이유 없음)
- "잘 하고 계세요!" (무엇을 잘하고 있는지 불명확)
"""
