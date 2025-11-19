"""Debug Report Agent (디버그용 리포트 생성 에이전트).

실제 LLM이나 API를 사용하지 않고, input으로 제공된 데이터를 그대로 출력하는
디버그/테스트용 에이전트입니다.

Workflow:
1. create_empty_report: 리포트 ID 생성
2. aggregate_insights: 인사이트 통합 (input 데이터 그대로 전달)
3. update_report: 리포트 개요 및 조언 반환 (5초 delay 후 출력)
"""

import time
from typing import NotRequired, TypedDict

from langgraph.graph import END, START, StateGraph

from agent.utils import update_report

# ============================================================================
# State Definition
# ============================================================================


class ReportState(TypedDict):
    """디버그 리포트 생성 에이전트의 상태 정의.

    Required fields (입력):
        user_id: 사용자 ID
        report_date: 리포트 생성 날짜 (YYYY-MM-DD 형식)
        insights: 인사이트 리스트
        report_overview: 리포트 개요
        advice: 조언
        mission_success_rate: 미션 성공률

    Optional fields (워크플로우 중 생성):
        status: 리포트 상태
        report_id: 리포트 ID
    """

    # Required input fields
    user_id: int
    report_date: str  # "YYYY-MM-DD" format
    insights: list[dict]
    report_overview: str
    advice: str
    mission_success_rate: int

    # Optional - generated during workflow
    status: NotRequired[str]
    report_id: NotRequired[int]


# ============================================================================
# Node Functions
# ============================================================================


def create_empty_report_node(state: ReportState) -> dict:
    """1단계: 빈 리포트 생성 (시뮬레이션).

    실제 API 호출 없이 mock report_id를 생성합니다.
    """
    print("📝 [Debug] 빈 리포트 생성 시뮬레이션")

    # state에서 report_id 가져오기 또는 mock ID 생성
    report_id = state.get("report_id", 999)

    print(f"     ✅ Report ID: {report_id}")

    return {"report_id": report_id, "status": "IN_PROGRESS"}


def aggregate_insights_node(state: ReportState) -> dict:
    """2단계: 인사이트 통합.

    state에서 제공된 insights를 그대로 반환합니다.
    """
    print("📦 [Debug] 인사이트 통합")

    # input으로 제공된 insights를 그대로 사용
    insights = state["insights"]

    print(f"     ✅ 총 {len(insights)}개 인사이트:")
    for i, insight in enumerate(insights, 1):
        print(f"        {i}. [{insight.get('type')}] {insight.get('description')}")

    return {"insights": insights}


def update_report_node(state: ReportState) -> dict:
    """3단계: 리포트 개요 및 조언 반환 (5초 delay).

    state에서 제공된 report_overview와 advice를 그대로 반환합니다.
    마지막 출력 전에 5초 대기합니다.
    """
    print("📝 [Debug] 리포트 최종 생성")

    # input으로 제공된 데이터를 그대로 사용
    report_overview = state["report_overview"]
    advice = state["advice"]
    mission_success_rate = state["mission_success_rate"]
    insights = state["insights"]

    print(f"     📊 Overview: {report_overview[:80]}...")
    print(f"     💡 Advice: {advice[:80]}...")
    print(f"     📈 Mission Success Rate: {mission_success_rate}%")

    # 5초 대기
    print("     ⏳ 5초 대기 중...")
    time.sleep(5)
    print("     ✅ 대기 완료")

    # Spring 백엔드 업데이트
    report_id = state.get("report_id")

    if report_id and report_id > 0:
        # 유효한 report_id가 있을 때만 API 호출
        update_success = update_report(
            report_id=report_id,
            report_overview=report_overview,
            insights=insights,
            advice=advice,
            mission_success_rate=mission_success_rate,
        )

        if not update_success:
            print("     ⚠️ 리포트 업데이트 실패, 분석 결과만 반환합니다.")

    return {
        "report_overview": report_overview,
        "advice": advice,
        "mission_success_rate": mission_success_rate,
        "status": "COMPLETED",
    }


# ============================================================================
# Graph Builder
# ============================================================================


def build_debug_report_agent() -> StateGraph:
    """디버그 리포트 생성 에이전트 그래프 빌드.

    Workflow:
    ---------
    START
      ↓
    create_empty_report (1단계: 리포트 ID 생성)
      ↓
    aggregate_insights (2단계: 인사이트 통합)
      ↓
    update_report (3단계: 개요 및 조언 반환, 5초 delay)
      ↓
    END

    Returns:
        Compiled StateGraph (checkpointer 없음 - LangGraph Server가 관리)
    """
    workflow = StateGraph(ReportState)

    # Add nodes
    workflow.add_node("create_empty_report", create_empty_report_node)
    workflow.add_node("aggregate_insights", aggregate_insights_node)
    workflow.add_node("update_report", update_report_node)

    # Add edges
    workflow.add_edge(START, "create_empty_report")
    workflow.add_edge("create_empty_report", "aggregate_insights")
    workflow.add_edge("aggregate_insights", "update_report")
    workflow.add_edge("update_report", END)

    # Compile without checkpointer (LangGraph Server manages state)
    return workflow.compile()


# ============================================================================
# Agent Instance
# ============================================================================

debug_report_agent = build_debug_report_agent()
