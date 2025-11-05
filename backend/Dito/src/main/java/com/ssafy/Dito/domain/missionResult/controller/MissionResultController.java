package com.ssafy.Dito.domain.missionResult.controller;

import com.ssafy.Dito.domain.missionResult.dto.request.MissionResultReq;
import com.ssafy.Dito.domain.missionResult.service.MissionResultService;
import com.ssafy.Dito.global.dto.ApiResponse;
import com.ssafy.Dito.global.dto.CommonResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mission-result")
public class MissionResultController {
    private final MissionResultService missionResultService;

    // <AI> 미션 결과 반영
    @PostMapping
    public ResponseEntity<CommonResult> createMissionResult(
        @Valid @RequestBody MissionResultReq req) {
        missionResultService.createMissionResult(req);
        return ApiResponse.ok();
    }
}
