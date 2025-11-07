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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * MongoDB document for daily screen time summary
 * 그룹 챌린지 랭킹 조회 최적화를 위한 일별 스크린타임 집계 데이터
 *
 * 특징:
 * - 유저별, 날짜별로 하나의 Document 유지
 * - 앱에서 5분마다 upsert로 갱신
 * - 빠른 랭킹 조회 (Summary 데이터 직접 사용)
 */
@Document(collection = "screen_time_daily_summary")
@CompoundIndexes({
    @CompoundIndex(name = "group_user_date_unique_idx",
                   def = "{'group_id': 1, 'user_id': 1, 'date': 1}",
                   unique = true),
    @CompoundIndex(name = "group_date_idx",
                   def = "{'group_id': 1, 'date': -1}")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScreenTimeDailySummary extends MongoBaseDocument {

    @Field("group_id")
    @Indexed
    private Long groupId;

    @Field("user_id")
    @Indexed
    private Long userId;

    @Field("date")
    @Indexed
    private LocalDate date;

    @Field("total_minutes")
    private Integer totalMinutes;

    @Field("last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @Builder
    private ScreenTimeDailySummary(Long groupId, Long userId, LocalDate date,
                                   Integer totalMinutes, LocalDateTime lastUpdatedAt) {
        this.groupId = groupId;
        this.userId = userId;
        this.date = date;
        this.totalMinutes = totalMinutes;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    /**
     * 새로운 일별 Summary 생성
     */
    public static ScreenTimeDailySummary create(Long groupId, Long userId,
                                                LocalDate date, Integer totalMinutes) {
        return ScreenTimeDailySummary.builder()
            .groupId(groupId)
            .userId(userId)
            .date(date)
            .totalMinutes(totalMinutes)
            .lastUpdatedAt(LocalDateTime.now())
            .build();
    }

    /**
     * 스크린타임 갱신
     */
    public void updateScreenTime(Integer newTotalMinutes) {
        this.totalMinutes = newTotalMinutes;
        this.lastUpdatedAt = LocalDateTime.now();
    }
}
