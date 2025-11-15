package com.dito.app.core.data.screentime

import kotlinx.serialization.Serializable

@Serializable
data class ScreenTimeData(
    val rank: Int,
    val nickname: String,
    val costumeUrl: String?,
    val usageTime: Long
)
