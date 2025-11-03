"""지연된 평가 에이전트 (Evaluation Agent)
- 개입 효과 측정
- 전략 조정 판단 및 실행

이 에이전트는 개입 후 N분 뒤 실행되어:
1. 개입 전후 사용 패턴 비교
2. 효과성 점수 계산
3. 필요시 전략 조정
"""

from typing import Literal

from langchain_core.messages import HumanMessage, SystemMessage
from langgraph.graph import END, START, StateGraph
from langgraph.types import Command

from agent.prompts import (
    SYSTEM_MSG_EFFECTIVENESS_ANALYZER,
    SYSTEM_MSG_STRATEGY_ADJUSTER,
    get_adjustment_decision_prompt,
    get_effectiveness_analysis_prompt,
)
from agent.schemas import EvaluationState
from agent.utils import (
    effectiveness_analyzer,
    simulate_post_intervention_usage,
    strategy_adjuster,
)

# =============================================================================
# Evaluation Agent Nodes
# =============================================================================

def load_intervention_context(state: EvaluationState) -> dict:
    """0단계: 개입 컨텍스트 로드
    intervention_id로 개입 정보를 불러옴
    """
    print(f"\n[평가 시작] Intervention ID: {state['intervention_id']}")
    print("[0/4] 개입 컨텍스트 로드 중...")
    
    # 실제로는 missions 테이블에서 개입 정보 조회
    # intervention = get_intervention_from_db(state['intervention_id'])
    
    # 개입 전 사용 데이터 조회 (app_usage_logs)
    pre_usage = {
        "duration_seconds": 1200,  # 개입 전 20분 사용
        "app_switches": 5,
        "sessions": 2
    }
    
    # 개입 후 사용 데이터 조회
    post_usage = simulate_post_intervention_usage(
        state['user_id'],
        state['intervention_id']
    )
    
    print(f"     개입 전 사용: {pre_usage['duration_seconds']}초")
    print(f"     개입 후 사용: {post_usage['duration_after_intervention']}초")
    
    return {
        "pre_intervention_usage": pre_usage,
        "post_intervention_usage": post_usage
    }


def measure_effectiveness(state: EvaluationState) -> dict:
    """1단계: 개입 효과 측정
    개입 전후 데이터를 비교하여 효과성 점수 계산
    """
    print("\n[1/4] 개입 효과 측정 중...")

    pre = state["pre_intervention_usage"]
    post = state["post_intervention_usage"]

    # LLM을 사용한 효과성 분석
    analysis_prompt = get_effectiveness_analysis_prompt(
        state['intervention_type'],
        pre,
        post
    )

    analysis = effectiveness_analyzer.invoke([
        SystemMessage(content=SYSTEM_MSG_EFFECTIVENESS_ANALYZER),
        HumanMessage(content=analysis_prompt)
    ])
    
    print(f"     효과 점수: {analysis.effectiveness_score:.2f}")
    print(f"     행동 변화: {analysis.behavior_change_detected}")
    print(f"     분석: {analysis.summary}")
    
    return {
        "effectiveness_score": analysis.effectiveness_score,
        "behavior_change_detected": analysis.behavior_change_detected
    }


def analyze_and_decide_adjustment(state: EvaluationState) -> Command[Literal["adjust_strategy", "__end__"]]:
    """2단계: 전략 조정 필요성 판단
    효과성 점수가 낮으면 전략 조정 필요
    """
    print("\n[2/4] 전략 조정 필요성 판단 중...")

    effectiveness = state["effectiveness_score"]

    adjustment_prompt = get_adjustment_decision_prompt(
        state['intervention_type'],
        effectiveness,
        state['behavior_change_detected']
    )

    decision = strategy_adjuster.invoke([
        SystemMessage(content=SYSTEM_MSG_STRATEGY_ADJUSTER),
        HumanMessage(content=adjustment_prompt)
    ])
    
    print(f"     조정 필요: {decision.adjustment_needed}")
    print(f"     이유: {decision.adjustment_reason}")
    
    if decision.adjustment_needed:
        return Command(
            update={
                "adjustment_needed": True,
                "adjustment_reason": decision.adjustment_reason,
                "new_strategy": decision.new_strategy
            },
            goto="adjust_strategy"
        )
    else:
        return Command(
            update={
                "adjustment_needed": False,
                "adjustment_reason": decision.adjustment_reason
            },
            goto=END
        )


def adjust_strategy(state: EvaluationState) -> dict:
    """3단계: 전략 조정 실행
    새로운 전략을 적용하고 기록
    """
    print("\n[3/4] 전략 조정 실행 중...")
    
    new_strategy = state.get("new_strategy")
    
    # 실제로는 DB에 새로운 전략 저장
    # update_intervention_strategy(
    #     intervention_type=state['intervention_type'],
    #     new_strategy=new_strategy
    # )
    
    print(f"     새로운 전략: {new_strategy}")
    print("     ✅ 전략 조정 완료")
    
    # 실제 환경에서는 프롬프트 템플릿 업데이트
    # 또는 개입 파라미터 조정 등 수행
    
    return {}


# =============================================================================
# Evaluation Agent Graph 구성
# =============================================================================

def build_evaluation_agent() -> StateGraph:
    """지연된 평가 에이전트 그래프 구성"""
    workflow = StateGraph(EvaluationState)
    
    # 노드 추가
    workflow.add_node("load_intervention_context", load_intervention_context)
    workflow.add_node("measure_effectiveness", measure_effectiveness)
    workflow.add_node("analyze_and_decide_adjustment", analyze_and_decide_adjustment)
    workflow.add_node("adjust_strategy", adjust_strategy)
    
    # 엣지 추가
    workflow.add_edge(START, "load_intervention_context")
    workflow.add_edge("load_intervention_context", "measure_effectiveness")
    workflow.add_edge("measure_effectiveness", "analyze_and_decide_adjustment")
    # analyze_and_decide_adjustment에서 조건부 라우팅 (Command 사용)
    workflow.add_edge("adjust_strategy", END)
    
    # 컴파일 (LangGraph Server가 자동으로 checkpointer 관리)
    return workflow.compile()


# 그래프 생성
evaluation_agent = build_evaluation_agent()

print("Evaluation Agent 그래프 구성 완료")


# =============================================================================
# 테스트 실행 함수
# =============================================================================

def test_evaluation_agent(intervention_id: int = 12345, user_id: int = 1):
    """평가 에이전트 테스트"""
    print("\n" + "="*60)
    print("디토 평가 에이전트 테스트 시작")
    print("="*60)
    
    # 초기 상태
    initial_state = {
        "intervention_id": intervention_id,
        "user_id": user_id,
        "intervention_type": "short-form-overuse",
    }
    
    # 그래프 실행
    config = {"configurable": {"thread_id": f"eval_{intervention_id}"}}
    result = evaluation_agent.invoke(initial_state, config)
    
    print("\n" + "="*60)
    print("평가 결과")
    print("="*60)
    print(f"효과 점수: {result['effectiveness_score']:.2f}")
    print(f"행동 변화: {result['behavior_change_detected']}")
    print(f"조정 필요: {result['adjustment_needed']}")
    
    if result['adjustment_needed']:
        print(f"조정 이유: {result['adjustment_reason']}")
        print(f"새로운 전략: {result.get('new_strategy', 'N/A')}")
    
    print("="*60)
    
    return result


if __name__ == "__main__":
    # 테스트 실행
    result = test_evaluation_agent(intervention_id=12345, user_id=1)
