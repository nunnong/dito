package com.ssafy.Dito.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InsightDto(
    @NotNull(message = "Insight type is required")
    InsightType type,

    @NotBlank(message = "Insight description is required")
    String description
) {
}
