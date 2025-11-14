"""실시간 개입 에이전트 (Intervention Agent)
- 행동 패턴 분석
- 개입 필요성 판단
- 넛지 메시지 생성 및 발송.
"""

from typing import Literal

from langchain_core.messages import HumanMessage, SystemMessage
from langgraph.graph import END, START, StateGraph
from langgraph.types import Command

from agent.prompts import (
    SYSTEM_MSG_NUDGE_GENERATOR,
    get_behavior_analysis_prompt,
    get_intervention_decision_prompt,
    get_mission_generation_prompt,
)
from agent.schemas import InterventionState, MissionData
from agent.utils import (
    behavior_analyzer,
    create_mission,
    get_time_slot_from_timestamp,
    intervention_decider,
    message_generator,
    mission_generator,
    send_notification,
    truncate_message,
    youtube_analyzer,
)

# =============================================================================
# Intervention Agent Nodes
# =============================================================================


def youtube_analyze_node(state: InterventionState) -> dict:
    """0단계: 유튜브 영상 분석 (조건부)

    유튜브 영상인 경우 video_type과 keywords를 분류.
    비-유튜브 앱인 경우 빈 dict 반환하여 스킵.
    """
    print("youtube analyze")

    # behavior_log에서 app_metadata 추출
    behavior_log = state.get("behavior_log", {})
    app_metadata = behavior_log.get("app_metadata")

    # app_metadata가 없으면 스킵
    if not app_metadata:
        return {}

    # title 또는 channel 중 하나라도 있으면 분석 수행
    title = app_metadata.get("title", "")
    channel = app_metadata.get("channel", "")

    if not title and not channel:
        return {}

    # 유튜브 영상 분석 수행
    result = youtube_analyzer.invoke(f"""
You are a YouTube video analysis expert.
Your task is to accurately classify the **type** and **main content keywords** of a video using only the title and channel name.

---

**Input:**
- Video title: "{title if title else "Unknown"}"
- Channel name: "{channel if channel else "Unknown"}"

---

### 🎬 Video Type Categories (10 total)
Select the **primary format** of the video (choose one):

1. **EDUCATIONAL**
   - Keywords: lecture, course, learn, understand, concept, principle, explain
   - Examples: "Introduction to Calculus", "Programming Basics Course"

2. **ENTERTAINMENT**
   - Keywords: funny, reaction, challenge, prank, mukbang, show
   - Examples: "Friends Try Extreme Challenge", "Funny Reaction Compilation"

3. **NEWS_INFO**
   - Keywords: news, issue, analysis, report, trend, commentary
   - Examples: "Today's IT News", "Real Estate Market Analysis"

4. **VLOG**
   - Keywords: daily, routine, vlog, trip, travel, life
   - Examples: "A Day in My Life", "Travel Vlog in Tokyo"

5. **SHORT_FORM**
   - Keywords: shocking, legend, twist, viral, wow, unbelievable, short clip
   - Examples: "Unbelievable Twist Ending!", "Shocking True Story"
   - **Note:** Often short-form content (under 1 minute) with clickbait or emotional titles.

6. **GAMING**
   - Keywords: gameplay, walkthrough, highlight, live, playthrough
   - Examples: "League of Legends Highlights", "Minecraft Survival"

7. **MUSIC**
   - Keywords: song, music video, cover, mv, performance, instrument
   - Examples: "New Music Video", "Piano Cover of BTS Song"

8. **REVIEW**
   - Keywords: review, unboxing, comparison, recommendation, feedback
   - Examples: "iPhone 15 Review", "Top 5 Restaurants in Seoul"

9. **TUTORIAL**
   - Keywords: how to, guide, make, create, tutorial, tips
   - Examples: "Photoshop Editing Tutorial", "How to Code in Python"

10. **UNKNOWN**
    - For cases where the title is too vague or lacks sufficient information.

---

### 🏷️ Content Keywords (multiple allowed)

After deciding the `video_type`, select **one or more keywords** that describe the **main topics or subjects** the video covers.

**Available keywords:**
HISTORY, SCIENCE, TECH, FOOD, TRAVEL, HEALTH, FITNESS, BEAUTY, FASHION,
IDOL, SPORTS, POLITICS, ECONOMY, ART, MOVIE, MUSIC, GAME, ANIMAL,
EDUCATION, DAILY, LIFESTYLE, OTHER

**Guidelines for keyword selection:**
- Choose keywords that reflect **what the video is about**, not its format.
  (e.g., "TRAVEL" for a vlog about a trip, "TECH" for a review of new gadgets)
- If multiple themes are covered, include all relevant ones (e.g., ["FOOD", "TRAVEL"]).
- Use "OTHER" only if no listed keyword fits.

---

**Classification Rules:**
1. Focus on the main ideas or subjects in the title.
2. Use the channel name as a supporting cue (e.g., "Cooking with Emma" → FOOD, EDUCATIONAL).
""")

    return {"video_type": result.video_type, "keywords": result.keywords}


