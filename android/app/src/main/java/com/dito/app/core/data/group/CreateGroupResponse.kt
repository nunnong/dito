package com.dito.app.core.data.group

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 그룹 생성 응답 DTO
 * 챌린지 그룹 생성 성공 시 받는 데이터
 */
@Serializable
data class CreateGroupResponse(
    @SerialName("id")
    val id: Long? = null,
    @SerialName("groupName")
    val groupName: String? = null,
    @SerialName("inviteCode")
    val inviteCode: String? = null,
    @SerialName("startDate")
    val startDate: String? = null,
    @SerialName("endDate")
    val endDate: String? = null,
    @SerialName("period")
    val period: Int? = null,
    @SerialName("goalDescription")
    val goalDescription: String? = null,
    @SerialName("penaltyDescription")
    val penaltyDescription: String? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("betCoins")
    val betCoins: Int? = null,
    @SerialName("totalBetCoins")
    val totalBetCoins: Int? = null,
    @SerialName("createdAt")
    val createdAt: String? = null,
    @SerialName("host")
    val host: GroupHost? = null
)

@Serializable
data class GroupHost(
    @SerialName("userId")
    val userId: Long? = null,
    @SerialName("role")
    val role: String? = null
)
