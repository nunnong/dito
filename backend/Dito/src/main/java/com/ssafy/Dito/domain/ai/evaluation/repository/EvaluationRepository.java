package com.ssafy.Dito.domain.ai.evaluation.repository;

import com.ssafy.Dito.domain.ai.evaluation.document.EvaluationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for mission evaluation results
 * Provides query methods for evaluation history and analytics
 */
@Repository
public interface EvaluationRepository extends MongoRepository<EvaluationDocument, String> {

    /**
     * Find evaluation by user ID and mission ID
     * Used to check if a mission has already been evaluated
     *
     * @param userId User ID (personalId)
     * @param missionId Mission ID
     * @return Optional of EvaluationDocument
     */
    Optional<EvaluationDocument> findByUserIdAndMissionId(String userId, String missionId);

    /**
     * Find all evaluations for a user ordered by evaluation time descending
     * Used for evaluation history retrieval
     *
     * @param userId User ID (personalId)
     * @return List of EvaluationDocument ordered by evaluatedAt descending
     */
    List<EvaluationDocument> findByUserIdOrderByEvaluatedAtDesc(String userId);

    /**
     * Count evaluations for a user within a time range
     * Used for statistics and analytics
     *
     * @param userId User ID (personalId)
     * @param start Start time
     * @param end End time
     * @return Count of evaluations
     */
    long countByUserIdAndEvaluatedAtBetween(String userId, LocalDateTime start, LocalDateTime end);

    /**
     * Find evaluations by user ID and mission type
     * Used for mission-type specific analytics
     *
     * @param userId User ID (personalId)
     * @param missionType Mission type (REST, EXERCISE, etc.)
     * @return List of EvaluationDocument
     */
    List<EvaluationDocument> findByUserIdAndMissionType(String userId, String missionType);

    /**
     * Find successful evaluations for a user
     * Used to calculate success rate
     *
     * @param userId User ID (personalId)
     * @param success Success status
     * @return List of EvaluationDocument
     */
    @Query("{ 'user_id': ?0, 'success': ?1 }")
    List<EvaluationDocument> findByUserIdAndSuccess(String userId, Boolean success);

    /**
     * Find evaluations within a time range
     * Used for time-based analytics
     *
     * @param userId User ID (personalId)
     * @param start Start time
     * @param end End time
     * @return List of EvaluationDocument
     */
    List<EvaluationDocument> findByUserIdAndEvaluatedAtBetween(String userId, LocalDateTime start, LocalDateTime end);
}
