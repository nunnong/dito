"""
Intervention Agent Evaluation Script
LangSmith를 사용한 자동화된 평가 실행 및 시각화
"""

import os
from datetime import datetime
from typing import Any

import matplotlib.pyplot as plt
from langsmith import Client

from agent.eval.intervention_dataset import langsmith_examples
from agent.intervention_agent import intervention_agent

# =============================================================================
# Configuration
# =============================================================================

DATASET_NAME = "Intervention Agent Evaluation Dataset v1.0"
EXPERIMENT_PREFIX = "Intervention Agent"

# LangSmith Client 초기화
client = Client()

# =============================================================================
# Target Function (에이전트 실행)
# =============================================================================

def target_intervention_agent(inputs: dict) -> dict:
    """
    Intervention Agent를 실행하고 결과를 반환

    Args:
        inputs: {"user_id": int, "behavior_log": dict}

    Returns:
        dict: 에이전트 실행 결과
    """
    try:
        # Thread config 생성
        config = {"configurable": {"thread_id": f"eval_user_{inputs['user_id']}"}}

        # 에이전트 실행
        result = intervention_agent.invoke(inputs, config)

        # 결과 포맷팅
        return {
            "trigger_event": result.get("trigger_event", "none"),
            "pattern_type": result.get("pattern_type", "normal"),
            "severity_score": result.get("severity_score", 0),
            "intervention_needed": result.get("intervention_needed", False),
            "intervention_type": result.get("intervention_type", "none"),
            "urgency_level": result.get("urgency_level", "low"),
            "nudge_message": result.get("nudge_message", ""),
            "nudge_type": result.get("nudge_type", ""),
            "key_indicators": result.get("key_indicators", []),
            "status": "success"
        }
    except Exception as e:
        print(f"Error executing agent: {e}")
        return {
            "trigger_event": "error",
            "pattern_type": "error",
            "severity_score": -1,
            "intervention_needed": False,
            "intervention_type": "error",
            "urgency_level": "error",
            "nudge_message": "",
            "nudge_type": "",
            "key_indicators": [],
            "status": "error",
            "error_message": str(e)
        }

# =============================================================================
# Evaluator Functions
# =============================================================================

def trigger_event_accuracy_evaluator(outputs: dict, reference_outputs: dict) -> dict:
    """
    트리거 이벤트 분류 정확성 평가

    Returns:
        dict: {"key": "trigger_event_accuracy", "score": float}
    """
    if outputs.get("status") == "error":
        return {"key": "trigger_event_accuracy", "score": 0.0}

    predicted = outputs.get("trigger_event", "none")
    expected_list = reference_outputs.get("trigger_event", ["none"])

    # expected가 리스트가 아니면 리스트로 변환
    if not isinstance(expected_list, list):
        expected_list = [expected_list]

    # 예측이 정답 리스트 중 하나와 일치하면 정확
    is_correct = predicted in expected_list

    return {
        "key": "trigger_event_accuracy",
        "score": 1.0 if is_correct else 0.0
    }

def intervention_decision_evaluator(outputs: dict, reference_outputs: dict) -> dict:
    """
    개입 필요성 판단 정확성 평가

    Returns:
        dict: {"key": "intervention_decision_accuracy", "score": float}
    """
    if outputs.get("status") == "error":
        return {"key": "intervention_decision_accuracy", "score": 0.0}

    predicted = outputs.get("intervention_needed", False)
    expected = reference_outputs.get("intervention_needed", False)

    is_correct = predicted == expected

    return {
        "key": "intervention_decision_accuracy",
        "score": 1.0 if is_correct else 0.0
    }

def severity_score_evaluator(outputs: dict, reference_outputs: dict) -> dict:
    """
    심각도 점수 적절성 평가 (범위 내 포함 여부)

    Returns:
        dict: {"key": "severity_in_range", "score": float}
    """
    if outputs.get("status") == "error":
        return {"key": "severity_in_range", "score": 0.0}

    predicted_score = outputs.get("severity_score", 0)
    expected_range = reference_outputs.get("severity_range", (0, 10))

    # 범위 내에 있으면 정확
    is_in_range = expected_range[0] <= predicted_score <= expected_range[1]

    return {
        "key": "severity_in_range",
        "score": 1.0 if is_in_range else 0.0
    }

def severity_mae_evaluator(outputs: dict, reference_outputs: dict) -> dict:
    """
    심각도 점수 MAE (Mean Absolute Error) 계산

    Returns:
        dict: {"key": "severity_mae", "score": float}
    """
    if outputs.get("status") == "error":
        return {"key": "severity_mae", "score": 10.0}  # 최대 오류

    predicted_score = outputs.get("severity_score", 0)
    expected_range = reference_outputs.get("severity_range", (0, 10))

    # 범위의 중간값을 정답으로 사용
    expected_midpoint = (expected_range[0] + expected_range[1]) / 2

    # MAE 계산
    mae = abs(predicted_score - expected_midpoint)

    return {
        "key": "severity_mae",
        "score": mae
    }

