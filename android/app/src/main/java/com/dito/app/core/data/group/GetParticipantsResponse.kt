package com.dito.app.core.data.group

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 그룹 참가자 목록 응답 DTO
 * 챌린지 그룹의 참가자 정보를 받는 데이터
 */
@Serializable
data class GetParticipantsResponse(
    @SerialName("groupId")
    val groupId: Long,
    @SerialName("count")
    val count: Int,
    @SerialName("participants")
    val participants: List<Participant>
)

@Serializable
data class Participant(
    @SerialName("userId")
    val userId: Long,
    @SerialName("nickname")
    val nickname: String,
    @SerialName("role")
    val role: String,
    @SerialName("betAmount")
    val betAmount: Int,
    @SerialName("equipedItems")
    val equipedItems: List<EquipedItem>
)

@Serializable
data class EquipedItem(
    @SerialName("user_item_id")
    val userItemId: String,
    @SerialName("item_id")
    val itemId: Long,
    @SerialName("type")
    val type: String,
    @SerialName("name")
    val name: String,
    @SerialName("img_url")
    val imgUrl: String
)
