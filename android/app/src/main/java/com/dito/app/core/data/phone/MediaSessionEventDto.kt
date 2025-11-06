package com.dito.app.core.data.phone
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class MediaSessionEventDto(
    @SerialName("eventId")
    val event_id: String,

    @SerialName("eventType")
    val event_type: String,

    @SerialName("packageName")
    val package_name: String,

    @SerialName("appName")
    val app_name: String?,

    @SerialName("title")
    val title: String?,

    @SerialName("channel")
    val channel: String?,

    @SerialName("eventTimestamp")
    val event_timestamp: Long,

    @SerialName("videoDuration")
    val video_duration: Long?,

    @SerialName("watchTime")
    val watch_time: Long?,

    @SerialName("pauseTime")
    val pause_time: Long?,

    @SerialName("eventDate")
    val event_date: String
)