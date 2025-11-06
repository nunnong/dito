package com.ssafy.Dito.domain.ai.evaluation.document;

import com.ssafy.Dito.domain.ai.evaluation.dto.Violation;
import com.ssafy.Dito.domain.log.common.MongoBaseDocument;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB document for mission evaluation results
 * Stores AI-generated evaluation data for analytics and history tracking
 */
@Document(collection = "evaluations")
@CompoundIndexes({
        @CompoundIndex(name = "user_mission_idx", def = "{'user_id': 1, 'mission_id': 1}"),
        @CompoundIndex(name = "user_evaluated_idx", def = "{'user_id': 1, 'evaluated_at': -1}")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EvaluationDocument extends MongoBaseDocument {

    @Field("evaluation_id")
    @Indexed(unique = true)
    private String evaluationId;

    @Field("user_id")
    @Indexed
    private String userId;

    @Field("mission_id")
    @Indexed
    private String missionId;

    @Field("run_id")
    private String runId;

    @Field("thread_id")
    private String threadId;

    @Field("mission_type")
    private String missionType;

    @Field("score")
    private Integer score;

    @Field("success")
    private Boolean success;

    @Field("feedback")
    private String feedback;

    @Field("violations")
    private List<Violation> violations;

    @Field("recommendations")
    private List<String> recommendations;

    @Field("evaluated_at")
    @Indexed
    private LocalDateTime evaluatedAt;

    @Field("status")
    private String status;

    @Builder
    private EvaluationDocument(String evaluationId, String userId, String missionId,
                               String runId, String threadId, String missionType,
                               Integer score, Boolean success, String feedback,
                               List<Violation> violations, List<String> recommendations,
                               LocalDateTime evaluatedAt, String status) {
        this.evaluationId = evaluationId;
        this.userId = userId;
        this.missionId = missionId;
        this.runId = runId;
        this.threadId = threadId;
        this.missionType = missionType;
        this.score = score;
        this.success = success;
        this.feedback = feedback;
        this.violations = violations;
        this.recommendations = recommendations;
        this.evaluatedAt = evaluatedAt;
        this.status = status;
    }

    /**
     * Factory method to create EvaluationDocument
     *
     * @param evaluationId Unique evaluation ID
     * @param userId User ID (personalId)
     * @param missionId Mission ID
     * @param runId AI run ID
     * @param threadId AI thread ID
     * @param missionType Mission type (REST, EXERCISE, etc.)
     * @param score Evaluation score (0-100)
     * @param success Mission success status
     * @param feedback AI-generated feedback
     * @param violations List of violations
     * @param recommendations List of recommendations
     * @param status Evaluation status
     * @return EvaluationDocument instance
     */
    public static EvaluationDocument of(String evaluationId, String userId, String missionId,
                                        String runId, String threadId, String missionType,
                                        Integer score, Boolean success, String feedback,
                                        List<Violation> violations, List<String> recommendations,
                                        String status) {
        return EvaluationDocument.builder()
                .evaluationId(evaluationId)
                .userId(userId)
                .missionId(missionId)
                .runId(runId)
                .threadId(threadId)
                .missionType(missionType)
                .score(score)
                .success(success)
                .feedback(feedback)
                .violations(violations)
                .recommendations(recommendations)
                .evaluatedAt(LocalDateTime.now())
                .status(status)
                .build();
    }
}
