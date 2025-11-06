package com.ssafy.Dito.domain.ai.evaluation.service;

import com.ssafy.Dito.domain.ai.evaluation.document.EvaluationDocument;
import com.ssafy.Dito.domain.ai.evaluation.dto.BehaviorLog;
import com.ssafy.Dito.domain.ai.evaluation.dto.EvaluationRequest;
import com.ssafy.Dito.domain.ai.evaluation.dto.EvaluationResponse;
import com.ssafy.Dito.domain.ai.evaluation.repository.EvaluationRepository;
import com.ssafy.Dito.domain.mission.entity.Mission;
import com.ssafy.Dito.domain.mission.repository.MissionRepository;
import com.ssafy.Dito.domain.user.entity.User;
import com.ssafy.Dito.domain.user.repository.UserRepository;
import com.ssafy.Dito.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Evaluation Service
 * Handles mission evaluation business logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final UserRepository userRepository;
    private final MissionRepository missionRepository;

    /**
     * Evaluate mission - returns immediately with pending status
     * Actual evaluation will be processed asynchronously
     *
     * @param request EvaluationRequest containing mission and behavior data
     * @return EvaluationResponse with run_id, thread_id, and status
     */
    public EvaluationResponse evaluateMission(EvaluationRequest request) {
        log.info("Evaluation request - userId: {}, missionId: {}",
                request.userId(), request.missionId());

        // Step 1: Validate user
        User user = userRepository.getByPersonalId(request.userId());
        log.debug("User validated - userId: {}, personalId: {}", user.getId(), request.userId());

        // Step 2: Validate mission
        long missionId = parseMissionId(request.missionId());
        Mission mission = missionRepository.getById(missionId);

        // Check if mission is already completed
        if (mission.getStatus().equals("COMPLETED")) {
            log.warn("Mission already completed - missionId: {}", missionId);
            throw new BadRequestException("이미 완료된 미션입니다: " + request.missionId());
        }

        // Step 3: Validate behavior logs
        validateBehaviorLogs(request.behaviorLogs());

        // Step 4: Generate run_id and thread_id
        String runId = UUID.randomUUID().toString();
        String threadId = UUID.randomUUID().toString();

        // Step 5: Save initial evaluation document to MongoDB (pending status)
        String evaluationId = UUID.randomUUID().toString();
        EvaluationDocument document = EvaluationDocument.of(
                evaluationId,
                request.userId(),
                request.missionId(),
                runId,
                threadId,
                request.missionInfo().type(),
                null,  // score - to be updated later
                null,  // success - to be updated later
                null,  // feedback - to be updated later
                null,  // violations - to be updated later
                null,  // recommendations - to be updated later
                "pending"  // status
        );
        evaluationRepository.save(document);
        log.info("Evaluation initiated - evaluationId: {}, runId: {}", evaluationId, runId);

        // TODO: Step 6: Call AI server asynchronously (to be implemented)
        // Future implementation: async AI server call, then update MongoDB and Mission status

        log.info("Evaluation response returned - runId: {}, threadId: {}, status: pending",
                runId, threadId);

        // Step 7: Return response immediately
        return new EvaluationResponse(runId, threadId, "pending");
    }

    /**
     * Validate behavior logs
     *
     * @param logs Behavior logs
     */
    private void validateBehaviorLogs(List<BehaviorLog> logs) {
        if (logs == null || logs.isEmpty()) {
            throw new BadRequestException("행동 로그는 최소 1개 이상이어야 합니다");
        }

        // Validate sequence order
        for (int i = 0; i < logs.size(); i++) {
            if (logs.get(i).sequence() != i + 1) {
                throw new BadRequestException(
                        String.format("행동 로그 순서가 잘못되었습니다. 예상: %d, 실제: %d", i + 1, logs.get(i).sequence())
                );
            }
        }

        log.debug("Behavior logs validated - count: {}", logs.size());
    }

    /**
     * Parse mission ID from String to Long
     *
     * @param missionIdStr Mission ID as String
     * @return Mission ID as Long
     */
    private long parseMissionId(String missionIdStr) {
        try {
            return Long.parseLong(missionIdStr);
        } catch (NumberFormatException e) {
            log.error("Invalid mission ID format: {}", missionIdStr);
            throw new BadRequestException("잘못된 미션 ID 형식입니다: " + missionIdStr);
        }
    }
}
