package com.ssafy.Dito.domain.log.appUsageEvent.dto.request;

import com.ssafy.Dito.domain.log.appUsageEvent.entity.EventType;
import java.time.LocalDate;

public record AppUsageEventReq(
    String eventId,
    EventType eventType,
    String packageName,
    String appName,
    Long eventTimestamp,
    Long duration,
    LocalDate eventDate
) {

}
