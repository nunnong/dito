package com.dito.app.core.data.group

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EnterGroupResponse(
    @SerialName("groupId")
    val groupId: Long,
    @SerialName("status")
    val status: String,
    @SerialName("startDate")
    val startDate: String,
    @SerialName("endDate")
    val endDate: String,
    @SerialName("totalBetCoins")
    val totalBetCoins: Int
)