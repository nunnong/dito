package com.dito.app.core.data.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 회원가입 요청 DTO
 * FCM 토큰을 포함하여 서버에 전송
 */
@Serializable
data class SignUpRequest(
    @SerialName("personalId")
    val username: String,
    val password: String,
    val nickname: String? = null,
    val fcmToken: String? = null
)
