package com.ssafy.Dito.domain.weaklyGoal.controller;

import com.ssafy.Dito.domain.weaklyGoal.dto.request.WeeklyGoalReq;
import com.ssafy.Dito.domain.weaklyGoal.service.WeeklyGoalService;
import com.ssafy.Dito.global.dto.ApiResponse;
import com.ssafy.Dito.global.dto.CommonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WeeklyController {

    private final WeeklyGoalService weeklyGoalService;

    // 주간 목표 생성
    @PostMapping("/weekly-goal")
    public ResponseEntity<CommonResult> createWeeklyGoal(
            @RequestBody WeeklyGoalReq req
    ){
        weeklyGoalService.createWeeklyGoal(req);
        return ApiResponse.ok();
    }

}
