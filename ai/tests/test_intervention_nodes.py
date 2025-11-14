"""
Intervention Agent Node-level Unit Tests
pytest 기반 노드별 단위 테스트
"""

import pytest
from langsmith import testing as t

from agent.eval.intervention_dataset import test_inputs, test_names, test_expected
from agent.intervention_agent import (
    analyze_behavior,
    decide_intervention,
    generate_nudge,
    send_intervention
)
from agent.schemas import InterventionState

# =============================================================================
# Helper Functions
# =============================================================================

def create_test_cases_for_analysis():
    """analyze_behavior 테스트 케이스 생성"""
    test_cases = []
    for inp, name, expected in zip(test_inputs, test_names, test_expected):
        # 모든 케이스를 분석 노드 테스트에 포함
        test_cases.append((inp, name, expected))
    return test_cases

def create_test_cases_for_decision():
    """decide_intervention 테스트 케이스 생성 (개입 필요한 케이스만)"""
    test_cases = []
    for inp, name, expected in zip(test_inputs, test_names, test_expected):
        if expected.get("intervention_needed") in [True, False]:  # 명확한 케이스만
            test_cases.append((inp, name, expected))
    return test_cases

def create_test_cases_for_nudge():
    """generate_nudge 테스트 케이스 생성 (개입 필요한 케이스만)"""
    test_cases = []
    for inp, name, expected in zip(test_inputs, test_names, test_expected):
        if expected.get("intervention_needed") is True:
            test_cases.append((inp, name, expected))
    return test_cases

# =============================================================================
# Test: analyze_behavior Node
# =============================================================================

@pytest.mark.langsmith(output_keys=["trigger_event", "severity_score"])
@pytest.mark.parametrize("input_data,name,expected", create_test_cases_for_analysis())
def test_analyze_behavior_trigger_event(input_data, name, expected):
    """행동 분석 노드: 트리거 이벤트 분류 테스트"""
    # LangSmith 로깅
    t.log_inputs({"test": name, "node": "analyze_behavior"})

    # 상태 구성
    state: InterventionState = {
        "user_id": input_data["user_id"],
        "behavior_log": input_data["behavior_log"]
    }

    # 노드 실행
    result = analyze_behavior(state)

    # 결과 로깅
    t.log_outputs({
        "trigger_event": result.get("trigger_event"),
        "pattern_type": result.get("pattern_type"),
        "severity_score": result.get("severity_score")
    })

    # 트리거 이벤트 검증
    predicted_trigger = result.get("trigger_event")
    expected_triggers = expected.get("trigger_event", ["none"])

    if not isinstance(expected_triggers, list):
        expected_triggers = [expected_triggers]

    assert predicted_trigger in expected_triggers, \
        f"Expected trigger in {expected_triggers}, got {predicted_trigger}"

@pytest.mark.langsmith(output_keys=["severity_score"])
@pytest.mark.parametrize("input_data,name,expected", create_test_cases_for_analysis())
def test_analyze_behavior_severity_score(input_data, name, expected):
    """행동 분석 노드: 심각도 점수 범위 테스트"""
    # LangSmith 로깅
    t.log_inputs({"test": name, "node": "analyze_behavior"})

    # 상태 구성
    state: InterventionState = {
        "user_id": input_data["user_id"],
        "behavior_log": input_data["behavior_log"]
    }

    # 노드 실행
    result = analyze_behavior(state)

    # 심각도 점수 검증
    severity_score = result.get("severity_score", 0)
    expected_range = expected.get("severity_range", (0, 10))

    # 결과 로깅
    t.log_outputs({
        "severity_score": severity_score,
        "expected_range": expected_range
    })

    assert expected_range[0] <= severity_score <= expected_range[1], \
        f"Severity score {severity_score} not in range {expected_range}"

# =============================================================================
# Test: decide_intervention Node
# =============================================================================

@pytest.mark.langsmith(output_keys=["intervention_needed"])
@pytest.mark.parametrize("input_data,name,expected", create_test_cases_for_decision())
def test_decide_intervention_decision(input_data, name, expected):
    """개입 판단 노드: 개입 필요성 결정 테스트"""
    # LangSmith 로깅
    t.log_inputs({"test": name, "node": "decide_intervention"})

    # 상태 구성 (analyze_behavior 결과 시뮬레이션)
    state: InterventionState = {
        "user_id": input_data["user_id"],
        "behavior_log": input_data["behavior_log"],
        "behavior_pattern": "Test pattern",  # 실제로는 analyze_behavior 출력
        "trigger_event": expected.get("trigger_event", ["none"])[0] if isinstance(expected.get("trigger_event"), list) else expected.get("trigger_event", "none"),
        "severity_score": (expected.get("severity_range", (0, 0))[0] + expected.get("severity_range", (10, 10))[1]) // 2,
        "pattern_type": expected.get("pattern_type", ["normal"])[0] if isinstance(expected.get("pattern_type"), list) else expected.get("pattern_type", "normal")
    }

    # 노드 실행
    command = decide_intervention(state)

    # intervention_needed 추출
    if hasattr(command, 'update'):
        intervention_needed = command.update.get("intervention_needed", False)
    else:
        intervention_needed = False

    # 결과 로깅
    t.log_outputs({
        "intervention_needed": intervention_needed,
        "expected": expected.get("intervention_needed")
    })

    # 검증
    expected_needed = expected.get("intervention_needed", False)
    assert intervention_needed == expected_needed, \
        f"Expected intervention_needed={expected_needed}, got {intervention_needed}"

