package com.ssafy.Dito.domain.log.mediaSessionEvent.repository;

import com.ssafy.Dito.domain.log.mediaSessionEvent.document.MediaSessionEventDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * MongoDB repository for media session event logs
 * Replaces MediaSessionEventRepository (JPA) - queries MongoDB instead of PostgreSQL
 * Tracks YouTube, music, and other media playback for behavioral intervention
 */
@Repository
public interface MediaSessionLogRepository extends MongoRepository<MediaSessionEventDocument, String> {

    /**
     * Find all logs for a user within a date range
     * Used by AI agents for media consumption pattern analysis
     */
    List<MediaSessionEventDocument> findByUserIdAndEventDateBetween(
        Long userId, LocalDate startDate, LocalDate endDate
    );

    /**
     * Find recent logs for a user by timestamp
     * Used for detecting excessive media consumption
     */
    @Query("{ 'user_id': ?0, 'event_timestamp': { $gte: ?1 } }")
    List<MediaSessionEventDocument> findByUserIdAndEventTimestampAfter(
        Long userId, Long timestampMillis
    );

    /**
     * Find logs for a specific user ordered by timestamp descending
     * Used for getting recent media activity
     */
    List<MediaSessionEventDocument> findByUserIdOrderByEventTimestampDesc(Long userId);

    /**
     * Find logs by user ID and package name
     * Used for app-specific media consumption analysis (e.g., YouTube Shorts)
     */
    List<MediaSessionEventDocument> findByUserIdAndPackageName(Long userId, String packageName);

    /**
     * Find logs by user ID and event date for daily reports
     */
    List<MediaSessionEventDocument> findByUserIdAndEventDate(Long userId, LocalDate eventDate);

    /**
     * Count total watch time for a user in a date range
     * Requires aggregation - this is a basic query method
     */
    List<MediaSessionEventDocument> findByUserIdAndEventDateBetweenAndWatchTimeNotNull(
        Long userId, LocalDate startDate, LocalDate endDate
    );
}