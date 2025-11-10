package com.dito.app.core.data.group

import kotlinx.serialization.Serializable

/**
 * 그룹 참여 요청
 */
@Serializable
data class JoinGroupRequest(
    val inviteCode: String
)

/**
 * 그룹 참여 응답
 */
@Serializable
data class JoinGroupResponse(
    val error: Boolean,
    val message: String?,
    val data: JoinGroupData?
)

@Serializable
data class JoinGroupData(
    val groupId: Long,
    val groupName: String,
    val role: String,  // "LEADER" or "MEMBER"
    val participantCount: Int,
    val maxParticipants: Int,
    val betCoins: Int
)