# =============================================================================
# Test: generate_nudge Node
# =============================================================================

@pytest.mark.langsmith(output_keys=["nudge_message", "has_frame"])
@pytest.mark.parametrize("input_data,name,expected", create_test_cases_for_nudge())
def test_generate_nudge_message(input_data, name, expected):
    """넛지 생성 노드: 메시지 생성 테스트"""
    # LangSmith 로깅
    t.log_inputs({"test": name, "node": "generate_nudge"})

    # 상태 구성
    intervention_type_list = expected.get("intervention_type", ["none"])
    if not isinstance(intervention_type_list, list):
        intervention_type_list = [intervention_type_list]

    urgency_level_list = expected.get("urgency_level", ["low"])
    if not isinstance(urgency_level_list, list):
        urgency_level_list = [urgency_level_list]

    state: InterventionState = {
        "user_id": input_data["user_id"],
        "behavior_log": input_data["behavior_log"],
        "behavior_pattern": "Test pattern",
        "intervention_needed": True,
        "intervention_type": intervention_type_list[0],
        "urgency_level": urgency_level_list[0]
    }

    # 노드 실행
    result = generate_nudge(state)

    # 메시지 검증
    nudge_message = result.get("nudge_message", "")

    # 기본 검증: 메시지가 생성되었는가?
    assert len(nudge_message) > 0, "Nudge message is empty"

    # 길이 검증: 50-200자 권장
    assert 20 <= len(nudge_message) <= 250, \
        f"Nudge message length {len(nudge_message)} not in recommended range"

    # 결과 로깅
    t.log_outputs({
        "nudge_message": nudge_message,
        "message_length": len(nudge_message)
    })

# =============================================================================
# Test: send_intervention Node
# =============================================================================

@pytest.mark.langsmith(output_keys=["intervention_id", "evaluation_scheduled"])
def test_send_intervention_execution():
    """개입 실행 노드: 실행 및 스케줄링 테스트"""
    # LangSmith 로깅
    t.log_inputs({"test": "send_intervention", "node": "send_intervention"})

    # 상태 구성
    state: InterventionState = {
        "user_id": 1,
        "behavior_log": {
            "app_name": "Test App",
            "duration_seconds": 1200,
            "usage_timestamp": "2025-01-03T14:00:00",
            "recent_app_switches": 2
        },
        "intervention_needed": True,
        "intervention_type": "short-form-overuse",
        "urgency_level": "high",
        "nudge_message": "Test nudge message",
        "nudge_type": "rest-suggestion"
    }

    # 노드 실행
    result = send_intervention(state)

    # 결과 검증
    assert "intervention_id" in result, "intervention_id not in result"
    assert "evaluation_scheduled_time" in result, "evaluation_scheduled_time not in result"
    assert result["intervention_id"] > 0, "intervention_id should be positive"

    # 결과 로깅
    t.log_outputs({
        "intervention_id": result["intervention_id"],
        "evaluation_scheduled_time": result["evaluation_scheduled_time"],
        "evaluation_delay_minutes": result.get("evaluation_delay_minutes")
    })

# =============================================================================
# Test: Integration (Sequential Node Execution)
# =============================================================================

@pytest.mark.langsmith(output_keys=["final_state"])
@pytest.mark.parametrize("input_data,name,expected", create_test_cases_for_decision()[:5])  # 처음 5개만
def test_sequential_node_execution(input_data, name, expected):
    """노드 순차 실행 통합 테스트"""
    # LangSmith 로깅
    t.log_inputs({"test": name, "integration": "sequential_nodes"})

    # 1. analyze_behavior
    state: InterventionState = {
        "user_id": input_data["user_id"],
        "behavior_log": input_data["behavior_log"]
    }

    analysis_result = analyze_behavior(state)
    state.update(analysis_result)

    # 2. decide_intervention
    decision_command = decide_intervention(state)

    if hasattr(decision_command, 'update'):
        state.update(decision_command.update)

    # 결과 로깅
    t.log_outputs({
        "trigger_event": state.get("trigger_event"),
        "intervention_needed": state.get("intervention_needed"),
        "severity_score": state.get("severity_score")
    })

    # 검증: 상태가 올바르게 업데이트되었는가?
    assert "trigger_event" in state, "trigger_event not in state"
    assert "intervention_needed" in state, "intervention_needed not in state"
    assert "severity_score" in state, "severity_score not in state"


