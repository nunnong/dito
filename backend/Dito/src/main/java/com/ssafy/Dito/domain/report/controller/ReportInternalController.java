package com.ssafy.Dito.domain.report.controller;

import com.ssafy.Dito.domain.ai.report.dto.DailyActivityQueryRes;
import com.ssafy.Dito.domain.ai.report.service.DailyUserActivityService;
import com.ssafy.Dito.domain.report.dto.request.ReportReq;
import com.ssafy.Dito.domain.report.dto.request.ReportUpdateReq;
import com.ssafy.Dito.domain.report.dto.response.ReportRes;
import com.ssafy.Dito.domain.report.service.ReportService;
import com.ssafy.Dito.global.dto.ApiResponse;
import com.ssafy.Dito.global.dto.SingleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Report Internal API", description = "AI 서버 전용 리포트 API (X-API-Key 인증 필요)")
@SecurityRequirement(name = "X-API-Key")
public class ReportInternalController {

    private final ReportService reportService;
    private final DailyUserActivityService dailyUserActivityService;

    @PostMapping("/report")
    @Operation(
        summary = "사용자 리포트 생성",
        description = """
            AI 서버에서 생성한 사용자 리포트를 저장합니다.
            - X-API-Key 헤더 인증이 필요합니다.
            - user_id를 통해 사용자를 지정합니다.
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "리포트 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "X-API-Key 인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다")
    })
    public ResponseEntity<SingleResult<ReportRes>> createReport(
            @Parameter(description = "API Key (X-API-Key 헤더)", required = true)
            @RequestHeader("X-API-Key") String apiKey,
            @Valid @RequestBody ReportReq request
    ) {
        ReportRes response = reportService.createReportForAi(request);
        return ApiResponse.create(response);
    }

    @PatchMapping("/report/{reportId}")
    @Operation(
        summary = "사용자 리포트 업데이트",
        description = """
            AI 서버에서 비동기 처리 완료 후 리포트를 업데이트합니다.
            - X-API-Key 헤더 인증이 필요합니다.
            - 모든 필드는 선택사항이며, 제공된 필드만 업데이트됩니다.
            - 주로 status를 "IN_PROGRESS"에서 "COMPLETED"로 변경할 때 사용됩니다.

            요청 Body 예시:
            {
              "report_overview": "업데이트된 리포트 요약...",
              "insights": [
                {"type": "POSITIVE", "description": "긍정적 인사이트..."},
                {"type": "NEGATIVE", "description": "개선이 필요한 부분..."}
              ],
              "advice": "AI 조언...",
              "mission_success_rate": 85,
              "status": "COMPLETED"
            }
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리포트 업데이트 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "X-API-Key 인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리포트를 찾을 수 없습니다")
    })
    public ResponseEntity<SingleResult<ReportRes>> updateReport(
            @Parameter(description = "API Key (X-API-Key 헤더)", required = true)
            @RequestHeader("X-API-Key") String apiKey,
            @Parameter(description = "리포트 ID", required = true)
            @PathVariable Long reportId,
            @Valid @RequestBody ReportUpdateReq request
    ) {
        ReportRes response = reportService.updateReportForAi(reportId, request);
        return ApiResponse.ok(response);
    }

    @GetMapping("/activity/{userId}")
    @Operation(
        summary = "일일 사용자 활동 조회 (MongoDB)",
        description = """
            특정 날짜의 사용자 활동 데이터를 MongoDB에서 조회합니다.
            - X-API-Key 헤더 인증이 필요합니다.
            - 앱 사용 로그와 미디어 세션 이벤트를 포함합니다.
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "활동 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "X-API-Key 인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 활동을 찾을 수 없습니다")
    })
    public ResponseEntity<SingleResult<DailyActivityQueryRes>> getDailyActivity(
            @Parameter(description = "API Key (X-API-Key 헤더)", required = true)
            @RequestHeader("X-API-Key") String apiKey,
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "조회할 날짜 (ISO 8601 형식: YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        DailyActivityQueryRes res = dailyUserActivityService.getActivity(userId, date);
        return ApiResponse.ok(res);
    }
}
