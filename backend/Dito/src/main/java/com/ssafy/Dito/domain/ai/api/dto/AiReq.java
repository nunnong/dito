package com.ssafy.Dito.domain.ai.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiReq(
    @JsonProperty("user_id")
    long userId
) {

}
