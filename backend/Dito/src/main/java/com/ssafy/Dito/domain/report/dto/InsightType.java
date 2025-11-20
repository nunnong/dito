package com.ssafy.Dito.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum InsightType {
    POSITIVE("POSITIVE"),
    NEGATIVE("NEGATIVE"),
    NEUTRAL("NEUTRAL");

    private final String value;

    InsightType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
