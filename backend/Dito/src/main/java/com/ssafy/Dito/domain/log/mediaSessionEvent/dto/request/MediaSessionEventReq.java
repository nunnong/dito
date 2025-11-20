package com.ssafy.Dito.domain.log.mediaSessionEvent.dto.request;

import com.ssafy.Dito.domain.log.mediaSessionEvent.entity.EventType;
import java.time.LocalDate;

public record MediaSessionEventReq(
    String eventId,
    EventType eventType,
    String packageName,
    String appName,
    String title,
    String channel,
    Long eventTimestamp,
    Long videoDuration,
    Long watchTime,
    Long PauseTime,
    LocalDate eventDate
) {

}
