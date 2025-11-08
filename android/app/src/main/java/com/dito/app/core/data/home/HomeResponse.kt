package com.dito.app.core.data.home

import kotlinx.serialization.Serializable

/**
 * 홈화면 API 응답 DTO
 * 홈 화면 입장 시 받는 데이터
 */

@Serializable
data class HomeResponse(
    val error: Boolean,
    val message: String,
    val data: HomeData?
)

@Serializable
data class HomeData(
    val nickname: String,
    val costumeUrl: String,
    val backgroundUrl : String? = null,
    val coinBalance: Int,
    val weeklyGoal: String? = null,
    val selfCareStatus: Int,
    val focusStatus: Int,
    val sleepStatus: Int
)