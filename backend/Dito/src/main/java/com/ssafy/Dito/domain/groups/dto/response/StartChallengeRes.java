package com.ssafy.Dito.domain.groups.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ssafy.Dito.domain.groups.entity.GroupChallenge;
import java.time.LocalDate;

public record StartChallengeRes(
    @JsonProperty("group_id")
    Long groupId,

    String status,

    @JsonProperty("start_date")
    LocalDate startDate,

    @JsonProperty("end_date")
    LocalDate endDate,

    @JsonProperty("total_bet_coins")
    Integer totalBetCoins
) {
    public static StartChallengeRes from(GroupChallenge groupChallenge) {
        return new StartChallengeRes(
            groupChallenge.getId(),
            groupChallenge.getStatus(),
            groupChallenge.getStartDate(),
            groupChallenge.getEndDate(),
            groupChallenge.getTotalBetCoins()
        );
    }
}