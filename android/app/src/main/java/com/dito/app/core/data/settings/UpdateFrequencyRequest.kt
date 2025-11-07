package com.dito.app.core.data.settings

import kotlinx.serialization.Serializable

/**
 * 알림 빈도 업데이트 요청 DTO
 * 사용자 알림 빈도를 변경하기 위한 데이터
 */
@Serializable
data class UpdateFrequencyRequest(
    val frequency: String
)
