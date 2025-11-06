package com.ssafy.Dito.domain.ai.evaluation.service;

import com.ssafy.Dito.domain.ai.evaluation.document.EvaluationDocument;
import com.ssafy.Dito.domain.ai.evaluation.dto.*;
import com.ssafy.Dito.domain.ai.evaluation.repository.EvaluationRepository;
import com.ssafy.Dito.domain.mission.entity.Mission;
import com.ssafy.Dito.domain.mission.entity.Status;
import com.ssafy.Dito.domain.mission.repository.MissionRepository;
import com.ssafy.Dito.domain.missionResult.dto.request.MissionResultReq;
import com.ssafy.Dito.domain.missionResult.entity.Result;
import com.ssafy.Dito.domain.missionResult.service.MissionResultService;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Evaluation Service
 * Handles mission evaluation business logic and AI server integration
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final RestTemplate restTemplate;
    private final EvaluationRepository evaluationRepository;
    private final UserRepository userRepository;
    private final MissionRepository missionRepository;
    private final MissionResultService missionResultService;

    @Value("${ai.server.url:http://52.78.96.102:8000}")
    private String aiServerUrl;

    /**
     * Evaluate mission using AI server
     *
     * @param request EvaluationRequest containing mission and behavior data
     * @return EvaluationResponse with evaluation results
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
        if (mission.getStatus() == Status.COMPLETED) {
            log.warn("Mission already completed - missionId: {}", missionId);
            throw new BadRequestException("이미 완료된 미션입니다: " + request.missionId());
        }

        // Step 3: Validate behavior logs
        validateBehaviorLogs(request.behaviorLogs());

        // Step 4: Call AI server
        EvaluationResult result = callAiServer(request);
        String runId = UUID.randomUUID().toString();
        String threadId = UUID.randomUUID().toString();
        String status = "completed";

        // Step 5: Save evaluation result to MongoDB
        String evaluationId = UUID.randomUUID().toString();
        EvaluationDocument document = EvaluationDocument.of(
                evaluationId,
                request.userId(),
                request.missionId(),
                runId,
                threadId,
                request.missionInfo().type(),
                result.score(),
                result.success(),
                result.feedback(),
                result.violations(),
                result.recommendations(),
                status
        );
        evaluationRepository.save(document);
        log.info("Evaluation saved to MongoDB - evaluationId: {}", evaluationId);

        // Step 6: Update mission status and create mission result
        mission.updateStatus();
        missionRepository.save(mission);

        Result missionResult = result.success() ? Result.SUCCESS : Result.FAILURE;
        MissionResultReq missionResultReq = new MissionResultReq(missionId, missionResult);
        missionResultService.createMissionResult(missionResultReq);

        log.info("Evaluation completed - runId: {}, score: {}, success: {}",
                runId, result.score(), result.success());

        // Step 7: Return response
        return new EvaluationResponse(runId, threadId, status, result);
    }

    /**
     * Call AI server for evaluation
     *
     * @param request EvaluationRequest
     * @return EvaluationResult from AI server
     */
    private EvaluationResult callAiServer(EvaluationRequest request) {
        // Construct AI request payload
        Map<String, Object> aiRequest = Map.of(
                "assistant_id", "evaluation",
                "input", Map.of(
                        "user_id", request.userId(),
                        "mission_id", request.missionId(),
                        "mission_info", convertMissionInfoToMap(request.missionInfo()),
                        "behavior_logs", convertBehaviorLogsToList(request.behaviorLogs())
                )
        );

        try {
            // Call AI server
            String aiEndpoint = aiServerUrl + "/runs";
            log.info("Calling AI server: {}", aiEndpoint);

            ResponseEntity<Map> aiResponse = restTemplate.postForEntity(
                    aiEndpoint,
                    aiRequest,
                    Map.class
            );

            if (!aiResponse.getStatusCode().is2xxSuccessful()) {
                log.error("AI server error: {}", aiResponse.getStatusCode());
                throw new ExternalApiException("AI 서버 오류");
            }

            // Parse AI response
            Map<String, Object> aiResult = aiResponse.getBody();
            if (aiResult == null) {
                throw new ExternalApiException("AI 서버 응답이 비어있습니다");
            }

            Map<String, Object> output = (Map<String, Object>) aiResult.get("output");
            if (output == null) {
                throw new ExternalApiException("AI 평가 결과가 없습니다");
            }

            return parseEvaluationResult(output);

        } catch (Exception e) {
            log.error("Failed to call AI server", e);
            throw new ExternalApiException("AI 서버 호출 실패: " + e.getMessage());
        }
    }

    /**
     * Parse evaluation result from AI server response
     *
     * @param output Output map from AI server
     * @return EvaluationResult
     */
    private EvaluationResult parseEvaluationResult(Map<String, Object> output) {
        Integer score = (Integer) output.get("score");
        Boolean success = (Boolean) output.get("success");
        String feedback = (String) output.get("feedback");
        List<Map<String, Object>> violationsData = (List<Map<String, Object>>) output.get("violations");
        List<String> recommendations = (List<String>) output.get("recommendations");

        // Parse violations
        List<Violation> violations = null;
        if (violationsData != null) {
            violations = violationsData.stream()
                    .map(v -> new Violation(
                            (String) v.get("app_name"),
                            (Integer) v.get("duration_seconds"),
                            (String) v.get("timestamp")
                    ))
                    .toList();
        }

        return new EvaluationResult(score, success, feedback, violations, recommendations);
    }

    /**
     * Convert MissionInfo to Map for AI request
     */
    private Map<String, Object> convertMissionInfoToMap(MissionInfo info) {
        return Map.of(
                "type", info.type(),
                "instruction", info.instruction(),
                "duration_seconds", info.durationSeconds(),
                "target_apps", info.targetApps() != null ? info.targetApps() : List.of(),
                "start_time", info.startTime(),
                "end_time", info.endTime()
        );
    }

    /**
     * Convert BehaviorLog list to List of Maps for AI request
     */
    private List<Map<String, Object>> convertBehaviorLogsToList(List<BehaviorLog> logs) {
        return logs.stream()
                .map(log -> {
                    Map<String, Object> logMap = new java.util.HashMap<>();
                    logMap.put("log_type", log.logType());
                    logMap.put("sequence", log.sequence());
                    logMap.put("timestamp", log.timestamp());

                    if (log.packageName() != null) logMap.put("package_name", log.packageName());
                    if (log.appName() != null) logMap.put("app_name", log.appName());
                    if (log.durationSeconds() != null) logMap.put("duration_seconds", log.durationSeconds());
                    if (log.videoTitle() != null) logMap.put("video_title", log.videoTitle());
                    if (log.channelName() != null) logMap.put("channel_name", log.channelName());
                    if (log.eventType() != null) logMap.put("event_type", log.eventType());
                    if (log.watchTimeSeconds() != null) logMap.put("watch_time_seconds", log.watchTimeSeconds());
                    if (log.contentType() != null) logMap.put("content_type", log.contentType());
                    if (log.isTargetApp() != null) logMap.put("is_target_app", log.isTargetApp());

                    return logMap;
                })
                .toList();
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
