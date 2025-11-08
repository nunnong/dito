package com.dito.app.core.data.settings

import kotlinx.serialization.Serializable

/**
 * 닉네임 업데이트 요청 DTO
 * 사용자 닉네임을 변경하기 위한 데이터
 */
@Serializable
data class UpdateNicknameRequest(
    val nickname: String
)
