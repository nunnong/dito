package com.ssafy.Dito.domain.item.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import java.util.List;

public record ShopItemRes(
    int coin_balance,
    List<ItemRes> items
) {
    @QueryProjection
    public ShopItemRes{

    }
}
