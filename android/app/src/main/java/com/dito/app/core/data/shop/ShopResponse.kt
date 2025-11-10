package com.dito.app.core.data.shop

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 상점 아이템 조회 API
 * GET /item/shop
 */
@Serializable
data class ShopResponse(
    val error: Boolean,
    val message: String?,
    val data: List<ShopData>,
    val pageInfo: PageInfo
)

@Serializable
data class ShopData(
    @SerialName("coin_balance")
    val coinBalance: Int,
    val items: List<ShopItem>,
)

@Serializable
data class ShopItem(
    @SerialName("ItemId")
    val itemId: Long,
    val name: String,
    val price: Int,
    @SerialName("imageUrl")
    val imageUrl: String,
    val onSale: Boolean,
    val isPurchased: Boolean
)

@Serializable
data class PageInfo(
    val totalElements: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int,
    val hasPrevious: Boolean,
    val hasNext: Boolean
)