"""미션 평가 에이전트 (Evaluation Agent)
- 미션 수행 결과 평가
- LLM 기반 피드백 생성
- FCM 알림 전송
- 전략 조정 판단 및 실행

처리 흐름:
1. 미션 정보 조회 (GET /api/mission/{id})
2. behavior_logs와 비교하여 성공/실패 판정
3. LLM으로 피드백 생성
4. FCM 알림 전송
5. 필요시 전략 조정
"""

from typing import Literal

from langchain_core.messages import HumanMessage, SystemMessage
from langgraph.graph import END, START, StateGraph
from langgraph.types import Command

from agent.prompts import (
    SYSTEM_MSG_STRATEGY_ADJUSTER,
    get_adjustment_decision_prompt,
)
from agent.schemas import EvaluationState
from agent.utils import (
    evaluate_mission_with_llm,
    fetch_mission_info,
    send_evaluation_fcm,
    strategy_adjuster,
    submit_mission_result,
)

# =============================================================================
# Evaluation Agent Nodes
# =============================================================================

def load_mission_context(state: EvaluationState) -> dict:
    """1단계: 미션 정보 로드
    mission_id로 미션 정보를 조회합니다.
    """
    print(f"\n[평가 시작] Mission ID: {state['mission_id']}, User ID: {state['user_id']}")
    print("[1/5] 미션 정보 로드 중...")

    mission_id = state["mission_id"]
    mission_info = fetch_mission_info(mission_id)

    if not mission_info:
        raise ValueError(f"미션 정보 조회 실패: mission_id={mission_id}")

    mission_type = mission_info.get("missionType", "UNKNOWN")
    target_app = mission_info.get("targetApp", "")

    print(f"     미션 타입: {mission_type}")
    print(f"     타겟 앱: {target_app}")

    return {
        "mission_info": mission_info,
        "mission_type": mission_type,
        "target_app": target_app
    }


def evaluate_mission_result(state: EvaluationState) -> dict:
    """2단계: 미션 평가
    behavior_logs와 mission 정보를 비교하여 성공/실패 판정하고,
    LLM으로 피드백을 생성합니다.
    """
    print("\n[2/5] 미션 평가 중...")

    mission_info = state["mission_info"]
    mission_type = state.get("mission_type", "UNKNOWN")
    behavior_logs = state.get("behavior_logs", [])

    # behavior_logs가 없거나 비어있는 경우 처리
    if not behavior_logs:
        print("     ✅ behavior_logs가 없습니다. 타겟 앱을 사용하지 않음 -> SUCCESS")

        # 모든 미션 타입에서 동일하게 처리: 타겟 앱 미사용 = 성공
        evaluation_result = "SUCCESS"
        target_app = mission_info.get("targetApp", "앱")
        feedback = f"{target_app}을(를) 잘 참았어! 훌륭해! 💪"
    else:
        # 평가 및 피드백 생성
        evaluation_result, feedback = evaluate_mission_with_llm(mission_info, behavior_logs)

    print(f"     평가 결과: {evaluation_result}")
    print(f"     피드백: {feedback}")

    return {
        "evaluation_result": evaluation_result,
        "feedback": feedback
    }


def send_fcm_notification(state: EvaluationState) -> dict:
    """3단계: FCM 알림 전송 및 미션 결과 저장
    평가 결과를 사용자에게 FCM으로 전송하고, 미션 결과를 DB에 저장합니다.
    """
    print("\n[3/5] FCM 알림 전송 및 미션 결과 저장 중...")

    user_id = state["user_id"]
    mission_id = state["mission_id"]
    evaluation_result = state["evaluation_result"]
    feedback = state["feedback"]

    # FCM 알림 전송
    fcm_sent = send_evaluation_fcm(user_id, evaluation_result, feedback, mission_id)

    if fcm_sent:
        print("     ✅ FCM 전송 완료")
    else:
        print("     ⚠️ FCM 전송 실패 (계속 진행)")

    # 미션 결과 DB 저장 (feedback 포함)
    result_saved = submit_mission_result(mission_id, evaluation_result, feedback)

    if result_saved:
        print("     ✅ 미션 결과 DB 저장 완료")
    else:
        print("     ⚠️ 미션 결과 저장 실패 (계속 진행)")

    return {
        "fcm_sent": fcm_sent
    }


