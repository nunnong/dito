package com.ssafy.Dito.domain.weaklyGoal.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.Dito.domain.weaklyGoal.dto.response.QUserWeeklyGoalRes;
import com.ssafy.Dito.domain.weaklyGoal.dto.response.UserWeeklyGoalRes;
import com.ssafy.Dito.domain.weaklyGoal.entity.QWeeklyGoal;
import com.ssafy.Dito.domain.weaklyGoal.exception.NotFoundWeeklyGoalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WeeklyGoalQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final QWeeklyGoal weeklyGoal = QWeeklyGoal.weeklyGoal;

    public UserWeeklyGoalRes getUserWeeklyGoal(long userId) {
        UserWeeklyGoalRes res = jpaQueryFactory
            .select(new QUserWeeklyGoalRes(
                weeklyGoal.goal,
                weeklyGoal.startAt
            ))
            .from(weeklyGoal)
            .where(weeklyGoal.user.id.eq(userId))
            .fetchOne();

        if(res == null){
            throw new NotFoundWeeklyGoalException();
        }

        return res;
    }
}
