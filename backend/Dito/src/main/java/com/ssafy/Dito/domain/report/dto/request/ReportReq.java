package com.ssafy.Dito.domain.report.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReportReq(
    @JsonProperty("user_id")
    @NotNull(message = "User ID is required")
    Long userId,

    @JsonProperty("report_overview")
    String reportOverview,

    @JsonProperty("insight_night")
    String insightNight,

    @JsonProperty("insight_content")
    String insightContent,

    @JsonProperty("insight_self")
    String insightSelf,

    String advice,

    @JsonProperty("mission_success_rate")
    @Min(value = 0, message = "Success rate must be >= 0")
    @Max(value = 100, message = "Success rate must be <= 100")
    Integer missionSuccessRate
) {
}
