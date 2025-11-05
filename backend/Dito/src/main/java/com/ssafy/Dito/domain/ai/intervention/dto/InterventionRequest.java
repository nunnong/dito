package com.ssafy.Dito.domain.ai.intervention.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * AI Intervention 요청 DTO
 * Phase 0: Echo Server 테스트용
 *
 * 요청 형식:
 * {
 *   "assistant_id": "intervention",
 *   "input": {
 *     "user_id": 1,
 *     "behavior_log": {
 *       "app_name": "YouTube Shorts",
 *       "duration_seconds": 1200,
 *       "usage_timestamp": "2025-01-03T23:45:00"
 *     }
 *   }
 * }
 */
public record InterventionRequest(
        @NotBlank(message = "assistant_id는 필수입니다")
        @JsonProperty("assistant_id")
        String assistantId,

        @NotNull(message = "input은 필수입니다")
        @Valid
        @JsonProperty("input")
        InterventionInput input
) {
}