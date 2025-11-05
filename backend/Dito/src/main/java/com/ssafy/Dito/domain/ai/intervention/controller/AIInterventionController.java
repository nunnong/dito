package com.ssafy.Dito.domain.ai.intervention.controller;

import com.ssafy.Dito.domain.ai.intervention.dto.InterventionRequest;
import com.ssafy.Dito.domain.ai.intervention.dto.InterventionResponse;
import com.ssafy.Dito.domain.auth.exception.NotFoundUserException;
import com.ssafy.Dito.domain.fcm.dto.FcmNotificationRequest;
import com.ssafy.Dito.domain.fcm.service.FcmService;
import com.ssafy.Dito.domain.user.entity.User;
import com.ssafy.Dito.domain.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * AI Intervention Controller (Phase 0: Echo Server)
 * FCM 테스트를 위한 Echo Server 구현
 *
 * 처리 흐름:
 * 1. 요청 수신 및 검증
 * 2. UUID 생성 (run_id, thread_id)
 * 3. FCM 메시지 구성
 * 4. 기존 FcmService를 통해 푸시 알림 전송
 * 5. 응답 반환 (status: "pending")
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "AI Intervention API", description = "AI 개입 테스트 API (Phase 0: Echo Server)")
public class AIInterventionController {

    private final FcmService fcmService;
    private final UserRepository userRepository;

    /**
     * POST /ai/intervention
     * AI 개입 요청 처리 (Echo Server)
     *
     * @param request AI 개입 요청 (user_id, behavior_log)
     * @return InterventionResponse (run_id, thread_id, status)
     */
    @PostMapping("/intervention")
    @Operation(
            summary = "AI 개입 요청 (Echo Server)",
            description = "행동 로그를 받아 FCM 푸시 알림을 전송합니다. Phase 0 테스트용 구현입니다."
    )
    public ResponseEntity<InterventionResponse> handleIntervention(
            @Valid @RequestBody InterventionRequest request
    ) {
        log.info("Received intervention request - personalId: {}, appName: {}",
                request.userId(),
                request.behaviorLog().appName());

        // 1. 사용자 검증 (personalId로 조회)
        String personalId = request.userId();
        User user = userRepository.getByPersonalId(personalId);

        // FCM 토큰 검증 (로그만 남김, 전송은 FcmService에서 처리)
        if (user.getFcmToken() == null || user.getFcmToken().isBlank()) {
            log.warn("User {} has no FCM token. FCM notification will be skipped.", personalId);
        }

        // 2. UUID 생성
        String runId = UUID.randomUUID().toString();
        String threadId = UUID.randomUUID().toString();

        // 3. FCM 메시지 구성
        String appName = request.behaviorLog().appName();
        int durationSeconds = request.behaviorLog().durationSeconds();
        int durationMinutes = durationSeconds / 60;

        String title = "디토 AI 개입 테스트";
        String body = String.format("%s을(를) %d분 사용 중입니다", appName, durationMinutes);

        // Data 필드 추가
        Map<String, String> data = new HashMap<>();
        data.put("type", "intervention_test");
        data.put("run_id", runId);
        data.put("thread_id", threadId);
        data.put("app_name", appName);
        data.put("duration_seconds", String.valueOf(request.behaviorLog().durationSeconds()));
        data.put("action", "rest_suggestion");
        data.put("deep_link", "dito://intervention/" + runId);

        FcmNotificationRequest fcmRequest = new FcmNotificationRequest(
                title,
                body,
                data,
                "high",  // priority
                300      // timeToLive: 5분
        );

        // 4. FCM 푸시 알림 전송 (DB ID 사용)
        fcmService.sendNotificationToUser(user.getId(), fcmRequest);

        log.info("Intervention processing completed - personalId: {}, runId: {}, threadId: {}",
                personalId, runId, threadId);

        // 5. 응답 반환
        InterventionResponse response = new InterventionResponse(runId, threadId, "pending");
        return ResponseEntity.ok(response);
    }
}