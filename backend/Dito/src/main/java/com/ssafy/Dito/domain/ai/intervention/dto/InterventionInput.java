package com.ssafy.Dito.domain.ai.intervention.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Intervention 입력 데이터
 * Phase 0: Echo Server 테스트용
 */
public record InterventionInput(
        @NotNull(message = "사용자 ID는 필수입니다")
        @JsonProperty("user_id")
        Long userId,

        @NotNull(message = "행동 로그는 필수입니다")
        @Valid
        @JsonProperty("behavior_log")
        BehaviorLog behaviorLog
) {
}