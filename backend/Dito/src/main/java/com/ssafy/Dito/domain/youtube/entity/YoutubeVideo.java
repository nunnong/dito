package com.ssafy.Dito.domain.youtube.entity;

import com.ssafy.Dito.domain._common.IdentifiableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "youtube_video",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_youtube_video_channel_title", columnNames = {"channel", "title"})
    }
)
@Comment("YouTube 영상 캐싱 테이블")
public class YoutubeVideo extends IdentifiableEntity {

    @Column(name = "title", columnDefinition = "TEXT", nullable = false)
    @Comment("영상 제목")
    private String title;

    @Column(name = "channel", columnDefinition = "TEXT", nullable = false)
    @Comment("채널명")
    private String channel;

    @Column(name = "thumbnail_base64", columnDefinition = "TEXT")
    @Comment("썸네일 이미지 (Base64 인코딩)")
    private String thumbnailBase64;

    @Column(name = "app_package", length = 255)
    @Comment("앱 패키지명 (예: com.google.android.youtube)")
    private String appPackage;

    @Column(name = "platform", length = 50)
    @Comment("플랫폼명 (예: YouTube, Netflix)")
    private String platform;

    @Column(name = "video_type", length = 100)
    @Comment("영상 타입 (예: educational, entertainment)")
    private String videoType;

    @Column(name = "keywords", columnDefinition = "TEXT")
    @Comment("키워드 목록 (향후 분류용)")
    private String keywords;

    @Column(name = "created_at", nullable = false)
    @Comment("최초 저장 시각")
    private Instant createdAt;

    @Column(name = "updated_at")
    @Comment("마지막 업데이트 시각")
    private Instant updatedAt;

    private YoutubeVideo(String title, String channel, String thumbnailBase64,
                        String appPackage, String platform, String videoType, String keywords) {
        this.title = title;
        this.channel = channel;
        this.thumbnailBase64 = thumbnailBase64;
        this.appPackage = appPackage;
        this.platform = platform;
        this.videoType = videoType;
        this.keywords = keywords;
        this.createdAt = Instant.now();
        this.updatedAt = null;
    }

    public static YoutubeVideo of(String title, String channel, String thumbnailBase64,
                                 String appPackage, String platform, String videoType, String keywords) {
        return new YoutubeVideo(title, channel, thumbnailBase64, appPackage, platform, videoType, keywords);
    }

    public void update(String thumbnailBase64, String videoType, String keywords) {
        if (thumbnailBase64 != null) {
            this.thumbnailBase64 = thumbnailBase64;
        }
        if (videoType != null) {
            this.videoType = videoType;
        }
        if (keywords != null) {
            this.keywords = keywords;
        }
        this.updatedAt = Instant.now();
    }
}
