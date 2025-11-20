package com.ssafy.Dito.domain.mission.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MissionReq(
        @JsonProperty("user_id")
        long userId,

        @JsonProperty("mission_type")
        String missionType,

        @JsonProperty("mission_text")
        String missionText,

        @JsonProperty("coin_reward")
        int coinReward,

        @JsonProperty("duration_seconds")
        int durationSeconds,

        @JsonProperty("target_app")
        String targetApp,

        @JsonProperty("stat_change_self_care")
        int statChangeSelfCare,

        @JsonProperty("stat_change_focus")
        int statChangeFocus,

        @JsonProperty("stat_change_sleep")
        int statChangeSleep,

        String prompt
) {

}
