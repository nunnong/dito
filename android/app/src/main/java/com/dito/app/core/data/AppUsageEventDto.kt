package com.dito.app.core.data
import kotlinx.serialization.Serializable


@Serializable
data class AppUsageEventDto(
    val event_id: String,
    val event_type: String,
    val package_name: String,
    val app_name: String?,
    val event_timestamp: Long,
    val duration: Long?,
    val event_date: String
)