"""유틸리티 함수 및 LLM 설정 (Utility Functions and LLM Configuration)
- LLM 초기화 및 구조화된 출력 설정
- 시간 관련 유틸리티
- 데이터베이스 시뮬레이션 함수
"""

import os
from datetime import datetime, timedelta

import httpx
from langchain_anthropic import ChatAnthropic
from langgraph.checkpoint.memory import MemorySaver

from agent.schemas import (
    BehaviorAnalysis,
    EffectivenessAnalysis,
    InterventionDecision,
    InterventionState,
    Mission,
    MissionData,
    MissionNotificationResult,
    NudgeMessage,
    StrategyAdjustment,
    VideoType,
)

# =============================================================================
# LLM 설정 (LLM Configuration)
# =============================================================================

# LLM 초기화
llm = ChatAnthropic(model="claude-sonnet-4-5")

# 구조화된 출력을 위한 LLM들
behavior_analyzer = llm.with_structured_output(BehaviorAnalysis)
intervention_decider = llm.with_structured_output(InterventionDecision)

mission_generator = llm.with_structured_output(Mission)
message_generator = llm.with_structured_output(NudgeMessage)


effectiveness_analyzer = llm.with_structured_output(EffectivenessAnalysis)
strategy_adjuster = llm.with_structured_output(StrategyAdjustment)


llm_fast = ChatAnthropic(model="claude-haiku-4-5")

youtube_analyzer = llm_fast.with_structured_output(VideoType)


# Checkpointer (상태 영속성)
checkpointer = MemorySaver()


# =============================================================================
# Spring 서버 연동 설정 (Spring Server Integration)
# =============================================================================

SPRING_SERVER_URL = os.getenv("SPRING_SERVER_URL", "http://52.78.96.102:8080")
SECURITY_INTERNAL_API_KEY = os.getenv("SECURITY_INTERNAL_API_KEY")


# =============================================================================
# 시간 유틸리티 함수 (Time Utility Functions)
# =============================================================================


def get_current_timestamp() -> str:
    """현재 시간을 ISO 포맷으로 반환"""
    return datetime.now().isoformat()


def schedule_evaluation(intervention_time: str, delay_minutes: int) -> str:
    """평가 스케줄 시간 계산"""
    intervention_dt = datetime.fromisoformat(intervention_time)
    evaluation_dt = intervention_dt + timedelta(minutes=delay_minutes)
    return evaluation_dt.isoformat()


def get_time_slot_from_timestamp(timestamp_str: str) -> str:
    """타임스탬프에서 time_slot 계산

    Args:
        timestamp_str: ISO 8601 타임스탬프 (예: "2025-01-03T23:45:00")

    Returns:
        time_slot: "morning", "afternoon", "evening", "night"
    """
    dt = datetime.fromisoformat(timestamp_str)
    hour = dt.hour

    if 6 <= hour < 12:
        return "morning"
    elif 12 <= hour < 18:
        return "afternoon"
    elif 18 <= hour < 22:
        return "evening"
    else:
        return "night"


def truncate_message(message: str, max_length: int = 100) -> str:
    """메시지를 최대 길이로 잘라냄

    Args:
        message: 원본 메시지
        max_length: 최대 길이 (기본값: 100자)

    Returns:
        잘라낸 메시지 (한글 기준)
    """
    if len(message) <= max_length:
        return message

    # 100자로 자르되, 마침표나 느낌표가 있으면 그 앞에서 자름
    truncated = message[:max_length]

    # 문장 부호 찾기 (뒤에서부터)
    for i in range(len(truncated) - 1, max(0, len(truncated) - 20), -1):
        if truncated[i] in [".", "!", "?", "。", "!", "?"]:
            return truncated[: i + 1]

    # 문장 부호가 없으면 그냥 100자에서 자르고 '...' 추가 (단, 97자까지만)
    return message[:97] + "..."


# =============================================================================
# 데이터베이스 시뮬레이션 함수 (Database Simulation Functions)
# =============================================================================


