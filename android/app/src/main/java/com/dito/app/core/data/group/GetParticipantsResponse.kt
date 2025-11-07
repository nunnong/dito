package com.dito.app.core.data.group

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 그룹 참가자 목록 응답 DTO
 * 챌린지 그룹의 참가자 정보를 받는 데이터
 */
@Serializable
data class GetParticipantsResponse(
    val groupId: Long,
    val count: Int,
    val participants: List<Participant>
)

@Serializable
data class Participant(
    val userId: Long,
    val nickname: String,
    val role: String,
    val betAmount: Int,
    val equipedItems: List<EquipedItem>
)

@Serializable
data class EquipedItem(
    @SerialName("user_item_id")
    val userItemId: String,
    @SerialName("item_id")
    val itemId: Long,
    val type: String,
    val name: String,
    @SerialName("img_url")
    val imgUrl: String
)
