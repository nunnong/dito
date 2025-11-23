package com.dito.app.core.data.screentime

import kotlinx.serialization.Serializable
import java.time.LocalDate

/**
 * 스크린타임 갱신 요청 데이터
 */
@Serializable
data class ScreenTimeUpdateRequest(
    val groupId: Long,
    val date: String,  // "2025-01-07" 형식
    val totalMinutes: Int,
    val youtubeMinutes: Int? = null
)

/**
 * 스크린타임 갱신 응답 데이터
 */
@Serializable
data class ScreenTimeUpdateResponse(
    val error: Boolean,
    val message: String?,
    val data: ScreenTimeUpdateData?
)

@Serializable
data class ScreenTimeUpdateData(
    val groupId: Long,
    val userId: Long,
    val date: String,
    val totalMinutes: Int,
    val youtubeMinutes: Int?,
    val status: String  // "created" or "updated"
)

/**
 * 그룹 랭킹 조회 응답 데이터
 */
@Serializable
data class GroupRankingResponse(
    val error: Boolean,
    val message: String?,
    val data: GroupRankingData?
)

@Serializable
data class GroupRankingData(
    val groupInfo: GroupInfo,
    val rankings: List<ParticipantRank>
)

@Serializable
data class GroupInfo(
    val groupId: Long,
    val groupName: String,
    val startDate: String?,
    val endDate: String?,
    val goalDescription: String?,
    val penaltyDescription: String?,
    val totalBetCoins: Int,
    val status: String,
    val daysElapsed: Int,
    val daysTotal: Int,
    val progressPercentage: Double,
    val participantCount: Int,
    val maxParticipants: Int
)

@Serializable
data class ParticipantRank(
    val rank: Int,
    val userId: Long,
    val nickname: String,
    val profileImage: String?,
    val totalScreenTimeFormatted: String,  // "10h 30m"
    val avgDailyScreenTimeFormatted: String,  // "3h 30m"
    val betCoins: Int,
    val potentialPrize: Int,
    val isMe: Boolean
)

/**
 * 현재 사용 중인 앱 정보 전송 요청
 */
@Serializable
data class UpdateCurrentAppRequest(
    val groupId: Long,
    val appPackage: String,
    val appName: String,
    val mediaEventId: String? = null,
    val mediaEducational: Boolean? = null,
    val mediaEventTimestamp: Long? = null
)
