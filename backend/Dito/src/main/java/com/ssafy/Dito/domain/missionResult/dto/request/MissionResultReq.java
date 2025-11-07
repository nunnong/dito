package com.ssafy.Dito.domain.missionResult.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ssafy.Dito.domain.missionResult.entity.Result;

public record MissionResultReq(
        @JsonProperty("mission_id")
        long missionId,

        Result result
) {

}
