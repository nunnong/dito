package com.dito.app.core.data
import kotlinx.serialization.Serializable


@Serializable
data class MediaSessionEventDto(
    val event_id: String,
    val event_type: String,
    val package_name: String,
    val app_name: String?,
    val title: String?,
    val channel: String?,
    val event_timestamp: Long,
    val video_duration: Long?,
    val watch_time: Long?,
    val pause_time: Long?,
    val event_date: String
)
