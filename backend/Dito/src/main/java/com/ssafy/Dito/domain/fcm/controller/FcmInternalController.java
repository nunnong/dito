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
     * AI 서버에서 호출 - 개입 알림 전송 (V2)
     * - intervention_id 제거, mission_id 사용
     * - fcm_type에 따라 notification/data/mixed 메시지 전송
     *
     * @param apiKey  X-API-Key 헤더 (ApiKeyAuthFilter에서 검증)
     * @param request 알림 요청 (personalId, message, missionId, type, fcmType, title, data)
     * @return 성공 응답 (missionId 포함)
     */
    @PostMapping("/send")
    @Operation(
            summary = "AI 개입 알림 전송 (V2)",
            description = "AI 서버에서 사용자에게 개입 알림을 전송합니다. " +
                          "X-API-Key 헤더 인증이 필요합니다. " +
                          "fcm_type에 따라 notification/data/mixed 메시지를 전송합니다."
    )
    public ResponseEntity<?> sendInterventionNotification(
            @Parameter(description = "API Key (X-API-Key 헤더)", required = true)
            @RequestHeader("X-API-Key") String apiKey,
            @Valid @RequestBody FcmSendRequest request
    ) {
        log.info("Received FCM send request from AI server - user: {}, type: {}, fcmType: {}, missionId: {}",
                request.personalId(), request.type(), request.fcmType(), request.missionId());

        try {
            fcmService.sendInterventionNotification(request);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Notification sent successfully",
                    "personalId", request.personalId(),
                    "missionId", request.missionId() != null ? request.missionId() : "none",
                    "fcmType", request.fcmType()
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
