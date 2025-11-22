package com.ssafy.Dito.domain.ai.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record RealtimeUsageReq(
    @JsonProperty("package_name")
    @NotNull
    String packageName,

    @JsonProperty("app_name")
    String appName,

    @JsonProperty("timestamp")
    Long timestamp
) {}
