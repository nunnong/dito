package com.dito.app.core.data.missionNotification

import com.dito.app.core.data.shop.PageInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 미션 알림화면 API 응답 DTO
 */
@Serializable
data class MissionNotificationResponse(
    val error: Boolean,
    val message: String?,
    val data: List<MissionNotificationData>,
    val pageInfo: PageInfo
    )

@Serializable
data class MissionNotificationData(
    val id: Long,
    val missionType: String,
    val missionText: String,
    val coinReward: Int,
    val status: MissionStatus,
    val result: MissionResult
)

@Serializable
enum class MissionStatus {
    IN_PROGRESS,
    COMPLETED
}

@Serializable
enum class MissionResult {
    SUCCESS,
    FAILURE,
    IGNORE,
}