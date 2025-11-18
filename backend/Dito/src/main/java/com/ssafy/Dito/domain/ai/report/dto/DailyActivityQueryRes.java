package com.ssafy.Dito.domain.ai.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ssafy.Dito.domain.ai.report.document.DailyUserActivityDocument;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response DTO for querying daily user activity
 * Returns complete activity data including summary, app usage, and media sessions
 */
public record DailyActivityQueryRes(
    @JsonProperty("date")
    LocalDate date,

    @JsonProperty("user_id")
    Long userId,

    @JsonProperty("summary")
    SummaryRes summary,

    @JsonProperty("app_usage_stats")
    List<AppUsageStatRes> appUsageStats,

    @JsonProperty("media_sessions")
    List<MediaSessionRes> mediaSessions
) {

    /**
     * Converts DailyUserActivityDocument to DailyActivityQueryRes
     */
    public static DailyActivityQueryRes from(DailyUserActivityDocument document) {
        return new DailyActivityQueryRes(
            document.getDate(),
            document.getUserId(),
            document.getSummary() != null ? SummaryRes.from(document.getSummary()) : null,
            document.getAppUsageStats() != null
                ? document.getAppUsageStats().stream()
                    .map(AppUsageStatRes::from)
                    .collect(Collectors.toList())
                : Collections.emptyList(),
            document.getMediaSessions() != null
                ? document.getMediaSessions().stream()
                    .map(MediaSessionRes::from)
                    .collect(Collectors.toList())
                : Collections.emptyList()
        );
    }

    /**
     * Summary statistics for the day
     */
    public record SummaryRes(
        @JsonProperty("total_app_usage_time")
        Integer totalAppUsageTime,

        @JsonProperty("total_media_watch_time")
        Double totalMediaWatchTime,

        @JsonProperty("most_used_app")
        String mostUsedApp
    ) {
        public static SummaryRes from(DailyUserActivityDocument.Summary summary) {
            return new SummaryRes(
                summary.getTotalAppUsageTime(),
                summary.getTotalMediaWatchTime(),
                summary.getMostUsedApp()
            );
        }
    }

    /**
     * App usage statistics per application
     */
    public record AppUsageStatRes(
        @JsonProperty("app_name")
        String appName,

        @JsonProperty("package_name")
        String packageName,

        @JsonProperty("total_duration")
        Long totalDuration,

        @JsonProperty("session_count")
        Integer sessionCount
    ) {
        public static AppUsageStatRes from(DailyUserActivityDocument.AppUsageStat stat) {
            return new AppUsageStatRes(
                stat.getAppName(),
                stat.getPackageName(),
                stat.getTotalDuration(),
                stat.getSessionCount()
            );
        }
    }

    /**
     * Media session information
     */
    public record MediaSessionRes(
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
    ) {
        public static MediaSessionRes from(DailyUserActivityDocument.MediaSession session) {
            return new MediaSessionRes(
                session.getPlatform(),
                session.getTitle(),
                session.getChannel(),
                session.getTimestamp(),
                session.getWatchTime(),
                session.getVideoType(),
                session.getKeywords()
            );
        }
    }
}
