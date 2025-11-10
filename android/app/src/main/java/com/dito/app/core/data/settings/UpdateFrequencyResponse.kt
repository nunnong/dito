package com.dito.app.core.data.settings

import kotlinx.serialization.Serializable

@Serializable
data class UpdateFrequencyResponse(
    val error: Boolean,
    val message: String? = null
)