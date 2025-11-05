"""
Intervention Agent Evaluation Dataset
Ground Truth 데이터셋 for testing and evaluation
"""

from datetime import datetime

# =============================================================================
# Easy Cases (명확한 트리거와 예상 결과)
# =============================================================================

# Easy Case 1: 숏폼 과다 시청 (야간)
easy_case_1 = {
    "name": "E1_shortform_night",
    "input": {
        "user_id": 1,
        "behavior_log": {
            "app_name": "YouTube Shorts",
            "duration_seconds": 1500,  # 25분
            "usage_timestamp": "2025-01-03T23:30:00",
            "recent_app_switches": 0
        }
    },
    "expected": {
        "trigger_event": ["bedtime-usage", "short-form-overuse"],  # 둘 다 허용
        "pattern_type": "critical",
        "severity_range": (8, 10),
        "intervention_needed": True,
        "intervention_type": ["bedtime-usage", "short-form-overuse"],
        "urgency_level": "high"
    },
    "nudge_criteria": {
        "must_mention_sleep_or_time": True,
        "must_have_frame_complete": True,  # [인식]-[제안]-[보상]
        "tone_friendly": True
    }
}

# Easy Case 2: 숏폼 과다 시청 (주간)
easy_case_2 = {
    "name": "E2_shortform_daytime",
    "input": {
        "user_id": 2,
        "behavior_log": {
            "app_name": "TikTok",
            "duration_seconds": 1800,  # 30분
            "usage_timestamp": "2025-01-03T15:00:00",
            "recent_app_switches": 1
        }
    },
    "expected": {
        "trigger_event": ["short-form-overuse"],
        "pattern_type": ["concerning", "critical"],
        "severity_range": (7, 9),
        "intervention_needed": True,
        "intervention_type": ["short-form-overuse"],
        "urgency_level": ["medium", "high"]
    },
    "nudge_criteria": {
        "must_mention_usage_time": True,
        "must_have_frame_complete": True
    }
}

# Easy Case 3: 정상 사용 (짧은 시간)
easy_case_3 = {
    "name": "E3_normal_short",
    "input": {
        "user_id": 3,
        "behavior_log": {
            "app_name": "Calculator",
            "duration_seconds": 120,  # 2분
            "usage_timestamp": "2025-01-03T10:00:00",
            "recent_app_switches": 1
        }
    },
    "expected": {
        "trigger_event": ["none"],
        "pattern_type": "normal",
        "severity_range": (0, 2),
        "intervention_needed": False,
        "intervention_type": None,
        "urgency_level": None
    },
    "nudge_criteria": None  # 개입 없음
}

# Easy Case 4: 앱 전환 과다
easy_case_4 = {
    "name": "E4_app_switching",
    "input": {
        "user_id": 4,
        "behavior_log": {
            "app_name": "Instagram",
            "duration_seconds": 300,  # 5분
            "usage_timestamp": "2025-01-03T14:00:00",
            "recent_app_switches": 15
        }
    },
    "expected": {
        "trigger_event": ["app-switching"],
        "pattern_type": ["concerning", "critical"],
        "severity_range": (6, 8),
        "intervention_needed": True,
        "intervention_type": ["app-switching"],
        "urgency_level": ["medium", "high"]
    },
    "nudge_criteria": {
        "must_mention_focus_or_switching": True,
        "must_have_frame_complete": True
    }
}

# Easy Case 5: 취침 시간 사용
easy_case_5 = {
    "name": "E5_bedtime_usage",
    "input": {
        "user_id": 5,
        "behavior_log": {
            "app_name": "Instagram",
            "duration_seconds": 900,  # 15분
            "usage_timestamp": "2025-01-03T23:00:00",
            "recent_app_switches": 3
        }
    },
    "expected": {
        "trigger_event": ["bedtime-usage"],
        "pattern_type": ["concerning", "critical"],
        "severity_range": (6, 9),
        "intervention_needed": True,
        "intervention_type": ["bedtime-usage"],
        "urgency_level": ["medium", "high"]
    },
    "nudge_criteria": {
        "must_mention_sleep": True,
        "must_have_frame_complete": True
    }
}