def simulate_behavior_log(user_id: int) -> dict:
    """실제 환경에서는 app_usage_logs 테이블에서 가져옴
    MVP에서는 시뮬레이션
    """
    return {
        "app_name": "YouTube Shorts",
        "duration_seconds": 1200,  # 20분
        "session_count": 1,
        "usage_timestamp": datetime.now().isoformat(),  # ISO 8601 전체 타임스탬프
        "recent_app_switches": 2,
    }


def simulate_post_intervention_usage(user_id: int, intervention_id: int) -> dict:
    """실제 환경에서는 intervention 후의 app_usage_logs를 조회
    MVP에서는 시뮬레이션
    """
    return {
        "user_id": user_id,
        "intervention_id": intervention_id,
        "duration_after_intervention": 300,  # 5분 (개선됨)
        "behavior_changed": True,
    }


# =============================================================================
# 미션 및 알림 함수 (Mission and Notification Functions)
# =============================================================================


def get_db_user_id(personal_id: str) -> int | None:
    """personalId로 DB user_id 조회

    Args:
        personal_id: 사용자 personalId (로그인 ID)

    Returns:
        DB user_id (int) if successful, None if failed
    """
    if not SECURITY_INTERNAL_API_KEY:
        print("❌ SECURITY_INTERNAL_API_KEY environment variable is not set")
        return None

    print(f"     🔍 DB user_id 조회 중... (personalId={personal_id})")

    headers = {
        "X-API-Key": SECURITY_INTERNAL_API_KEY,
        "Content-Type": "application/json",
    }

    try:
        with httpx.Client(timeout=10.0) as client:
            response = client.get(
                f"{SPRING_SERVER_URL}/api/user/{personal_id}",
                headers=headers,
            )
            response.raise_for_status()
            user_data = response.json()

            db_user_id = user_data.get("data", {}).get("profile", {}).get("userId")
            if not db_user_id:
                print("     ❌ DB user_id 조회 실패: 응답에 userId 없음")
                return None

            print(f"     ✅ DB user_id 조회 완료: {db_user_id}")
            return db_user_id

    except httpx.HTTPError as e:
        print(f"     ❌ DB user_id 조회 실패: {e}")
        if hasattr(e, "response") and e.response:
            print(f"        응답 코드: {e.response.status_code}")
            try:
                error_detail = e.response.json()
                print(f"        오류 상세: {error_detail}")
            except:
                print(f"        오류 텍스트: {e.response.text[:200]}")
        return None


def create_mission(mission_data: MissionData) -> str | None:
    """미션 생성 API 호출

    Args:
        mission_data: 미션 생성 데이터 (Pydantic model)

    Returns:
        mission_id (str) if successful, None if failed
    """
    if not SECURITY_INTERNAL_API_KEY:
        print("❌ SECURITY_INTERNAL_API_KEY environment variable is not set")
        return None

    print("     📝 미션 생성 중...")

    headers = {
        "X-API-Key": SECURITY_INTERNAL_API_KEY,
        "Content-Type": "application/json",
    }

    # Pydantic model을 dict로 변환
    mission_payload = mission_data.model_dump()

    try:
        with httpx.Client(timeout=10.0) as client:
            response = client.post(
                f"{SPRING_SERVER_URL}/api/mission",
                json=mission_payload,
                headers=headers,
            )
            response.raise_for_status()
            result = response.json()

            mission_id = result.get("data", {}).get("missionId")

            if mission_id:
                print(f"     ✅ 미션 생성 완료: ID={mission_id}")
                return str(mission_id)
            else:
                print("     ⚠️ 미션 생성 응답에 missionId 없음")
                return None

    except httpx.HTTPError as e:
        print(f"     ❌ 미션 생성 실패: {e}")
        if hasattr(e, "response") and e.response:
            print(f"        응답 코드: {e.response.status_code}")
            try:
                error_detail = e.response.json()
                print(f"        오류 상세: {error_detail}")
            except:
                print(f"        오류 텍스트: {e.response.text[:200]}")
        return None


