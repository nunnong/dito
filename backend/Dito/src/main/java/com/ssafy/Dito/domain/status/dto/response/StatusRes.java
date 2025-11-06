package com.ssafy.Dito.domain.status.dto.response;

import com.querydsl.core.annotations.QueryProjection;

public record StatusRes(
    int selfCareStatus,
    int focusStatus,
    int sleepStatus,
    int totalStatus
) {
    @QueryProjection
    public StatusRes {
    }
}
