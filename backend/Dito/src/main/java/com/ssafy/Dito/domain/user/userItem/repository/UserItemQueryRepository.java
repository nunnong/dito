package com.ssafy.Dito.domain.user.userItem.repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.Dito.domain.item.entity.Item;
import com.ssafy.Dito.domain.item.entity.QItem;
import com.ssafy.Dito.domain.item.entity.Type;
import com.ssafy.Dito.domain.user.userItem.dto.response.ClosetRes;
import com.ssafy.Dito.domain.user.userItem.dto.response.QClosetRes;
import com.ssafy.Dito.domain.user.userItem.entity.QUserItem;
import com.ssafy.Dito.domain.user.userItem.entity.UserItem;
import com.ssafy.Dito.global.util.PageUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserItemQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    private final int PAGE_SIZE = 9;
    private final QUserItem userItem = QUserItem.userItem;

    public Page<ClosetRes> getUserCloset(long userId, Type type, long pageNumber) {
        Pageable pageRequest = PageRequest.of((int) pageNumber, PAGE_SIZE);

        JPAQuery<Long> countQuery = jpaQueryFactory
            .from(userItem)
            .where(
                userItem.id.user.id.eq(userId)
                    .and(userItem.id.item.type.eq(type))
            )
            .select(userItem.countDistinct());

        List<ClosetRes> res = jpaQueryFactory
            .select(new QClosetRes(
                userItem.id.item.id,
                userItem.id.item.name,
                userItem.id.item.imgUrl,
                userItem.isEquipped
            ))
            .from(userItem)
            .where(
                userItem.id.user.id.eq(userId)
                    .and(userItem.id.item.type.eq(type))
            )
            .orderBy(userItem.id.item.id.desc())
            .offset(pageRequest.getOffset())
            .limit(pageRequest.getPageSize())
            .fetch();

        return PageUtils.of(res, pageRequest, countQuery.fetchOne());
    }

    public UserItem getEquippedItem(long userId, Type type) {
        return jpaQueryFactory
            .selectFrom(userItem)
            .where(
                userItem.id.user.id.eq(userId)
                    .and(userItem.id.item.type.eq(type))
                    .and(userItem.isEquipped.isTrue())
            )
            .fetchOne();
    }
}