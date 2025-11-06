package com.dito.app.core.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class AIEvaluationRequest(

    @SerialName("user_id")
    val userId: String,

    @SerialName("mission_id")
    val missionId: String,

    @SerialName("mission_info")
    val missionInfo: MissionInfo,

    @SerialName("behavior_logs")
    val behaviorLogs: List<BehaviorLogEntry>
)

@Serializable
data class MissionInfo(
    @SerialName("type")
    val type: String,

    @SerialName("instruction")
    val instruction: String,

    @SerialName("duration_seconds")
    val durationSeconds: Int,

    @SerialName("target_apps")
    val targetApps: List<String>,

    @SerialName("start_time")
    val startTime: String,

    @SerialName("end_time")
    val endTime: String
)

@Serializable
data class BehaviorLogEntry(
    @SerialName("log_type")
    val logType: String,

    @SerialName("sequence")
    val sequence: Int,

    @SerialName("timestamp")
    val timestamp: String,

    // APP_USAGE
    @SerialName("package_name")
    val packageName: String? = null,

    @SerialName("app_name")
    val appName: String? = null,

    @SerialName("duration_seconds")
    val durationSeconds: Int? = null,

    @SerialName("is_target_app")
    val isTargetApp: Boolean? = null,

    // MEDIA_SESSION
    @SerialName("video_title")
    val videoTitle: String? = null,

    @SerialName("channel_name")
    val channelName: String? = null,

    @SerialName("event_type")
    val eventType: String? = null,

    @SerialName("watch_time_seconds")
    val watchTimeSeconds: Int? = null,

    @SerialName("content_type")
    val contentType: String? = null
)


@Serializable
data class AIEvaluationResponse(
    @SerialName("run_id")
    val runId: String,

    @SerialName("thread_id")
    val threadId: String,

    @SerialName("status")
    val status: String
)
