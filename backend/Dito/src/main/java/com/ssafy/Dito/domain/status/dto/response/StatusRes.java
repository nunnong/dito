package com.ssafy.Dito.domain.status.dto.response;

public record StatusRes(
    int selfCareStatus,
    int focusStatus,
    int sleepStatus,
    int totalStatus
) {

}
