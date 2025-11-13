package com.dito.app.core.data.group

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetRankingResponse(
    @SerialName("rankings")
    val rankings: List<RankingItem>
)

@Serializable
data class GroupInfo(
    @SerialName("groupId")
    val groupId: Long,
    @SerialName("groupName")
    val groupName: String,
    @SerialName("startDate")
    val startDate: String,
    @SerialName("endDate")
    val endDate: String,
    @SerialName("goalDescription")
    val goalDescription: String,
    @SerialName("penaltyDescription")
    val penaltyDescription: String,
    @SerialName("totalBetCoins")
    val totalBetCoins: Int,
    @SerialName("status")
    val status: String,
    @SerialName("daysElapsed")
    val daysElapsed: Int,
    @SerialName("daysTotal")
    val daysTotal: Int,
    @SerialName("progressPercentage")
    val progressPercentage: Double,
    @SerialName("participantCount")
    val participantCount: Int,
    @SerialName("maxParticipants")
    val maxParticipants: Int
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
