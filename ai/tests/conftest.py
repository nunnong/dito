"""
Pytest Configuration and Shared Fixtures
인수인계 문서 패턴 적용: Mock, LLM-as-Judge, 재사용 가능한 fixture
"""

import os
from unittest.mock import Mock, patch

import pytest
from agent.schemas import (
    BehaviorAnalysis,
    EffectivenessAnalysis,
    InterventionDecision,
    NudgeMessage,
    StrategyAdjustment,
)


# =============================================================================
# Session Configuration
# =============================================================================

@pytest.fixture(scope="session")
def anyio_backend():
    """Async backend for anyio"""
    return "asyncio"


@pytest.fixture(scope="session", autouse=True)
def setup_langsmith_logging():
    """
    Auto-enable LangSmith logging for all tests
    인수인계 문서 패턴: 자동 LangSmith 추적
    """
    os.environ.setdefault("LANGSMITH_TRACING_V2", "true")
    os.environ.setdefault("LANGSMITH_PROJECT", "intervention-agent-tests")


# =============================================================================
# Mock LLM Fixtures (인수인계 문서 패턴: Mock 사용)
# =============================================================================

@pytest.fixture
def mock_behavior_analyzer():
    """
    Mock LLM for behavior analysis (deterministic testing)

    Usage:
        def test_my_function(mock_behavior_analyzer):
            mock_llm = mock_behavior_analyzer(pattern_type="critical", severity=9)
            with patch('agent.utils.behavior_analyzer', mock_llm):
                # Your test code
    """
    def create_mock(
        pattern_type: str = "concerning",
        trigger_event: str = "short-form-overuse",
        severity_score: int = 7,
        key_indicators: list = None,
        summary: str = "Test behavior analysis"
    ):
        mock = Mock()
        mock.invoke.return_value = BehaviorAnalysis(
            pattern_type=pattern_type,
            trigger_event=trigger_event,
            severity_score=severity_score,
            key_indicators=key_indicators or ["Test indicator 1", "Test indicator 2"],
            summary=summary
        )
        return mock
    return create_mock


@pytest.fixture
def mock_intervention_decider():
    """
    Mock LLM for intervention decision (deterministic testing)

    Usage:
        def test_my_function(mock_intervention_decider):
            mock_llm = mock_intervention_decider(needed=True, urgency="high")
            with patch('agent.utils.intervention_decider', mock_llm):
                # Your test code
    """
    def create_mock(
        intervention_needed: bool = True,
        intervention_type: str = "short-form-overuse",
        urgency_level: str = "high",
        reasoning: str = "Test reasoning"
    ):
        mock = Mock()
        mock.invoke.return_value = InterventionDecision(
            intervention_needed=intervention_needed,
            intervention_type=intervention_type,
            urgency_level=urgency_level,
            reasoning=reasoning
        )
        return mock
    return create_mock


@pytest.fixture
def mock_nudge_generator():
    """
    Mock LLM for nudge generation (deterministic testing)

    Usage:
        def test_my_function(mock_nudge_generator):
            mock_llm = mock_nudge_generator(message="Custom test message")
            with patch('agent.utils.nudge_generator', mock_llm):
                # Your test code
    """
    def create_mock(
        message: str = "30분째 시청 중이에요 → 5분 휴식 어때요? → 성공 시 +5 코인 보상",
        nudge_type: str = "rest-suggestion"
    ):
        mock = Mock()
        mock.invoke.return_value = NudgeMessage(
            message=message,
            nudge_type=nudge_type
        )
        return mock
    return create_mock


@pytest.fixture
def mock_effectiveness_analyzer():
    """Mock LLM for effectiveness analysis"""
    def create_mock(
        effectiveness_score: float = 0.8,
        behavior_change_detected: bool = True,
        summary: str = "Test effectiveness analysis"
    ):
        mock = Mock()
        mock.invoke.return_value = EffectivenessAnalysis(
            effectiveness_score=effectiveness_score,
            behavior_change_detected=behavior_change_detected,
            summary=summary
        )
        return mock
    return create_mock


@pytest.fixture
def mock_strategy_adjuster():
    """Mock LLM for strategy adjustment"""
    def create_mock(
        adjustment_needed: bool = False,
        adjustment_reason: str = "Test adjustment reason",
        new_strategy: str = None
    ):
        mock = Mock()
        mock.invoke.return_value = StrategyAdjustment(
            adjustment_needed=adjustment_needed,
            adjustment_reason=adjustment_reason,
            new_strategy=new_strategy
        )
        return mock
    return create_mock


