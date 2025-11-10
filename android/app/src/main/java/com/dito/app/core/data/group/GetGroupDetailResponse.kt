package com.dito.app.core.data.group

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetGroupDetailResponse(

    // api 받기 전 임시로 작성
    // 소속 그룹 정보 가져오기
    val groupId: Long? = null,
    val groupName: String? = null,
    val inviteCode: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val period: Int? = null,
    val goalDescription: String? = null,
    val penaltyDescription: String? = null,
    val status: String? = null,
    val betCoins: Int? = null,
    val totalBetCoins: Int? = null,
    val createdAt: String? = null,
    val host: GroupHost? = null
)
