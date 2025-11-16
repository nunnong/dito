package com.ssafy.Dito.domain.report.dto.response;

import com.ssafy.Dito.domain.report.entity.Report;
import java.time.Instant;

public record ReportRes(
    Long id,
    String reportOverview,
    String insightNight,
    String insightContent,
    String insightSelf,
    String advice,
    Integer missionSuccessRate,
    Instant createdAt
) {
    public static ReportRes from(Report report) {
        return new ReportRes(
            report.getId(),
            report.getReportOverview(),
            report.getInsightNight(),
            report.getInsightContent(),
            report.getInsightSelf(),
            report.getAdvice(),
            report.getMissionSuccessRate(),
            report.getCreatedAt()
        );
    }
}
