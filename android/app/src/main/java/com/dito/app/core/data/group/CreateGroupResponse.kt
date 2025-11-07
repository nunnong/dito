package com.dito.app.core.data.group

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 그룹 생성 응답 DTO
 * 챌린지 그룹 생성 성공 시 받는 데이터
 */
@Serializable
data class CreateGroupResponse(
    val id: Long,
    @SerialName("group_name")
    val groupName: String,
    @SerialName("invite_code")
    val inviteCode: String,
    @SerialName("start_date")
    val startDate: String,
    @SerialName("end_date")
    val endDate: String,
    val period: Int,
    @SerialName("goal_description")
    val goalDescription: String,
    @SerialName("penalty_description")
    val penaltyDescription: String,
    val status: String,
    @SerialName("bet_coins")
    val betCoins: Int,
    @SerialName("total_bet_coins")
    val totalBetCoins: Int,
    @SerialName("created_at")
    val createdAt: String,
    val host: GroupHost
)

@Serializable
data class GroupHost(
    @SerialName("user_id")
    val userId: Long,
    val role: String
)
