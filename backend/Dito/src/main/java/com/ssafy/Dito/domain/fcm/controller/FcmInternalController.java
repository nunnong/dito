package com.ssafy.Dito.domain.fcm.controller;

import com.ssafy.Dito.domain.fcm.dto.FcmSendRequest;
import com.ssafy.Dito.domain.fcm.service.FcmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * FCM Internal API Controller
 * AI 서버 전용 내부 API
 * X-API-Key 인증 필요 (ApiKeyAuthFilter에서 처리)
 */
@Slf4j
@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
@Tag(name = "FCM Internal API", description = "AI 서버 전용 FCM 알림 API (X-API-Key 인증 필요)")
public class FcmInternalController {

    private final FcmService fcmService;

    /**
     * POST /api/fcm/send
     * AI 서버에서 호출 - 알림 전송 (Data 타입 통일)
     * - FCM 타입은 항상 data로 전송
     * - mission_id가 있으면: Mission 테이블 조회 후 풍부한 정보 전송
     * - mission_id가 없으면: title, message만 전송
     *
     * @param apiKey  X-API-Key 헤더 (ApiKeyAuthFilter에서 검증)
     * @param request 알림 요청 (personalId, title, message, missionId)
     * @return 성공 응답 (hasMission 포함)
     */
    @PostMapping("/send")
    @Operation(
            summary = "AI 알림 전송 (Data 타입)",
            description = """
                    AI 서버에서 사용자에게 알림을 전송합니다.
                    - mission_id가 있으면: Mission 테이블 조회 후 미션 정보 포함
                    - mission_id가 없으면: title, message만 전송 (격려 메시지 등)
                    - X-API-Key 헤더 인증이 필요합니다.
                    """
    )
    public ResponseEntity<?> sendInterventionNotification(
            @Parameter(description = "API Key (X-API-Key 헤더)", required = true)
            @RequestHeader("X-API-Key") String apiKey,
            @Valid @RequestBody FcmSendRequest request
    ) {
        log.info("FCM request - user: {}, missionId: {}, hasMission: {}",
                request.personalId(),
                request.missionId(),
                request.missionId() != null);

        try {
            fcmService.sendInterventionNotification(request);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Notification sent successfully",
                    "personalId", request.personalId(),
                    "missionId", request.missionId() != null ? request.missionId() : "none",
                    "hasMission", request.missionId() != null
            ));

        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));

        } catch (Exception e) {
            log.error("Failed to send notification", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", "Failed to send notification: " + e.getMessage()
            ));
        }
    }
}
