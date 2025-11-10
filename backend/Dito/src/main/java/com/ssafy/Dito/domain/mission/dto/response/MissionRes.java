package com.ssafy.Dito.domain.mission.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import com.ssafy.Dito.domain.mission.entity.Status;
import com.ssafy.Dito.domain.missionResult.entity.Result;

public record MissionRes (
        long id,
        String missionType,
        String missionText,
        int coinReward,
        Status status,
        Result result
) {
    @QueryProjection
    public MissionRes {
    }

}
