package com.dito.app.core.data.group

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 그룹 생성 요청 DTO
 * 챌린지 그룹을 생성하기 위한 데이터
 */
@Serializable
data class CreateGroupRequest(
    @SerialName("group_name")
    val groupName: String,
    @SerialName("goal_description")
    val goalDescription: String,
    @SerialName("penalty_description")
    val penaltyDescription: String,
    val period: Int,
    @SerialName("bet_coins")
    val betCoins: Int
)