def analyze_behavior_node(state: InterventionState) -> dict:
    """1단계: 행동 패턴 분석
    사용자의 앱 사용 로그를 분석하여 패턴 파악.
    유튜브 영상 정보가 있으면 함께 고려.
    """

    # Validate behavior_log is present
    if "behavior_log" not in state or state.get("behavior_log") is None:
        raise ValueError("behavior_log is required for analyze_behavior")

    behavior_log = state["behavior_log"]

    # usage_timestamp에서 time_slot 계산, 시간대 반환
    time_slot = get_time_slot_from_timestamp(behavior_log["usage_timestamp"])

    # 유튜브 영상 정보 가져오기 (youtube_analyze_node에서 생성됨)
    video_info = None
    if state.get("video_type"):
        video_info = {
            "video_type": state.get("video_type"),
            "keywords": state.get("keywords", []),
        }

    # LLM을 사용한 행동 패턴 분석 (video 정보 포함)
    analysis_prompt = get_behavior_analysis_prompt(behavior_log, time_slot, video_info)

    # with_structured_output()을 사용할 때는 문자열로 직접 전달
    analysis = behavior_analyzer.invoke(analysis_prompt)

    return {
        "behavior_pattern": analysis.summary,
        "trigger_event": analysis.trigger_event,
        "severity_score": analysis.severity_score,
        "key_indicators": analysis.key_indicators,
    }


def decide_intervention_node(
    state: InterventionState,
) -> Command[Literal["mission_node", END]]:
    """2단계: 개입 필요성 판단"""

    decision_prompt = get_intervention_decision_prompt(
        state["behavior_pattern"], state["trigger_event"]
    )

    # with_structured_output()을 사용할 때는 문자열로 직접 전달
    decision = intervention_decider.invoke(decision_prompt)

    return Command(
        update={
            "intervention_needed": decision.intervention_needed,
            "intervention_reason": decision.reasoning,
        },
        goto="mission_node" if decision.intervention_needed else END,
    )


def mission_node(state: InterventionState) -> dict:
    """3단계: 미션 생성

    역할:
    1. LLM이 상황에 따라 적절한 mission_type (REST/MEDITATION)과 duration_seconds 결정
    2. Spring 백엔드 /api/mission API 호출하여 실제 미션 생성
    3. 생성된 mission_id를 state에 저장
    """

    # 1단계: LLM을 사용한 미션 파라미터 결정
    mission_prompt = get_mission_generation_prompt(
        state["behavior_pattern"], state["trigger_event"], state["severity_score"]
    )

    # with_structured_output()을 사용할 때는 문자열로 직접 전달
    mission = mission_generator.invoke(mission_prompt)

    # 2단계: Spring 백엔드 API로 미션 생성
    # MissionData 객체 생성 (임시 메시지 사용, 나중에 generate_message에서 업데이트 가능)
    mission_data = MissionData(
        user_id=state["user_id"],
        mission_type=mission.mission_type,
        mission_text=f"{mission.mission_type} 미션을 시작하세요",  # 임시 메시지
        coin_reward=10,
        duration_seconds=mission.duration_seconds,
        target_app=state.get("behavior_log", {}).get("app_name", "All Apps"),
        stat_change_self_care=10,
        stat_change_focus=20,
        stat_change_sleep=30,
        prompt="AI Intervention V1",
    )

    # API 호출하여 미션 생성
    mission_id = create_mission(mission_data)

    # 생성된 미션 정보를 state에 저장
    return {
        "mission_id": int(mission_id) if mission_id else None,
        "mission_type": mission.mission_type,
        "duration_seconds": mission.duration_seconds,
    }


