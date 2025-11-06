package com.ssafy.Dito.domain.user.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.Dito.domain.ai.api.dto.AiReq;
import com.ssafy.Dito.domain.auth.exception.NotFoundUserException;
import com.ssafy.Dito.domain.item.entity.QItem;
import com.ssafy.Dito.domain.item.entity.Type;
import com.ssafy.Dito.domain.status.dto.response.QStatusRes;
import com.ssafy.Dito.domain.status.entity.QStatus;
import com.ssafy.Dito.domain.user.dto.response.MainRes;
import com.ssafy.Dito.domain.user.dto.response.ProfileRes;
import com.ssafy.Dito.domain.user.dto.response.QMainRes;
import com.ssafy.Dito.domain.user.dto.response.QProfileRes;
import com.ssafy.Dito.domain.user.dto.response.QUserInfoRes;
import com.ssafy.Dito.domain.user.dto.response.UserInfoRes;
import com.ssafy.Dito.domain.user.entity.QUser;
import com.ssafy.Dito.domain.user.userItem.entity.QUserItem;
import com.ssafy.Dito.domain.weaklyGoal.entity.QWeeklyGoal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final QUser user = QUser.user;
    private final QStatus status = QStatus.status;
    private final QWeeklyGoal weeklyGoal = QWeeklyGoal.weeklyGoal;
    private final QUserItem userItem = QUserItem.userItem;
    private final QItem item = QItem.item;

    public ProfileRes getProfile(long userId) {
        ProfileRes res = jpaQueryFactory
            .select(new QProfileRes(
                user.id,
                user.personalId,
                user.nickname,
                user.birth,
                user.gender,
                user.job,
                user.coinBalance,
                user.frequency,
                user.lastLoginAt,
                user.createdAt,
                user.fcmToken
            ))
            .from(user)
            .where(user.id.eq(userId))
            .fetchOne();

        if (res == null) {
            throw new NotFoundUserException();
        }
        return res;
    }

    public MainRes getMainPage(long userId) {
        MainRes res = jpaQueryFactory
            .select(new QMainRes(
                user.nickname,
                JPAExpressions.select(item.imgUrl)
                    .from(userItem)
                    .join(userItem.id.item, item)
                    .where(
                        userItem.id.user.id.eq(userId),
                        userItem.isEquipped.isTrue(),
                        item.type.eq(Type.COSTUME)
                    )
                    .limit(1),

                JPAExpressions.select(item.imgUrl)
                    .from(userItem)
                    .join(userItem.id.item, item)
                    .where(
                        userItem.id.user.id.eq(userId),
                        userItem.isEquipped.isTrue(),
                        item.type.eq(Type.BACKGROUND)
                    )
                    .limit(1),

                user.coinBalance.stringValue(),

                JPAExpressions.select(weeklyGoal.goal)
                    .from(weeklyGoal)
                    .where(weeklyGoal.user.id.eq(userId))
                    .orderBy(weeklyGoal.startAt.desc())
                    .limit(1),

                status.selfCareStat,
                status.focusStat,
                status.sleepStat
            ))
            .from(user)
            .join(status).on(status.user.id.eq(user.id))
            .where(user.id.eq(userId))
            .fetchOne();

        return res;
    }

    public UserInfoRes getUserInfoForAi(AiReq req) {
        long userId = req.userId();

        return jpaQueryFactory
            .select(new QUserInfoRes(
                new QProfileRes(
                    user.id,
                    user.personalId,
                    user.nickname,
                    user.birth,
                    user.gender,
                    user.job,
                    user.coinBalance,
                    user.frequency,
                    user.lastLoginAt,
                    user.createdAt,
                    user.fcmToken
                ),
                new QStatusRes(
                    status.selfCareStat,
                    status.focusStat,
                    status.sleepStat,
                    status.totalStat
                )
            ))
            .from(user)
            .join(status).on(status.user.id.eq(user.id))
            .where(user.id.eq(userId))
            .fetchOne();
    }
}