package com.ssafy.Dito.domain.mission.dto.request;

public record MissionReq(
        String missionType,
        String missionText,
        int coinReward,
        int durationSeconds,
        String targetApp,
        int statChangeSelfCare,
        int statChangeFocus,
        int statChangeSleep,
        String prompt
) {

}
