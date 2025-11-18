package com.ssafy.Dito.domain.ai.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for triggering AI report generation
 * Sent from mobile app to request report analysis
 */
public record ReportRequestReq(
    @JsonProperty("user_id")
    @NotNull(message = "유저 ID는 필수입니다")
    Long userId,

    @JsonProperty("date")
    @NotBlank(message = "날짜는 필수입니다")
    String date  // Format: "2025-11-17"
) {
}
