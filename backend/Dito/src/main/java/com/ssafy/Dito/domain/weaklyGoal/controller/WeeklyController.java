package com.ssafy.Dito.domain.weaklyGoal.controller;

import com.ssafy.Dito.domain.weaklyGoal.dto.request.UserWeeklyGoalReq;
import com.ssafy.Dito.domain.weaklyGoal.dto.request.WeeklyGoalReq;
import com.ssafy.Dito.domain.weaklyGoal.dto.response.UserWeeklyGoalRes;
import com.ssafy.Dito.domain.weaklyGoal.service.WeeklyGoalService;
import com.ssafy.Dito.global.dto.ApiResponse;
import com.ssafy.Dito.global.dto.CommonResult;
import com.ssafy.Dito.global.dto.SingleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "weekly-goal", description = "주간 목표 관련 API")
@RequestMapping("/weekly-goal")
public class WeeklyController {

    private final WeeklyGoalService weeklyGoalService;

    @Operation(summary = "주간 목표 조회")
    @PostMapping
    public ResponseEntity<CommonResult> createWeeklyGoal(
            @RequestBody WeeklyGoalReq req
    ){
        weeklyGoalService.createWeeklyGoal(req);
        return ApiResponse.ok();
    }
}
