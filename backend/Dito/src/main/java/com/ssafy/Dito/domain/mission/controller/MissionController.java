package com.ssafy.Dito.domain.mission.controller;

import com.ssafy.Dito.domain.mission.dto.response.MissionRes;
import com.ssafy.Dito.domain.mission.service.MissionService;
import com.ssafy.Dito.global.dto.ApiResponse;
import com.ssafy.Dito.global.dto.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "mission", description = "미션 관련 API")
@RequestMapping("/mission")
public class MissionController {

    private final MissionService missionService;

    @Operation(summary = "미션 조회")
    @GetMapping
    public ResponseEntity<PageResult<MissionRes>> getMissions(
            @RequestParam long page_number
    ) {
        Page<MissionRes> res = missionService.getMissions(page_number);
        return ApiResponse.ok(res);
    }
}
