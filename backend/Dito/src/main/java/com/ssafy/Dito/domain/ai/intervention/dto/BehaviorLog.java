package com.ssafy.Dito.domain.ai.intervention.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 행동 로그 정보
 * Phase 0: Echo Server 테스트용
 */
public record BehaviorLog(
        @NotBlank(message = "앱 이름은 필수입니다")
        @JsonProperty("app_name")
        @Schema(description = "앱 이름", example = "YouTube Shorts")
        String appName,

        @NotNull(message = "사용 시간은 필수입니다")
        @Positive(message = "사용 시간은 양수여야 합니다")
        @JsonProperty("duration_seconds")
        @Schema(description = "사용 시간 (초)", example = "1200")
        Integer durationSeconds,

        @NotBlank(message = "사용 시점은 필수입니다")
        @JsonProperty("usage_timestamp")
        @Schema(description = "사용 시점 (ISO 8601)", example = "2025-01-03T23:45:00")
        String usageTimestamp
) {
}
