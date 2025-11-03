package com.dito.app.core.data
import kotlinx.serialization.Serializable

@Serializable
data class AppUsageBatchRequest(
    val user_id: Int,
    val events: List<AppUsageEventDto>
)

@Serializable
data class MediaSessionBatchRequest(
    val user_id: Int,
    val events: List<MediaSessionEventDto>
)

@Serializable
data class BatchUploadResponse(
    val success: Boolean,
    val received_count: Int,
    val saved_count: Int,
    val failed_event_ids: List<String> = emptyList(),
    val message: String? = null
)