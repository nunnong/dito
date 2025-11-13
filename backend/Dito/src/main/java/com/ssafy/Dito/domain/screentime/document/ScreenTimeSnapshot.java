package com.ssafy.Dito.domain.screentime.document;

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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * MongoDB document for screen time snapshots
 * 스크린타임 이력 저장용 (검증 및 분석)
 *
 * 특징:
 * - 앱에서 5분마다 새로운 Snapshot INSERT
 * - 시간대별 스크린타임 패턴 분석 가능
 * - 비정상 데이터 감지 및 검증
 * - 30일 후 자동 삭제 (TTL 인덱스)
 */
@Document(collection = "screen_time_snapshots")
@CompoundIndexes({
    @CompoundIndex(name = "user_date_idx",
                   def = "{'user_id': 1, 'date': -1}"),
    @CompoundIndex(name = "group_user_recorded_idx",
                   def = "{'group_id': 1, 'user_id': 1, 'recorded_at': -1}")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScreenTimeSnapshot extends MongoBaseDocument {

    @Field("group_id")
    @Indexed
    private Long groupId;

    @Field("user_id")
    @Indexed
    private Long userId;

    @Field("date")
    @Indexed
    private LocalDate date;

    @Field("recorded_at")
    @Indexed
    private LocalDateTime recordedAt;

    @Field("screen_time_minutes")
    private Integer screenTimeMinutes;

    @Field("youtube_minutes")
    private Integer youtubeMinutes;

    /**
     * TTL 인덱스 - 30일 후 자동 삭제
     * MongoDB에서 자동으로 문서 삭제 처리
     */
    @Field("expire_at")
    @Indexed(expireAfter = "0s")
    private Instant expireAt;

    @Builder
    private ScreenTimeSnapshot(Long groupId, Long userId, LocalDate date,
                               LocalDateTime recordedAt, Integer screenTimeMinutes,Integer youtubeMinutes,
                               Instant expireAt) {
        this.groupId = groupId;
        this.userId = userId;
        this.date = date;
        this.recordedAt = recordedAt;
        this.screenTimeMinutes = screenTimeMinutes;
        this.youtubeMinutes = youtubeMinutes;
        this.expireAt = expireAt;
    }

    /**
     * 새로운 Snapshot 생성
     * TTL은 30일 후로 자동 설정
     */
    public static ScreenTimeSnapshot create(Long groupId, Long userId,
                                            LocalDate date, Integer screenTimeMinutes, Integer youtubeMinutes) {
        LocalDateTime now = LocalDateTime.now();
        // 30일 후 자동 삭제 설정
        Instant expireAt = now.plusDays(30).atZone(java.time.ZoneId.systemDefault()).toInstant();

        return ScreenTimeSnapshot.builder()
            .groupId(groupId)
            .userId(userId)
            .date(date)
            .recordedAt(now)
            .screenTimeMinutes(screenTimeMinutes)
            .youtubeMinutes(youtubeMinutes)
            .expireAt(expireAt)
            .build();
    }
}
