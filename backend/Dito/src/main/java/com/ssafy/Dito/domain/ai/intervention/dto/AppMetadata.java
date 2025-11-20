package com.ssafy.Dito.domain.ai.intervention.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 앱 메타데이터 정보 (옵션)
 * Phase 1: AI Integration - 유튜브 등 특정 앱의 추가 정보
 */
@Schema(description = "앱 메타데이터 (유튜브용)", example = """
    {
      "title": "디토는 무엇일까요?",
      "channel": "dito"
    }
    """)
public record AppMetadata(
        @JsonProperty("title")
        @Schema(description = "영상 제목", example = "디토는 무엇일까요?")
        String title,

        @JsonProperty("channel")
        @Schema(description = "채널명", example = "dito")
        String channel
) {
}
