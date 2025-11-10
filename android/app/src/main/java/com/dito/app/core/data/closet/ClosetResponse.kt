package com.dito.app.core.data.closet

import com.dito.app.core.data.shop.PageInfo
import kotlinx.serialization.Serializable

@Serializable
data class ClosetResponse(
    val error: Boolean,
    val message: String?,
    val data: List<ClosetItem>,
    val pageInfo: PageInfo
)

@Serializable
data class ClosetItem(
    val itemId: Long,
    val name: String,
    val imageUrl: String,
    val isEquipped: Boolean
)
