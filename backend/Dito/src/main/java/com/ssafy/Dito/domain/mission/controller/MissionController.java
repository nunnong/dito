package com.ssafy.Dito.domain.mission.controller;

import com.ssafy.Dito.domain.mission.dto.request.AiMissionReq;
import com.ssafy.Dito.domain.mission.dto.request.MissionReq;
import com.ssafy.Dito.domain.mission.dto.response.AiMissionRes;
import com.ssafy.Dito.domain.mission.dto.response.MissionRes;
import com.ssafy.Dito.domain.mission.service.MissionService;
import com.ssafy.Dito.global.dto.ApiResponse;
import com.ssafy.Dito.global.dto.CommonResult;
import com.ssafy.Dito.global.dto.ListResult;
import com.ssafy.Dito.global.dto.PageResult;
import com.ssafy.Dito.global.dto.SingleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
@Tag(name = "mission", description = "미션 관련 API")
@RequestMapping("/mission")
public class MissionController {

    private final MissionService missionService;

    @Operation(summary = "미션 조회")
    @GetMapping("/{page_number}")
    public ResponseEntity<PageResult<MissionRes>> getMissions(
            @PathVariable long page_number
    ) {
        Page<MissionRes> res = missionService.getMissions(page_number);
        return ApiResponse.ok(res);
    }
}
