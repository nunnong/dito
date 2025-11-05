package com.ssafy.Dito.domain.weaklyGoal.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import java.sql.Timestamp;

public record UserWeeklyGoalRes(
    String goal,
    Timestamp startAt
) {
    @QueryProjection
    public UserWeeklyGoalRes {
    }
}
