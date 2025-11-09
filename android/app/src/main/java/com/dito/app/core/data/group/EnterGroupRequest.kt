package com.dito.app.core.data.group

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EnterGroupRequest(
    @SerialName("group_id")
    val groupId: Long,
    @SerialName("bet_coin")
    val betCoin: Int
)