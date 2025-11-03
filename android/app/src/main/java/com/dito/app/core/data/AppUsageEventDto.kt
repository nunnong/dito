package com.dito.app.core.data

data class AppUsageEventDto(
    val event_id: String,
    val event_type: String,
    val package_name: String,
    val app_name: String?,
    val timestamp: Long,
    val duration: Long?,
    val date: String
)