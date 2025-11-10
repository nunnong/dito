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
    @SerialName("startDate")
    val startDate: String? = null,
    @SerialName("endDate")
    val endDate: String? = null,
    @SerialName("goalDescription")
    val goalDescription: String,
    @SerialName("penaltyDescription")
    val penaltyDescription: String
)
