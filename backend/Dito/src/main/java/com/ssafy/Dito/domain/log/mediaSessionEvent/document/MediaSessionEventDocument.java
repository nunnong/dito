package com.ssafy.Dito.domain.log.mediaSessionEvent.document;

import com.ssafy.Dito.domain.log.common.MongoBaseDocument;
import com.ssafy.Dito.domain.log.mediaSessionEvent.dto.request.MediaSessionEventReq;
import com.ssafy.Dito.domain.log.mediaSessionEvent.entity.EventType;
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
 * MongoDB document for media session event logs
 * Replaces MediaSessionEvent JPA entity - stores data in MongoDB instead of PostgreSQL
 * Tracks YouTube, music, and other media playback events for behavioral analysis
 */
@Document(collection = "media_session_events")
@CompoundIndexes({
    @CompoundIndex(name = "user_date_idx", def = "{'user_id': 1, 'event_date': -1}"),
    @CompoundIndex(name = "user_timestamp_idx", def = "{'user_id': 1, 'event_timestamp': -1}")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MediaSessionEventDocument extends MongoBaseDocument {

    @Field("event_id")
    @Indexed(unique = true)
    private String eventId;

    @Field("event_type")
    private EventType eventType;

    @Field("package_name")
    private String packageName;

    @Field("app_name")
    private String appName;

    @Field("title")
    private String title;

    @Field("channel")
    private String channel;

    @Field("event_timestamp")
    @Indexed
    private Long eventTimestamp;

    @Field("video_duration")
    private Long videoDuration;

    @Field("watch_time")
    private Long watchTime;

    @Field("pause_time")
    private Long pauseTime;

    @Field("event_date")
    @Indexed
    private LocalDate eventDate;

    // Denormalized user reference - stores user ID directly instead of FK
    @Field("user_id")
    @Indexed
    private Long userId;

    @Field("is_educational")
    private Boolean isEducational;

    @Builder
    private MediaSessionEventDocument(String eventId, EventType eventType, String packageName,
                                      String appName, String title, String channel,
                                      Long eventTimestamp, Long videoDuration,
                                      Long watchTime, Long pauseTime,
                                      LocalDate eventDate, Long userId, Boolean isEducational) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.packageName = packageName;
        this.appName = appName;
        this.title = title;
        this.channel = channel;
        this.eventTimestamp = eventTimestamp;
        this.videoDuration = videoDuration;
        this.watchTime = watchTime;
        this.pauseTime = pauseTime;
        this.eventDate = eventDate;
        this.userId = userId;
        this.isEducational = isEducational;
    }

    /**
     * Factory method to create MongoDB document from request DTO
     * @param req MediaSessionEventReq from Android client
     * @param userId User ID (denormalized from User entity)
     * @return MediaSessionEventDocument instance
     */
    public static MediaSessionEventDocument of(MediaSessionEventReq req, Long userId) {
        return MediaSessionEventDocument.builder()
            .eventId(req.eventId())
            .eventType(req.eventType())
            .packageName(req.packageName())
            .appName(req.appName())
            .title(req.title())
            .channel(req.channel())
            .eventTimestamp(req.eventTimestamp())
            .videoDuration(req.videoDuration())
            .watchTime(req.watchTime())
            .pauseTime(req.PauseTime())
            .eventDate(req.eventDate())
            .userId(userId)
            .isEducational(req.isEducational())
            .build();
    }

    public void updateWatchTime(Long watchTime, Long eventTimestamp) {
        this.watchTime = watchTime;
        this.eventTimestamp = eventTimestamp;
    }
}
