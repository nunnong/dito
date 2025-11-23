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
    private String date;  // "yyyy-MM-dd" 형식의 String (timezone 문제 방지)

    @Field("total_minutes")
    private Integer totalMinutes;

    @Field("youtube_minutes")
    private Integer youtubeMinutes;

    @Field("initial_total_minutes")
    private Integer initialTotalMinutes;

    @Field("initial_youtube_minutes")
    private Integer initialYoutubeMinutes;

    @Field("last_reported_total_minutes")
    private Integer lastReportedTotalMinutes;

    @Field("last_reported_youtube_minutes")
    private Integer lastReportedYoutubeMinutes;

    @Field("last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @Builder
    private ScreenTimeDailySummary(Long groupId, Long userId, LocalDate date,
        Integer totalMinutes, Integer youtubeMinutes,
        Integer initialTotalMinutes, Integer initialYoutubeMinutes,
        Integer lastReportedTotalMinutes, Integer lastReportedYoutubeMinutes,
        LocalDateTime lastUpdatedAt) {
        this.groupId = groupId;
        this.userId = userId;
        this.date = date.toString();  // LocalDate를 "yyyy-MM-dd" String으로 변환
        this.totalMinutes = totalMinutes != null ? totalMinutes : 0;
        this.youtubeMinutes = youtubeMinutes != null ? youtubeMinutes : 0;
        this.initialTotalMinutes = initialTotalMinutes != null ? initialTotalMinutes : 0;
        this.initialYoutubeMinutes = initialYoutubeMinutes != null ? initialYoutubeMinutes : 0;
        this.lastReportedTotalMinutes = lastReportedTotalMinutes;
        this.lastReportedYoutubeMinutes = lastReportedYoutubeMinutes;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    /**
     * 새로운 일별 Summary 생성
     */
    public static ScreenTimeDailySummary create(Long groupId, Long userId,
        LocalDate date,Integer reportedTotalMinutes, Integer reportedYoutubeMinutes) {
        int safeTotal = reportedTotalMinutes != null ? reportedTotalMinutes : 0;
        int safeYoutube = reportedYoutubeMinutes != null ? reportedYoutubeMinutes : 0;
        return ScreenTimeDailySummary.builder()
            .groupId(groupId)
            .userId(userId)
            .date(date)
            .totalMinutes(0)
            .youtubeMinutes(0)
            .initialTotalMinutes(safeTotal)
            .initialYoutubeMinutes(safeYoutube)
            .lastReportedTotalMinutes(safeTotal)
            .lastReportedYoutubeMinutes(safeYoutube)
            .lastUpdatedAt(LocalDateTime.now())
            .build();
    }

    /**
     * 스크린타임 갱신
     */
    public void updateScreenTime(Integer additionalTotalMinutes, Integer additionalYoutubeMinutes,
                                 Integer reportedTotalMinutes, Integer reportedYoutubeMinutes) {
        if (additionalTotalMinutes != null) {
            this.totalMinutes = (this.totalMinutes != null ? this.totalMinutes : 0) + additionalTotalMinutes;
        }
        if (additionalYoutubeMinutes != null) {
            this.youtubeMinutes = (this.youtubeMinutes != null ? this.youtubeMinutes : 0) + additionalYoutubeMinutes;
        }
        this.lastReportedTotalMinutes = reportedTotalMinutes != null ? reportedTotalMinutes : this.lastReportedTotalMinutes;
        this.lastReportedYoutubeMinutes = reportedYoutubeMinutes != null ? reportedYoutubeMinutes : this.lastReportedYoutubeMinutes;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    /**
     * date를 LocalDate로 반환 (프론트 호환성)
     */
    public LocalDate getDateAsLocalDate() {
        return LocalDate.parse(this.date);
    }
}
