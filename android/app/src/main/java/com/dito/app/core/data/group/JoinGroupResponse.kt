package com.dito.app.core.data.group

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 그룹 참가 응답 DTO
 * 챌린지 그룹 참가 성공 시 받는 데이터
 */
@Serializable
data class JoinGroupResponse(
    @SerialName("group_id")
    val groupId: Long,
    @SerialName("group_name")
    val groupName: String,
    @SerialName("start_date")
    val startDate: String,
    @SerialName("end_date")
    val endDate: String,
    @SerialName("goal_description")
    val goalDescription: String,
    @SerialName("penalty_description")
    val penaltyDescription: String
)
