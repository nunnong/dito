package com.ssafy.Dito.domain.mission.controller;

import com.ssafy.Dito.domain.mission.dto.request.MissionReq;
import com.ssafy.Dito.domain.mission.dto.response.MissionRes;
import com.ssafy.Dito.domain.mission.service.MissionService;
import com.ssafy.Dito.global.dto.ApiResponse;
import com.ssafy.Dito.global.dto.CommonResult;
import com.ssafy.Dito.global.dto.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mission")
public class MissionController {

    private final MissionService missionService;

    // 알람 조회(미션 목록)
    @GetMapping("/{page_number}")
    public ResponseEntity<PageResult<MissionRes>> getMissions(
            @PathVariable long page_number
    ) {
        Page<MissionRes> res = missionService.getMissions(page_number);
        return ApiResponse.ok(res);
    }

    // 미션 등록
    @PostMapping
    public ResponseEntity<CommonResult> createMission(
            @Valid @RequestBody MissionReq req
    ) {
        missionService.createMission(req);
        return ApiResponse.ok();
    }
}
