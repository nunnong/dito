package com.ssafy.Dito.domain.report.dto.response;

import com.ssafy.Dito.domain.report.dto.InsightDto;
import com.ssafy.Dito.domain.report.entity.Report;
import java.time.Instant;
import java.util.List;

public record ReportRes(
    Long id,
    String reportOverview,
    List<InsightDto> insights,
    String advice,
    Integer missionSuccessRate,
    Instant createdAt
) {
    public static ReportRes from(Report report) {
        return new ReportRes(
            report.getId(),
            report.getReportOverview(),
            report.getInsights(),
            report.getAdvice(),
            report.getMissionSuccessRate(),
            report.getCreatedAt()
        );
    }
}
