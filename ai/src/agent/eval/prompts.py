"""
Evaluation Prompts for Intervention Agent
평가용 프롬프트 템플릿 모음
"""

# =============================================================================
# 1. Trigger Event Classification Evaluator
# =============================================================================

TRIGGER_EVENT_EVALUATOR_PROMPT = """
<Task>
당신은 디지털 웰빙 에이전트의 트리거 이벤트 분류 정확성을 평가합니다.

트리거 이벤트 카테고리:
1. **short-form-overuse**: 20분 이상 숏폼 비디오 시청 (YouTube Shorts, TikTok, Reels)
2. **bedtime-usage**: 취침 시간(22시 이후) 스마트폰 사용
3. **app-switching**: 5분 내 10회 이상 앱 전환
4. **focus-break**: 30분 이상 연속 사용 (휴식 필요)
5. **none**: 정상 사용 패턴

평가 기준:
- 예측된 트리거가 정답 트리거와 정확히 일치하는가?
- 정답이 여러 개인 경우, 그 중 하나와 일치하면 정확함
- 컨텍스트(시간대, 앱 종류, 사용 시간)를 고려했는가?
</Task>

<input>
입력 데이터:
{input_data}
</input>

<agent_prediction>
에이전트가 예측한 트리거:
{predicted_trigger}
</agent_prediction>

<ground_truth>
정답 트리거:
{expected_triggers}
</ground_truth>

예측이 정답과 일치하는지 평가하고, 근거를 제시하세요.
"""

# =============================================================================
# 2. Intervention Decision Evaluator
# =============================================================================

INTERVENTION_DECISION_EVALUATOR_PROMPT = """
<Task>
당신은 디지털 웰빙 에이전트의 개입 필요성 판단 적절성을 평가합니다.

개입 결정 기준:
- **True (개입 필요)**:
  - 심각도 점수 >= 5
  - 트리거 이벤트가 "none"이 아님
  - 사용자 웰빙에 부정적 영향 우려

- **False (개입 불필요)**:
  - 심각도 점수 < 5
  - 트리거 이벤트가 "none"
  - 정상 사용 패턴

빈도 제한 고려:
- 하루 최대 4회 개입
- 개입 간 최소 2-3시간 간격

False Positive vs False Negative:
- False Positive (불필요한 개입): 사용자 경험 저하, 피로감 유발
- False Negative (필요한데 안함): 웰빙 개선 기회 상실

평가 기준:
- 개입 결정이 트리거 이벤트 및 심각도와 일관성 있는가?
- False Positive/Negative의 비용을 적절히 고려했는가?
</Task>

<input>
입력 데이터:
{input_data}
</input>

<analysis_result>
행동 분석 결과:
- 트리거 이벤트: {trigger_event}
- 심각도 점수: {severity_score}
- 패턴 유형: {pattern_type}
</analysis_result>

<agent_decision>
에이전트 개입 결정:
- intervention_needed: {intervention_needed}
- intervention_type: {intervention_type}
- urgency_level: {urgency_level}
</agent_decision>

<ground_truth>
정답:
- 예상 개입 필요: {expected_intervention}
- 예상 개입 유형: {expected_type}
- 예상 긴급도: {expected_urgency}
</ground_truth>

개입 결정이 적절한지 평가하고, 근거를 제시하세요.
"""

# =============================================================================
# 3. Nudge Quality Evaluator (LLM-as-Judge)
# =============================================================================

NUDGE_QUALITY_SYSTEM_PROMPT = """
당신은 디지털 웰빙 넛지 메시지의 품질을 평가하는 전문가입니다.

평가 기준 (각 1-5점 척도):

1. **프레임 완성도** (1-5점)
   - [인식]: 현재 행동/상황을 명확히 인지시키는가?
   - [제안]: 구체적인 행동 제안을 제공하는가?
   - [보상]: 긍정적 보상(코인, 칭찬 등)을 제시하는가?
   - 5점: 3요소 모두 명확함
   - 3점: 2요소 포함
   - 1점: 1요소만 포함 또는 불명확

2. **친근함 (Friendliness)** (1-5점)
   - 따뜻하고 공감적인 톤인가?
   - 비난하거나 명령하는 톤이 아닌가?
   - 5점: 매우 친근하고 공감적
   - 3점: 중립적
   - 1점: 명령적이거나 차가움

3. **구체성 (Specificity)** (1-5점)
   - 제안이 구체적이고 실행 가능한가?
   - 막연하지 않고 명확한가?
   - 5점: 매우 구체적
   - 3점: 보통
   - 1점: 막연함

4. **긍정성 (Positivity)** (1-5점)
   - 긍정적 프레이밍을 사용하는가?
   - 부정적 표현("하지 마세요")을 피했는가?
   - 5점: 매우 긍정적
   - 3점: 중립적
   - 1점: 부정적

5. **길이 적절성** (1-5점)
   - 1-2문장으로 간결한가?
   - 50-150자 범위인가?
   - 5점: 매우 적절
   - 3점: 약간 길거나 짧음
   - 1점: 너무 길거나 짧음

최종 평가:
- 총점: 각 항목 점수 합계 (5-25점)
- 정규화 점수: 총점 / 25 * 5 (1-5점 척도로 변환)
- 근거: 각 항목에 대한 구체적인 평가 이유

중요: 객관적이고 일관되게 평가하세요.
"""

