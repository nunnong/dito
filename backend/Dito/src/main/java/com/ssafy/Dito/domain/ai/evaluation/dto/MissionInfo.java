package com.ssafy.Dito.domain.ai.evaluation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 미션 정보 DTO
 */
@Schema(description = "미션 정보")
public record MissionInfo(
        @Schema(description = "미션 유형 (REST, EXERCISE, MEDITATION, EDUCATIONAL)", example = "REST")
        @JsonProperty("type")
        @NotBlank(message = "미션 유형은 필수입니다")
        String type,

        @Schema(description = "미션 지시사항", example = "5분간 휴식하세요")
        @JsonProperty("instruction")
        @NotBlank(message = "미션 지시사항은 필수입니다")
        String instruction,

        @Schema(description = "미션 전체 수행 시간 (초)", example = "300")
        @JsonProperty("duration_seconds")
        @NotNull(message = "미션 수행 시간은 필수입니다")
        Integer durationSeconds,

        @Schema(description = "제한 대상 앱 패키지명 목록", example = "[\"com.google.android.youtube\", \"com.instagram.android\"]")
        @JsonProperty("target_apps")
        List<String> targetApps,

        @Schema(description = "미션 시작 시각 (ISO 8601)", example = "2025-11-05T14:30:00+09:00")
        @JsonProperty("start_time")
        @NotBlank(message = "미션 시작 시각은 필수입니다")
        String startTime,

        @Schema(description = "미션 종료 시각 (ISO 8601)", example = "2025-11-05T14:35:00+09:00")
        @JsonProperty("end_time")
        @NotBlank(message = "미션 종료 시각은 필수입니다")
        String endTime
) {
}
