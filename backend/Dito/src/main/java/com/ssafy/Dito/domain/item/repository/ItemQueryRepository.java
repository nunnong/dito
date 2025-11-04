package com.ssafy.Dito.domain.item.repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.Dito.domain.item.dto.response.ShopItemRes;
import com.ssafy.Dito.domain.item.entity.QItem;
import com.ssafy.Dito.domain.item.entity.Type;
import com.ssafy.Dito.domain.user.userItem.entity.QUserItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ItemQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    private final int PAGE_SIZE = 9;
    private final QItem item = QItem.item;
    private final QUserItem userItem = QUserItem.userItem;

    public Page<ShopItemRes> getItemPage(long userId, Type type, long pageNumber) {

        Pageable pageRequest = PageRequest.of((int) pageNumber, PAGE_SIZE);

        JPAQuery<Long> countQuery = jpaQueryFactory
            .from(item)

    }
}


