package com.dito.app.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class AIRunRequest(
    @SerialName("user_id")
    val userId: String,

    @SerialName("behavior_log")
    val behaviorLog: BehaviorLog
)


@Serializable
data class BehaviorLog(
    @SerialName("app_name")
    val appName: String,

    @SerialName("duration_seconds")
    val durationSeconds: Int,

    @SerialName("usage_timestamp")
    val usageTimestamp: String,

    @SerialName("video_title")
    val videoTitle: String? = null,

    @SerialName("channel_name")
    val channelName: String? = null
)


@Serializable
data class AIRunResponse(
    @SerialName("run_id")
    val runId: String,

    @SerialName("status")
    val status: String,

    @SerialName("message")
    val message: String? = null
)