NUDGE_QUALITY_USER_PROMPT = """
<개입_상황>
- 앱: {app_name}
- 사용 시간: {duration_seconds}초
- 트리거 이벤트: {trigger_event}
- 개입 유형: {intervention_type}
</개입_상황>

<넛지_메시지>
{nudge_message}
</넛지_메시지>

<평가_기준>
{additional_criteria}
</평가_기준>

위 기준에 따라 넛지 메시지를 평가하고, 각 항목별 점수와 근거를 제시하세요.
"""

# =============================================================================
# 4. Nudge Frame Completeness Checker
# =============================================================================

NUDGE_FRAME_CHECKER_PROMPT = """
<Task>
넛지 메시지가 [인식] → [제안] → [보상] 프레임을 완전히 포함하는지 확인합니다.

프레임 요소:
1. **[인식]**: 현재 행동/상황을 인지시킴
   - 예: "30분째 시청 중이에요", "밤 11시가 넘었네요", "여러 앱을 빠르게 전환하고 있어요"

2. **[제안]**: 구체적인 행동 제안
   - 예: "5분 휴식 어때요?", "이제 잠자리에 들 시간이에요", "집중 모드를 켜보세요"

3. **[보상]**: 긍정적 보상 제시
   - 예: "+5 코인 보상", "내일 더 상쾌한 아침", "집중력 향상"

평가 기준:
- **완전 (Complete)**: 3요소 모두 명확히 포함
- **부분 (Partial)**: 2요소만 포함
- **불완전 (Incomplete)**: 1요소 이하
</Task>

<nudge_message>
{nudge_message}
</nudge_message>

메시지를 분석하여:
1. 각 요소가 포함되어 있는지 판단
2. 포함된 요소를 구체적으로 인용
3. 완성도 평가 (Complete/Partial/Incomplete)
4. 개선 제안 (필요 시)

를 제시하세요.
"""

# =============================================================================
# 5. Severity Score Calibration Evaluator
# =============================================================================

SEVERITY_CALIBRATION_EVALUATOR_PROMPT = """
<Task>
당신은 행동 패턴의 심각도 점수(0-10) 적절성을 평가합니다.

심각도 점수 가이드라인:
- **0-2 (정상)**: 일상적 사용, 웰빙에 영향 없음
- **3-4 (주의)**: 약간 우려되지만 개입 불필요
- **5-6 (관심)**: 모니터링 필요, 경고 수준
- **7-8 (우려)**: 개입 권장, 웰빙에 부정적 영향
- **9-10 (심각)**: 즉각 개입 필요, 건강 위험

평가 요소:
1. **사용 시간**: 앱 카테고리별 적정 사용 시간
   - 숏폼: 20분+ → 높은 심각도
   - 생산성: 60분+ → 중간 심각도
   - 유틸리티: 시간과 무관

2. **시간대**: 취침 시간(22시 이후) → 심각도 +2
3. **앱 전환**: 10회+ → 심각도 +1-2
4. **앱 카테고리**: 중독성 높은 앱 → 심각도 높음

평가 기준:
- 예측 점수가 정답 범위 내에 있는가?
- 컨텍스트를 적절히 고려했는가?
- 일관성 있는 점수 체계인가?
</Task>

<input>
입력 데이터:
{input_data}
</input>

<agent_analysis>
에이전트 분석:
- 심각도 점수: {severity_score}
- 트리거 이벤트: {trigger_event}
- 패턴 유형: {pattern_type}
- 주요 지표: {key_indicators}
</agent_analysis>

<ground_truth>
정답 심각도 범위:
{expected_severity_range}
</ground_truth>

심각도 점수가 적절한지 평가하고, 근거를 제시하세요.
만약 범위를 벗어났다면, 어떤 요소를 과대/과소평가했는지 설명하세요.
"""

# =============================================================================
# Helper Functions
# =============================================================================

def format_input_data(behavior_log: dict) -> str:
    """입력 데이터를 읽기 쉬운 형식으로 포맷팅"""
    return f"""
- 앱: {behavior_log.get('app_name', 'Unknown')}
- 사용 시간: {behavior_log.get('duration_seconds', 0)}초 ({behavior_log.get('duration_seconds', 0) // 60}분)
- 사용 시점: {behavior_log.get('usage_timestamp', 'Unknown')}
- 앱 전환 횟수: {behavior_log.get('recent_app_switches', 0)}회
"""

def format_trigger_list(triggers: list) -> str:
    """트리거 리스트를 포맷팅"""
    if isinstance(triggers, str):
        return triggers
    return " 또는 ".join(triggers)

def format_severity_range(range_tuple: tuple) -> str:
    """심각도 범위를 포맷팅"""
    if isinstance(range_tuple, tuple) and len(range_tuple) == 2:
        return f"{range_tuple[0]}-{range_tuple[1]}점"
    return str(range_tuple)
