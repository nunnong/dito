package com.ssafy.Dito.domain.ai.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for daily activity save operation
 */
public record DailyActivityRes(
    @JsonProperty("activity_id")
    String activityId,

    @JsonProperty("message")
    String message
) {
    public static DailyActivityRes of(String activityId) {
        return new DailyActivityRes(activityId, "일일 활동 데이터가 성공적으로 저장되었습니다");
    }
}
