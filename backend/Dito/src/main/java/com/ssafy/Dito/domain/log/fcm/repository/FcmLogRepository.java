package com.ssafy.Dito.domain.log.fcm.repository;

import com.ssafy.Dito.domain.log.fcm.document.FcmLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB repository for FCM notification logs
 * Provides query methods for FCM send tracking and analytics
 */
@Repository
public interface FcmLogRepository extends MongoRepository<FcmLogDocument, String> {

    /**
     * Find all logs for a user ordered by sent time descending
     * Used for viewing user's notification history
     */
    List<FcmLogDocument> findByUserIdOrderBySentAtDesc(Long userId);

    /**
     * Find logs by personal ID ordered by sent time descending
     * Used when querying by personal ID instead of internal user ID
     */
    List<FcmLogDocument> findByPersonalIdOrderBySentAtDesc(String personalId);

    /**
     * Find logs by success status after a certain time
     * Used for monitoring recent failures or success rate
     */
    List<FcmLogDocument> findBySuccessAndSentAtAfter(Boolean success, LocalDateTime after);

    /**
     * Count successful/failed notifications for a user in a time range
     * Used for calculating delivery success rate per user
     */
    long countByUserIdAndSuccessAndSentAtBetween(
        Long userId, Boolean success, LocalDateTime start, LocalDateTime end
    );

    /**
     * Find logs for a specific mission
     * Used for tracking notifications related to a specific mission
     */
    List<FcmLogDocument> findByMissionIdOrderBySentAtDesc(Long missionId);

    /**
     * Find recent logs for a user (limit by count)
     * Used for getting last N notifications
     */
    List<FcmLogDocument> findTop10ByUserIdOrderBySentAtDesc(Long userId);

    /**
     * Count total logs by success status
     * Used for overall system health monitoring
     */
    long countBySuccess(Boolean success);
}