def send_fcm_with_mission(user_id: int, mission_id: str, message: str) -> bool:
    """FCM 알림 전송 (미션 ID 포함)

    Args:
        user_id: DB user ID (int)
        mission_id: 미션 ID (백엔드가 Mission 테이블에서 enrichment)
        message: 알림 메시지

    Returns:
        True if successful, False if failed
    """
    if not SECURITY_INTERNAL_API_KEY:
        print("❌ SECURITY_INTERNAL_API_KEY environment variable is not set")
        return False

    print("     📱 FCM 알림 전송 중...")

    headers = {
        "X-API-Key": SECURITY_INTERNAL_API_KEY,
        "Content-Type": "application/json",
    }

    fcm_payload = {
        "user_id": user_id,
        "title": "디토",
        "message": message,
        "mission_id": mission_id,
    }

    try:
        with httpx.Client(timeout=10.0) as client:
            response = client.post(
                f"{SPRING_SERVER_URL}/api/fcm/send",
                json=fcm_payload,
                headers=headers,
            )
            response.raise_for_status()
            result = response.json()

            if result.get("success"):
                print(f"     ✅ FCM 전송 완료: mission_id={mission_id}")
                return True
            else:
                print(f"     ❌ FCM 전송 실패: {result.get('error')}")
                return False

    except httpx.HTTPError as e:
        print(f"     ❌ FCM HTTP 오류: {e}")
        if hasattr(e, "response") and e.response:
            print(f"        응답 코드: {e.response.status_code}")
            try:
                error_detail = e.response.json()
                print(f"        오류 상세: {error_detail}")
            except:
                print(f"        오류 텍스트: {e.response.text[:200]}")
        return False


def send_notification(state: InterventionState) -> MissionNotificationResult:
    """이미 생성된 미션에 대해 FCM 알림만 전송 (모듈화)

    역할:
    - 이미 생성된 mission_id를 사용해 FCM 알림만 전송
    - generate_mission에서 미션 생성 후, 별도로 FCM 전송할 때 사용

    Args:
        state: Intervention state containing:
            - mission_id (int): 이미 생성된 미션 ID
            - nudge_message (str): 전송할 메시지
            - user_id (int): DB user ID

    Returns:
        MissionNotificationResult with FCM send status
    """
    db_user_id = state["user_id"]
    mission_id = state.get("mission_id")
    nudge_message = state.get("nudge_message")

    # Validation
    if not mission_id:
        return MissionNotificationResult(
            success=False,
            mission_id=None,
            fcm_sent=False,
            db_user_id=db_user_id,
            error_stage="validation",
        )

    if not nudge_message:
        return MissionNotificationResult(
            success=False,
            mission_id=str(mission_id),
            fcm_sent=False,
            db_user_id=db_user_id,
            error_stage="validation",
        )

    # FCM 전송
    fcm_sent = send_fcm_with_mission(db_user_id, str(mission_id), nudge_message)

    if not fcm_sent:
        return MissionNotificationResult(
            success=False,
            mission_id=str(mission_id),
            fcm_sent=False,
            db_user_id=db_user_id,
            error_stage="fcm_send",
        )

    # Success
    return MissionNotificationResult(
        success=True,
        mission_id=str(mission_id),
        fcm_sent=True,
        db_user_id=db_user_id,
        error_stage=None,
    )


# =============================================================================
# 평가 관련 함수 (Evaluation Functions)
# =============================================================================


def fetch_mission_info(mission_id: int) -> dict | None:
    """미션 정보 조회 API 호출

    Args:
        mission_id: 미션 ID

    Returns:
        미션 정보 dict if successful, None if failed
    """
    if not SECURITY_INTERNAL_API_KEY:
        print("❌ SECURITY_INTERNAL_API_KEY environment variable is not set")
        return None

    print(f"     🔍 미션 정보 조회 중... (mission_id={mission_id})")

    headers = {
        "X-API-Key": SECURITY_INTERNAL_API_KEY,
        "Content-Type": "application/json",
    }

    try:
        with httpx.Client(timeout=10.0) as client:
            response = client.get(
                f"{SPRING_SERVER_URL}/api/mission/{mission_id}",
                headers=headers,
            )
            response.raise_for_status()
            result = response.json()

            mission_data = result.get("data")
            if not mission_data:
                print("     ❌ 미션 정보 조회 실패: data 필드 없음")
                return None

            # data가 리스트인 경우 첫 번째 요소 추출
            if isinstance(mission_data, list) and len(mission_data) > 0:
                mission_data = mission_data[0]

            print(f"     ✅ 미션 정보 조회 완료: {mission_data.get('missionType')}")
            return mission_data

    except httpx.HTTPError as e:
        print(f"     ❌ 미션 정보 조회 실패: {e}")
        if hasattr(e, "response") and e.response:
            print(f"        응답 코드: {e.response.status_code}")
            try:
                error_detail = e.response.json()
                print(f"        오류 상세: {error_detail}")
            except:
                print(f"        오류 텍스트: {e.response.text[:200]}")
        return None