# =============================================================================
# Database Mock Fixtures (인수인계 문서 패턴: DB 격리)
# =============================================================================

@pytest.fixture
def mock_db_operations():
    """
    Mock database operations for isolation

    Usage:
        def test_my_function(mock_db_operations):
            # save_intervention_to_db, simulate_* functions are mocked
            result = send_intervention(state)
            mock_db_operations["save"].assert_called_once()
    """
    with patch('agent.utils.save_intervention_to_db') as mock_save, \
         patch('agent.utils.simulate_behavior_log') as mock_behavior, \
         patch('agent.utils.simulate_post_intervention_usage') as mock_post:

        # Default mock return values
        mock_save.return_value = 12345  # intervention_id
        mock_behavior.return_value = {
            "app_name": "YouTube Shorts",
            "duration_seconds": 1200,
            "usage_timestamp": "2025-01-03T14:00:00",
            "recent_app_switches": 2
        }
        mock_post.return_value = {
            "user_id": 1,
            "intervention_id": 12345,
            "duration_after_intervention": 300,
            "behavior_changed": True
        }

        yield {
            "save": mock_save,
            "behavior": mock_behavior,
            "post_usage": mock_post
        }


# =============================================================================
# Sample Data Fixtures (인수인계 문서 패턴: 재사용 가능한 테스트 데이터)
# =============================================================================

@pytest.fixture
def sample_intervention_state():
    """
    Sample intervention state for testing

    Usage:
        def test_my_function(sample_intervention_state):
            state = sample_intervention_state
            result = analyze_behavior(state)
    """
    return {
        "user_id": 1,
        "behavior_log": {
            "app_name": "YouTube Shorts",
            "duration_seconds": 1500,  # 25분
            "usage_timestamp": "2025-01-03T14:00:00",
            "recent_app_switches": 2
        }
    }


@pytest.fixture
def sample_high_severity_input():
    """High severity input for testing intervention needed"""
    return {
        "user_id": 1,
        "behavior_log": {
            "app_name": "TikTok",
            "duration_seconds": 2400,  # 40분
            "usage_timestamp": "2025-01-03T23:30:00",  # 야간
            "recent_app_switches": 1
        }
    }


@pytest.fixture
def sample_low_severity_input():
    """Low severity input for testing no intervention"""
    return {
        "user_id": 1,
        "behavior_log": {
            "app_name": "Calculator",
            "duration_seconds": 120,  # 2분
            "usage_timestamp": "2025-01-03T10:00:00",
            "recent_app_switches": 1
        }
    }


@pytest.fixture
def sample_evaluation_state():
    """Sample evaluation state for testing"""
    return {
        "intervention_id": 12345,
        "user_id": 1,
        "intervention_type": "short-form-overuse",
        "pre_intervention_usage": {
            "duration_seconds": 1200,
            "app_switches": 5,
            "sessions": 2
        },
        "post_intervention_usage": {
            "user_id": 1,
            "intervention_id": 12345,
            "duration_after_intervention": 300,
            "behavior_changed": True
        }
    }


# =============================================================================
# Frequency Control Mock Fixtures (인수인계 문서 패턴: 비즈니스 로직 Mock)
# =============================================================================

@pytest.fixture
def mock_frequency_checker():
    """
    Mock frequency control checker

    Usage:
        def test_frequency_limit(mock_frequency_checker):
            mock_check = mock_frequency_checker(interventions_today=3, can_intervene=True)
            with patch('agent.intervention_agent.check_frequency', mock_check):
                # Your test code
    """
    def create_mock(
        can_intervene: bool = True,
        interventions_today: int = 0,
        last_intervention_time: str = None,
        hours_since_last: float = None
    ):
        mock = Mock()
        mock.return_value = {
            "can_intervene": can_intervene,
            "interventions_today": interventions_today,
            "last_intervention_time": last_intervention_time,
            "hours_since_last": hours_since_last
        }
        return mock
    return create_mock


# =============================================================================
# Test Utilities
# =============================================================================

@pytest.fixture
def assert_state_fields():
    """Helper to assert required fields in state"""
    def checker(state: dict, required_fields: list):
        for field in required_fields:
            assert field in state, f"Missing required field: {field}"
    return checker


@pytest.fixture
def create_thread_config():
    """Helper to create thread config for agent invocation"""
    def creator(thread_id: str = "test_thread"):
        return {"configurable": {"thread_id": thread_id}}
    return creator
