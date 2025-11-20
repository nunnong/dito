package com.dito.app.core.data.group

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokeRequest(
    @SerialName("targetUserId")
    val targetUserId: Long,
)