package com.ssafy.Dito.domain.ai.report.repository;

import com.ssafy.Dito.domain.ai.report.document.DailyUserActivityDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for daily user activity documents
 */
@Repository
public interface DailyUserActivityRepository extends MongoRepository<DailyUserActivityDocument, String> {

    /**
     * Find activity by user ID and date
     * @param userId User ID
     * @param date Activity date
     * @return Optional DailyUserActivityDocument
     */
    Optional<DailyUserActivityDocument> findByUserIdAndDate(Long userId, LocalDate date);

    /**
     * Find all activities for a user within date range
     * @param userId User ID
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of activities
     */
    List<DailyUserActivityDocument> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Find all activities for a user ordered by date descending
     * @param userId User ID
     * @return List of activities
     */
    List<DailyUserActivityDocument> findByUserIdOrderByDateDesc(Long userId);
}
