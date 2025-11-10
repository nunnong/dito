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

    @SerialName("recent_app_switches")
    val recentAppSwitches: Int? = null,

    @SerialName("app_metadata")
    val appMetadata: AppMetadata? = null
)

@Serializable
data class AppMetadata(
    @SerialName("title")
    val title: String,

    @SerialName("channel")
    val channel: String
)


@Serializable
data class AIRunResponse(
    @SerialName("run_id")
    val runId: String,

    @SerialName("thread_id")
    val threadId: String,

    @SerialName("status")
    val status: String,

    @SerialName("message")
    val message: String? = null
)