package com.ssafy.Dito.domain.report.repository;

import com.ssafy.Dito.domain.report.entity.Report;
import com.ssafy.Dito.domain.report.exception.ReportNotFoundException;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    Optional<Report> findTopByUser_IdOrderByCreatedAtDesc(Long userId);

    default Report getLatestByUserId(Long userId) {
        return findTopByUser_IdOrderByCreatedAtDesc(userId)
            .orElseThrow(ReportNotFoundException::new);
    }
}
