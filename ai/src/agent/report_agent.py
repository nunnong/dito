"""Report Agent (리포트 생성 에이전트).

일일 사용자 활동 데이터를 분석하여 종합 리포트를 생성하는 에이전트.
Spring 백엔드와 비동기로 통신하며, 3단계 워크플로우를 통해 리포트를 생성합니다.

Workflow:
1. fetch_daily_activity: DailyUserActivity 데이터 조회
2. create_empty_report: 빈 리포트 생성 (IN_PROGRESS 상태)
3. analyze_and_update_report: LLM 분석 후 리포트 업데이트 (COMPLETED 상태)
"""

from typing import NotRequired, TypedDict

from langchain_core.messages import HumanMessage, SystemMessage
from langgraph.graph import END, START, StateGraph

# ============================================================================
# State Definition
# ============================================================================


class ReportState(TypedDict):
    """리포트 생성 에이전트의 상태 정의.

    Required fields (입력):
        user_id: 사용자 ID
        report_date: 리포트 생성 날짜 (YYYY-MM-DD 형식)

    Optional fields (워크플로우 중 생성):
        daily_activity: 1단계에서 조회한 일일 활동 데이터
        missions: 1단계에서 조회한 미션 목록
        mission_success_rate: 1단계에서 계산한 미션 성공률 (백분율)
        report_id: 2단계에서 생성된 리포트 ID
        sleep_insight: 3단계-병렬에서 생성한 수면 패턴 인사이트
        focus_insight: 3단계-병렬에서 생성한 집중력 인사이트
        self_control_insight: 3단계-병렬에서 생성한 자기 조절력 인사이트
        insights: 4단계 aggregator에서 합쳐진 인사이트 리스트
        report_overview: 5단계에서 LLM이 생성한 리포트 개요
        advice: 5단계에서 LLM이 생성한 조언
        status: 리포트 상태 (IN_PROGRESS → COMPLETED)
    """

    # Required input fields
    user_id: int
    report_date: str  # "YYYY-MM-DD" format

    # Optional - generated during workflow
    daily_activity: NotRequired[dict]
    missions: NotRequired[list[dict]]
    mission_success_rate: NotRequired[int]
    report_id: NotRequired[int]
    sleep_insight: NotRequired[dict]  # {"type": "POSITIVE/NEGATIVE", "description": "..."}
    focus_insight: NotRequired[dict]
    self_control_insight: NotRequired[dict]
    insights: NotRequired[list[dict]]
    report_overview: NotRequired[str]
    advice: NotRequired[str]
    status: NotRequired[str]


# ============================================================================
# Node Functions
# ============================================================================


def fetch_daily_activity_node(state: ReportState) -> dict:
    """1단계: AI 서버에서 DailyUserActivity 및 미션 목록 조회.

    Spring 백엔드 API를 호출하여 사용자의 일일 활동 데이터와 미션 목록을 가져옵니다.
    미션 목록에서 성공률을 계산하여 state에 저장합니다.

    API 호출 정보:
    --------------
    1) 일일 활동 데이터:
       Method: GET
       URL: /api/ai/activity/{userId}?date={report_date}
       Headers:
           - X-API-Key: {SECURITY_INTERNAL_API_KEY}

    2) 미션 목록:
       Method: GET
       URL: /api/mission/{userId}?date={report_date}
       Headers:
           - X-API-Key: {SECURITY_INTERNAL_API_KEY}

    요청 예시:
        GET /api/ai/activity/23?date=2025-01-15
        GET /api/mission/23?date=2025-01-15
        X-API-Key: your-api-key-here

    응답 예시:
        일일 활동: {
            "date": "2025-01-15",
            "userId": 23,
            "summary": {
                "total_app_usage_time": 180,
                "total_media_watch_time": 45.5,
                "most_used_app": "com.instagram.android"
            },
            "app_usage_stats": [...],
            "media_sessions": [...]
        }

        미션 목록: {
            "data": [
                {
                    "id": 123,
                    "status": "COMPLETED",
                    "result": "SUCCESS",
                    ...
                }
            ]
        }
    """
    from agent.utils import (
        calculate_mission_success_rate,
        fetch_daily_activity,
        fetch_missions_by_date,
    )

    print("📊 1단계: 일일 활동 데이터 및 미션 목록 조회 시작")

    # 일일 활동 데이터 조회
    daily_activity = fetch_daily_activity(state["user_id"], state["report_date"])

    if not daily_activity:
        # API 호출 실패 시 빈 데이터 반환
        print("     ⚠️ 일일 활동 데이터를 가져올 수 없어 빈 데이터로 진행합니다.")
        daily_activity = {
            "date": state["report_date"],
            "userId": state["user_id"],
            "summary": {
                "total_app_usage_time": 0,
                "total_media_watch_time": 0.0,
                "most_used_app": None,
            },
            "app_usage_stats": [],
            "media_sessions": [],
        }

    # 미션 목록 조회
    missions = fetch_missions_by_date(state["user_id"], state["report_date"])

    if not missions:
        print("     ⚠️ 미션 목록을 가져올 수 없어 빈 배열로 진행합니다.")
        missions = []

    # 미션 성공률 계산
    mission_success_rate = calculate_mission_success_rate(missions)

    return {
        "daily_activity": daily_activity,
        "missions": missions,
        "mission_success_rate": mission_success_rate,
        "status": "fetched",
    }


