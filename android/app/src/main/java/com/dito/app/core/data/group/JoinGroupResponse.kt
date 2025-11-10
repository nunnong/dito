package com.dito.app.core.data.group

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 그룹 참가 응답 DTO
 * 챌린지 초대 코드를 입력했을 때, 다음 페이지에 뜨는 그룹 정보
 */
@Serializable
data class JoinGroupResponse(
    @SerialName("group_id")
    val groupId: Long,
    @SerialName("group_name")
    val groupName: String,
    val period: Long,
    @SerialName("goalDescription")
    val goalDescription: String,
    @SerialName("penaltyDescription")
    val penaltyDescription: String
)
