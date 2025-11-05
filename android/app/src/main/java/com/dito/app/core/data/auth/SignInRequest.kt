package com.dito.app.core.data.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 로그인 요청 DTO
 * FCM 토큰을 포함하여 서버에 전송
 */
@Serializable
data class SignInRequest(
    @SerialName("personalId")
    val username: String,
    val password: String,
    val fcmToken: String? = null
)
