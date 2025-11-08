package com.dito.app.core.data.group

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 그룹 참가 요청 DTO
 * 초대 코드로 챌린지 그룹에 참가하기 위한 데이터
 */
@Serializable
data class JoinGroupRequest(
    @SerialName("invite_code")
    val inviteCode: String,
    @SerialName("bet_coins")
    val betCoins: Int
)