def create_empty_report_node(state: ReportState) -> dict:
    """2단계: AI 서버가 빈 리포트 생성.

    Spring 백엔드에 빈 리포트를 생성하고 IN_PROGRESS 상태로 설정합니다.
    이후 3단계에서 분석 결과를 업데이트할 리포트 ID를 받습니다.

    API 호출 정보:
    --------------
    Method: POST
    URL: /api/report
    Headers:
        - Content-Type: application/json
        - X-API-Key: {SECURITY_INTERNAL_API_KEY}
    Body:
        {
            "user_id": 23,
            "report_date": "2025-01-15",
            "status": "IN_PROGRESS"
        }

    응답 예시:
        {
            "id": 123,
            "user_id": 23,
            "report_date": "2025-01-15",
            "status": "IN_PROGRESS",
            "created_at": "2025-01-15T10:00:00",
            "updated_at": "2025-01-15T10:00:00"
        }
    """
    from agent.utils import create_empty_report

    print(f"📝 2단계: 빈 리포트 생성 시작")

    report_id = create_empty_report(state["user_id"], state["report_date"])

    if not report_id:
        # API 호출 실패 시 placeholder ID 사용 (워크플로우 계속 진행)
        print("     ⚠️ 빈 리포트 생성 실패, placeholder ID로 진행합니다.")
        report_id = -1  # 실패를 나타내는 특수 ID

    return {"report_id": report_id, "status": "IN_PROGRESS"}


def analyze_sleep_pattern_node(state: ReportState) -> dict:
    """3단계-병렬 (1/3): 수면 패턴 분석.

    밤 시간대(22시~6시) 사용량, 수면 관련 미션 여부, 미션 실패 여부를 분석합니다.
    LLM을 사용하여 짧은 한 문장으로 인사이트를 생성합니다.

    분석 항목:
    - 밤 시간대(22시~6시) 앱 사용 시간
    - 수면 방해 앱 사용 여부
    - 취침 전 30분 사용 여부
    """
    import json
    from datetime import datetime

    from agent.utils import llm

    print("🌙 [병렬 1/3] 수면 패턴 분석 중...")

    daily_activity = state.get("daily_activity", {})
    missions = state.get("missions", [])

    # 밤 시간대(22시~6시) 사용량 계산
    night_usage_minutes = 0
    app_usage_stats = daily_activity.get("app_usage_stats", [])

    # TODO: app_usage_stats에 시간대별 데이터가 있다면 계산
    # 현재는 summary만 사용 가능하므로 간단히 처리
    total_usage = daily_activity.get("summary", {}).get("total_app_usage_time", 0)

    # 수면 관련 미션 확인
    sleep_mission_failed = False
    for mission in missions:
        if mission.get("status") == "COMPLETED" and mission.get("result") == "FAILURE":
            # 밤 시간대 미션 실패 여부 확인 (triggerTime 기반)
            trigger_time = mission.get("triggerTime", "")
            if trigger_time:
                try:
                    hour = datetime.fromisoformat(
                        trigger_time.replace("+00:00", "")
                    ).hour
                    if hour >= 22 or hour < 6:
                        sleep_mission_failed = True
                        break
                except:
                    pass

    # LLM 프롬프트
    system_prompt = """당신은 수면 패턴 분석 전문가입니다.
다음 데이터를 분석하여 수면 패턴에 대한 인사이트를 **짧은 한 문장**으로 작성하세요.

출력 형식:
- type: "POSITIVE" (수면에 긍정적) 또는 "NEGATIVE" (수면에 부정적)
- description: 짧은 한 문장 (최대 50자)"""

    user_prompt = f"""날짜: {state["report_date"]}
총 디지털 사용 시간: {total_usage}분
밤 시간대(22시~6시) 미션 실패: {"예" if sleep_mission_failed else "아니오"}

수면 패턴에 대한 인사이트를 한 문장으로 작성하세요."""

    # Pydantic 스키마 사용
    from agent.schemas import InsightItem

    sleep_analyzer = llm.with_structured_output(InsightItem)

    result = sleep_analyzer.invoke(
        [SystemMessage(content=system_prompt), HumanMessage(content=user_prompt)]
    )

    sleep_insight = {"type": result.type, "description": result.description}

    print(f"     ✅ [{result.type}] {result.description}")

    return {"sleep_insight": sleep_insight}