def workflow_success_evaluator(outputs: dict, reference_outputs: dict) -> dict:
    """
    워크플로우 실행 성공 여부

    Returns:
        dict: {"key": "workflow_success", "score": float}
    """
    is_success = outputs.get("status") == "success"

    return {
        "key": "workflow_success",
        "score": 1.0 if is_success else 0.0
    }

def nudge_frame_completeness_evaluator(outputs: dict, reference_outputs: dict) -> dict:
    """
    넛지 메시지 프레임 완성도 평가 (간단한 키워드 기반)

    Returns:
        dict: {"key": "nudge_frame_complete", "score": float}
    """
    if outputs.get("status") == "error" or not outputs.get("intervention_needed"):
        return {"key": "nudge_frame_complete", "score": 1.0}  # N/A는 완벽

    nudge_message = outputs.get("nudge_message", "")

    if not nudge_message:
        return {"key": "nudge_frame_complete", "score": 0.0}

    # 간단한 키워드 체크 (실제로는 LLM 평가가 더 정확)
    # [인식] 키워드: 분, 시간, 중, 넘, 전환, 사용
    recognition_keywords = ["분", "시간", "째", "넘", "전환", "사용", "하고"]
    has_recognition = any(kw in nudge_message for kw in recognition_keywords)

    # [제안] 키워드: 어때, 권장, 제안, 해보, 해볼, 가요, 까요
    suggestion_keywords = ["어때", "권장", "제안", "해보", "해볼", "가요", "까요", "드려", "세요"]
    has_suggestion = any(kw in nudge_message for kw in suggestion_keywords)

    # [보상] 키워드: 코인, 보상, 포인트, 상쾌, 집중, 건강
    reward_keywords = ["코인", "보상", "포인트", "상쾌", "집중", "건강", "좋", "향상"]
    has_reward = any(kw in nudge_message for kw in reward_keywords)

    # 3요소 중 몇 개 포함?
    components_count = sum([has_recognition, has_suggestion, has_reward])

    # 점수: 3개 모두 = 1.0, 2개 = 0.67, 1개 = 0.33, 0개 = 0.0
    score = components_count / 3.0

    return {
        "key": "nudge_frame_complete",
        "score": score
    }

# =============================================================================
# Main Evaluation Function
# =============================================================================

def run_evaluation(max_concurrency: int = 2, limit: int = None):
    """
    평가 실행 메인 함수

    Args:
        max_concurrency: 병렬 실행 수
        limit: 테스트할 케이스 수 제한 (None이면 전체)
    """
    print("=" * 60)
    print("Intervention Agent Evaluation")
    print("=" * 60)

    # 1. 데이터셋 생성 또는 로드
    print(f"\n[1/5] 데이터셋 준비 중...")
    if not client.has_dataset(dataset_name=DATASET_NAME):
        print(f"   새 데이터셋 생성: {DATASET_NAME}")
        dataset = client.create_dataset(
            dataset_name=DATASET_NAME,
            description="Intervention Agent 평가를 위한 Ground Truth 데이터셋"
        )
        # 예제 추가
        examples_to_create = langsmith_examples[:limit] if limit else langsmith_examples
        client.create_examples(
            dataset_id=dataset.id,
            examples=examples_to_create
        )
        print(f"   {len(examples_to_create)}개 테스트 케이스 추가 완료")
    else:
        print(f"   기존 데이터셋 사용: {DATASET_NAME}")

    # 2. 평가 함수 리스트
    evaluators = [
        trigger_event_accuracy_evaluator,
        intervention_decision_evaluator,
        severity_score_evaluator,
        severity_mae_evaluator,
        workflow_success_evaluator,
        nudge_frame_completeness_evaluator,
    ]

    # 3. 평가 실행
    print(f"\n[2/5] 평가 실행 중... (병렬도: {max_concurrency})")
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    experiment_name = f"{EXPERIMENT_PREFIX}_{timestamp}"

    try:
        experiment_results = client.evaluate(
            target_intervention_agent,
            data=DATASET_NAME,
            evaluators=evaluators,
            experiment_prefix=experiment_name,
            max_concurrency=max_concurrency,
        )
        print(f"   평가 완료! 실험 이름: {experiment_name}")
    except Exception as e:
        print(f"   평가 실행 중 오류 발생: {e}")
        return

    # 4. 결과 분석
    print(f"\n[3/5] 결과 분석 중...")
    try:
        df = experiment_results.to_pandas()

        # 메트릭별 평균 점수 계산
        metrics = {
            "Trigger Event Accuracy": df.get('feedback.trigger_event_accuracy', pd.Series([0])).mean(),
            "Intervention Decision Accuracy": df.get('feedback.intervention_decision_accuracy', pd.Series([0])).mean(),
            "Severity In Range": df.get('feedback.severity_in_range', pd.Series([0])).mean(),
            "Severity MAE": df.get('feedback.severity_mae', pd.Series([10])).mean(),
            "Workflow Success Rate": df.get('feedback.workflow_success', pd.Series([0])).mean(),
            "Nudge Frame Completeness": df.get('feedback.nudge_frame_complete', pd.Series([0])).mean(),
        }

        print("\n   === 평가 결과 ===")
        for metric_name, score in metrics.items():
            if "MAE" in metric_name:
                print(f"   {metric_name}: {score:.3f}")
            else:
                print(f"   {metric_name}: {score:.2%}")

    except Exception as e:
        print(f"   결과 분석 중 오류: {e}")
        metrics = {}

    # 5. 시각화
    print(f"\n[4/5] 결과 시각화 중...")
    try:
        visualize_results(metrics, timestamp)
        print(f"   그래프 저장 완료: eval/results/evaluation_{timestamp}.png")
    except Exception as e:
        print(f"   시각화 중 오류: {e}")

    # 6. 요약
    print(f"\n[5/5] 평가 완료!")
    print(f"\n   LangSmith에서 상세 결과 확인:")
    print(f"   https://smith.langchain.com")
    print(f"   실험 이름: {experiment_name}")
    print("=" * 60)

    return experiment_results, metrics

