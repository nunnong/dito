package com.ssafy.Dito.domain.ai.intervention.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 행동 로그 정보
 * Phase 1: AI Integration
 */
@Schema(description = "앱 사용 행동 로그", example = """
    {
      "app_name": "YouTube",
      "duration_seconds": 1200,
      "usage_timestamp": "2025-01-10T15:30:00",
      "recent_app_switches": 7,
      "app_metadata": {
        "title": "디토는 무엇일까요?",
        "channel": "dito"
      }
    }
    """)
public record BehaviorLog(
        @NotBlank(message = "앱 이름은 필수입니다")
        @JsonProperty("app_name")
        @Schema(description = "앱 이름", example = "YouTube")
        String appName,

        @NotNull(message = "사용 시간은 필수입니다")
        @Positive(message = "사용 시간은 양수여야 합니다")
        @JsonProperty("duration_seconds")
        @Schema(description = "사용 시간 (초)", example = "1200")
        Integer durationSeconds,

        @NotBlank(message = "사용 시점은 필수입니다")
        @JsonProperty("usage_timestamp")
        @Schema(description = "사용 시점 (ISO 8601)", example = "2025-01-10T15:30:00")
        String usageTimestamp,

        @JsonProperty("recent_app_switches")
        @Schema(description = "최근 앱 전환 횟수 (옵션)", example = "7")
        Integer recentAppSwitches,

        @JsonProperty("app_metadata")
        @Schema(description = "앱 메타데이터 (옵션, 유튜브용)")
        AppMetadata appMetadata
) {
}
