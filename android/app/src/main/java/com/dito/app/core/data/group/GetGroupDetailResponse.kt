package com.dito.app.core.data.group

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetGroupDetailResponse(
    @SerialName("groupId")
    val groupId: Long? = null,
    @SerialName("groupName")
    val groupName: String? = null,
    @SerialName("goalDescription")
    val goalDescription: String? = null,
    @SerialName("penaltyDescription")
    val penaltyDescription: String? = null,
    @SerialName("period")
    val period: Int? = null,
    @SerialName("betCoin")
    val betCoin: Int? = null,
    @SerialName("totalBetCoin")
    val totalBetCoin: Int? = null,
    @SerialName("inviteCode")
    val inviteCode: String? = null,
    @SerialName("startDate")
    val startDate: String? = null,
    @SerialName("endDate")
    val endDate: String? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("isHost")
    val isHost: Boolean? = null
)