# =============================================================================
# Visualization
# =============================================================================

def visualize_results(metrics: dict, timestamp: str):
    """
    평가 결과 시각화

    Args:
        metrics: 메트릭 딕셔너리
        timestamp: 타임스탬프
    """
    # MAE는 제외하고 accuracy/rate 메트릭만 시각화
    accuracy_metrics = {
        k: v for k, v in metrics.items()
        if "MAE" not in k
    }

    if not accuracy_metrics:
        print("   시각화할 메트릭이 없습니다.")
        return

    # 그래프 생성
    fig, ax = plt.subplots(figsize=(12, 6))

    metric_names = list(accuracy_metrics.keys())
    scores = list(accuracy_metrics.values())

    # 색상 (목표치 달성 여부에 따라)
    colors = []
    targets = {
        "Trigger Event Accuracy": 0.85,
        "Intervention Decision Accuracy": 0.80,
        "Severity In Range": 0.80,
        "Workflow Success Rate": 0.95,
        "Nudge Frame Completeness": 0.80,
    }

    for name, score in zip(metric_names, scores):
        target = targets.get(name, 0.80)
        if score >= target:
            colors.append('#5DA5DA')  # 파란색 (달성)
        else:
            colors.append('#FAA43A')  # 주황색 (미달성)

    # 막대 그래프
    bars = ax.bar(range(len(metric_names)), scores, color=colors, alpha=0.8)

    # 목표선 추가
    for i, name in enumerate(metric_names):
        target = targets.get(name, 0.80)
        ax.hlines(target, i - 0.4, i + 0.4, colors='red', linestyles='dashed', alpha=0.5)

    # 레이블
    ax.set_xticks(range(len(metric_names)))
    ax.set_xticklabels(metric_names, rotation=15, ha='right')
    ax.set_ylabel('Score')
    ax.set_ylim(0, 1.1)
    ax.set_title('Intervention Agent Evaluation Results', fontsize=14, fontweight='bold')

    # 점수 표시
    for bar in bars:
        height = bar.get_height()
        ax.text(bar.get_x() + bar.get_width() / 2., height + 0.02,
                f'{height:.2%}',
                ha='center', va='bottom', fontsize=9)

    # 범례
    from matplotlib.patches import Patch
    legend_elements = [
        Patch(facecolor='#5DA5DA', label='Target Achieved'),
        Patch(facecolor='#FAA43A', label='Below Target'),
        plt.Line2D([0], [0], color='red', linestyle='--', label='Target Line')
    ]
    ax.legend(handles=legend_elements, loc='upper right')

    plt.tight_layout()

    # 저장
    os.makedirs('src/agent/eval/results', exist_ok=True)
    plt.savefig(f'src/agent/eval/results/evaluation_{timestamp}.png', dpi=150)
    print(f"   그래프 저장: src/agent/eval/results/evaluation_{timestamp}.png")

# =============================================================================
# CLI Execution
# =============================================================================

if __name__ == "__main__":
    import sys

    # pandas import 추가
    import pandas as pd

    # 커맨드 라인 인자 파싱
    max_concurrency = int(sys.argv[1]) if len(sys.argv) > 1 else 2
    limit = int(sys.argv[2]) if len(sys.argv) > 2 else None

    # 평가 실행
    run_evaluation(max_concurrency=max_concurrency, limit=limit)
