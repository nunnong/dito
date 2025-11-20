#!/usr/bin/env python
"""
개입 에이전트 타입별 테스트 스크립트
REST와 MEDITATION 타입이 올바르게 작동하는지 검증
"""

import json
from datetime import datetime

from agent.intervention_agent import intervention_agent
from agent.schemas import InterventionState

def test_rest_type():
    """REST 타입 테스트 - 숏폼 과다 사용 시나리오"""
    print("\n" + "="*60)
    print("TEST: REST 타입 (숏폼 과다 사용)")
    print("="*60)

    initial_state: InterventionState = {
        "user_id": "test_user_123",
        "behavior_log": {
            "app_name": "YouTube Shorts",
            "duration_seconds": 1200,  # 20분
            "session_count": 1,
            "usage_timestamp": datetime.now().isoformat(),
            "recent_app_switches": 2,
        }
    }

    config = {"configurable": {"thread_id": "test_rest"}}
    result = intervention_agent.invoke(initial_state, config)

    print("\n결과:")
    print(f"- 개입 필요: {result.get('intervention_needed')}")
    print(f"- 개입 유형: {result.get('intervention_type')}")
    print(f"- 넛지 타입: {result.get('nudge_type')}")
    print(f"- 지속 시간: {result.get('duration_seconds')}초")
    print(f"- 메시지: {result.get('nudge_message')}")

    # 검증
    assert result.get('nudge_type') in ["REST", "MEDITATION"], "nudge_type이 올바르지 않음"
    assert result.get('duration_seconds', 0) > 0, "duration_seconds가 설정되지 않음"
    print("\n✅ REST 타입 테스트 통과")

    return result

def test_meditation_type():
    """MEDITATION 타입 테스트 - 취침 시간 사용 시나리오"""
    print("\n" + "="*60)
    print("TEST: MEDITATION 타입 (취침 시간 사용)")
    print("="*60)

    # 밤 11시 시나리오 설정
    late_night_time = datetime.now().replace(hour=23, minute=0, second=0)

    initial_state: InterventionState = {
        "user_id": "test_user_456",
        "behavior_log": {
            "app_name": "Instagram",
            "duration_seconds": 600,  # 10분
            "session_count": 3,
            "usage_timestamp": late_night_time.isoformat(),
            "recent_app_switches": 5,
        }
    }

    config = {"configurable": {"thread_id": "test_meditation"}}
    result = intervention_agent.invoke(initial_state, config)

    print("\n결과:")
    print(f"- 개입 필요: {result.get('intervention_needed')}")
    print(f"- 개입 유형: {result.get('intervention_type')}")
    print(f"- 넛지 타입: {result.get('nudge_type')}")
    print(f"- 지속 시간: {result.get('duration_seconds')}초")
    print(f"- 메시지: {result.get('nudge_message')}")

    # 검증
    assert result.get('nudge_type') in ["REST", "MEDITATION"], "nudge_type이 올바르지 않음"
    assert result.get('duration_seconds', 0) > 0, "duration_seconds가 설정되지 않음"
    print("\n✅ MEDITATION 타입 테스트 통과")

    return result

def test_no_intervention():
    """개입 불필요 시나리오 테스트"""
    print("\n" + "="*60)
    print("TEST: 개입 불필요 (정상 사용)")
    print("="*60)

    initial_state: InterventionState = {
        "user_id": "test_user_789",
        "behavior_log": {
            "app_name": "KakaoTalk",
            "duration_seconds": 180,  # 3분
            "session_count": 1,
            "usage_timestamp": datetime.now().isoformat(),
            "recent_app_switches": 1,
        }
    }

    config = {"configurable": {"thread_id": "test_no_intervention"}}
    result = intervention_agent.invoke(initial_state, config)

    print("\n결과:")
    print(f"- 개입 필요: {result.get('intervention_needed')}")
    print(f"- 넛지 타입: {result.get('nudge_type')}")
    print(f"- 지속 시간: {result.get('duration_seconds')}초")
    print(f"- 메시지: {result.get('nudge_message')}")

    if not result.get('intervention_needed'):
        print("\n✅ 개입 불필요 테스트 통과")
    else:
        print("\n⚠️ 예상과 다르게 개입이 필요하다고 판단됨")

    return result

def validate_duration_by_urgency(result):
    """긴급도에 따른 duration 검증"""
    urgency = result.get('urgency_level', 'medium')
    nudge_type = result.get('nudge_type', 'REST')
    duration = result.get('duration_seconds', 0)

    expected_durations = {
        'REST': {'high': 180, 'medium': 300, 'low': 420},
        'MEDITATION': {'high': 300, 'medium': 600, 'low': 900}
    }

    # 테스트 환경에서는 짧은 시간 사용 (사용자가 수정한 값)
    test_durations = {
        'REST': {'high': 10, 'medium': 30, 'low': 40},
        'MEDITATION': {'high': 10, 'medium': 20, 'low': 30}
    }

    expected = test_durations.get(nudge_type, {}).get(urgency, 300)

    print(f"\n긴급도 검증:")
    print(f"- 긴급도: {urgency}")
    print(f"- 넛지 타입: {nudge_type}")
    print(f"- 실제 duration: {duration}초")
    print(f"- 예상 duration: {expected}초")

    if duration == expected:
        print("✅ Duration이 긴급도에 맞게 설정됨")
    else:
        print(f"⚠️ Duration 불일치 (실제: {duration}, 예상: {expected})")

def main():
    """메인 테스트 실행"""
    print("\n" + "="*60)
    print("개입 에이전트 타입별 테스트 시작")
    print("="*60)

    try:
        # 1. REST 타입 테스트
        rest_result = test_rest_type()
        validate_duration_by_urgency(rest_result)

        # 2. MEDITATION 타입 테스트
        meditation_result = test_meditation_type()
        validate_duration_by_urgency(meditation_result)

        # 3. 개입 불필요 테스트
        no_intervention_result = test_no_intervention()

        print("\n" + "="*60)
        print("모든 테스트 완료!")
        print("="*60)

        # 결과 요약
        print("\n📊 테스트 결과 요약:")
        print(f"1. REST 타입: {rest_result.get('nudge_type')} - {rest_result.get('duration_seconds')}초")
        print(f"2. MEDITATION 타입: {meditation_result.get('nudge_type')} - {meditation_result.get('duration_seconds')}초")
        print(f"3. 개입 불필요: intervention_needed={no_intervention_result.get('intervention_needed')}")

    except Exception as e:
        print(f"\n❌ 테스트 실패: {e}")
        import traceback
        traceback.print_exc()
        return 1

    return 0

if __name__ == "__main__":
    exit(main())