# Easy Case 6: 집중 사용 (장시간 생산성 앱)
easy_case_6 = {
    "name": "E6_focus_break_needed",
    "input": {
        "user_id": 6,
        "behavior_log": {
            "app_name": "Microsoft Word",
            "duration_seconds": 2400,  # 40분
            "usage_timestamp": "2025-01-03T15:00:00",
            "recent_app_switches": 0
        }
    },
    "expected": {
        "trigger_event": ["focus-break"],
        "pattern_type": ["normal", "concerning"],
        "severity_range": (4, 7),
        "intervention_needed": True,
        "intervention_type": ["focus-break"],
        "urgency_level": ["low", "medium"]
    },
    "nudge_criteria": {
        "must_mention_rest_or_break": True,
        "tone_positive": True  # "잘하고 있어요!"
    }
}

# Easy Case 7: 정상 사용 (중간 시간)
easy_case_7 = {
    "name": "E7_normal_medium",
    "input": {
        "user_id": 7,
        "behavior_log": {
            "app_name": "Email",
            "duration_seconds": 600,  # 10분
            "usage_timestamp": "2025-01-03T09:00:00",
            "recent_app_switches": 2
        }
    },
    "expected": {
        "trigger_event": ["none"],
        "pattern_type": "normal",
        "severity_range": (0, 3),
        "intervention_needed": False,
        "intervention_type": None,
        "urgency_level": None
    },
    "nudge_criteria": None
}

# Easy Case 8: 게임 과다 (주간)
easy_case_8 = {
    "name": "E8_game_overuse",
    "input": {
        "user_id": 8,
        "behavior_log": {
            "app_name": "Mobile Game",
            "duration_seconds": 2100,  # 35분
            "usage_timestamp": "2025-01-03T16:00:00",
            "recent_app_switches": 0
        }
    },
    "expected": {
        "trigger_event": ["focus-break"],
        "pattern_type": ["concerning", "critical"],
        "severity_range": (6, 8),
        "intervention_needed": True,
        "intervention_type": ["focus-break"],
        "urgency_level": ["medium", "high"]
    },
    "nudge_criteria": {
        "must_mention_rest": True,
        "must_have_frame_complete": True
    }
}

# Easy Case 9: 소셜 미디어 야간 과다
easy_case_9 = {
    "name": "E9_social_night_overuse",
    "input": {
        "user_id": 9,
        "behavior_log": {
            "app_name": "Facebook",
            "duration_seconds": 1800,  # 30분
            "usage_timestamp": "2025-01-03T22:30:00",
            "recent_app_switches": 5
        }
    },
    "expected": {
        "trigger_event": ["bedtime-usage"],
        "pattern_type": "critical",
        "severity_range": (7, 9),
        "intervention_needed": True,
        "intervention_type": ["bedtime-usage"],
        "urgency_level": "high"
    },
    "nudge_criteria": {
        "must_mention_sleep": True,
        "must_have_frame_complete": True
    }
}

# Easy Case 10: 정상 사용 (유틸리티)
easy_case_10 = {
    "name": "E10_normal_utility",
    "input": {
        "user_id": 10,
        "behavior_log": {
            "app_name": "Weather",
            "duration_seconds": 60,  # 1분
            "usage_timestamp": "2025-01-03T07:00:00",
            "recent_app_switches": 0
        }
    },
    "expected": {
        "trigger_event": ["none"],
        "pattern_type": "normal",
        "severity_range": (0, 1),
        "intervention_needed": False,
        "intervention_type": None,
        "urgency_level": None
    },
    "nudge_criteria": None
}

# Easy Case 11: 숏폼 임계값 정확히 (20분)
easy_case_11 = {
    "name": "E11_shortform_threshold",
    "input": {
        "user_id": 11,
        "behavior_log": {
            "app_name": "YouTube Shorts",
            "duration_seconds": 1200,  # 정확히 20분
            "usage_timestamp": "2025-01-03T14:00:00",
            "recent_app_switches": 0
        }
    },
    "expected": {
        "trigger_event": ["short-form-overuse"],
        "pattern_type": ["concerning", "critical"],
        "severity_range": (5, 8),
        "intervention_needed": True,  # 임계값에서는 개입
        "intervention_type": ["short-form-overuse"],
        "urgency_level": ["medium", "high"]
    },
    "nudge_criteria": {
        "must_mention_usage_time": True,
        "must_have_frame_complete": True
    }
}

