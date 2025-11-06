package com.dito.app.core.data.phone
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class AppUsageEventDto(
    @SerialName("eventId")
    val event_id: String,

    @SerialName("eventType")
    val event_type: String,

    @SerialName("packageName")
    val package_name: String,

    @SerialName("appName")
    val app_name: String?,

    @SerialName("eventTimestamp")
    val event_timestamp: Long,

    @SerialName("duration")
    val duration: Long?,

    @SerialName("eventDate")
    val event_date: String
)