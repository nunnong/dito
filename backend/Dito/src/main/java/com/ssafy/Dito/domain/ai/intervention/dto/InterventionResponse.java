package com.ssafy.Dito.domain.ai.intervention.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * AI Intervention 응답 DTO
 * Phase 0: Echo Server 테스트용
 *
 * 응답 형식:
 * {
 *   "run_id": "550e8400-e29b-41d4-a716-446655440001",
 *   "thread_id": "660e8400-e29b-41d4-a716-446655440000",
 *   "status": "pending"
 * }
 */
public record InterventionResponse(
        @JsonProperty("run_id")
        String runId,

        @JsonProperty("thread_id")
        String threadId,

        @JsonProperty("status")
        String status
) {
    /**
     * 편의 생성자 - status는 기본값으로 "pending" 사용
     */
    public InterventionResponse(String runId, String threadId) {
        this(runId, threadId, "pending");
    }
}