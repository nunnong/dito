package com.dito.app.core.data.group

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EnterGroupResponse(
    @SerialName("group_id")
    val groupId: Long,

    @SerialName("status")
    val status: String,

    @SerialName("start_date")
    val startDate: String,

    @SerialName("end_date")
    val endDate: String,

    @SerialName("total_bet_coins")
    val TotalBetCoins: Int
)