package com.dito.app.core.data.report

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HeartbeatRequest(
    @SerialName("timestamp") val timestamp: Long,
    @SerialName("media_session") val mediaSession: MediaSessionInfo? = null,
    @SerialName("current_app") val currentApp: CurrentAppInfo? = null
) {
    @Serializable
    data class MediaSessionInfo(
        @SerialName("video_id") val videoId: String,
        @SerialName("title") val title: String,
        @SerialName("channel") val channel: String,
        @SerialName("app_package") val appPackage: String,
        @SerialName("thumbnail_uri") val thumbnailUri: String,
        @SerialName("status") val status: String, // PLAYING, PAUSED, STOPPED
        @SerialName("watch_time") val watchTime: Long,
        @SerialName("video_duration") val videoDuration: Long,
        @SerialName("pause_time") val pauseTime: Long
    )

    @Serializable
    data class CurrentAppInfo(
        @SerialName("package_name") val packageName: String,
        @SerialName("app_name") val appName: String
    )
}