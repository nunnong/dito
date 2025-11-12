package com.ssafy.Dito.domain.groups.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ParticipantInfo(
    @JsonProperty("userId")
    Long userId,

    String nickname,

    String role,

    @JsonProperty("betAmount")
    Integer betAmount,

    @JsonProperty("equipedItems")
    List<EquippedItemInfo> equipedItems
) {
    public static ParticipantInfo of(Long userId, String nickname, String role, Integer betAmount,
        List<EquippedItemInfo> equipedItems) {
        return new ParticipantInfo(userId, nickname, role, betAmount, equipedItems);
    }
}