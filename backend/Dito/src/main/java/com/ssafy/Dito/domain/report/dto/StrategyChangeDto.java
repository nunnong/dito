package com.ssafy.Dito.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * 전략 변경 이력 DTO
 *
 * @param timeSlot 시간대
 * @param previous 이전 전략 레벨
 * @param current 현재 전략 레벨
 * @param reason 변경 이유
 */
public record StrategyChangeDto(
        @NotBlank(message = "시간대는 필수입니다")
        @JsonProperty("time_slot")
        String timeSlot,

        @NotBlank(message = "이전 전략 레벨은 필수입니다")
        String previous,

        @NotBlank(message = "현재 전략 레벨은 필수입니다")
        String current,

        @NotBlank(message = "변경 이유는 필수입니다")
        String reason
) {
}
