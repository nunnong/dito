package com.dito.app.core.data.shop

import kotlinx.serialization.Serializable

@Serializable
data class PurchaseRequest(
    val itemId: Long
)
