package com.ssafy.Dito.domain.user.dto.response;

import com.querydsl.core.annotations.QueryProjection;

public record MainRes(
    String nickname,
    String costumeUrl,
    String backgroundUrl,
    int coinBalance,
    String weeklyGoal,
    int selfCareStatus,
    int focusStatus,
    int sleepStatus
) {
    @QueryProjection
    public MainRes {
    }
}
