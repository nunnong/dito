package com.ssafy.Dito.domain.mission.dto.response;

import com.querydsl.core.annotations.QueryProjection;

public record MissionRes (
        long id,
        String missionType,
        String missionText,
        int coinReward,
        String status) {
    @QueryProjection
    public MissionRes {
    }

}
