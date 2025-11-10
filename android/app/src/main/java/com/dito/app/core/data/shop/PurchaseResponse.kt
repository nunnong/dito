package com.dito.app.core.data.shop

import kotlinx.serialization.Serializable

@Serializable
data class PurchaseResponse(
    val error: Boolean,
    val message: String?
)
