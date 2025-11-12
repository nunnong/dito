package com.ssafy.Dito.domain.item.dto.response;

import com.querydsl.core.annotations.QueryProjection;

public record ItemRes(
    long ItemId,
    String name,
    int price,
    String imageUrl,
    boolean onSale,
    boolean isPurchased
) {
    @QueryProjection
    public ItemRes{

    }

}
