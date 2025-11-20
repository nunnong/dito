package com.ssafy.Dito.domain.log.appUsageEvent.controller;

import com.ssafy.Dito.domain.log.appUsageEvent.dto.request.AppUsageEventBatchReq;
import com.ssafy.Dito.domain.log.appUsageEvent.dto.response.AppUsageEventRes;
import com.ssafy.Dito.domain.log.appUsageEvent.service.AppUsageEventService;
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
public class AppUsageEventController {

    private final AppUsageEventService appUsageEventService;

    @PostMapping("/app-usage")
    public ResponseEntity<SingleResult<AppUsageEventRes>> saveAppUsageEvent(
        @Valid @RequestBody AppUsageEventBatchReq req
    ) {
        AppUsageEventRes res = appUsageEventService.saveAppUsageEvent(req);
        return ApiResponse.ok(res);
    }
}
