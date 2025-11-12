package com.ssafy.Dito.domain.user.userItem.dto.response;

import com.querydsl.core.annotations.QueryProjection;

public record ClosetRes(
    long itemId,
    String name,
    String imageUrl,
    boolean isEquipped
) {
    @QueryProjection
    public ClosetRes {
    }
}
