package com.ssafy.Dito.domain.log.appUsageEvent.dto.request;

import java.util.List;

public record AppUsageEventBatchReq(
    List<AppUsageEventReq> appUsageEvents
) {

}
