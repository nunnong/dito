package com.ssafy.Dito.domain.item.repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import com.ssafy.Dito.domain.item.dto.response.QItemRes;
import com.ssafy.Dito.domain.item.dto.response.QShopItemRes;
import com.ssafy.Dito.domain.item.dto.response.ShopItemRes;
import com.ssafy.Dito.domain.item.entity.QItem;
import com.ssafy.Dito.domain.item.entity.Type;
import com.ssafy.Dito.domain.user.entity.QUser;
import com.ssafy.Dito.domain.user.userItem.entity.QUserItem;
import com.ssafy.Dito.global.util.PageUtils;
import java.util.List;
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
    private final QUser user = QUser.user;

    public Page<ShopItemRes> getItemPage(long userId, Type type, long pageNumber) {

        Pageable pageRequest = PageRequest.of((int) pageNumber, PAGE_SIZE);

        JPAQuery<Long> countQuery = jpaQueryFactory
            .select(item.countDistinct())
            .from(item)
            .where(
                item.type.eq(type)
                    .and(item.onSale.isTrue())
            );

        List<ShopItemRes> result = jpaQueryFactory
            .from(item)
            .leftJoin(userItem).on(userItem.id.user.id.eq(userId)
                .and(userItem.id.item.id.eq(item.id)))
            .join(user).on(user.id.eq(userId))
            .where(item.type.eq(type)
                .and(item.onSale.isTrue()))
            .orderBy(item.id.desc())
            .offset(pageRequest.getOffset())
            .limit(pageRequest.getPageSize())
            .transform(groupBy(user.coinBalance).list(
                new QShopItemRes(
                    user.coinBalance,
                    list(new QItemRes(
                        item.id,
                        item.name,
                        item.price,
                        item.imgUrl,
                        item.onSale,
                        userItem.isEquipped.isNotNull()
                    ))
                )
            ));

        return PageUtils.of(result, pageRequest, countQuery.fetchOne());
    }
}


