package com.ssafy.Dito.domain.ai.evaluation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 미션 수행 중 위반 항목 DTO
 *
 * @param appName 위반 앱 이름
 * @param durationSeconds 위반 지속 시간 (초)
 * @param timestamp 위반 발생 시각 (ISO 8601 형식)
 */
@Schema(description = "미션 위반 항목")
public record Violation(
        @Schema(description = "위반 앱 이름", example = "Instagram")
        @JsonProperty("app_name")
        @NotBlank(message = "앱 이름은 필수입니다")
        String appName,

        @Schema(description = "위반 지속 시간 (초)", example = "125")
        @JsonProperty("duration_seconds")
        @NotNull(message = "위반 지속 시간은 필수입니다")
        Integer durationSeconds,

        @Schema(description = "위반 발생 시각 (ISO 8601)", example = "2025-11-05T14:30:15+09:00")
        @JsonProperty("timestamp")
        @NotBlank(message = "위반 발생 시각은 필수입니다")
        String timestamp
) {
}