def analyze_and_decide_adjustment(state: EvaluationState) -> Command[Literal["adjust_strategy", "__end__"]]:
    """4단계: 전략 조정 필요성 판단
    평가 결과를 바탕으로 전략 조정이 필요한지 판단합니다.
    """
    print("\n[4/5] 전략 조정 필요성 판단 중...")

    evaluation_result = state["evaluation_result"]
    mission_type = state.get("mission_type", "UNKNOWN")

    # 간단한 효과성 점수 계산 (성공=1.0, 실패=0.0)
    effectiveness = 1.0 if evaluation_result == "SUCCESS" else 0.0
    behavior_change_detected = evaluation_result == "SUCCESS"

    adjustment_prompt = get_adjustment_decision_prompt(
        mission_type,
        effectiveness,
        behavior_change_detected
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
                "new_strategy": decision.new_strategy,
                "effectiveness_score": effectiveness,
                "behavior_change_detected": behavior_change_detected
            },
            goto="adjust_strategy"
        )
    else:
        return Command(
            update={
                "adjustment_needed": False,
                "adjustment_reason": decision.adjustment_reason,
                "effectiveness_score": effectiveness,
                "behavior_change_detected": behavior_change_detected
            },
            goto=END
        )


def adjust_strategy(state: EvaluationState) -> dict:
    """5단계: 전략 조정 실행
    새로운 전략을 적용하고 기록합니다.
    """
    print("\n[5/5] 전략 조정 실행 중...")

    new_strategy = state.get("new_strategy")

    # 실제로는 DB에 새로운 전략 저장
    # update_intervention_strategy(
    #     mission_type=state['mission_type'],
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
    """미션 평가 에이전트 그래프 구성"""
    workflow = StateGraph(EvaluationState)

    # 노드 추가
    workflow.add_node("load_mission_context", load_mission_context)
    workflow.add_node("evaluate_mission_result", evaluate_mission_result)
    workflow.add_node("send_fcm_notification", send_fcm_notification)
    workflow.add_node("analyze_and_decide_adjustment", analyze_and_decide_adjustment)
    workflow.add_node("adjust_strategy", adjust_strategy)

    # 엣지 추가
    workflow.add_edge(START, "load_mission_context")
    workflow.add_edge("load_mission_context", "evaluate_mission_result")
    workflow.add_edge("evaluate_mission_result", "send_fcm_notification")
    workflow.add_edge("send_fcm_notification", "analyze_and_decide_adjustment")
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

def test_evaluation_agent(user_id: int = 1, mission_id: int = 123):
    """평가 에이전트 테스트"""
    print("\n" + "="*60)
    print("디토 평가 에이전트 테스트 시작")
    print("="*60)

    # 초기 상태
    initial_state = {
        "user_id": user_id,
        "mission_id": mission_id,
        "behavior_logs": [
            {
                "log_type": "APP_USAGE",
                "sequence": 1,
                "timestamp": "2025-11-11T10:30:15+09:00",
                "package_name": "com.instagram.android",
                "app_name": "Instagram",
                "duration_seconds": 125,
                "is_target_app": True
            },
            {
                "log_type": "SCREEN_OFF",
                "sequence": 2,
                "timestamp": "2025-11-11T10:33:30+09:00"
            }
        ]
    }

    # 그래프 실행
    config = {"configurable": {"thread_id": f"eval_{mission_id}"}}
    result = evaluation_agent.invoke(initial_state, config)

    print("\n" + "="*60)
    print("평가 결과")
    print("="*60)
    print(f"평가: {result.get('evaluation_result')}")
    print(f"피드백: {result.get('feedback')}")
    print(f"FCM 전송: {result.get('fcm_sent')}")
    print(f"효과 점수: {result.get('effectiveness_score', 0):.2f}")
    print(f"조정 필요: {result.get('adjustment_needed')}")

    if result.get('adjustment_needed'):
        print(f"조정 이유: {result.get('adjustment_reason')}")
        print(f"새로운 전략: {result.get('new_strategy', 'N/A')}")

    print("="*60)

    return result


if __name__ == "__main__":
    # 테스트 실행
    result = test_evaluation_agent(user_id=1, mission_id=123)