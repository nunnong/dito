package com.ssafy.Dito.domain.ai.evaluation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 평가 결과 DTO
 * AI 서버가 생성한 평가 결과 정보
 */
@Schema(description = "평가 결과")
public record EvaluationResult(
        @Schema(description = "평가 점수 (0-100)", example = "75")
        @JsonProperty("score")
        @NotNull(message = "평가 점수는 필수입니다")
        @Min(value = 0, message = "점수는 0 이상이어야 합니다")
        @Max(value = 100, message = "점수는 100 이하이어야 합니다")
        Integer score,

        @Schema(description = "미션 성공 여부", example = "true")
        @JsonProperty("success")
        @NotNull(message = "미션 성공 여부는 필수입니다")
        Boolean success,

        @Schema(description = "AI 피드백 메시지", example = "미션 수행 중 Instagram을 2분 동안 사용했지만, 대부분의 시간 동안 휴식을 취했습니다.")
        @JsonProperty("feedback")
        @NotBlank(message = "AI 피드백은 필수입니다")
        String feedback,

        @Schema(description = "위반 항목 목록")
        @JsonProperty("violations")
        @Valid
        List<Violation> violations,

        @Schema(description = "개선 제안 사항", example = "[\"휴식 시간에는 스마트폰을 다른 방에 두세요\", \"알림을 일시적으로 끄는 것도 도움이 됩니다\"]")
        @JsonProperty("recommendations")
        List<String> recommendations
) {
}
