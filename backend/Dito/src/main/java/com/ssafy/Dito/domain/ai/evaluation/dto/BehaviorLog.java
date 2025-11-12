package com.ssafy.Dito.domain.ai.evaluation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 행동 로그 DTO
 * 미션 수행 중 발생한 사용자 행동 이벤트
 */
@Schema(description = "행동 로그")
public record BehaviorLog(
        @Schema(description = "로그 유형 (APP_USAGE, MEDIA_SESSION, SCREEN_ON, SCREEN_OFF)", example = "APP_USAGE")
        @JsonProperty("log_type")
        @NotBlank(message = "로그 유형은 필수입니다")
        String logType,

        @Schema(description = "이벤트 발생 순서 (1부터 시작)", example = "1")
        @JsonProperty("sequence")
        @NotNull(message = "이벤트 순서는 필수입니다")
        Integer sequence,

        @Schema(description = "이벤트 발생 시각 (ISO 8601)", example = "2025-11-05T14:30:15+09:00")
        @JsonProperty("timestamp")
        @NotBlank(message = "이벤트 발생 시각은 필수입니다")
        String timestamp,

        // APP_USAGE, MEDIA_SESSION 관련 필드
        @Schema(description = "앱 패키지명", example = "com.instagram.android")
        @JsonProperty("package_name")
        String packageName,

        @Schema(description = "앱 이름", example = "Instagram")
        @JsonProperty("app_name")
        String appName,

        // APP_USAGE 전용 필드
        @Schema(description = "앱 사용 시간 (초)", example = "125")
        @JsonProperty("duration_seconds")
        Integer durationSeconds,

        // MEDIA_SESSION 전용 필드
        @Schema(description = "영상 제목", example = "쇼츠 모음")
        @JsonProperty("video_title")
        String videoTitle,

        @Schema(description = "채널명", example = "채널명")
        @JsonProperty("channel_name")
        String channelName,

        @Schema(description = "미디어 이벤트 (VIDEO_START, VIDEO_END, VIDEO_PAUSE)", example = "VIDEO_START")
        @JsonProperty("event_type")
        String eventType,

        @Schema(description = "실제 시청 시간 (초)", example = "60")
        @JsonProperty("watch_time_seconds")
        Integer watchTimeSeconds,

        @Schema(description = "콘텐츠 분류 (EDUCATIONAL, ENTERTAINMENT, UNKNOWN)", example = "ENTERTAINMENT")
        @JsonProperty("content_type")
        String contentType,

        // 공통 필드
        @Schema(description = "타겟 앱 여부", example = "true")
        @JsonProperty("is_target_app")
        Boolean isTargetApp
) {
}
