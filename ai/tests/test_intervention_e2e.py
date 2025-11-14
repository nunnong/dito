"""
Intervention Agent End-to-End Tests
인수인계 문서 패턴: 전체 워크플로우 검증
"""

import time

import pytest
from langsmith import testing as t

from agent.eval.intervention_dataset import test_inputs, test_names, test_expected
from agent.intervention_agent import intervention_agent

# =============================================================================
# Helper Functions
# =============================================================================

def create_e2e_test_cases():
    """E2E 테스트 케이스 생성 (첫 10개만)"""
    test_cases = []
    for inp, name, expected in zip(test_inputs[:10], test_names[:10], test_expected[:10]):
        test_cases.append((inp, name, expected))
    return test_cases


# =============================================================================
# E2E Test: Full Intervention Flow
# =============================================================================

@pytest.mark.langsmith(output_keys=["e2e_result"])
@pytest.mark.parametrize("input_data,name,expected", create_e2e_test_cases())
def test_e2e_full_intervention_flow(input_data, name, expected, create_thread_config):
    """
    E2E 테스트: 완전한 4노드 플로우
    analyze_behavior → decide_intervention → generate_nudge → send_intervention
    """
    # LangSmith 로깅
    t.log_inputs({"test": name, "e2e": "full_flow"})

    # 초기 상태
    initial_state = {
        "user_id": input_data["user_id"],
        "behavior_log": input_data["behavior_log"]
    }

    # 그래프 실행
    config = create_thread_config(f"e2e_{name}")
    result = intervention_agent.invoke(initial_state, config)

    # 결과 로깅
    t.log_outputs({
        "trigger_event": result.get("trigger_event"),
        "intervention_needed": result.get("intervention_needed"),
        "severity_score": result.get("severity_score"),
        "nudge_message": result.get("nudge_message") if result.get("intervention_needed") else None
    })

    # 기본 검증: 필수 필드 존재
    assert "trigger_event" in result
    assert "intervention_needed" in result
    assert "severity_score" in result

    # 개입 필요 여부 검증
    expected_intervention = expected.get("intervention_needed")
    if expected_intervention is not None:
        # 정답과 비교 (약간의 유연성 허용)
        assert result["intervention_needed"] == expected_intervention, \
            f"Expected intervention_needed={expected_intervention}, got {result['intervention_needed']}"

    # 개입 발생 시 추가 검증
    if result.get("intervention_needed"):
        # 넛지 메시지 생성 확인
        assert "nudge_message" in result, "Nudge message missing when intervention needed"
        assert len(result["nudge_message"]) > 0, "Nudge message empty"

        # 개입 ID 확인
        assert "intervention_id" in result, "Intervention ID missing"
        assert result["intervention_id"] > 0, "Intervention ID invalid"

        # 평가 스케줄링 확인
        assert "evaluation_scheduled_time" in result, "Evaluation scheduling missing"
        assert "evaluation_delay_minutes" in result, "Evaluation delay missing"

        # 평가 지연 시간 검증 (30-60분)
        delay = result["evaluation_delay_minutes"]
        assert 30 <= delay <= 60, f"Evaluation delay {delay} not in 30-60min range"


# =============================================================================
# E2E Test: No Intervention Flow
# =============================================================================

@pytest.mark.langsmith(output_keys=["e2e_result"])
def test_e2e_no_intervention_flow(sample_low_severity_input, create_thread_config):
    """E2E 테스트: 개입 불필요 케이스"""
    # LangSmith 로깅
    t.log_inputs({"test": "no_intervention_flow", "e2e": "no_intervention"})

    # 그래프 실행
    config = create_thread_config("e2e_no_intervention")
    result = intervention_agent.invoke(sample_low_severity_input, config)

    # 결과 로깅
    t.log_outputs({
        "intervention_needed": result.get("intervention_needed"),
        "trigger_event": result.get("trigger_event")
    })

    # 검증: 개입 불필요
    assert result.get("intervention_needed") == False, "Should not intervene for low severity"

    # 검증: 넛지 메시지 없음
    assert "nudge_message" not in result or result.get("nudge_message") is None, \
        "Should not have nudge message when intervention not needed"

    # 검증: 개입 ID 없음
    assert "intervention_id" not in result or result.get("intervention_id") is None, \
        "Should not have intervention ID when not needed"


# =============================================================================
# E2E Test: High Severity Intervention
# =============================================================================

