package com.ssafy.Dito.domain.mission.dto.request;

public record MissionTextUpdateReq(
    long missionId,
    String missionText
) {
}
