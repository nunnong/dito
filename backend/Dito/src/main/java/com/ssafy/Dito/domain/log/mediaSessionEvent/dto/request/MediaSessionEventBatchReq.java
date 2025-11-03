package com.ssafy.Dito.domain.log.mediaSessionEvent.dto.request;

import java.util.List;

public record MediaSessionEventBatchReq(
    List<MediaSessionEventReq> mediaSessionEvent
) {

}
