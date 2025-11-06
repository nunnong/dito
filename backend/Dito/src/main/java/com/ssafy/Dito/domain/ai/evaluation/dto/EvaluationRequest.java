package com.ssafy.Dito.domain.ai.evaluation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 미션 평가 요청 DTO
 */
@Schema(description = "미션 평가 요청")
public record EvaluationRequest(
        @Schema(description = "사용자 고유 ID (personalId)", example = "user_12345")
        @JsonProperty("user_id")
        @NotBlank(message = "사용자 ID는 필수입니다")
        String userId,

        @Schema(description = "평가 대상 미션의 고유 ID", example = "mission_20251105_001")
        @JsonProperty("mission_id")
        @NotBlank(message = "미션 ID는 필수입니다")
        String missionId,

        @Schema(description = "미션 정보")
        @JsonProperty("mission_info")
        @NotNull(message = "미션 정보는 필수입니다")
        @Valid
        MissionInfo missionInfo,

        @Schema(description = "행동 로그 목록")
        @JsonProperty("behavior_logs")
        @NotEmpty(message = "행동 로그는 최소 1개 이상이어야 합니다")
        @Valid
        List<BehaviorLog> behaviorLogs
) {
}
