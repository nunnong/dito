package com.dito.app.core.data.group

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateScreenTimeResponse(
    @SerialName("groupId")
    val groupId: Int,
    @SerialName("userId")
    val userId: Int,
    @SerialName("date")
    val date: String,
    @SerialName("totalMinutes")
    val totalMinutes: Int,
    @SerialName("status")
    val status: String
)
