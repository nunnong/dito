package com.ssafy.Dito.domain.missionResult.dto.request;

import com.ssafy.Dito.domain.missionResult.entity.Result;

public record MissionResultReq(
        long missionId,
        Result result
) {

}
