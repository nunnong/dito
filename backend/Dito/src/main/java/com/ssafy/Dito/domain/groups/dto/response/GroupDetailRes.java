package com.ssafy.Dito.domain.groups.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDate;

public record GroupDetailRes(
    long groupId,
    String groupName,
    String goalDescription,
    String penaltyDescription,
    int period,
    int betCoin,
    int totalBetCoin,
    String inviteCode,
    LocalDate startDate,
    LocalDate endDate,
    String status,
    boolean isHost
) {
    @QueryProjection
    public GroupDetailRes{
    }
}
