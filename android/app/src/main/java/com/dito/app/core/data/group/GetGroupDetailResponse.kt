package com.dito.app.core.data.group

import kotlinx.serialization.Serializable

@Serializable
data class GetGroupDetailResponse(
    val groupId: Long? = null,
    val groupName: String? = null,
    val goalDescription: String? = null,
    val penaltyDescription: String? = null,
    val period: Int? = null,
    val betCoin: Int? = null,
    val totalBetCoin: Int? = null,
    val inviteCode: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val status: String? = null,
    val isHost: Boolean? = null
)
