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
    @SerialName("group_name")
    val groupName: String? = null,
    @SerialName("invite_code")
    val inviteCode: String? = null,
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null,
    @SerialName("period")
    val period: Int? = null,
    @SerialName("goal_description")
    val goalDescription: String? = null,
    @SerialName("penalty_description")
    val penaltyDescription: String? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("bet_coins")
    val betCoins: Int? = null,
    @SerialName("total_bet_coins")
    val totalBetCoins: Int? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("host")
    val host: GroupHost? = null
)

@Serializable
data class GroupHost(
    @SerialName("user_id")
    val userId: Long? = null,
    @SerialName("role")
    val role: String? = null
)
