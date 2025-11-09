package com.dito.app.core.data.group

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateScreenTimeRequest(

    @SerialName("group_id")
    val groupId: Int,
    @SerialName("date")
    val date: String,
    @SerialName("total_minutes")
    val totalMinutes: Int
)