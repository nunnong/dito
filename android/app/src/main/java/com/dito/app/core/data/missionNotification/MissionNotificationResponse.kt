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
    val title: String,
    val coinReward: Int,
    val status: MissionStatus,
    val result: MissionResult? = null,
    val triggerTime: String? = null,  // 미션 트리거 시각 (ISO 8601)
    val duration: Int? = null,  // 실행되는 시간 (초 단위)
    val feedback: String? = null,  // AI 피드백
    val statChangeSelfCare: Int = 0,  // 자기관리 스탯 변화
    val statChangeFocus: Int = 0,     // 집중 스탯 변화
    val statChangeSleep: Int = 0      // 수면 스탯 변화
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