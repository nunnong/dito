package com.ssafy.Dito.domain.report.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ssafy.Dito.domain.report.dto.InsightDto;
import com.ssafy.Dito.domain.report.dto.StrategyChangeDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

/**
 * Request DTO for updating existing report (PATCH)
 * All fields are optional for partial updates
 * Used by AI server to update report after async processing
 */
public record ReportUpdateReq(
    @JsonProperty("report_overview")
    @Schema(description = "리포트 요약 (선택)", example = "오늘은 총 3시간 30분 동안 앱을 사용했습니다...")
    String reportOverview,

    @Valid
    @Schema(description = "인사이트 목록 (선택)")
    List<InsightDto> insights,

    @Valid
    @Schema(description = "전략 변경 이력 목록 (선택)")
    List<StrategyChangeDto> strategy,

    @Schema(description = "AI 조언 (선택)", example = "화면 사용 시간을 줄이기 위해...")
    String advice,

    @JsonProperty("mission_success_rate")
    @Min(value = 0, message = "Success rate must be >= 0")
    @Max(value = 100, message = "Success rate must be <= 100")
    @Schema(description = "미션 성공률 (0-100, 선택)", example = "85")
    Integer missionSuccessRate,

    @Schema(description = "리포트 상태 (선택)", example = "COMPLETED", allowableValues = {"IN_PROGRESS", "COMPLETED", "FAILED"})
    String status
) {
}
