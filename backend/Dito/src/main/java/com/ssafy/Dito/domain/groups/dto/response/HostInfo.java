package com.ssafy.Dito.domain.groups.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HostInfo(
    @JsonProperty("user_id")
    Long userId,

    String role
) {
}