# Easy Case 12: 앱 전환 임계값 (10회)
easy_case_12 = {
    "name": "E12_switching_threshold",
    "input": {
        "user_id": 12,
        "behavior_log": {
            "app_name": "Twitter",
            "duration_seconds": 400,  # 6.7분
            "usage_timestamp": "2025-01-03T13:00:00",
            "recent_app_switches": 10
        }
    },
    "expected": {
        "trigger_event": ["app-switching"],
        "pattern_type": ["normal", "concerning"],
        "severity_range": (5, 7),
        "intervention_needed": True,
        "intervention_type": ["app-switching"],
        "urgency_level": ["low", "medium"]
    },
    "nudge_criteria": {
        "must_mention_focus": True
    }
}

# Easy Case 13: 새벽 시간 사용
easy_case_13 = {
    "name": "E13_early_morning",
    "input": {
        "user_id": 13,
        "behavior_log": {
            "app_name": "Instagram Reels",
            "duration_seconds": 1200,  # 20분
            "usage_timestamp": "2025-01-04T02:00:00",
            "recent_app_switches": 1
        }
    },
    "expected": {
        "trigger_event": ["bedtime-usage", "short-form-overuse"],
        "pattern_type": "critical",
        "severity_range": (8, 10),
        "intervention_needed": True,
        "intervention_type": ["bedtime-usage", "short-form-overuse"],
        "urgency_level": "high"
    },
    "nudge_criteria": {
        "must_mention_sleep": True,
        "must_have_frame_complete": True
    }
}

# Easy Case 14: 집중 작업 매우 장시간
easy_case_14 = {
    "name": "E14_long_focus",
    "input": {
        "user_id": 14,
        "behavior_log": {
            "app_name": "Notion",
            "duration_seconds": 3600,  # 60분
            "usage_timestamp": "2025-01-03T14:00:00",
            "recent_app_switches": 0
        }
    },
    "expected": {
        "trigger_event": ["focus-break"],
        "pattern_type": ["concerning"],
        "severity_range": (5, 7),
        "intervention_needed": True,
        "intervention_type": ["focus-break"],
        "urgency_level": ["low", "medium"]
    },
    "nudge_criteria": {
        "must_mention_rest": True,
        "tone_positive": True
    }
}

# Easy Case 15: 정상 사용 (소셜 미디어 짧게)
easy_case_15 = {
    "name": "E15_normal_social_short",
    "input": {
        "user_id": 15,
        "behavior_log": {
            "app_name": "Instagram",
            "duration_seconds": 300,  # 5분
            "usage_timestamp": "2025-01-03T12:00:00",
            "recent_app_switches": 2
        }
    },
    "expected": {
        "trigger_event": ["none"],
        "pattern_type": "normal",
        "severity_range": (0, 3),
        "intervention_needed": False,
        "intervention_type": None,
        "urgency_level": None
    },
    "nudge_criteria": None
}

# Easy Case 16: 숏폼 약간 미만 (19분)
easy_case_16 = {
    "name": "E16_shortform_just_under",
    "input": {
        "user_id": 16,
        "behavior_log": {
            "app_name": "TikTok",
            "duration_seconds": 1140,  # 19분
            "usage_timestamp": "2025-01-03T15:00:00",
            "recent_app_switches": 0
        }
    },
    "expected": {
        "trigger_event": ["none"],  # 임계값 미만
        "pattern_type": ["normal", "concerning"],
        "severity_range": (3, 6),
        "intervention_needed": False,  # 아직 개입 불필요
        "intervention_type": None,
        "urgency_level": None
    },
    "nudge_criteria": None
}

# Easy Case 17: 취침 직전 (21:50)
easy_case_17 = {
    "name": "E17_near_bedtime",
    "input": {
        "user_id": 17,
        "behavior_log": {
            "app_name": "YouTube",
            "duration_seconds": 900,  # 15분
            "usage_timestamp": "2025-01-03T21:50:00",
            "recent_app_switches": 1
        }
    },
    "expected": {
        "trigger_event": ["none"],  # 아직 22시 전
        "pattern_type": ["normal", "concerning"],
        "severity_range": (3, 6),
        "intervention_needed": False,
        "intervention_type": None,
        "urgency_level": None
    },
    "nudge_criteria": None
}

