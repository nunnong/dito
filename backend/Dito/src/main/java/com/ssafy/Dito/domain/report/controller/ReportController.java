package com.ssafy.Dito.domain.report.controller;

import com.ssafy.Dito.domain.report.dto.response.ReportRes;
import com.ssafy.Dito.domain.report.service.ReportService;
import com.ssafy.Dito.global.dto.ApiResponse;
import com.ssafy.Dito.global.dto.SingleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "report", description = "리포트 관련 API")
@RequestMapping("/user/report")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "최신 리포트 조회", description = "사용자의 가장 최근 AI 리포트를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리포트가 존재하지 않습니다.")
    })
    @GetMapping
    public ResponseEntity<SingleResult<ReportRes>> getLatestReport() {
        ReportRes res = reportService.getLatestReport();
        return ApiResponse.ok(res);
    }
}
