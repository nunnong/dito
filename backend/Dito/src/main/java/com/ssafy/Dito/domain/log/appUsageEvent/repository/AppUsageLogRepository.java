package com.ssafy.Dito.domain.log.appUsageEvent.repository;

import com.ssafy.Dito.domain.log.appUsageEvent.document.AppUsageLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * MongoDB repository for app usage logs
 * Replaces AppUsageEventRepository (JPA) - queries MongoDB instead of PostgreSQL
 * Provides query methods optimized for time-series analytics and AI agent data retrieval
 */
@Repository
public interface AppUsageLogRepository extends MongoRepository<AppUsageLogDocument, String> {

    /**
     * Find all logs for a user within a date range
     * Used by AI agents for behavior analysis
     */
    List<AppUsageLogDocument> findByUserIdAndEventDateBetween(
        Long userId, LocalDate startDate, LocalDate endDate
    );

    /**
     * Find recent logs for a user by timestamp
     * Used for real-time intervention trigger detection
     */
    @Query("{ 'user_id': ?0, 'event_timestamp': { $gte: ?1 } }")
    List<AppUsageLogDocument> findByUserIdAndEventTimestampAfter(
        Long userId, Long timestampMillis
    );

    /**
     * Find logs for a specific user ordered by timestamp descending
     * Used for getting recent activity
     */
    List<AppUsageLogDocument> findByUserIdOrderByEventTimestampDesc(Long userId);

    /**
     * Count logs for a specific app in a date range
     * Used for app usage frequency analysis
     */
    long countByUserIdAndPackageNameAndEventDateBetween(
        Long userId, String packageName, LocalDate startDate, LocalDate endDate
    );

    /**
     * Find logs by user ID and event date for daily reports
     */
    List<AppUsageLogDocument> findByUserIdAndEventDate(Long userId, LocalDate eventDate);
}