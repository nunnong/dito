package com.dito.app.core.data.group

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetRankingResponse(
    @SerialName("rankings")
    val rankings: List<RankingItem>
)

@Serializable
data class RankingItem(
    @SerialName("rank")
    val rank: Int,
    @SerialName("userId")
    val userId: Long,
    @SerialName("nickname")
    val nickname: String,
    @SerialName("profileImage")
    val profileImage: String?,
    @SerialName("itemId")
    val costumeItemId: Int? = null,
    @SerialName("totalScreenTimeFormatted")
    val totalScreenTimeFormatted: String,
    @SerialName("avgDailyScreenTimeFormatted")
    val avgDailyScreenTimeFormatted: String,
    @SerialName("betCoins")
    val betCoins: Int,
    @SerialName("potentialPrize")
    val potentialPrize: Int,
    @SerialName("isMe")
    val isMe: Boolean,
    @SerialName("currentAppPackage")
    val currentAppPackage: String? = null,
    @SerialName("currentAppName")
    val currentAppName: String? = null
)