package com.ssafy.Dito.domain.ai.api.controller;

import com.ssafy.Dito.domain.ai.api.dto.AiReq;
import com.ssafy.Dito.domain.mission.dto.request.AiMissionReq;
import com.ssafy.Dito.domain.mission.dto.request.MissionReq;
import com.ssafy.Dito.domain.mission.dto.response.AiMissionRes;
import com.ssafy.Dito.domain.mission.service.MissionService;
import com.ssafy.Dito.domain.missionResult.dto.request.MissionResultReq;
import com.ssafy.Dito.domain.missionResult.service.MissionResultService;
import com.ssafy.Dito.domain.user.dto.response.UserInfoRes;
import com.ssafy.Dito.domain.user.service.UserService;
import com.ssafy.Dito.domain.weaklyGoal.dto.request.UserWeeklyGoalReq;
import com.ssafy.Dito.domain.weaklyGoal.dto.response.UserWeeklyGoalRes;
import com.ssafy.Dito.domain.weaklyGoal.service.WeeklyGoalService;
import com.ssafy.Dito.global.dto.ApiResponse;
import com.ssafy.Dito.global.dto.CommonResult;
import com.ssafy.Dito.global.dto.ListResult;
import com.ssafy.Dito.global.dto.SingleResult;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiController {

    private final MissionService missionService;
    private final MissionResultService missionResultService;
    private final UserService userService;
    private final WeeklyGoalService weeklyGoalService;

    // <AI> 미션 등록
    @PostMapping("/mission")
    public ResponseEntity<CommonResult> createMission(
        @Valid @RequestBody MissionReq req
    ) {
        missionService.createMission(req);
        return ApiResponse.ok();
    }

    // <AI> 미션 조회
    @GetMapping("/mission")
    public ResponseEntity<ListResult<AiMissionRes>> getMissionForAi(
        @RequestBody AiReq req
    ) {
        List<AiMissionRes> res = missionService.getMissionForAi(req);
        return ApiResponse.ok(res);
    }

    // <AI> 미션 결과 반영
    @PostMapping("/mission/result")
    public ResponseEntity<CommonResult> createMissionResult(
        @Valid @RequestBody MissionResultReq req) {
        missionResultService.createMissionResult(req);
        return ApiResponse.ok();
    }

    // <AI> 해당 유저 주간 목표 조회
    @GetMapping("/weekly-goal")
    public ResponseEntity<SingleResult<UserWeeklyGoalRes>> getUserWeeklyGoal(
        @RequestBody AiReq req
    ){
        UserWeeklyGoalRes res = weeklyGoalService.getUserWeeklyGoal(req);
        return ApiResponse.ok(res);
    }

    // <AI> 유저 정보 조회
    @GetMapping("/user")
    public ResponseEntity<SingleResult<UserInfoRes>> getUserInfo(
        @RequestBody AiReq req
    ) {
        UserInfoRes res = userService.getUserInfoForAi(req);
        return ApiResponse.ok(res);
    }

}
