package com.ssafy.Dito.domain.ai.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record HeartbeatReq(
    @JsonProperty("timestamp")
    @NotNull
    Long timestamp,

    @JsonProperty("media_session")
    MediaSessionInfo mediaSession,

    @JsonProperty("current_app")
    CurrentAppInfo currentApp
) {
    public record MediaSessionInfo(
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
        Long pauseTime
    ) {}

    public record CurrentAppInfo(
        @JsonProperty("package_name")
        @NotNull
        String packageName,

        @JsonProperty("app_name")
        String appName
    ) {}
}