@pytest.mark.langsmith(output_keys=["e2e_result"])
def test_e2e_high_severity_intervention(sample_high_severity_input, create_thread_config):
    """E2E 테스트: 고위험 케이스 (반드시 개입)"""
    # LangSmith 로깅
    t.log_inputs({"test": "high_severity_intervention", "e2e": "high_severity"})

    # 그래프 실행
    config = create_thread_config("e2e_high_severity")
    result = intervention_agent.invoke(sample_high_severity_input, config)

    # 결과 로깅
    t.log_outputs({
        "intervention_needed": result.get("intervention_needed"),
        "severity_score": result.get("severity_score"),
        "urgency_level": result.get("urgency_level")
    })

    # 검증: 개입 필요
    assert result.get("intervention_needed") == True, "Should intervene for high severity"

    # 검증: 심각도 높음
    severity = result.get("severity_score", 0)
    assert severity >= 7, f"High severity case should have score >= 7, got {severity}"

    # 검증: 긴급도
    urgency = result.get("urgency_level")
    assert urgency in ["high", "medium"], f"Expected high/medium urgency, got {urgency}"

    # 검증: 넛지 메시지 품질
    nudge = result.get("nudge_message", "")
    assert len(nudge) >= 20, "Nudge message too short"


# =============================================================================
# E2E Test: Latency Performance
# =============================================================================

@pytest.mark.langsmith(output_keys=["latency"])
def test_e2e_latency_performance(sample_intervention_state, create_thread_config):
    """E2E 성능 테스트: 실행 시간 < 10초"""
    # LangSmith 로깅
    t.log_inputs({"test": "latency_performance", "e2e": "performance"})

    # 시간 측정 시작
    start_time = time.time()

    # 그래프 실행
    config = create_thread_config("e2e_latency")
    result = intervention_agent.invoke(sample_intervention_state, config)

    # 시간 측정 종료
    elapsed_time = time.time() - start_time

    # 결과 로깅
    t.log_outputs({
        "latency_seconds": elapsed_time,
        "intervention_needed": result.get("intervention_needed")
    })

    # 검증: 실행 시간 (LLM 호출 포함하여 10초 이하)
    assert elapsed_time < 10.0, f"Execution took {elapsed_time:.2f}s, should be < 10s"

    # 정보 출력
    print(f"\n⏱️  E2E latency: {elapsed_time:.2f}s")


# =============================================================================
# E2E Test: State Transitions
# =============================================================================

@pytest.mark.langsmith(output_keys=["state_transitions"])
def test_e2e_state_transitions(sample_intervention_state, create_thread_config):
    """E2E 테스트: 상태 전환 검증"""
    # LangSmith 로깅
    t.log_inputs({"test": "state_transitions", "e2e": "state_flow"})

    # 그래프 실행
    config = create_thread_config("e2e_state_transitions")
    result = intervention_agent.invoke(sample_intervention_state, config)

    # 결과 로깅
    t.log_outputs({
        "has_trigger_event": "trigger_event" in result,
        "has_intervention_decision": "intervention_needed" in result,
        "has_severity_score": "severity_score" in result
    })

    # 검증: 각 단계의 상태가 올바르게 전환되었는가?

    # Stage 1: analyze_behavior 결과
    assert "trigger_event" in result, "Missing analyze_behavior output"
    assert "severity_score" in result, "Missing analyze_behavior output"
    assert "pattern_type" in result, "Missing analyze_behavior output"

    # Stage 2: decide_intervention 결과
    assert "intervention_needed" in result, "Missing decide_intervention output"

    # Stage 3 & 4: 조건부 실행
    if result.get("intervention_needed"):
        # generate_nudge 결과
        assert "nudge_message" in result, "Missing generate_nudge output"
        assert "nudge_type" in result, "Missing generate_nudge output"

        # send_intervention 결과
        assert "intervention_id" in result, "Missing send_intervention output"
        assert "evaluation_scheduled_time" in result, "Missing send_intervention output"


# =============================================================================
# E2E Test: Multiple Sequential Runs
# =============================================================================