def analyze_focus_level_node(state: ReportState) -> dict:
    """3단계-병렬 (2/3): 집중력 분석.

    앱 전환 횟수, 총 디지털 사용량을 분석합니다.
    LLM을 사용하여 짧은 한 문장으로 인사이트를 생성합니다.

    분석 항목:
    - 앱 전환 횟수
    - 총 디지털 사용 시간
    - 앱 사용 집중도
    """
    from agent.utils import llm

    print("🎯 [병렬 2/3] 집중력 분석 중...")

    daily_activity = state.get("daily_activity", {})

    total_usage = daily_activity.get("summary", {}).get("total_app_usage_time", 0)

    # 앱 전환 횟수 계산 (launch_count 합계)
    app_switches = 0
    for app_stat in daily_activity.get("app_usage_stats", []):
        app_switches += app_stat.get("launch_count", 0)

    # LLM 프롬프트
    system_prompt = """당신은 디지털 집중력 분석 전문가입니다.
다음 데이터를 분석하여 집중력에 대한 인사이트를 **짧은 한 문장**으로 작성하세요.

출력 형식:
- type: "POSITIVE" (집중력에 긍정적) 또는 "NEGATIVE" (집중력에 부정적)
- description: 짧은 한 문장 (최대 50자)"""

    user_prompt = f"""날짜: {state["report_date"]}
총 디지털 사용 시간: {total_usage}분
앱 전환 횟수: {app_switches}회

집중력에 대한 인사이트를 한 문장으로 작성하세요."""

    from agent.schemas import InsightItem

    focus_analyzer = llm.with_structured_output(InsightItem)

    result = focus_analyzer.invoke(
        [SystemMessage(content=system_prompt), HumanMessage(content=user_prompt)]
    )

    focus_insight = {"type": result.type, "description": result.description}

    print(f"     ✅ [{result.type}] {result.description}")

    return {"focus_insight": focus_insight}


def analyze_self_control_node(state: ReportState) -> dict:
    """3단계-병렬 (3/3): 자기 조절력 분석.

    미션 성공/실패 비율을 분석합니다.
    LLM을 사용하여 짧은 한 문장으로 인사이트를 생성합니다.

    분석 항목:
    - 미션 성공률
    - 미션 성공/실패 개수
    """
    from agent.utils import llm

    print("💪 [병렬 3/3] 자기 조절력 분석 중...")

    missions = state.get("missions", [])
    mission_success_rate = state.get("mission_success_rate", 0)

    # 성공/실패 개수 계산
    completed_missions = [m for m in missions if m.get("status") == "COMPLETED"]
    success_count = len([m for m in completed_missions if m.get("result") == "SUCCESS"])
    failure_count = len([m for m in completed_missions if m.get("result") == "FAILURE"])

    # LLM 프롬프트
    system_prompt = """당신은 자기 조절력 분석 전문가입니다.
다음 데이터를 분석하여 자기 조절력에 대한 인사이트를 **짧은 한 문장**으로 작성하세요.

출력 형식:
- type: "POSITIVE" (자기 조절력이 좋음) 또는 "NEGATIVE" (자기 조절력 개선 필요)
- description: 짧은 한 문장 (최대 50자)"""

    user_prompt = f"""날짜: {state["report_date"]}
미션 성공률: {mission_success_rate}%
성공한 미션: {success_count}개
실패한 미션: {failure_count}개

자기 조절력에 대한 인사이트를 한 문장으로 작성하세요."""

    from agent.schemas import InsightItem

    self_control_analyzer = llm.with_structured_output(InsightItem)

    result = self_control_analyzer.invoke(
        [SystemMessage(content=system_prompt), HumanMessage(content=user_prompt)]
    )

    self_control_insight = {"type": result.type, "description": result.description}

    print(f"     ✅ [{result.type}] {result.description}")

    return {"self_control_insight": self_control_insight}


