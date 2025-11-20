package com.ssafy.Dito.domain.ai.intervention.controller;

import com.ssafy.Dito.domain.ai.intervention.dto.InterventionRequest;
import com.ssafy.Dito.domain.ai.intervention.dto.InterventionResponse;
import com.ssafy.Dito.domain.user.entity.User;
import com.ssafy.Dito.domain.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * AI Intervention Controller (Phase 1: AI Integration)
 * LangGraph AI 서버와 연동하여 실시간 개입 처리
 *
 * 처리 흐름:
 * 1. 요청 수신 및 사용자 검증
 * 2. AI 서버 호출 (LangGraph /runs 엔드포인트)
 * 3. AI가 비동기로 FCM 전송
 * 4. 응답 반환 (run_id, thread_id, status)
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "에이전트서버로 보냄", description = "AI 에이전트 서버 통신 API")
public class AIInterventionController {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${ai.server.url:http://52.78.96.102:8000}")
    private String aiServerUrl;

    /**
     * POST /ai/intervention
     * AI 개입 요청 처리 (AI Integration)
     *
     * @param request AI 개입 요청 (user_id, behavior_log)
     * @return InterventionResponse (run_id, thread_id, status)
     */
    @PostMapping("/intervention")
    @Operation(
            summary = "AI 개입 요청",
            description = """
                행동 로그를 AI 서버에 전달하여 실시간 개입 처리를 요청합니다.

                요청 Body 예시:
                {
                  "user_id": "catch",
                  "behavior_log": {
                    "app_name": "YouTube",
                    "duration_seconds": 1200,
                    "usage_timestamp": "2025-01-10T15:30:00",
                    "recent_app_switches": 7,
                    "app_metadata": {
                      "title": "디토는 무엇일까요?",
                      "channel": "dito"
                    }
                  }
                }

                * recent_app_switches와 app_metadata는 옵션 필드입니다.
                """
    )
    public ResponseEntity<InterventionResponse> handleIntervention(
            @Valid @RequestBody InterventionRequest request
    ) {
        log.info("Received intervention request - personalId: {}, appName: {}",
                request.userId(), request.behaviorLog().appName());

        // 1. 사용자 검증 (personalId 존재 확인)
        String personalId = request.userId();
        User user = userRepository.getByPersonalId(personalId);

        // 2. AI 요청 페이로드 구성
        Map<String, Object> behaviorLog = new HashMap<>();
        behaviorLog.put("app_name", request.behaviorLog().appName());
        behaviorLog.put("duration_seconds", request.behaviorLog().durationSeconds());
        behaviorLog.put("usage_timestamp", request.behaviorLog().usageTimestamp());

        // 옵션 필드: null이 아닌 경우만 추가
        if (request.behaviorLog().recentAppSwitches() != null) {
            behaviorLog.put("recent_app_switches", request.behaviorLog().recentAppSwitches());
        }
        if (request.behaviorLog().appMetadata() != null) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("title", request.behaviorLog().appMetadata().title());
            metadata.put("channel", request.behaviorLog().appMetadata().channel());
            behaviorLog.put("app_metadata", metadata);
        }



        Map<String, Object> aiRequest = Map.of(
                "assistant_id", "intervention",
                "input", Map.of(
                        "user_id", user.getId(),  // User의 userId (Long) 전달
                        "behavior_log", behaviorLog
                )
        );

        try {
            // 3. AI 서버 호출
            String aiEndpoint = aiServerUrl + "/runs";
            log.info("Calling AI server: {}", aiEndpoint);

            ResponseEntity<Map> aiResponse = restTemplate.postForEntity(
                    aiEndpoint,
                    aiRequest,
                    Map.class
            );

            if (!aiResponse.getStatusCode().is2xxSuccessful()) {
                log.error("AI server error: {}", aiResponse.getStatusCode());
                throw new RuntimeException("AI server error");
            }

            Map<String, Object> aiResult = aiResponse.getBody();
            String runId = (String) aiResult.get("run_id");
            String threadId = (String) aiResult.get("thread_id");
            String status = (String) aiResult.get("status");

            log.info("AI intervention initiated - runId: {}, threadId: {}", runId, threadId);

            // 4. 응답 반환 (AI가 비동기로 FCM 전송)
            return ResponseEntity.ok(new InterventionResponse(runId, threadId, status));

        } catch (Exception e) {
            log.error("Failed to call AI server", e);
            throw new RuntimeException("Failed to process intervention: " + e.getMessage());
        }
    }
}