# =============================================================================
# NEW: LLM-as-Judge Nudge Quality Tests (인수인계 문서 패턴)
# =============================================================================

from pydantic import BaseModel, Field
from langchain_anthropic import ChatAnthropic
from langchain_core.messages import SystemMessage, HumanMessage
from agent.eval.prompts import NUDGE_QUALITY_SYSTEM_PROMPT


class NudgeQualityGrade(BaseModel):
    """Pydantic schema for nudge quality evaluation (LLM-as-Judge)"""
    frame_completeness: int = Field(ge=1, le=5, description="[인식]-[제안]-[보상] 프레임 완성도")
    friendliness: int = Field(ge=1, le=5, description="친근함")
    specificity: int = Field(ge=1, le=5, description="구체성")
    positivity: int = Field(ge=1, le=5, description="긍정성")
    length_appropriateness: int = Field(ge=1, le=5, description="길이 적절성")
    total_score: int = Field(ge=5, le=25, description="총점")
    reasoning: str = Field(description="평가 근거")


@pytest.mark.langsmith(output_keys=["nudge_quality_grade"])
@pytest.mark.parametrize("input_data,name,expected", create_test_cases_for_nudge()[:5])  # 처음 5개만 (비용 절감)
def test_generate_nudge_quality_llm_judge(input_data, name, expected):
    """넛지 품질 평가 (LLM-as-Judge) - 인수인계 문서 패턴"""
    # LangSmith 로깅
    t.log_inputs({"test": name, "evaluation": "llm_as_judge"})

    # 상태 구성
    intervention_type_list = expected.get("intervention_type", ["none"])
    if not isinstance(intervention_type_list, list):
        intervention_type_list = [intervention_type_list]

    urgency_level_list = expected.get("urgency_level", ["low"])
    if not isinstance(urgency_level_list, list):
        urgency_level_list = [urgency_level_list]

    state: InterventionState = {
        "user_id": input_data["user_id"],
        "behavior_log": input_data["behavior_log"],
        "behavior_pattern": "Test pattern",
        "intervention_needed": True,
        "intervention_type": intervention_type_list[0],
        "urgency_level": urgency_level_list[0]
    }

    # 넛지 생성
    result = generate_nudge(state)
    nudge_message = result.get("nudge_message", "")

    # LLM-as-Judge evaluation
    judge_llm = ChatAnthropic(model="claude-3-5-sonnet-20241022").with_structured_output(NudgeQualityGrade)

    # 평가 프롬프트 구성
    eval_prompt = f"""
<개입_상황>
- 앱: {input_data["behavior_log"]["app_name"]}
- 사용 시간: {input_data["behavior_log"]["duration_seconds"]}초
- 트리거 이벤트: {state.get("intervention_type")}
- 개입 유형: {state.get("intervention_type")}
</개입_상황>

<넛지_메시지>
{nudge_message}
</넛지_메시지>

위 기준에 따라 넛지 메시지를 평가하고, 각 항목별 점수와 근거를 제시하세요.
"""

    grade = judge_llm.invoke([
        SystemMessage(content=NUDGE_QUALITY_SYSTEM_PROMPT),
        HumanMessage(content=eval_prompt)
    ])

    # 결과 로깅
    t.log_outputs({
        "nudge_message": nudge_message,
        "quality_grade": grade.model_dump()
    })

    # Assertions (품질 기준)
    assert grade.frame_completeness >= 3, f"Frame incomplete: {grade.reasoning}"
    assert grade.friendliness >= 3, f"Not friendly enough: {grade.reasoning}"
    assert grade.total_score >= 15, f"Quality too low: {grade.total_score}/25"
    assert 3 <= grade.length_appropriateness <= 5, f"Length inappropriate: {grade.reasoning}"


# =============================================================================
# NEW: Mock-based Unit Tests (인수인계 문서 패턴: 빠른 테스트)
# =============================================================================

from unittest.mock import patch


def test_analyze_behavior_with_mock(mock_behavior_analyzer, sample_intervention_state):
    """Mock LLM을 사용한 빠른 단위 테스트"""
    # Mock LLM 생성
    mock_llm = mock_behavior_analyzer(
        pattern_type="critical",
        trigger_event="short-form-overuse",
        severity_score=9
    )

    # Mock을 사용하여 노드 실행
    with patch('agent.utils.behavior_analyzer', mock_llm):
        result = analyze_behavior(sample_intervention_state)

        # 검증: Mock이 호출되었는가?
        mock_llm.invoke.assert_called_once()

        # 검증: 예상된 결과인가?
        assert result["pattern_type"] == "critical"
        assert result["trigger_event"] == "short-form-overuse"
        assert result["severity_score"] == 9


