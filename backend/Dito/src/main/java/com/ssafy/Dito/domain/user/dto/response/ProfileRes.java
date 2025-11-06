package com.ssafy.Dito.domain.user.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import com.ssafy.Dito.domain.user.entity.Frequency;
import com.ssafy.Dito.domain.user.entity.Gender;
import com.ssafy.Dito.domain.user.entity.Job;
import java.time.Instant;
import java.time.LocalDate;

public record ProfileRes(
    long userId,
    String personalId,
    String nickname,
    LocalDate birth,
    Gender gender,
    Job job,
    int coinBalance,
    Frequency frequency,
    Instant lastLoginAt,
    Instant createdAt,
    String fcmToken
) {
    @QueryProjection
    public ProfileRes {
    }

}
