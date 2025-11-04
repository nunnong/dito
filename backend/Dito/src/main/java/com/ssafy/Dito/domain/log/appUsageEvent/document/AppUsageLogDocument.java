package com.ssafy.Dito.domain.log.appUsageEvent.document;

import com.ssafy.Dito.domain.log.appUsageEvent.dto.request.AppUsageEventReq;
import com.ssafy.Dito.domain.log.appUsageEvent.entity.EventType;
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

/**
 * MongoDB document for app usage event logs
 * Replaces AppUsageEvent JPA entity - stores data in MongoDB instead of PostgreSQL
 * Optimized for time-series analytics and AI agent queries
 */
@Document(collection = "app_usage_logs")
@CompoundIndexes({
    @CompoundIndex(name = "user_date_idx", def = "{'user_id': 1, 'event_date': -1}"),
    @CompoundIndex(name = "user_timestamp_idx", def = "{'user_id': 1, 'event_timestamp': -1}")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppUsageLogDocument extends MongoBaseDocument {

    @Field("event_id")
    @Indexed(unique = true)
    private String eventId;

    @Field("event_type")
    private EventType eventType;

    @Field("package_name")
    private String packageName;

    @Field("app_name")
    private String appName;

    @Field("event_timestamp")
    @Indexed
    private Long eventTimestamp;

    @Field("duration")
    private Long duration;

    @Field("event_date")
    @Indexed
    private LocalDate eventDate;

    // Denormalized user reference - stores user ID directly instead of FK
    @Field("user_id")
    @Indexed
    private Long userId;

    @Builder
    private AppUsageLogDocument(String eventId, EventType eventType, String packageName,
                                String appName, Long eventTimestamp, Long duration,
                                LocalDate eventDate, Long userId) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.packageName = packageName;
        this.appName = appName;
        this.eventTimestamp = eventTimestamp;
        this.duration = duration;
        this.eventDate = eventDate;
        this.userId = userId;
    }

    /**
     * Factory method to create MongoDB document from request DTO
     * @param req AppUsageEventReq from Android client
     * @param userId User ID (denormalized from User entity)
     * @return AppUsageLogDocument instance
     */
    public static AppUsageLogDocument of(AppUsageEventReq req, Long userId) {
        return AppUsageLogDocument.builder()
            .eventId(req.eventId())
            .eventType(req.eventType())
            .packageName(req.packageName())
            .appName(req.appName())
            .eventTimestamp(req.eventTimestamp())
            .duration(req.duration())
            .eventDate(req.eventDate())
            .userId(userId)
            .build();
    }
}