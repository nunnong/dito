# Intervention Agent Evaluation System

## 📋 개요

Intervention Agent의 성능을 정량적으로 평가하기 위한 시스템입니다.

## 📁 파일 구조

```
eval/
├── __init__.py
├── README.md                    # 이 파일
├── intervention_dataset.py      # Ground Truth 데이터셋 (20개 Easy 케이스)
├── prompts.py                   # 평가용 프롬프트 템플릿
├── evaluate_intervention.py     # LangSmith 기반 평가 실행 스크립트
└── results/                     # 평가 결과 저장 (git에서 제외)
    └── .gitkeep
```

## 🎯 평가 메트릭

1. **Trigger Event Accuracy**: 트리거 이벤트 분류 정확도 (목표: 85%)
2. **Intervention Decision Accuracy**: 개입 필요성 판단 정확도 (목표: 80%)
3. **Severity In Range**: 심각도 점수 범위 내 정확도 (목표: 80%)
4. **Severity MAE**: 심각도 점수 평균 절대 오차 (목표: < 1.5)
5. **Workflow Success Rate**: 워크플로우 실행 성공률 (목표: 95%)
6. **Nudge Frame Completeness**: 넛지 메시지 프레임 완성도 (목표: 80%)

## 🚀 사용 방법

### 1. 환경 변수 설정

```bash
# .env 파일 생성
export LANGSMITH_API_KEY="your-api-key"
export LANGSMITH_PROJECT="intervention-agent-eval"
export LANGSMITH_TRACING_V2=true
export ANTHROPIC_API_KEY="your-anthropic-key"
```

### 2. LangSmith 평가 실행

```bash
cd ai
python src/agent/eval/evaluate_intervention.py

# 병렬도 조정 (기본값: 2)
python src/agent/eval/evaluate_intervention.py 4

# 테스트 케이스 수 제한 (처음 10개만)
python src/agent/eval/evaluate_intervention.py 2 10
```

### 3. pytest 단위 테스트 실행

```bash
cd ai

# 모든 노드 테스트
pytest tests/test_intervention_nodes.py -v

# 특정 테스트만 실행
pytest tests/test_intervention_nodes.py::test_analyze_behavior_trigger_event -v

# LangSmith 로깅 포함
LANGSMITH_TRACING_V2=true pytest tests/test_intervention_nodes.py -v
```

## 📊 결과 확인

### LangSmith 대시보드
- URL: https://smith.langchain.com
- 프로젝트: `intervention-agent-eval`
- 모든 실행 추적 및 상세 분석 가능

### 로컬 결과
- 그래프: `eval/results/evaluation_<timestamp>.png`
- 콘솔 출력: 각 메트릭별 점수

## 📝 데이터셋 확장

현재 20개 Easy 케이스가 포함되어 있습니다. 추가 케이스를 작성하려면:

```python
# intervention_dataset.py에 추가

medium_case_1 = {
    "name": "M1_boundary_case",
    "input": {
        "user_id": 21,
        "behavior_log": {
            "app_name": "YouTube Shorts",
            "duration_seconds": 1200,  # 정확히 20분 (경계값)
            "usage_timestamp": "2025-01-03T14:00:00",
            "recent_app_switches": 5
        }
    },
    "expected": {
        "trigger_event": ["short-form-overuse"],
        "pattern_type": ["concerning", "critical"],
        "severity_range": (5, 7),
        "intervention_needed": True,
        "intervention_type": ["short-form-overuse"],
        "urgency_level": ["medium"]
    },
    "nudge_criteria": {
        "must_mention_usage_time": True
    }
}

# medium_cases 리스트에 추가
medium_cases = [medium_case_1, ...]

# all_cases에 병합
all_cases = easy_cases + medium_cases
```

## 🔧 커스터마이징

### 새 평가 함수 추가

`evaluate_intervention.py`에 evaluator 함수를 추가:

```python
def custom_evaluator(outputs: dict, reference_outputs: dict) -> dict:
    """커스텀 평가 로직"""
    # 평가 로직 구현
    score = calculate_score(outputs, reference_outputs)

    return {
        "key": "custom_metric",
        "score": score
    }

# evaluators 리스트에 추가
evaluators = [
    trigger_event_accuracy_evaluator,
    # ...
    custom_evaluator,  # 추가
]
```

### 새 프롬프트 템플릿 추가

`prompts.py`에 프롬프트를 추가:

```python
CUSTOM_EVALUATOR_PROMPT = """
<Task>
커스텀 평가 기준 설명
</Task>

<input>{input_data}</input>
<output>{outputs}</output>
<reference>{reference_outputs}</reference>

평가하세요.
"""
```

## 📈 성능 목표

| 메트릭 | 현재 | 목표 |
|--------|------|------|
| Trigger Event Accuracy | TBD | 85% |
| Intervention Decision Accuracy | TBD | 80% |
| Severity In Range | TBD | 80% |
| Severity MAE | TBD | < 1.5 |
| Workflow Success Rate | TBD | 95% |
| Nudge Frame Completeness | TBD | 80% |

## 🐛 문제 해결

### LangSmith 연결 오류
```bash
# API 키 확인
echo $LANGSMITH_API_KEY

# 수동 설정
export LANGSMITH_API_KEY="your-key"
```

### pytest 실행 오류
```bash
# 의존성 확인
pip install pytest langsmith

# Python 경로 확인
export PYTHONPATH="${PYTHONPATH}:/path/to/ai/src"
```

## 📚 참고 자료

- [LangSmith Evaluation Guide](https://docs.smith.langchain.com/evaluation)
- [LangGraph Testing](https://langchain-ai.github.io/langgraph/how-tos/testing/)
- [인수인계 문서](../../../CLAUDE.md)

## 👥 기여

새로운 테스트 케이스나 평가 메트릭을 추가하고 싶다면:
1. `intervention_dataset.py`에 케이스 추가
2. 필요시 `prompts.py`에 프롬프트 추가
3. `evaluate_intervention.py`에 evaluator 추가
4. PR 생성

---

**Happy Evaluating! 🎉**
