package com.dito.app.core.data.settings

import kotlinx.serialization.Serializable

@Serializable
data class UpdateNicknameResponse(
    val error: Boolean = false,
    val message: String? = null
)