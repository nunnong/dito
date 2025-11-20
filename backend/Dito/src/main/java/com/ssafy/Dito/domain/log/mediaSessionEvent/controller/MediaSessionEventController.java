package com.ssafy.Dito.domain.log.mediaSessionEvent.controller;

import com.ssafy.Dito.domain.log.mediaSessionEvent.dto.request.MediaSessionEventBatchReq;
import com.ssafy.Dito.domain.log.mediaSessionEvent.dto.response.MediaSessionEventRes;
import com.ssafy.Dito.domain.log.mediaSessionEvent.service.MediaSessionEventService;
import com.ssafy.Dito.global.dto.ApiResponse;
import com.ssafy.Dito.global.dto.SingleResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/event")
public class MediaSessionEventController {

    private final MediaSessionEventService mediaSessionEventService;

    @PostMapping("/media-session")
    public ResponseEntity<SingleResult<MediaSessionEventRes>> saveMediaSessionEvent(
        @Valid @RequestBody MediaSessionEventBatchReq req
    ) {
        MediaSessionEventRes res = mediaSessionEventService.saveMediaSessionEvent(req);
        return ApiResponse.ok(res);
    }
}
