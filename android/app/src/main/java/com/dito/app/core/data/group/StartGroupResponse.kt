package com.dito.app.core.data.group

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 그룹 시작 응답 DTO
 * 챌린지 그룹 시작 시 받는 데이터
 */
@Serializable
data class StartGroupResponse(
    @SerialName("group_id")
    val groupId: Long,
    val status: String,
    @SerialName("start_date")
    val startDate: String,
    @SerialName("end_date")
    val endDate: String,
    @SerialName("total_bet_coins")
    val totalBetCoins: Int
)