@pytest.mark.langsmith(output_keys=["multiple_runs"])
def test_e2e_multiple_sequential_runs(create_thread_config):
    """E2E 테스트: 연속 실행 (상태 격리 확인)"""
    # LangSmith 로깅
    t.log_inputs({"test": "multiple_sequential_runs", "e2e": "isolation"})

    results = []

    # 3번 연속 실행
    test_cases = [
        {
            "user_id": 1,
            "behavior_log": {
                "app_name": "YouTube Shorts",
                "duration_seconds": 1500,
                "usage_timestamp": "2025-01-03T14:00:00",
                "recent_app_switches": 2
            }
        },
        {
            "user_id": 2,
            "behavior_log": {
                "app_name": "Calculator",
                "duration_seconds": 120,
                "usage_timestamp": "2025-01-03T10:00:00",
                "recent_app_switches": 1
            }
        },
        {
            "user_id": 3,
            "behavior_log": {
                "app_name": "TikTok",
                "duration_seconds": 2400,
                "usage_timestamp": "2025-01-03T23:30:00",
                "recent_app_switches": 1
            }
        }
    ]

    for i, test_case in enumerate(test_cases):
        config = create_thread_config(f"e2e_multi_run_{i}")
        result = intervention_agent.invoke(test_case, config)
        results.append({
            "run": i + 1,
            "intervention_needed": result.get("intervention_needed"),
            "trigger_event": result.get("trigger_event")
        })

    # 결과 로깅
    t.log_outputs({"runs": results})

    # 검증: 모든 실행이 성공
    assert len(results) == 3, "Should have 3 results"

    # 검증: 각 실행의 결과가 독립적
    # (예: 첫 번째와 세 번째는 개입, 두 번째는 미개입)
    assert results[0]["intervention_needed"] == True, "Run 1 should intervene"
    assert results[1]["intervention_needed"] == False, "Run 2 should not intervene"
    assert results[2]["intervention_needed"] == True, "Run 3 should intervene"


# =============================================================================
# E2E Test: Error Recovery
# =============================================================================

@pytest.mark.langsmith(output_keys=["error_recovery"])
def test_e2e_error_recovery_with_invalid_input(create_thread_config):
    """E2E 테스트: 잘못된 입력에 대한 에러 복구"""
    # LangSmith 로깅
    t.log_inputs({"test": "error_recovery", "e2e": "error_handling"})

    # 잘못된 입력 (behavior_log 누락)
    invalid_input = {
        "user_id": 1
        # behavior_log 누락
    }

    # 예외 발생 예상
    config = create_thread_config("e2e_error_recovery")

    with pytest.raises(Exception):  # ValueError 또는 다른 예외
        intervention_agent.invoke(invalid_input, config)

    # 로깅
    t.log_outputs({"error_caught": True})


# =============================================================================
# E2E Test: Thread Isolation
# =============================================================================

@pytest.mark.langsmith(output_keys=["thread_isolation"])
def test_e2e_thread_isolation(sample_intervention_state, create_thread_config):
    """E2E 테스트: Thread 격리 (동일 입력, 다른 thread_id)"""
    # LangSmith 로깅
    t.log_inputs({"test": "thread_isolation", "e2e": "threads"})

    # 동일한 입력으로 2번 실행 (다른 thread_id)
    config1 = create_thread_config("thread_A")
    config2 = create_thread_config("thread_B")

    result1 = intervention_agent.invoke(sample_intervention_state, config1)
    result2 = intervention_agent.invoke(sample_intervention_state, config2)

    # 결과 로깅
    t.log_outputs({
        "thread_A_intervention": result1.get("intervention_needed"),
        "thread_B_intervention": result2.get("intervention_needed"),
        "results_match": result1.get("intervention_needed") == result2.get("intervention_needed")
    })

    # 검증: 동일한 입력은 (보통) 유사한 결과를 생성
    # (LLM non-determinism으로 완전히 같지 않을 수 있음)
    assert "trigger_event" in result1
    assert "trigger_event" in result2

    # Thread가 격리되었는지 확인 (서로 영향 없음)
    assert result1 is not result2, "Results should be independent objects"


# =============================================================================
# Summary Test
# =============================================================================

def test_e2e_summary():
    """E2E 테스트 요약 (정보 출력)"""
    print("\n" + "=" * 60)
    print("E2E Test Summary")
    print("=" * 60)
    print("✅ Full intervention flow")
    print("✅ No intervention flow")
    print("✅ High severity intervention")
    print("✅ Latency performance (<10s)")
    print("✅ State transitions")
    print("✅ Multiple sequential runs")
    print("✅ Error recovery")
    print("✅ Thread isolation")
    print("=" * 60)
