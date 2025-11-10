package com.dito.app.core.data.group

import kotlinx.serialization.Serializable

@Serializable
data class EnterGroupRequest(
    val groupId: Long,
    val betCoin: Int
)