package com.dito.app.core.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 서버 API 공통 응답 래퍼
 */
@Serializable
data class ApiResponse<T>(
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: T,
    @SerialName("error")
    val error: Boolean = false
)
