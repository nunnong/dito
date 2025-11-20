package com.ssafy.Dito.domain.ai.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for AI report generation request
 * Returns immediately with async processing status
 */
public record ReportRequestRes(
    @JsonProperty("run_id")
    String runId,

    @JsonProperty("thread_id")
    String threadId,

    @JsonProperty("status")
    String status  // "pending"
) {
    public static ReportRequestRes of(String runId, String threadId, String status) {
        return new ReportRequestRes(runId, threadId, status);
    }
}
