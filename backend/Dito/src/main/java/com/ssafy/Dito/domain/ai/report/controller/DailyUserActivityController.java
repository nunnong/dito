package com.ssafy.Dito.domain.ai.report.controller;

import com.ssafy.Dito.domain.ai.report.dto.ClientDailyActivityReq;
import com.ssafy.Dito.domain.ai.report.dto.DailyActivityRes;
import com.ssafy.Dito.domain.ai.report.service.DailyUserActivityService;
import com.ssafy.Dito.global.dto.ApiResponse;
import com.ssafy.Dito.global.dto.SingleResult;
import com.ssafy.Dito.global.jwt.util.JwtAuthentication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Daily User Activity Controller
 * Handles uploading of daily activity logs (app usage, media sessions) from mobile app
 */
@Slf4j
@RestController
@RequestMapping("/ai/activity")
@RequiredArgsConstructor
@Tag(name = "Daily Activity", description = "일일 활동 로그 관리 API")
public class DailyUserActivityController {

    private final DailyUserActivityService dailyUserActivityService;

    @PostMapping
    @Operation(
        summary = "일일 활동 로그 업로드",
        description = """
            앱에서 수집한 일일 활동 로그(앱 사용 시간, 미디어 세션 등)를 서버에 업로드합니다.
            MongoDB에 저장되며, 추후 AI 리포트 생성 및 그룹 랭킹 분석에 활용됩니다.
            
            기존 데이터가 있으면 덮어씁니다 (Upsert).
            """
    )
    public ResponseEntity<SingleResult<DailyActivityRes>> uploadDailyActivity(
        @Valid @RequestBody ClientDailyActivityReq req
    ) {
        Long userId = JwtAuthentication.getUserId();
        log.info("Received daily activity upload - userId: {}, date: {}", userId, req.date());

        DailyActivityRes res = dailyUserActivityService.saveActivity(req.toInternalReq(userId));

        return ApiResponse.ok(res);
    }
}