# Easy Case 18: 오전 정상 사용 (생산성)
easy_case_18 = {
    "name": "E18_morning_productive",
    "input": {
        "user_id": 18,
        "behavior_log": {
            "app_name": "Calendar",
            "duration_seconds": 480,  # 8분
            "usage_timestamp": "2025-01-03T08:00:00",
            "recent_app_switches": 3
        }
    },
    "expected": {
        "trigger_event": ["none"],
        "pattern_type": "normal",
        "severity_range": (0, 2),
        "intervention_needed": False,
        "intervention_type": None,
        "urgency_level": None
    },
    "nudge_criteria": None
}

# Easy Case 19: 앱 전환 매우 많음
easy_case_19 = {
    "name": "E19_excessive_switching",
    "input": {
        "user_id": 19,
        "behavior_log": {
            "app_name": "Various Apps",
            "duration_seconds": 600,  # 10분
            "usage_timestamp": "2025-01-03T16:00:00",
            "recent_app_switches": 20
        }
    },
    "expected": {
        "trigger_event": ["app-switching"],
        "pattern_type": "critical",
        "severity_range": (7, 9),
        "intervention_needed": True,
        "intervention_type": ["app-switching"],
        "urgency_level": "high"
    },
    "nudge_criteria": {
        "must_mention_focus": True,
        "must_have_frame_complete": True
    }
}

# Easy Case 20: 정상 사용 (뉴스)
easy_case_20 = {
    "name": "E20_normal_news",
    "input": {
        "user_id": 20,
        "behavior_log": {
            "app_name": "News App",
            "duration_seconds": 720,  # 12분
            "usage_timestamp": "2025-01-03T07:30:00",
            "recent_app_switches": 4
        }
    },
    "expected": {
        "trigger_event": ["none"],
        "pattern_type": "normal",
        "severity_range": (0, 3),
        "intervention_needed": False,
        "intervention_type": None,
        "urgency_level": None
    },
    "nudge_criteria": None
}

# =============================================================================
# Medium Cases (경계값, 애매한 상황, 복합 조건)
# =============================================================================

# Medium Case 1: 숏폼 임계값 정확히 20분 (경계값 - 야간)
medium_case_1 = {
    "name": "M1_shortform_boundary_night",
    "input": {
        "user_id": 21,
        "behavior_log": {
            "app_name": "YouTube Shorts",
            "duration_seconds": 1200,  # 정확히 20분
            "usage_timestamp": "2025-01-03T22:00:00",  # 정확히 22시
            "recent_app_switches": 0
        }
    },
    "expected": {
        "trigger_event": ["short-form-overuse", "bedtime-usage"],  # 둘 다 가능
        "pattern_type": ["concerning", "critical"],
        "severity_range": (6, 8),
        "intervention_needed": True,  # 경계값에서는 개입
        "intervention_type": ["short-form-overuse", "bedtime-usage"],
        "urgency_level": ["medium", "high"]
    },
    "nudge_criteria": {
        "must_mention_time_or_sleep": True,
        "must_have_frame_complete": True
    }
}

# Medium Case 2: 집중 작업 정확히 30분 (경계값)
medium_case_2 = {
    "name": "M2_focus_boundary",
    "input": {
        "user_id": 22,
        "behavior_log": {
            "app_name": "Coding IDE",
            "duration_seconds": 1800,  # 정확히 30분
            "usage_timestamp": "2025-01-03T14:00:00",
            "recent_app_switches": 0
        }
    },
    "expected": {
        "trigger_event": ["focus-break"],
        "pattern_type": ["normal", "concerning"],
        "severity_range": (4, 6),
        "intervention_needed": True,  # 경계값에서는 개입
        "intervention_type": ["focus-break"],
        "urgency_level": ["low", "medium"]
    },
    "nudge_criteria": {
        "must_mention_rest_or_break": True,
        "tone_positive": True
    }
}

