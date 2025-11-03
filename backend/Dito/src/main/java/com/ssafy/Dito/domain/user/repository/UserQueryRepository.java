package com.ssafy.Dito.domain.user.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.Dito.domain.auth.exception.NotFoundUserException;
import com.ssafy.Dito.domain.user.dto.response.ProfileRes;
import com.ssafy.Dito.domain.user.dto.response.QProfileRes;
import com.ssafy.Dito.domain.user.entity.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final QUser user = QUser.user;


    public ProfileRes getProfile(long userId) {
        ProfileRes res = jpaQueryFactory
            .select(new QProfileRes(
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
}