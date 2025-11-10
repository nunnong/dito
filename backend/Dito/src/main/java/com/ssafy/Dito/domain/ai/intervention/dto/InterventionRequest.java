package com.ssafy.Dito.domain.ai.intervention.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * AI Intervention 요청 DTO
 * Phase 1: AI Integration
 *
 * 요청 형식:
 * {
 *   "user_id": "catch",
 *   "behavior_log": {
 *     "app_name": "YouTube",
 *     "duration_seconds": 1200,
 *     "usage_timestamp": "2025-01-10T15:30:00"
 *   }
 * }
 */
@Schema(description = "AI 개입 요청 DTO", example = """
    {
      "user_id": "catch",
      "behavior_log": {
        "app_name": "YouTube",
        "duration_seconds": 1200,
        "usage_timestamp": "2025-01-10T15:30:00"
      }
    }
    """)
public record InterventionRequest(
        @NotBlank(message = "사용자 ID는 필수입니다")
        @JsonProperty("user_id")
        @Schema(description = "사용자 로그인 ID (personalId)", example = "catch")
        String userId,

        @NotNull(message = "행동 로그는 필수입니다")
        @Valid
        @JsonProperty("behavior_log")
        @Schema(description = "앱 사용 행동 로그")
        BehaviorLog behaviorLog
) {
}
