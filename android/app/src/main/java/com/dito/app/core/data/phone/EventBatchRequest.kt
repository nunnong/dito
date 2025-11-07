package com.dito.app.core.data.phone
import kotlinx.serialization.Serializable

@Serializable
data class AppUsageBatchRequest(
    val appUsageEvent: List<AppUsageEventDto>
)

@Serializable
data class MediaSessionBatchRequest(
    val mediaSessionEvent: List<MediaSessionEventDto>
)

@Serializable
data class BatchUploadResponse(
    val message: String? = null,
    val data: UploadData,
    val error: Boolean
)

@Serializable
data class UploadData(
    val uploaded_count: Int
)