# Medium Case 3: 앱 전환 9회 (임계값 바로 아래)
medium_case_3 = {
    "name": "M3_switching_just_under",
    "input": {
        "user_id": 23,
        "behavior_log": {
            "app_name": "Instagram",
            "duration_seconds": 300,
            "usage_timestamp": "2025-01-03T15:00:00",
            "recent_app_switches": 9  # 임계값 10 바로 아래
        }
    },
    "expected": {
        "trigger_event": ["none"],  # 아직 임계값 미만
        "pattern_type": ["normal", "concerning"],
        "severity_range": (3, 5),
        "intervention_needed": False,
        "intervention_type": None,
        "urgency_level": None
    },
    "nudge_criteria": None
}

# Medium Case 4: 숏폼 + 앱 전환 복합
medium_case_4 = {
    "name": "M4_shortform_with_switching",
    "input": {
        "user_id": 24,
        "behavior_log": {
            "app_name": "TikTok",
            "duration_seconds": 1500,  # 25분
            "usage_timestamp": "2025-01-03T16:00:00",
            "recent_app_switches": 12  # 전환도 많음
        }
    },
    "expected": {
        "trigger_event": ["short-form-overuse", "app-switching"],  # 둘 다
        "pattern_type": "critical",
        "severity_range": (7, 9),
        "intervention_needed": True,
        "intervention_type": ["short-form-overuse", "app-switching"],
        "urgency_level": "high"
    },
    "nudge_criteria": {
        "must_mention_usage_time_or_focus": True,
        "must_have_frame_complete": True
    }
}

# Medium Case 5: 취침 시간 경계 (21:59)
medium_case_5 = {
    "name": "M5_bedtime_boundary",
    "input": {
        "user_id": 25,
        "behavior_log": {
            "app_name": "YouTube",
            "duration_seconds": 1200,
            "usage_timestamp": "2025-01-03T21:59:00",  # 22시 1분 전
            "recent_app_switches": 2
        }
    },
    "expected": {
        "trigger_event": ["none"],  # 아직 22시 전
        "pattern_type": ["normal", "concerning"],
        "severity_range": (4, 6),
        "intervention_needed": False,  # 아직 취침 시간 아님
        "intervention_type": None,
        "urgency_level": None
    },
    "nudge_criteria": None
}

# Medium Case 6: 생산성 앱인데 야간
medium_case_6 = {
    "name": "M6_productive_app_night",
    "input": {
        "user_id": 26,
        "behavior_log": {
            "app_name": "Microsoft Excel",
            "duration_seconds": 2400,  # 40분
            "usage_timestamp": "2025-01-03T23:30:00",  # 야간
            "recent_app_switches": 0
        }
    },
    "expected": {
        "trigger_event": ["bedtime-usage", "focus-break"],  # 복합
        "pattern_type": ["concerning", "critical"],
        "severity_range": (6, 8),
        "intervention_needed": True,
        "intervention_type": ["bedtime-usage", "focus-break"],
        "urgency_level": ["medium", "high"]
    },
    "nudge_criteria": {
        "must_mention_sleep_or_rest": True,
        "must_have_frame_complete": True
    }
}

# Medium Case 7: 숏폼 19분 + 야간
medium_case_7 = {
    "name": "M7_shortform_under_but_night",
    "input": {
        "user_id": 27,
        "behavior_log": {
            "app_name": "Instagram Reels",
            "duration_seconds": 1140,  # 19분 (임계값 미만)
            "usage_timestamp": "2025-01-03T23:00:00",  # 하지만 야간
            "recent_app_switches": 1
        }
    },
    "expected": {
        "trigger_event": ["bedtime-usage"],  # 야간이 우선
        "pattern_type": ["concerning", "critical"],
        "severity_range": (6, 8),
        "intervention_needed": True,
        "intervention_type": ["bedtime-usage"],
        "urgency_level": ["medium", "high"]
    },
    "nudge_criteria": {
        "must_mention_sleep": True,
        "must_have_frame_complete": True
    }
}