def evaluate_mission_with_llm(mission_info: dict, behavior_logs: list[dict]) -> tuple[str, str]:
    """미션 평가 및 피드백 생성

    behavior_logs와 mission의 targetApp을 비교하여 성공/실패 판정하고,
    LLM을 사용하여 상세한 피드백을 생성합니다.

    Args:
        mission_info: 미션 정보 (missionType, targetApp 등)
        behavior_logs: BehaviorLog 목록 (빈 배열일 수 있음)

    Returns:
        (evaluation_result, feedback) tuple
        - evaluation_result: "SUCCESS" | "FAILURE"
        - feedback: 평가 피드백 메시지 (LLM 생성)
    """
    target_app = mission_info.get("targetApp", "")
    mission_type = mission_info.get("missionType", "")
    mission_text = mission_info.get("missionText", "")

    # behavior_logs가 빈 배열인 경우 처리
    if not behavior_logs or len(behavior_logs) == 0:
        print("     ℹ️ behavior_logs가 비어있음 - 앱 사용 기록 없음")
        evaluation_result = "SUCCESS"

        # 빈 배열인 경우 간단한 피드백 반환 (LLM 호출 없이)
        feedback = "미션 시간 동안 앱을 전혀 사용하지 않았어! 완벽한 디지털 디톡스야. 이 습관을 계속 유지해보자! 💪"

        print(f"     평가 결과: {evaluation_result}")
        print(f"     피드백: {feedback}")

        return evaluation_result, feedback

    # targetApp 사용 여부 확인
    has_violation = False
    violation_details = []

    for log in behavior_logs:
        if log.get("log_type") != "APP_USAGE":
            continue

        app_name = log.get("app_name", "")
        package_name = log.get("package_name", "")
        duration = log.get("duration_seconds", 0)

        # targetApp과 일치하는지 확인 (app_name 또는 package_name)
        if target_app in [app_name, package_name]:
            has_violation = True
            violation_details.append({
                "app": app_name or package_name,
                "duration": duration,
                "timestamp": log.get("timestamp", "")
            })

    evaluation_result = "FAILURE" if has_violation else "SUCCESS"

    # LLM으로 피드백 생성
    from langchain_core.messages import HumanMessage, SystemMessage

    system_prompt = """당신은 디지털 디톡스 앱 '디토'의 미션 평가 AI입니다.
사용자의 미션 수행 결과를 분석하고, 친근하고 격려하는 피드백을 제공합니다.

피드백 작성 가이드:
- 성공 시: 구체적으로 칭찬하고, 다음 목표를 제시
- 실패 시: 긍정적으로 격려하고, 개선 방안 제안
- 최대 2-3문장으로 간결하게 작성
- 반말 사용 (친근한 톤)
"""

    if evaluation_result == "SUCCESS":
        user_prompt = f"""미션: {mission_text}
미션 타입: {mission_type}
제한 앱: {target_app}

결과: 성공! 제한된 앱을 사용하지 않았습니다.

사용자를 칭찬하는 긍정적인 피드백을 작성해주세요."""
    else:
        violation_summary = ", ".join([
            f"{v['app']} ({v['duration']}초)"
            for v in violation_details
        ])
        user_prompt = f"""미션: {mission_text}
미션 타입: {mission_type}
제한 앱: {target_app}

결과: 실패. 다음 앱을 사용했습니다:
{violation_summary}

사용자를 격려하고 다음에는 성공할 수 있도록 응원하는 피드백을 작성해주세요."""

    response = llm.invoke([
        SystemMessage(content=system_prompt),
        HumanMessage(content=user_prompt)
    ])

    feedback = response.content.strip()

    print(f"     평가 결과: {evaluation_result}")
    print(f"     피드백: {feedback}")

    return evaluation_result, feedback


