package com.ssafy.Dito.domain.report.entity;

import com.ssafy.Dito.domain._common.IdentifiableEntity;
import com.ssafy.Dito.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

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

    @Column(name = "insight_night", columnDefinition = "TEXT")
    @Comment("야간 패턴 인사이트")
    private String insightNight;

    @Column(name = "insight_content", columnDefinition = "TEXT")
    @Comment("콘텐츠 소비 인사이트")
    private String insightContent;

    @Column(name = "insight_self", columnDefinition = "TEXT")
    @Comment("자기관리 인사이트")
    private String insightSelf;

    @Column(name = "advice", columnDefinition = "TEXT")
    @Comment("AI 조언(Advice)")
    private String advice;

    @Column(name = "mission_success_rate")
    @Comment("미션 성공률(%)")
    private Integer missionSuccessRate;

    @Column(name = "created_at", nullable = false)
    @Comment("생성일")
    private Instant createdAt;

    private Report(User user, String reportOverview, String insightNight,
                   String insightContent, String insightSelf, String advice,
                   Integer missionSuccessRate) {
        this.user = user;
        this.reportOverview = reportOverview;
        this.insightNight = insightNight;
        this.insightContent = insightContent;
        this.insightSelf = insightSelf;
        this.advice = advice;
        this.missionSuccessRate = missionSuccessRate;
        this.createdAt = Instant.now();
    }

    public static Report of(User user, String reportOverview, String insightNight,
                            String insightContent, String insightSelf, String advice,
                            Integer missionSuccessRate) {
        return new Report(user, reportOverview, insightNight, insightContent,
                         insightSelf, advice, missionSuccessRate);
    }
}