def aggregate_insights_node(state: ReportState) -> dict:
    """4단계: 3개의 인사이트를 합침.

    병렬로 생성된 sleep_insight, focus_insight, self_control_insight를
    하나의 리스트로 합칩니다.

    순서: [수면 패턴, 집중력, 자기 조절력]
    """
    print("📦 인사이트 통합 중...")

    sleep_insight = state.get("sleep_insight", {})
    focus_insight = state.get("focus_insight", {})
    self_control_insight = state.get("self_control_insight", {})

    insights = [sleep_insight, focus_insight, self_control_insight]

    print(f"     ✅ 총 {len(insights)}개 인사이트 통합 완료")

    return {"insights": insights}


def update_report_node(state: ReportState) -> dict:
    """5단계: 리포트 개요 및 조언 생성 후 Spring 백엔드 업데이트.

    insights는 이미 병렬 노드에서 생성되었으므로,
    report_overview와 advice만 LLM으로 생성합니다.

    생성 항목:
    - report_overview: 하루 사용 패턴 요약 (2-3문장)
    - advice: 개인화된 조언 (1-2문장)

    Spring 백엔드에 PATCH 요청으로 리포트를 업데이트합니다.

    API 호출 정보:
    --------------
    Method: PATCH
    URL: /api/report/{report_id}
    Headers:
        - Content-Type: application/json
        - X-API-Key: {SECURITY_INTERNAL_API_KEY}
    Body:
        {
            "report_overview": "오늘 총 180분 동안 스마트폰을 사용했습니다...",
            "insights": [
                {
                    "category": "app_usage",
                    "title": "인스타그램 과다 사용",
                    "description": "평소보다 2배 많은 시간을 사용했습니다."
                },
                {
                    "category": "media_consumption",
                    "title": "숏폼 콘텐츠 집중",
                    "description": "45분 동안 숏폼 영상을 시청했습니다."
                }
            ],
            "advice": "숏폼 영상 시청 시간을 줄이고, 더 의미있는 활동에 시간을 투자해보세요.",
            "mission_success_rate": 85,
            "status": "COMPLETED"
        }

    응답 예시:
        {
            "id": 123,
            "user_id": 23,
            "report_date": "2025-01-15",
            "report_overview": "...",
            "insights": [...],
            "advice": "...",
            "mission_success_rate": 85,
            "status": "COMPLETED",
            "created_at": "2025-01-15T10:00:00",
            "updated_at": "2025-01-15T10:05:00"
        }

    LLM 분석 구현 예정:
    -------------------
    from agent.utils import llm
    from pydantic import BaseModel, Field

    class ReportAnalysis(BaseModel):
        report_overview: str = Field(description="일일 활동 요약")
        insights: list[dict] = Field(description="주요 인사이트 리스트")
        advice: str = Field(description="개인화된 조언")
        mission_success_rate: int = Field(ge=0, le=100, description="미션 성공률")

    report_analyzer = llm.with_structured_output(ReportAnalysis)

    prompt = f\"\"\"
    다음 일일 활동 데이터를 분석하여 리포트를 생성하세요:

    {state['daily_activity']}

    요구사항:
    1. 사용자의 하루 디지털 사용 패턴 요약
    2. 주요 발견사항과 인사이트 도출
    3. 개인화된 조언 제공
    4. 미션 성공률 계산 (0-100)
    \"\"\"

    analysis = report_analyzer.invoke(prompt)

    # Spring 백엔드 업데이트
    url = f"{SPRING_SERVER_URL}/api/report/{state['report_id']}"
    headers = {
        "Content-Type": "application/json",
        "X-API-Key": SECURITY_INTERNAL_API_KEY
    }
    payload = {
        "report_overview": analysis.report_overview,
        "insights": analysis.insights,
        "advice": analysis.advice,
        "mission_success_rate": analysis.mission_success_rate,
        "status": "COMPLETED"
    }

    with httpx.Client() as client:
        response = client.patch(url, json=payload, headers=headers, timeout=10.0)
        response.raise_for_status()

    return {
        "report_overview": analysis.report_overview,
        "insights": analysis.insights,
        "advice": analysis.advice,
        "mission_success_rate": analysis.mission_success_rate,
        "status": "COMPLETED"
    }
    """
    import json

    from agent.utils import report_summary_generator, update_report

    print("📝 5단계: 리포트 개요 및 조언 생성 시작")

    # insights는 이미 state에 있음
    insights = state.get("insights", [])
    daily_activity = state.get("daily_activity", {})
    mission_success_rate = state.get("mission_success_rate", 0)

    # LLM 프롬프트 구성
    system_prompt = """당신은 디지털 디톡스 앱 '디토'의 리포트 작성 AI입니다.
이미 분석된 인사이트를 바탕으로 전체 리포트 개요와 조언을 작성합니다.

1. report_overview: 하루 사용 패턴을 2-3문장으로 요약
2. advice: 개인화된 조언 1-2문장 (친근한 톤, 반말)

친근하고 격려하는 톤으로 작성하되, 객관적인 데이터 분석에 기반해야 합니다."""

    # insights를 텍스트로 변환
    insights_text = "\n".join(
        [f"- [{i.get('type')}] {i.get('description')}" for i in insights]
    )

    user_prompt = f"""다음 데이터를 바탕으로 리포트 개요와 조언을 작성하세요:

날짜: {state["report_date"]}
사용자 ID: {state["user_id"]}
미션 성공률: {mission_success_rate}%

인사이트 (이미 분석됨):
{insights_text}

일일 활동 데이터:
{json.dumps(daily_activity, ensure_ascii=False, indent=2)}

요구사항:
- report_overview: 전체 하루를 요약하는 2-3문장
- advice: 인사이트를 반영한 실천 가능한 조언 (1-2문장, 반말)"""

    # LLM 분석 실행
    print("     🧠 LLM으로 개요 및 조언 생성 중...")
    summary = report_summary_generator.invoke(
        [SystemMessage(content=system_prompt), HumanMessage(content=user_prompt)]
    )

    print("     ✅ 개요 및 조언 생성 완료")
    print(f"        - Overview: {summary.report_overview[:50]}...")
    print(f"        - Advice: {summary.advice[:50]}...")

    # Spring 백엔드 업데이트
    report_id = state.get("report_id")

    if report_id and report_id > 0:
        # 유효한 report_id가 있을 때만 API 호출
        update_success = update_report(
            report_id=report_id,
            report_overview=summary.report_overview,
            insights=insights,  # 이미 dict 형태
            advice=summary.advice,
            mission_success_rate=mission_success_rate,
        )

        if not update_success:
            print("     ⚠️ 리포트 업데이트 실패, 분석 결과만 반환합니다.")
    else:
        print("     ⚠️ 유효한 report_id 없음, API 업데이트 건너뜀")

    return {
        "report_overview": summary.report_overview,
        "advice": summary.advice,
        "status": "COMPLETED",
    }


