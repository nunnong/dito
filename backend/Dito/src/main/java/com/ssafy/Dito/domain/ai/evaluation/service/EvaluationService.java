package com.ssafy.Dito.domain.ai.evaluation.service;

import com.ssafy.Dito.domain.ai.evaluation.document.EvaluationDocument;
import com.ssafy.Dito.domain.ai.evaluation.dto.BehaviorLog;
import com.ssafy.Dito.domain.ai.evaluation.dto.EvaluationRequest;
import com.ssafy.Dito.domain.ai.evaluation.dto.EvaluationResponse;
import com.ssafy.Dito.domain.ai.evaluation.repository.EvaluationRepository;
import com.ssafy.Dito.domain.mission.entity.Mission;
import com.ssafy.Dito.domain.mission.repository.MissionRepository;
import com.ssafy.Dito.domain.missionResult.dto.request.MissionResultReq;
import com.ssafy.Dito.domain.missionResult.entity.Result;
import com.ssafy.Dito.domain.missionResult.service.MissionResultService;
import com.ssafy.Dito.domain.user.entity.User;
import com.ssafy.Dito.domain.user.repository.UserRepository;
import com.ssafy.Dito.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final MissionResultService missionResultService;

    /**
     * Evaluate mission based on behavior logs and target apps
     * Evaluates mission performance, updates mission_result, user coins, and stats
     *
     * @param request EvaluationRequest containing mission and behavior data
     * @return EvaluationResponse with run_id, thread_id, and status
     */
    @Transactional
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
        if (mission.getStatus().name().equals("COMPLETED")) {
            log.warn("Mission already completed - missionId: {}", missionId);
            throw new BadRequestException("이미 완료된 미션입니다: " + request.missionId());
        }

        // Step 3: Validate behavior logs
        validateBehaviorLogs(request.behaviorLogs());

        // Step 4: Evaluate mission by checking if target apps were used
        List<String> targetApps = request.missionInfo().targetApps();
        boolean hasViolation = request.behaviorLogs().stream()
                .filter(behaviorLog -> "APP_USAGE".equals(behaviorLog.logType()))
                .anyMatch(behaviorLog -> {
                    String appName = behaviorLog.appName();
                    String packageName = behaviorLog.packageName();
                    // Check both app_name and package_name against target_apps
                    boolean matches = targetApps.contains(appName) || targetApps.contains(packageName);
                    if (matches) {
                        log.info("Violation detected - app: {}, package: {}", appName, packageName);
                    }
                    return matches;
                });

        Result evaluationResult = hasViolation ? Result.FAILURE : Result.SUCCESS;
        log.info("Mission evaluation completed - missionId: {}, result: {}", missionId, evaluationResult);

        // Step 5: Create mission result (updates mission status, coins, and stats)
        MissionResultReq missionResultReq = new MissionResultReq(missionId, evaluationResult);
        missionResultService.createMissionResult(missionResultReq);
        log.info("Mission result created - missionId: {}, result: {}", missionId, evaluationResult);

        // Step 6: Generate run_id and thread_id for response
        String runId = UUID.randomUUID().toString();
        String threadId = UUID.randomUUID().toString();

        // Step 7: Save evaluation document to MongoDB
        String evaluationId = UUID.randomUUID().toString();
        EvaluationDocument document = EvaluationDocument.of(
                evaluationId,
                request.userId(),
                request.missionId(),
                runId,
                threadId,
                request.missionInfo().type(),
                hasViolation ? 0 : 100,  // score
                !hasViolation,  // success
                hasViolation ? "목표 앱 사용이 감지되었습니다" : "미션을 성공적으로 완료했습니다",  // feedback
                null,  // violations
                null,  // recommendations
                "completed"  // status
        );
        evaluationRepository.save(document);
        log.info("Evaluation document saved - evaluationId: {}, runId: {}", evaluationId, runId);

        log.info("Evaluation completed - runId: {}, threadId: {}, result: {}",
                runId, threadId, evaluationResult);

        // Step 8: Return response
        return new EvaluationResponse(runId, threadId, "completed");
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
