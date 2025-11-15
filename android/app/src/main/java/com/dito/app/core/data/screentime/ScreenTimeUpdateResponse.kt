package com.dito.app.core.data.screentime

import kotlinx.serialization.Serializable

@Serializable
data class ScreenTimeUpdateResponse(
    val groupId: Long,
    val userId: Long,
    val date: String,
    val totalMinutes: Int,
    val youtubeMinutes: Int,
    val status: String
)
