package com.ssafy.Dito.domain.ai.evaluation.service;

import com.ssafy.Dito.domain.ai.evaluation.dto.BehaviorLog;
import com.ssafy.Dito.domain.ai.evaluation.dto.EvaluationRequest;
import com.ssafy.Dito.domain.ai.evaluation.dto.EvaluationResponse;
import com.ssafy.Dito.domain.mission.entity.Mission;
import com.ssafy.Dito.domain.mission.repository.MissionRepository;
import com.ssafy.Dito.domain.user.entity.User;
import com.ssafy.Dito.domain.user.repository.UserRepository;
import com.ssafy.Dito.global.exception.BadRequestException;
import com.ssafy.Dito.global.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Evaluation Service
 * Handles mission evaluation business logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final UserRepository userRepository;
    private final MissionRepository missionRepository;
    private final RestTemplate restTemplate;

    @Value("${ai.server.url:http://52.78.96.102:8000}")
    private String aiServerUrl;

    /**
     * Evaluate mission via AI server
     * Forwards evaluation request to external AI server for asynchronous processing
     *
     * @param request EvaluationRequest containing mission and behavior data
     * @return EvaluationResponse with run_id, thread_id, and status (pending)
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

        // Step 4: Build AI server request payload
        // Convert null to empty list for AI server consistency
        List<BehaviorLog> behaviorLogs = request.behaviorLogs() != null
                ? request.behaviorLogs()
                : Collections.emptyList();

        Map<String, Object> aiRequest = Map.of(
                "assistant_id", "evaluation",
                "input", Map.of(
                        "user_id", user.getId(),  // User DB ID (Long)
                        "mission_id", missionId,  // Mission DB ID (Long)
                        "behavior_logs", behaviorLogs  // Always send array (never null)
                )
        );

        try {
            // Step 5: Call AI server
            String aiEndpoint = aiServerUrl + "/runs";
            log.info("Calling AI server: {}", aiEndpoint);

            ResponseEntity<Map> aiResponse = restTemplate.postForEntity(
                    aiEndpoint,
                    aiRequest,
                    Map.class
            );

            if (!aiResponse.getStatusCode().is2xxSuccessful()) {
                log.error("AI server error: {}", aiResponse.getStatusCode());
                throw new ExternalApiException("AI 서버 호출 실패");
            }

            Map<String, Object> aiResult = aiResponse.getBody();
            String runId = (String) aiResult.get("run_id");
            String threadId = (String) aiResult.get("thread_id");
            String status = (String) aiResult.get("status");

            log.info("AI evaluation initiated - runId: {}, threadId: {}, status: {}", runId, threadId, status);

            // Step 6: Return response (AI will process evaluation asynchronously)
            return new EvaluationResponse(runId, threadId, status);

        } catch (Exception e) {
            log.error("Failed to call AI server", e);
            throw new ExternalApiException("AI 서버 호출 실패: " + e.getMessage());
        }
    }

    /**
     * Validate behavior logs
     * Allows null or empty logs - AI server will interpret the meaning
     *
     * @param logs Behavior logs
     */
    private void validateBehaviorLogs(List<BehaviorLog> logs) {
        // Allow null or empty logs
        if (logs == null || logs.isEmpty()) {
            log.debug("Behavior logs empty - AI will interpret meaning");
            return;
        }

        // Validate sequence order only when logs exist
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
