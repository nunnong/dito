package com.ssafy.Dito.domain.ai.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for saving daily user activity
 * Sent from AI server after daily analysis completion
 */
public record DailyActivityReq(
    @JsonProperty("date")
    @NotNull(message = "날짜는 필수입니다")
    LocalDate date,

    @JsonProperty("user_id")
    @NotNull(message = "유저 ID는 필수입니다")
    Long userId,

    @JsonProperty("summary")
    @NotNull(message = "요약 정보는 필수입니다")
    @Valid
    SummaryReq summary,

    @JsonProperty("app_usage_stats")
    List<AppUsageStatReq> appUsageStats,

    @JsonProperty("media_sessions")
    List<MediaSessionReq> mediaSessions
) {

    public record SummaryReq(
        @JsonProperty("total_app_usage_time")
        Integer totalAppUsageTime,

        @JsonProperty("total_media_watch_time")
        Double totalMediaWatchTime,

        @JsonProperty("most_used_app")
        String mostUsedApp
    ) {}

    public record AppUsageStatReq(
        @JsonProperty("app_name")
        @NotBlank(message = "앱 이름은 필수입니다")
        String appName,

        @JsonProperty("package_name")
        @NotBlank(message = "패키지 이름은 필수입니다")
        String packageName,

        @JsonProperty("total_duration")
        @NotNull(message = "총 사용 시간은 필수입니다")
        Long totalDuration,

        @JsonProperty("session_count")
        Integer sessionCount
    ) {}

    public record MediaSessionReq(
        @JsonProperty("platform")
        String platform,

        @JsonProperty("title")
        String title,

        @JsonProperty("channel")
        String channel,

        @JsonProperty("timestamp")
        Long timestamp,

        @JsonProperty("watch_time")
        Long watchTime,

        @JsonProperty("video_type")
        String videoType,

        @JsonProperty("keywords")
        List<String> keywords
    ) {}
}
