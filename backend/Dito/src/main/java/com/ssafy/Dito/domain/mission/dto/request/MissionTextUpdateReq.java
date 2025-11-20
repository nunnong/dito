package com.ssafy.Dito.domain.mission.dto.request;

public record MissionTextUpdateReq(
    long userId,
    long missionId,
    String missionText
) {
}