# ============================================================================
# Graph Builder
# ============================================================================


def build_report_agent() -> StateGraph:
    """리포트 생성 에이전트 그래프 빌드.

    Workflow:
    ---------
    START
      ↓
    fetch_daily_activity (1단계: DailyUserActivity 및 미션 목록 조회)
      ↓
    create_empty_report (2단계: 빈 리포트 생성)
      ↓
    ┌─────────────────────┬─────────────────────┐
    │                     │                     │
    analyze_sleep       analyze_focus     analyze_self_control
    (3-1: 수면 패턴)    (3-2: 집중력)      (3-3: 자기 조절력)
    │                     │                     │
    └─────────────────────┴─────────────────────┘
      ↓
    aggregate_insights (4단계: 인사이트 통합)
      ↓
    update_report (5단계: 개요 및 조언 생성, 리포트 업데이트)
      ↓
    END

    Returns:
        Compiled StateGraph (checkpointer 없음 - LangGraph Server가 관리)
    """
    workflow = StateGraph(ReportState)

    # Add nodes
    workflow.add_node("fetch_daily_activity", fetch_daily_activity_node)
    workflow.add_node("create_empty_report", create_empty_report_node)
    workflow.add_node("analyze_sleep_pattern", analyze_sleep_pattern_node)
    workflow.add_node("analyze_focus_level", analyze_focus_level_node)
    workflow.add_node("analyze_self_control", analyze_self_control_node)
    workflow.add_node("aggregate_insights", aggregate_insights_node)
    workflow.add_node("update_report", update_report_node)

    # Add edges
    workflow.add_edge(START, "fetch_daily_activity")
    workflow.add_edge("fetch_daily_activity", "create_empty_report")

    # 병렬 분석 노드들
    workflow.add_edge("create_empty_report", "analyze_sleep_pattern")
    workflow.add_edge("create_empty_report", "analyze_focus_level")
    workflow.add_edge("create_empty_report", "analyze_self_control")

    # aggregator로 수렴
    workflow.add_edge("analyze_sleep_pattern", "aggregate_insights")
    workflow.add_edge("analyze_focus_level", "aggregate_insights")
    workflow.add_edge("analyze_self_control", "aggregate_insights")

    # 최종 업데이트
    workflow.add_edge("aggregate_insights", "update_report")
    workflow.add_edge("update_report", END)

    # Compile without checkpointer (LangGraph Server manages state)
    return workflow.compile()


# ============================================================================
# Agent Instance
# ============================================================================

report_agent = build_report_agent()