# Medium Case 8: 반복 실행 3회 경계값
medium_case_8 = {
    "name": "M8_repeated_launch_boundary",
    "input": {
        "user_id": 28,
        "behavior_log": {
            "app_name": "Instagram",
            "duration_seconds": 300,
            "usage_timestamp": "2025-01-03T14:00:00",
            "recent_app_switches": 6  # 5분간 3회 재실행 가정
        }
    },
    "expected": {
        "trigger_event": ["app-switching"],  # 전환으로 분류 가능
        "pattern_type": ["normal", "concerning"],
        "severity_range": (4, 6),
        "intervention_needed": True,
        "intervention_type": ["app-switching"],
        "urgency_level": ["low", "medium"]
    },
    "nudge_criteria": {
        "must_mention_focus": True
    }
}

# Medium Case 9: 장시간이지만 학습 앱
medium_case_9 = {
    "name": "M9_long_educational",
    "input": {
        "user_id": 29,
        "behavior_log": {
            "app_name": "Duolingo",
            "duration_seconds": 2700,  # 45분
            "usage_timestamp": "2025-01-03T15:00:00",
            "recent_app_switches": 0
        }
    },
    "expected": {
        "trigger_event": ["focus-break"],
        "pattern_type": ["normal", "concerning"],
        "severity_range": (4, 7),
        "intervention_needed": True,  # 긴정적이지만 휴식 권유
        "intervention_type": ["focus-break"],
        "urgency_level": ["low", "medium"]
    },
    "nudge_criteria": {
        "must_mention_rest": True,
        "tone_positive": True  # "잘하고 있어요!"
    }
}

# Medium Case 10: 새벽 2시 경계
medium_case_10 = {
    "name": "M10_early_morning_boundary",
    "input": {
        "user_id": 30,
        "behavior_log": {
            "app_name": "Twitter",
            "duration_seconds": 600,  # 10분
            "usage_timestamp": "2025-01-04T02:00:00",  # 새벽 2시 정각
            "recent_app_switches": 3
        }
    },
    "expected": {
        "trigger_event": ["bedtime-usage"],
        "pattern_type": "critical",
        "severity_range": (7, 9),
        "intervention_needed": True,
        "intervention_type": ["bedtime-usage"],
        "urgency_level": "high"
    },
    "nudge_criteria": {
        "must_mention_sleep": True,
        "must_have_frame_complete": True
    }
}

# =============================================================================
# Hard Cases (복합 트리거, 엣지 케이스, 판단이 어려운 상황)
# =============================================================================

# Hard Case 1: 모든 트리거 동시 발생
hard_case_1 = {
    "name": "H1_all_triggers",
    "input": {
        "user_id": 31,
        "behavior_log": {
            "app_name": "YouTube Shorts",
            "duration_seconds": 2400,  # 40분
            "usage_timestamp": "2025-01-04T01:00:00",  # 새벽
            "recent_app_switches": 15  # 전환도 많음
        }
    },
    "expected": {
        "trigger_event": ["bedtime-usage", "short-form-overuse", "focus-break", "app-switching"],
        "pattern_type": "critical",
        "severity_range": (9, 10),
        "intervention_needed": True,
        "intervention_type": ["bedtime-usage", "short-form-overuse", "app-switching"],
        "urgency_level": "high"
    },
    "nudge_criteria": {
        "must_mention_multiple_concerns": True,
        "must_have_frame_complete": True,
        "tone_serious": True
    }
}

# Hard Case 2: 매우 짧은 시간에 극심한 전환
hard_case_2 = {
    "name": "H2_rapid_switching",
    "input": {
        "user_id": 32,
        "behavior_log": {
            "app_name": "Various Apps",
            "duration_seconds": 180,  # 3분
            "usage_timestamp": "2025-01-03T14:00:00",
            "recent_app_switches": 25  # 3분에 25회
        }
    },
    "expected": {
        "trigger_event": ["app-switching"],
        "pattern_type": "critical",
        "severity_range": (8, 10),
        "intervention_needed": True,
        "intervention_type": ["app-switching"],
        "urgency_level": "high"
    },
    "nudge_criteria": {
        "must_mention_focus": True,
        "must_have_frame_complete": True,
        "tone_concerned": True
    }
}

