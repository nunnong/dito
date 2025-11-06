package com.dito.app.core.data.auth

import kotlinx.serialization.Serializable

@Serializable
data class CheckUsernameResponse(
    val data: Boolean? = null,
    val error: Boolean? = null,
    val message: String? = null
)