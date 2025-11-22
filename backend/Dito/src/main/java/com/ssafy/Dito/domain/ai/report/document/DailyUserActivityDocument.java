package com.ssafy.Dito.domain.ai.report.document;

import com.ssafy.Dito.domain.log.common.MongoBaseDocument;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.List;

/**
 * MongoDB document for daily user activities
 * Stores aggregated daily analytics including app usage and media consumption
 * Custom _id format: "YYYYMMDD_userId" (e.g., "20251117_23")
 */
@Document(collection = "daily_user_activities")
@CompoundIndexes({
    @CompoundIndex(name = "user_date_idx", def = "{'user_id': 1, 'date': -1}")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyUserActivityDocument extends MongoBaseDocument {

    @Field("date")
    @Indexed
    private String date;

    @Field("user_id")
    @Indexed
    private Long userId;

    @Field("summary")
    private Summary summary;

    @Field("app_usage_stats")
    private List<AppUsageStat> appUsageStats;

    @Field("media_sessions")
    private List<MediaSession> mediaSessions;

    @Builder
    private DailyUserActivityDocument(String id, String date, Long userId, Summary summary,
                                   List<AppUsageStat> appUsageStats, List<MediaSession> mediaSessions) {
        this.setId(id);  // Custom ID format: "YYYYMMDD_userId"
        this.date = date;  // Format: "yyyy-MM-dd" (e.g., "2025-11-17")
        this.userId = userId;
        this.summary = summary;
        this.appUsageStats = appUsageStats;
        this.mediaSessions = mediaSessions;
    }

    /**
     * Summary statistics for the day
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Summary {
        @Field("total_app_usage_time")
        private Integer totalAppUsageTime;  // in minutes

        @Field("total_media_watch_time")
        private Double totalMediaWatchTime;  // in minutes

        @Field("most_used_app")
        private String mostUsedApp;  // package name

        @Builder(toBuilder = true)
        public Summary(Integer totalAppUsageTime, Double totalMediaWatchTime, String mostUsedApp) {
            this.totalAppUsageTime = totalAppUsageTime;
            this.totalMediaWatchTime = totalMediaWatchTime;
            this.mostUsedApp = mostUsedApp;
        }
    }

    /**
     * App usage statistics per application
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class AppUsageStat {
        @Field("app_name")
        private String appName;

        @Field("package_name")
        private String packageName;

        @Field("total_duration")
        private Long totalDuration;  // in seconds

        @Field("session_count")
        private Integer sessionCount;

        @Builder(toBuilder = true)
        public AppUsageStat(String appName, String packageName, Long totalDuration, Integer sessionCount) {
            this.appName = appName;
            this.packageName = packageName;
            this.totalDuration = totalDuration;
            this.sessionCount = sessionCount;
        }
    }

    /**
     * Media session information (YouTube, Netflix, etc.)
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class MediaSession {
        @Field("platform")
        private String platform;

        @Field("title")
        private String title;

        @Field("channel")
        private String channel;

        @Field("timestamp")
        private Long timestamp;  // Unix timestamp in milliseconds

        @Field("watch_time")
        private Long watchTime;  // in seconds

        @Field("video_type")
        private String videoType;

        @Field("keywords")
        private List<String> keywords;

        @Builder(toBuilder = true)
        public MediaSession(String platform, String title, String channel, Long timestamp,
                          Long watchTime, String videoType, List<String> keywords) {
            this.platform = platform;
            this.title = title;
            this.channel = channel;
            this.timestamp = timestamp;
            this.watchTime = watchTime;
            this.videoType = videoType;
            this.keywords = keywords;
        }
    }
}
