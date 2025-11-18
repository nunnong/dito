package com.ssafy.Dito.domain.ai.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for triggering AI report generation
 * Sent from mobile app to request report analysis
 */
public record ReportRequestReq(
    @JsonProperty("user_id")
    @NotBlank(message = "사용자 ID는 필수입니다")
    @Schema(description = "사용자 로그인 ID (personalId)", example = "catch")
    String userId,

    @JsonProperty("date")
    @NotBlank(message = "날짜는 필수입니다")
    @Schema(description = "리포트 생성 날짜", example = "2025-11-17")
    String date
) {
}
