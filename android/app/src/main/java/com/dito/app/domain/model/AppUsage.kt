package com.dito.app.domain.model

data class AppUsage(
    val id: String = "",
    val packageName: String = "",
    val appName: String = "",
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val duration: Long = 0L,
    val date: String = ""
)