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
@RequestMapping("/fcm")
@RequiredArgsConstructor
@Tag(name = "FCM Internal API", description = "AI 서버 전용 FCM 알림 API (X-API-Key 인증 필요)")
public class FcmInternalController {

    private final FcmService fcmService;

    /**
     * POST /fcm/send
     * AI 서버에서 호출 - 개입 알림 전송
     * TECH_SPEC.md:882 참조
     *
     * @param apiKey  X-API-Key 헤더 (ApiKeyAuthFilter에서 검증)
     * @param request 알림 요청 (userId, message, interventionId, type)
     * @return 성공 응답
     */
    @PostMapping("/send")
    @Operation(
            summary = "AI 개입 알림 전송",
            description = "AI 서버에서 사용자에게 개입 알림을 전송합니다. X-API-Key 헤더 인증이 필요합니다."
    )
    public ResponseEntity<?> sendInterventionNotification(
            @Parameter(description = "API Key (X-API-Key 헤더)", required = true)
            @RequestHeader("X-API-Key") String apiKey,
            @Valid @RequestBody FcmSendRequest request
    ) {
        log.info("Received FCM send request from AI server for user: {}", request.userId());

        try {
            fcmService.sendInterventionNotification(request);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Notification sent successfully",
                    "userId", request.userId(),
                    "interventionId", request.interventionId()
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
                    "error", "Failed to send notification"
            ));
        }
    }
}
