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
# Dataset Organization
# =============================================================================

# Easy cases list
easy_cases = [
    easy_case_1, easy_case_2, easy_case_3, easy_case_4, easy_case_5,
    easy_case_6, easy_case_7, easy_case_8, easy_case_9, easy_case_10,
    easy_case_11, easy_case_12, easy_case_13, easy_case_14, easy_case_15,
    easy_case_16, easy_case_17, easy_case_18, easy_case_19, easy_case_20
]

# All cases (will expand with Medium and Hard)
all_cases = easy_cases

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

print(f"Loaded {len(all_cases)} test cases ({len(easy_cases)} Easy)")
