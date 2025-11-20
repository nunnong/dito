package com.ssafy.Dito.domain.groups.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;

public record EquippedItemInfo(
    @JsonProperty("user_item_id")
    String userItemId,

    @JsonProperty("item_id")
    Long itemId,

    String type,

    String name,

    @JsonProperty("img_url")
    String imgUrl
) {
    @QueryProjection
    public EquippedItemInfo {
    }
}
