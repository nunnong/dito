package com.dito.app.core.data.screentime

import kotlinx.serialization.Serializable

@Serializable
data class ScreenTimeUpdateRequest(
    val groupId: Long,
    val date: String,
    val totalMinutes: Int,
    val youtubeMinutes: Int
)