def send_evaluation_fcm(user_id: int, result: str, feedback: str, mission_id: int) -> bool:
    """평가 결과 FCM 알림 전송

    Args:
        user_id: DB user ID
        result: "SUCCESS" | "FAILURE"
        feedback: 평가 피드백 메시지
        mission_id: 미션 ID

    Returns:
        True if successful, False if failed
    """
    if not SECURITY_INTERNAL_API_KEY:
        print("❌ SECURITY_INTERNAL_API_KEY environment variable is not set")
        return False

    print("     📱 평가 결과 FCM 전송 중...")

    headers = {
        "X-API-Key": SECURITY_INTERNAL_API_KEY,
        "Content-Type": "application/json",
    }

    # 제목 결정
    title = "🎉 미션 성공!" if result == "SUCCESS" else "💪 다음엔 성공!"

    fcm_payload = {
        "user_id": user_id,
        "title": title,
        "message": feedback,
        "mission_id": str(mission_id),
    }

    try:
        with httpx.Client(timeout=10.0) as client:
            response = client.post(
                f"{SPRING_SERVER_URL}/api/fcm/send",
                json=fcm_payload,
                headers=headers,
            )
            response.raise_for_status()
            result_data = response.json()

            if result_data.get("success"):
                print(f"     ✅ 평가 FCM 전송 완료")
                return True
            else:
                print(f"     ❌ 평가 FCM 전송 실패: {result_data.get('error')}")
                return False

    except httpx.HTTPError as e:
        print(f"     ❌ 평가 FCM HTTP 오류: {e}")
        if hasattr(e, "response") and e.response:
            print(f"        응답 코드: {e.response.status_code}")
            try:
                error_detail = e.response.json()
                print(f"        오류 상세: {error_detail}")
            except:
                print(f"        오류 텍스트: {e.response.text[:200]}")
        return False


def submit_mission_result(mission_id: int, result: str, feedback: str = "") -> bool:
    """미션 결과 제출 API 호출

    Args:
        mission_id: 미션 ID
        result: "SUCCESS" | "FAILURE" | "IGNORE"
        feedback: 평가 피드백 메시지

    Returns:
        True if successful, False if failed
    """
    if not SECURITY_INTERNAL_API_KEY:
        print("❌ SECURITY_INTERNAL_API_KEY environment variable is not set")
        return False

    print(f"     💾 미션 결과 저장 중... (mission_id={mission_id}, result={result})")

    headers = {
        "X-API-Key": SECURITY_INTERNAL_API_KEY,
        "Content-Type": "application/json",
    }

    payload = {
        "mission_id": mission_id,
        "result": result,  # "SUCCESS" | "FAILURE" | "IGNORE"
        "feedback": feedback  # 평가 피드백
    }

    try:
        with httpx.Client(timeout=10.0) as client:
            response = client.post(
                f"{SPRING_SERVER_URL}/api/mission/result",
                json=payload,
                headers=headers,
            )
            response.raise_for_status()
            result_data = response.json()

            if result_data.get("success"):
                print(f"     ✅ 미션 결과 저장 완료")
                return True
            else:
                print(f"     ❌ 미션 결과 저장 실패: {result_data.get('error')}")
                return False

    except httpx.HTTPError as e:
        print(f"     ❌ 미션 결과 저장 HTTP 오류: {e}")
        if hasattr(e, "response") and e.response:
            print(f"        응답 코드: {e.response.status_code}")
            try:
                error_detail = e.response.json()
                print(f"        오류 상세: {error_detail}")
            except:
                print(f"        오류 텍스트: {e.response.text[:200]}")
        return False
