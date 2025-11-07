package com.dito.app.core.data.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * API 에러 응답을 위한 데이터 클래스
 * @param error 에러 코드 (e.g., "invalid_username_format")
 * @param message 사용자에게 보여줄 에러 메시지
 * @param details 에러에 대한 상세 정보 (구조가 유동적일 수 있음)
 */
@Serializable
data class ApiErrorResponse(
    val error: String? = null,
    val message: String? = null,
    val details: JsonObject? = null
)