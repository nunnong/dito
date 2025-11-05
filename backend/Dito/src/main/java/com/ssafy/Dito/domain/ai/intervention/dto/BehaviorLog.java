package com.ssafy.Dito.domain.ai.intervention.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
        String appName,

        @NotNull(message = "사용 시간은 필수입니다")
        @Positive(message = "사용 시간은 양수여야 합니다")
        @JsonProperty("duration_seconds")
        Integer durationSeconds,

        @NotBlank(message = "사용 시점은 필수입니다")
        @JsonProperty("usage_timestamp")
        String usageTimestamp  // ISO 8601 형식
) {
    /**
     * 사용 시간을 분 단위로 반환
     */
    public int getDurationMinutes() {
        return durationSeconds / 60;
    }
}