package com.ssafy.Dito.domain.report.controller;

import com.ssafy.Dito.domain.report.dto.response.ReportRes;
import com.ssafy.Dito.domain.report.dto.response.VideoFeedbackItem;
import com.ssafy.Dito.domain.report.service.ReportService;
import com.ssafy.Dito.global.dto.ApiResponse;
import com.ssafy.Dito.global.dto.ListResult;
import com.ssafy.Dito.global.dto.SingleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @Operation(summary = "피드백 영상 조회", description = "사용자의 피드백이 필요한 영상 목록을 반환합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "피드백 영상 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰이 유효하지 않습니다.")
    })
    @GetMapping("/feedback-videos")
    public ResponseEntity<ListResult<VideoFeedbackItem>> getVideosForFeedback() {
        List<VideoFeedbackItem> videos = reportService.getVideosForFeedback();
        return ApiResponse.of(org.springframework.http.HttpStatus.OK, "피드백 영상 조회 성공", videos);
    }
}
