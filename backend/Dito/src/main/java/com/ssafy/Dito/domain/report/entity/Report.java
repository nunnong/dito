package com.ssafy.Dito.domain.report.entity;

import com.ssafy.Dito.domain._common.IdentifiableEntity;
import com.ssafy.Dito.domain.report.dto.InsightDto;
import com.ssafy.Dito.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "report")
@Comment("AI 리포트 데이터 테이블")
public class Report extends IdentifiableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Comment("user 테이블 FK")
    private User user;

    @Column(name = "report_overview", columnDefinition = "TEXT")
    @Comment("리포트 요약")
    private String reportOverview;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "insights", columnDefinition = "jsonb")
    @Comment("인사이트 목록 (JSONB)")
    private List<InsightDto> insights;

    @Column(name = "advice", columnDefinition = "TEXT")
    @Comment("AI 조언(Advice)")
    private String advice;

    @Column(name = "mission_success_rate")
    @Comment("미션 성공률(%)")
    private Integer missionSuccessRate;

    @Column(name = "status", length = 20)
    @Comment("리포트 상태")
    private String status;

    @Column(name = "report_date")
    @Comment("리포트 대상 날짜")
    private LocalDate reportDate;

    @Column(name = "created_at", nullable = false)
    @Comment("생성일")
    private Instant createdAt;

    private Report(User user, String reportOverview, List<InsightDto> insights,
                   String advice, Integer missionSuccessRate, LocalDate reportDate) {
        this.user = user;
        this.reportOverview = reportOverview;
        this.insights = insights;
        this.advice = advice;
        this.missionSuccessRate = missionSuccessRate;
        this.reportDate = reportDate != null ? reportDate : LocalDate.now();
        this.createdAt = Instant.now();
    }

    public static Report of(User user, String reportOverview, List<InsightDto> insights,
                            String advice, Integer missionSuccessRate, LocalDate reportDate) {
        return new Report(user, reportOverview, insights, advice, missionSuccessRate, reportDate);
    }
}
