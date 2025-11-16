package com.ssafy.Dito.domain.report.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ssafy.Dito.domain.report.dto.InsightDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReportReq(
    @JsonProperty("user_id")
    @NotNull(message = "User ID is required")
    Long userId,

    @JsonProperty("report_overview")
    String reportOverview,

    @Valid
    List<InsightDto> insights,

    String advice,

    @JsonProperty("mission_success_rate")
    @Min(value = 0, message = "Success rate must be >= 0")
    @Max(value = 100, message = "Success rate must be <= 100")
    Integer missionSuccessRate
) {
}
