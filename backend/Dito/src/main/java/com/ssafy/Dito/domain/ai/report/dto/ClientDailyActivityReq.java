package com.ssafy.Dito.domain.ai.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO for saving daily user activity from Client (App)
 * UserId is injected from JWT, so it is excluded from the request body
 */
public record ClientDailyActivityReq(
    @JsonProperty("date")
    @NotNull(message = "날짜는 필수입니다")
    String date,  // Format: "yyyy-MM-dd"

    @JsonProperty("summary")
    @NotNull(message = "요약 정보는 필수입니다")
    @Valid
    DailyActivityReq.SummaryReq summary,

    @JsonProperty("app_usage_stats")
    List<DailyActivityReq.AppUsageStatReq> appUsageStats,

    @JsonProperty("media_sessions")
    List<DailyActivityReq.MediaSessionReq> mediaSessions
) {
    /**
     * Convert to internal DailyActivityReq (adding userId)
     */
    public DailyActivityReq toInternalReq(Long userId) {
        return new DailyActivityReq(
            this.date,
            userId,
            this.summary,
            this.appUsageStats,
            this.mediaSessions
        );
    }
}
