package com.dito.app.core.data.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * AI 비디오 분류 API 요청/응답 모델
 */
@Serializable
data class VideoClassifyRequest(
    @SerialName("assistant_id")
    val assistantId: String = "youtube",
    val input: VideoInput
)

@Serializable
data class VideoInput(
    val title: String,
    val channel: String
)

@Serializable
data class VideoClassifyResponse(
    val title: String,
    val channel: String,
    @SerialName("video_type")
    val videoType: String,  // "EDUCATIONAL", "ENTERTAINMENT", "SHORT_FORM" 등
    val keywords: List<String>
)
