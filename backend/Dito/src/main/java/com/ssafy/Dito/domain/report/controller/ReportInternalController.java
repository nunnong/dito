package com.ssafy.Dito.domain.report.controller;

import com.ssafy.Dito.domain.report.dto.request.ReportReq;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Report Internal API", description = "AI 서버 전용 리포트 API (X-API-Key 인증 필요)")
@SecurityRequirement(name = "X-API-Key")
public class ReportInternalController {

    private final ReportService reportService;

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
}
