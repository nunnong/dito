package com.ssafy.Dito.domain.log.mediaSessionEvent.entity;

import com.ssafy.Dito.domain._common.IdentifiableEntity;
import com.ssafy.Dito.domain.log.mediaSessionEvent.dto.request.MediaSessionEventReq;
import com.ssafy.Dito.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.sql.Timestamp;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MediaSessionEvent extends IdentifiableEntity {

    @Column(name = "event_id", length = 50, nullable = false, unique = true)
    @Comment("이벤트 식별자")
    private String eventId;

    @Column(name = "event_type", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    @Comment("이벤트 타입")
    private EventType eventType;

    @Column(name = "package_name", length = 100, nullable = false)
    @Comment("패키지 이름")
    private String packageName;

    @Column(name = "app_name", length = 100, nullable = true)
    @Comment("앱 이름")
    private String appName;

    @Column(name = "title", length = 255, nullable = true)
    @Comment("미디어 제목")
    private String title;

    @Column(name = "chanel", length = 200, nullable = true)
    @Comment("채널 이름")
    private String chanel;

    @Column(name = "event_timestamp", nullable = false)
    @Comment("이벤트 발생 시간")
    private Long eventTimestamp;

    @Column(name = "video_duration", nullable = true)
    @Comment("비디오 재생 시간")
    private Long videoDuration;

    @Column(name = "watch_time", nullable = true)
    @Comment("시청 시간")
    private Long watchTime;

    @Column(name = "pause_time", nullable = true)
    @Comment("일시정지 시간")
    private Long pauseTime;

    @Column(name = "event_date", nullable = true)
    @Comment("이벤트 발생 날짜")
    private LocalDate eventDate;

    @Column(name = "created_at", nullable = false)
    @CreatedDate
    @Comment("수집 시각")
    private Timestamp createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Comment("사용자 식별자")
    private User user;

    private MediaSessionEvent(String eventId, EventType eventType, String packageName,
                              String appName, String title, String chanel,
                              Long eventTimestamp, Long videoDuration,
                              Long watchTime, Long pauseTime,
                              LocalDate eventDate, User user) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.packageName = packageName;
        this.appName = appName;
        this.title = title;
        this.chanel = chanel;
        this.eventTimestamp = eventTimestamp;
        this.videoDuration = videoDuration;
        this.watchTime = watchTime;
        this.pauseTime = pauseTime;
        this.eventDate = eventDate;
        this.user = user;
    }

    public static MediaSessionEvent of(MediaSessionEventReq req, User user) {
        return new MediaSessionEvent(
            req.eventId(),
            req.eventType(),
            req.packageName(),
            req.appName(),
            req.title(),
            req.channel(),
            req.eventTimestamp(),
            req.videoDuration(),
            req.watchTime(),
            req.PauseTime(),
            req.eventDate(),
            user
        );
    }
}