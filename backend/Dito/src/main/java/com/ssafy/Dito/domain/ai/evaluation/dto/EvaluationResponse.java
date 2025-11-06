package com.ssafy.Dito.domain.ai.evaluation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 미션 평가 응답 DTO
 * 평가 요청 즉시 반환되는 응답 (평가 결과는 나중에 조회)
 */
@Schema(description = "미션 평가 응답")
public record EvaluationResponse(
        @Schema(description = "평가 실행 고유 ID", example = "eval_550e8400-e29b-41d4-a716-446655440001")
        @JsonProperty("run_id")
        @NotBlank(message = "Run ID는 필수입니다")
        String runId,

        @Schema(description = "스레드 고유 ID", example = "thread_660e8400-e29b-41d4-a716-446655440000")
        @JsonProperty("thread_id")
        @NotBlank(message = "Thread ID는 필수입니다")
        String threadId,

        @Schema(description = "평가 상태 (pending, running, completed, failed)", example = "pending")
        @JsonProperty("status")
        @NotBlank(message = "평가 상태는 필수입니다")
        String status
) {
}
