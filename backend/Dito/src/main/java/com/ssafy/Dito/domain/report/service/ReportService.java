package com.ssafy.Dito.domain.report.service;

import com.ssafy.Dito.domain.report.dto.request.ReportReq;
import com.ssafy.Dito.domain.report.dto.request.ReportUpdateReq;
import com.ssafy.Dito.domain.report.dto.response.ReportRes;
import com.ssafy.Dito.domain.report.entity.Report;
import com.ssafy.Dito.domain.report.repository.ReportRepository;
import com.ssafy.Dito.domain.user.entity.User;
import com.ssafy.Dito.domain.user.repository.UserRepository;
import com.ssafy.Dito.global.jwt.util.JwtAuthentication;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ReportRes getLatestReport() {
        long userId = JwtAuthentication.getUserId();
        Report report = reportRepository.getLatestByUserId(userId);
        return ReportRes.from(report);
    }

    @Transactional
    public ReportRes createReportForAi(ReportReq req) {
        User user = userRepository.getById(req.userId());

        Report report = Report.of(
            user,
            req.reportOverview(),
            req.insights(),
            req.advice(),
            req.missionSuccessRate(),
            req.reportDate(),
            req.status()
        );

        Report savedReport = reportRepository.save(report);
        return ReportRes.from(savedReport);
    }

    /**
     * Update existing report (partial update)
     * Used by AI server after async report generation completes
     *
     * @param reportId Report ID to update
     * @param req Update request with optional fields
     * @return Updated report response
     */
    @Transactional
    public ReportRes updateReportForAi(Long reportId, ReportUpdateReq req) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + reportId));

        report.update(
            req.reportOverview(),
            req.insights(),
            req.advice(),
            req.missionSuccessRate(),
            req.status()
        );

        return ReportRes.from(report);
    }
}
