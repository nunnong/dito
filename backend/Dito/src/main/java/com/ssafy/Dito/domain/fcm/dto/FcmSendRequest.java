package com.ssafy.Dito.domain.fcm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * AI 서버에서 전달받는 FCM 알림 요청
 * TECH_SPEC.md:882 - POST /fcm/send 엔드포인트 명세
 */
public record FcmSendRequest(
        @NotNull(message = "personalId는 필수입니다")
        @JsonProperty("user_id")
        String personalId,

        @NotBlank(message = "message는 필수입니다")
        String message,

        @NotBlank(message = "interventionId는 필수입니다")
        String interventionId,

        @NotNull(message = "intervention_needed는 필수입니다")
        @JsonProperty("intervention_needed")
        Boolean interventionNeeded,

        @JsonProperty("intervention_type")
        String interventionType,

        @NotBlank(message = "type은 필수입니다")
        String type  // "intervention"
) {
}