# Hard Case 3: 극도로 긴 집중 (2시간)
hard_case_3 = {
    "name": "H3_extreme_long_focus",
    "input": {
        "user_id": 33,
        "behavior_log": {
            "app_name": "Programming IDE",
            "duration_seconds": 7200,  # 2시간
            "usage_timestamp": "2025-01-03T14:00:00",
            "recent_app_switches": 0
        }
    },
    "expected": {
        "trigger_event": ["focus-break"],
        "pattern_type": "critical",
        "severity_range": (7, 9),
        "intervention_needed": True,
        "intervention_type": ["focus-break"],
        "urgency_level": "high"  # 긴정적이지만 강하게 권유
    },
    "nudge_criteria": {
        "must_mention_health_concern": True,
        "must_have_frame_complete": True,
        "tone_caring": True
    }
}

# Hard Case 4: 자정 직전 (23:59)
hard_case_4 = {
    "name": "H4_midnight_boundary",
    "input": {
        "user_id": 34,
        "behavior_log": {
            "app_name": "Netflix",
            "duration_seconds": 1800,  # 30분
            "usage_timestamp": "2025-01-03T23:59:00",  # 자정 1분 전
            "recent_app_switches": 1
        }
    },
    "expected": {
        "trigger_event": ["bedtime-usage"],
        "pattern_type": "critical",
        "severity_range": (8, 9),
        "intervention_needed": True,
        "intervention_type": ["bedtime-usage"],
        "urgency_level": "high"
    },
    "nudge_criteria": {
        "must_mention_sleep": True,
        "must_have_frame_complete": True
    }
}

# Hard Case 5: 생산성 앱이지만 극단적으로 긴 시간 + 야간
hard_case_5 = {
    "name": "H5_productive_extreme_night",
    "input": {
        "user_id": 35,
        "behavior_log": {
            "app_name": "Google Docs",
            "duration_seconds": 5400,  # 90분
            "usage_timestamp": "2025-01-03T22:30:00",  # 야간
            "recent_app_switches": 0
        }
    },
    "expected": {
        "trigger_event": ["bedtime-usage", "focus-break"],
        "pattern_type": "critical",
        "severity_range": (8, 9),
        "intervention_needed": True,
        "intervention_type": ["bedtime-usage", "focus-break"],
        "urgency_level": "high"
    },
    "nudge_criteria": {
        "must_mention_sleep_and_health": True,
        "must_have_frame_complete": True,
        "tone_caring": True
    }
}

# Hard Case 6: 여러 소셜미디어 짧게 반복
hard_case_6 = {
    "name": "H6_social_hopping",
    "input": {
        "user_id": 36,
        "behavior_log": {
            "app_name": "Instagram",
            "duration_seconds": 900,  # 15분
            "usage_timestamp": "2025-01-03T16:00:00",
            "recent_app_switches": 18  # 소셜미디어 간 전환
        }
    },
    "expected": {
        "trigger_event": ["app-switching"],
        "pattern_type": "critical",
        "severity_range": (7, 9),
        "intervention_needed": True,
        "intervention_type": ["app-switching"],
        "urgency_level": "high"
    },
    "nudge_criteria": {
        "must_mention_focus_and_mindfulness": True,
        "must_have_frame_complete": True
    }
}

# Hard Case 7: 게임 + 야간 + 장시간
hard_case_7 = {
    "name": "H7_game_night_long",
    "input": {
        "user_id": 37,
        "behavior_log": {
            "app_name": "Mobile Game",
            "duration_seconds": 3600,  # 60분
            "usage_timestamp": "2025-01-03T23:00:00",
            "recent_app_switches": 2
        }
    },
    "expected": {
        "trigger_event": ["bedtime-usage", "focus-break"],
        "pattern_type": "critical",
        "severity_range": (8, 10),
        "intervention_needed": True,
        "intervention_type": ["bedtime-usage", "focus-break"],
        "urgency_level": "high"
    },
    "nudge_criteria": {
        "must_mention_sleep_and_rest": True,
        "must_have_frame_complete": True
    }
}