def message_node(state: InterventionState) -> dict:
    """4단계: 넛지 메시지 생성.

    역할:
    - 미션 정보를 바탕으로 사용자에게 전달할 공감적인 메시지 생성
    - "[인식] → [제안] → [보상]" 프레임 사용
    """

    # State에서 정보 가져오기
    mission_type = state.get("mission_type", "REST")
    duration_seconds = state.get("duration_seconds", 300)
    coin_reward = state.get("coin_reward", 10)

    nudge_prompt = f"""
사용자에게 전달할 넛지 메시지를 생성하세요.

상황:
- 행동 패턴: {state["behavior_pattern"]}
- 미션 유형: {mission_type}
- 미션 시간: {duration_seconds}초
- 보상: {coin_reward} 코인

메시지 프레임: "[인식] → [제안] → [보상]"
예시: "30분째 시청 중이에요 → {duration_seconds // 60}분 휴식 어때요? → 성공 시 +{coin_reward} 코인!"

요구사항:
1. 최대 100자 이내
2. 친근하고 공감적인 톤
3. 행동 패턴을 명확히 인식시킴
4. 구체적인 미션 시간과 보상 제시
"""

    try:
        # LLM 호출 - 넛지 메시지 생성
        # with_structured_output()을 사용할 때는 SystemMessage + HumanMessage 조합
        nudge = message_generator.invoke(
            [
                SystemMessage(content=SYSTEM_MSG_NUDGE_GENERATOR),
                HumanMessage(content=nudge_prompt),
            ]
        )
    except Exception as e:
        from agent.schemas import NudgeMessage

        nudge = NudgeMessage(
            message=f"잠시 {duration_seconds // 60}분 휴식 어때요? 성공 시 +{coin_reward} 코인!",
        )

    # 메시지 길이 검증 및 자르기 (최대 100자)
    truncated_message = truncate_message(nudge.message, max_length=100)

    # State에 nudge_message를 추가한 후 FCM 전송
    # LangGraph에서는 state를 직접 수정하지 말고 업데이트된 state를 만들어야 함
    updated_state = {**state, "nudge_message": truncated_message}

    result = send_notification(updated_state)

    # 결과 처리: nudge_message와 fcm_sent를 모두 반환
    return {"nudge_message": truncated_message, "fcm_sent": result.fcm_sent}


# =============================================================================
# Intervention Agent Graph 구성
# =============================================================================


def build_intervention_agent() -> StateGraph:
    """실시간 개입 에이전트 그래프 구성.

    워크플로우:
    0. youtube_analyze: 유튜브 영상 분석 (조건부)
    1. analyze_behavior: 행동 패턴 분석 (유튜브 정보 활용)
    2. decide_intervention: 개입 필요성 판단 (Command로 분기)
    3. generate_mission: 미션 생성 (LLM + API 호출)
    4. generate_message: 넛지 메시지 생성
    5. send_intervention: FCM 알림 전송
    """
    workflow = StateGraph(InterventionState)

    # 노드 추가
    workflow.add_node("youtube_analyze_node", youtube_analyze_node)
    workflow.add_node("analyze_behavior_node", analyze_behavior_node)
    workflow.add_node("decide_intervention_node", decide_intervention_node)
    workflow.add_node("mission_node", mission_node)
    workflow.add_node("message_node", message_node)

    # 엣지 추가
    workflow.add_edge(START, "youtube_analyze_node")
    workflow.add_edge("youtube_analyze_node", "analyze_behavior_node")
    workflow.add_edge("analyze_behavior_node", "decide_intervention_node")
    # decide_intervention에서 조건부 라우팅 (Command 사용)
    # intervention_needed=True → generate_mission, False → END
    workflow.add_edge("mission_node", "message_node")
    workflow.add_edge("message_node", END)

    # 컴파일 (LangGraph Server가 자동으로 checkpointer 관리)
    return workflow.compile()


# 그래프 생성
intervention_agent = build_intervention_agent()
