package com.dito.app.core.data.auth

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 회원가입 요청 DTO
 * FCM 토큰을 포함하여 서버에 전송
 *
 * frequency: null이면 JSON에 아예 포함되지 않음 (백엔드에서 NORMAL로 설정)
 * fcmToken: 값이 있으면 전송됨
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SignUpRequest(
    @SerialName("personalId")
    val username: String,
    val password: String,
    val nickname: String,
    val birth: String,
    val gender: String,
    val job: String,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val frequency: String? = null,
    val fcmToken: String? = null
)