def test_decide_intervention_with_mock(mock_intervention_decider, sample_intervention_state):
    """Mock LLM을 사용한 개입 판단 테스트"""
    # 상태 준비
    state = sample_intervention_state.copy()
    state.update({
        "behavior_pattern": "Test pattern",
        "trigger_event": "short-form-overuse",
        "severity_score": 8
    })

    # Mock LLM 생성
    mock_llm = mock_intervention_decider(
        intervention_needed=True,
        intervention_type="short-form-overuse",
        urgency_level="high"
    )

    # Mock을 사용하여 노드 실행
    with patch('agent.utils.intervention_decider', mock_llm):
        command = decide_intervention(state)

        # 검증
        mock_llm.invoke.assert_called_once()
        assert hasattr(command, 'update')
        assert command.update["intervention_needed"] == True


def test_generate_nudge_with_mock(mock_nudge_generator, sample_intervention_state):
    """Mock LLM을 사용한 넛지 생성 테스트"""
    # 상태 준비
    state = sample_intervention_state.copy()
    state.update({
        "behavior_pattern": "Test pattern",
        "intervention_needed": True,
        "intervention_type": "short-form-overuse",
        "urgency_level": "high"
    })

    # Mock LLM 생성 (커스텀 메시지)
    custom_message = "테스트 넛지 메시지 → 휴식 제안 → +5 코인"
    mock_llm = mock_nudge_generator(message=custom_message)

    # Mock을 사용하여 노드 실행
    with patch('agent.utils.nudge_generator', mock_llm):
        result = generate_nudge(state)

        # 검증
        mock_llm.invoke.assert_called_once()
        assert result["nudge_message"] == custom_message


# =============================================================================
# NEW: Error Handling Tests (인수인계 문서 패턴: 에러 처리)
# =============================================================================

def test_analyze_behavior_with_missing_behavior_log():
    """필수 필드 누락 시 에러 처리 테스트"""
    # behavior_log 없는 상태
    state: InterventionState = {
        "user_id": 1
    }

    # ValueError 발생 예상
    with pytest.raises(ValueError, match="behavior_log is required"):
        analyze_behavior(state)


def test_analyze_behavior_with_llm_failure(mock_behavior_analyzer, sample_intervention_state):
    """LLM 실패 시 우아한 실패 처리 테스트"""
    # Mock LLM이 예외를 발생시키도록 설정
    mock_llm = mock_behavior_analyzer()
    mock_llm.invoke.side_effect = Exception("LLM connection failed")

    # 예외가 전파되는지 확인 (현재는 처리되지 않음)
    with patch('agent.utils.behavior_analyzer', mock_llm):
        with pytest.raises(Exception, match="LLM connection failed"):
            analyze_behavior(sample_intervention_state)


def test_send_intervention_with_db_failure(mock_db_operations, sample_intervention_state):
    """DB 저장 실패 시 에러 처리 테스트"""
    # 상태 준비
    state = sample_intervention_state.copy()
    state.update({
        "intervention_needed": True,
        "intervention_type": "short-form-overuse",
        "urgency_level": "high",
        "nudge_message": "Test message",
        "nudge_type": "rest-suggestion"
    })

    # DB Mock이 예외를 발생시키도록 설정
    mock_db_operations["save"].side_effect = Exception("Database connection failed")

    # 예외가 전파되는지 확인
    with pytest.raises(Exception, match="Database connection failed"):
        send_intervention(state)


# =============================================================================
# NEW: DB Validation Tests (인수인계 문서 패턴: DB 호출 검증)
# =============================================================================

def test_send_intervention_db_called_correctly(mock_db_operations):
    """DB 저장 함수가 올바른 데이터로 호출되는지 검증"""
    # 상태 구성
    state: InterventionState = {
        "user_id": 1,
        "behavior_log": {
            "app_name": "YouTube Shorts",
            "duration_seconds": 1500,
            "usage_timestamp": "2025-01-03T14:00:00",
            "recent_app_switches": 2
        },
        "intervention_needed": True,
        "intervention_type": "short-form-overuse",
        "urgency_level": "high",
        "nudge_message": "Test nudge message",
        "nudge_type": "rest-suggestion"
    }

    # 노드 실행
    result = send_intervention(state)

    # DB save 호출 검증
    mock_db_operations["save"].assert_called_once()

    # 호출 인자 검증
    call_args = mock_db_operations["save"].call_args[0][0]
    assert call_args["user_id"] == 1
    assert call_args["intervention_type"] == "short-form-overuse"
    assert call_args["nudge_message"] == "Test nudge message"

    # 반환값 검증
    assert result["intervention_id"] == 12345  # Mock 반환값
