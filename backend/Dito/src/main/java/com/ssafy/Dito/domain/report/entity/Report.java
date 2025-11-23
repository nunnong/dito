package com.ssafy.Dito.domain.report.entity;

import com.ssafy.Dito.domain._common.IdentifiableEntity;
import com.ssafy.Dito.domain.report.dto.InsightDto;
import com.ssafy.Dito.domain.report.dto.StrategyChangeDto;
import com.ssafy.Dito.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "strategy", columnDefinition = "jsonb")
    @Comment("전략 변경 이력 목록 (JSONB)")
    private List<StrategyChangeDto> strategy;

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
                   List<StrategyChangeDto> strategy, String advice, Integer missionSuccessRate,
                   LocalDate reportDate, String status) {
        this.user = user;
        this.reportOverview = reportOverview;
        this.insights = insights;
        this.strategy = strategy != null ? strategy : new ArrayList<>();
        this.advice = advice;
        this.missionSuccessRate = missionSuccessRate;
        this.reportDate = reportDate != null ? reportDate : LocalDate.now();
        this.status = status;
        this.createdAt = Instant.now();
    }

    public static Report of(User user, String reportOverview, List<InsightDto> insights,
                            List<StrategyChangeDto> strategy, String advice, Integer missionSuccessRate,
                            LocalDate reportDate, String status) {
        return new Report(user, reportOverview, insights, strategy, advice, missionSuccessRate, reportDate, status);
    }

    /**
     * Update report fields (partial update)
     * Only non-null fields are updated
     *
     * @param reportOverview New report overview (optional)
     * @param insights New insights list (optional)
     * @param strategy New strategy changes list (optional)
     * @param advice New advice (optional)
     * @param missionSuccessRate New mission success rate (optional)
     * @param status New status (optional)
     */
    public void update(String reportOverview, List<InsightDto> insights,
                       List<StrategyChangeDto> strategy, String advice,
                       Integer missionSuccessRate, String status) {
        if (reportOverview != null) {
            this.reportOverview = reportOverview;
        }
        if (insights != null) {
            this.insights = insights;
        }
        if (strategy != null) {
            this.strategy = strategy;
        }
        if (advice != null) {
            this.advice = advice;
        }
        if (missionSuccessRate != null) {
            this.missionSuccessRate = missionSuccessRate;
        }
        if (status != null) {
            this.status = status;
        }
    }
}