# Hard Case 8: 0시 넘어가는 시점 (00:30)
hard_case_8 = {
    "name": "H8_past_midnight",
    "input": {
        "user_id": 38,
        "behavior_log": {
            "app_name": "YouTube",
            "duration_seconds": 1800,  # 30분
            "usage_timestamp": "2025-01-04T00:30:00",  # 자정 넘어감
            "recent_app_switches": 3
        }
    },
    "expected": {
        "trigger_event": ["bedtime-usage"],
        "pattern_type": "critical",
        "severity_range": (8, 10),
        "intervention_needed": True,
        "intervention_type": ["bedtime-usage"],
        "urgency_level": "high"
    },
    "nudge_criteria": {
        "must_mention_late_hour": True,
        "must_have_frame_complete": True,
        "tone_urgent": True
    }
}

# Hard Case 9: 매우 미묘한 경계값 (앱 전환 10회, 숏폼 20분, 22:00)
hard_case_9 = {
    "name": "H9_triple_boundary",
    "input": {
        "user_id": 39,
        "behavior_log": {
            "app_name": "TikTok",
            "duration_seconds": 1200,  # 정확히 20분
            "usage_timestamp": "2025-01-03T22:00:00",  # 정확히 22시
            "recent_app_switches": 10  # 정확히 10회
        }
    },
    "expected": {
        "trigger_event": ["short-form-overuse", "bedtime-usage", "app-switching"],
        "pattern_type": "critical",
        "severity_range": (7, 9),
        "intervention_needed": True,
        "intervention_type": ["short-form-overuse", "bedtime-usage", "app-switching"],
        "urgency_level": "high"
    },
    "nudge_criteria": {
        "must_mention_multiple_concerns": True,
        "must_have_frame_complete": True
    }
}

# Hard Case 10: 정상으로 보이지만 컨텍스트상 문제 (새벽 5시 기상 직후?)
hard_case_10 = {
    "name": "H10_context_dependent",
    "input": {
        "user_id": 40,
        "behavior_log": {
            "app_name": "Instagram",
            "duration_seconds": 600,  # 10분
            "usage_timestamp": "2025-01-04T05:00:00",  # 새벽 5시
            "recent_app_switches": 2
        }
    },
    "expected": {
        "trigger_event": ["bedtime-usage", "none"],  # 애매함
        "pattern_type": ["normal", "concerning"],
        "severity_range": (4, 7),
        "intervention_needed": True,  # 새벽 소셜미디어는 개입
        "intervention_type": ["bedtime-usage"],
        "urgency_level": ["medium", "high"]
    },
    "nudge_criteria": {
        "must_mention_early_hour_or_sleep": True
    }
}

# =============================================================================
# Dataset Organization
# =============================================================================

# Easy cases list
easy_cases = [
    easy_case_1, easy_case_2, easy_case_3, easy_case_4, easy_case_5,
    easy_case_6, easy_case_7, easy_case_8, easy_case_9, easy_case_10,
    easy_case_11, easy_case_12, easy_case_13, easy_case_14, easy_case_15,
    easy_case_16, easy_case_17, easy_case_18, easy_case_19, easy_case_20
]

# Medium cases list
medium_cases = [
    medium_case_1, medium_case_2, medium_case_3, medium_case_4, medium_case_5,
    medium_case_6, medium_case_7, medium_case_8, medium_case_9, medium_case_10
]

# Hard cases list
hard_cases = [
    hard_case_1, hard_case_2, hard_case_3, hard_case_4, hard_case_5,
    hard_case_6, hard_case_7, hard_case_8, hard_case_9, hard_case_10
]

# All cases
all_cases = easy_cases + medium_cases + hard_cases

# LangSmith format conversion
def convert_to_langsmith_format(cases):
    """Convert dataset to LangSmith format"""
    examples = []
    for case in cases:
        examples.append({
            "inputs": case["input"],
            "outputs": case["expected"],
            "metadata": {
                "name": case["name"],
                "nudge_criteria": case.get("nudge_criteria")
            }
        })
    return examples

langsmith_examples = convert_to_langsmith_format(all_cases)

# Export for pytest
test_inputs = [case["input"] for case in all_cases]
test_names = [case["name"] for case in all_cases]
test_expected = [case["expected"] for case in all_cases]
test_criteria = [case.get("nudge_criteria") for case in all_cases]

print(f"Loaded {len(all_cases)} test cases ({len(easy_cases)} Easy, {len(medium_cases)} Medium, {len(hard_cases)} Hard)")
