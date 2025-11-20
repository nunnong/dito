"""유튜브 에이전트 (Youtube Agent).

유튜브 영상 분석 에이전트.
"""

from langgraph.graph import END, START, StateGraph

from agent.schemas import YoutubeState
from agent.utils import youtube_analyzer

# =============================================================================
# Youtube Agent Nodes
# =============================================================================


def youtube_analyze_node(state: YoutubeState) -> dict:
    """YouTube video classification node."""
    result = youtube_analyzer.invoke(f"""
You are a YouTube video analysis expert.  
Your task is to accurately classify the **type** and **main content keywords** of a video using only the title and channel name.

---

**Input:**
- Video title: "{state["title"]}"
- Channel name: "{state["channel"]}"

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
   - Examples: "Today’s IT News", "Real Estate Market Analysis"

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
  (e.g., “TRAVEL” for a vlog about a trip, “TECH” for a review of new gadgets)  
- If multiple themes are covered, include all relevant ones (e.g., ["FOOD", "TRAVEL"]).  
- Use “OTHER” only if no listed keyword fits.

---

**Classification Rules:**
1. Focus on the main ideas or subjects in the title.  
2. Use the channel name as a supporting cue (e.g., “Cooking with Emma” → FOOD, EDUCATIONAL).  
""")

    return {"video_type": result.video_type, "keywords": result.keywords}


# =============================================================================
# Youtube Agent Graph 구성
# =============================================================================


def build_youtube_agent() -> StateGraph:
    """유튜브 에이전트 그래프 구성.

    워크플로우:
    1. youtube_analyze_node: 영상 타입 분류
    """
    workflow = StateGraph(YoutubeState)

    # 노드 추가
    workflow.add_node("youtube_analyze_node", youtube_analyze_node)

    # 엣지 추가
    workflow.add_edge(START, "youtube_analyze_node")
    workflow.add_edge("youtube_analyze_node", END)

    # 컴파일 (LangGraph Server가 자동으로 checkpointer 관리)
    return workflow.compile()


# 그래프 생성
youtube_agent = build_youtube_agent()
