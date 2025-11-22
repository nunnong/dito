package com.ssafy.Dito.domain.ai.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record RealtimeActivityReq(
    @JsonProperty("video_id")
    String videoId,

    @JsonProperty("title")
    String title,

    @JsonProperty("channel")
    String channel,

    @JsonProperty("app_package")
    String appPackage,

    @JsonProperty("thumbnail_uri")
    String thumbnailUri,

    @JsonProperty("status")
    @NotNull
    String status, // PLAYING, PAUSED, STOPPED

    @JsonProperty("watch_time")
    Long watchTime,

    @JsonProperty("video_duration")
    Long videoDuration,

    @JsonProperty("pause_time")
    Long pauseTime,

    @JsonProperty("timestamp")
    Long timestamp
) {}
