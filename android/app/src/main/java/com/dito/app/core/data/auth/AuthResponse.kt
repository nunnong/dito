package com.dito.app.core.data.auth

import kotlinx.serialization.Serializable

/**
 * 인증 API 응답 DTO
 * 로그인/회원가입 성공 시 받는 데이터
 */
@Serializable
data class AuthResponse(
    val error: Boolean = false,
    val message: String? = null,
    val data: AuthData? = null
)

@Serializable
data class AuthData(
    val accessToken: String,
    val refreshToken: String
)
