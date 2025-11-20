package com.ssafy.Dito.domain.ai.report.controller;

import com.ssafy.Dito.domain.ai.report.dto.ReportRequestReq;
import com.ssafy.Dito.domain.ai.report.dto.ReportRequestRes;
import com.ssafy.Dito.domain.ai.report.service.DailyUserActivityService;
import com.ssafy.Dito.global.dto.ApiResponse;
import com.ssafy.Dito.global.dto.SingleResult;
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
 * AI Report Controller
 * Handles report generation requests from mobile app
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "에이전트서버로 보냄", description = "AI 에이전트 서버 통신 API")
public class AIReportController {

    private final DailyUserActivityService dailyUserActivityService;

    /**
     * POST /ai/report
     * Request AI to generate report based on daily data
     *
     * @param req Report request (personalId, date)
     * @return AI response (run_id, thread_id, status)
     */
    @PostMapping("/report")
    @Operation(
        summary = "AI 리포트 생성 요청",
        description = """
            MongoDB에서 일일 데이터를 조회하여 AI 서버에 리포트 생성을 요청합니다.

            user_id는 사용자 로그인 ID(personalId)를 사용합니다.

            요청 Body 예시:
            {
              "user_id": "catch",
              "date": "2025-11-17"
            }

            응답 예시:
            {
              "run_id": "550e8400-e29b-41d4-a716-446655440001",
              "thread_id": "660e8400-e29b-41d4-a716-446655440000",
              "status": "pending"
            }

            AI 서버가 비동기로 리포트를 생성하고 완료 시 PostgreSQL에 저장합니다.
            """
    )
    public ResponseEntity<SingleResult<ReportRequestRes>> requestReport(
        @Valid @RequestBody ReportRequestReq req
    ) {
        log.info("Received report request - personalId: {}, date: {}", req.userId(), req.date());

        ReportRequestRes res = dailyUserActivityService.requestAiReport(req);

        return ApiResponse.ok(res);
    }
}
