package com.ssafy.Dito.domain.groups.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ssafy.Dito.domain.groups.entity.GroupChallenge;
import java.time.Instant;
import java.time.LocalDate;

public record GroupChallengeResponse(
    Long id,

    @JsonProperty("group_name")
    String groupName,

    @JsonProperty("invite_code")
    String inviteCode,

    @JsonProperty("start_date")
    LocalDate startDate,

    @JsonProperty("end_date")
    LocalDate endDate,

    Integer period,

    @JsonProperty("goal_description")
    String goalDescription,

    @JsonProperty("penalty_description")
    String penaltyDescription,

    String status,

    @JsonProperty("bet_coins")
    Integer betCoins,

    @JsonProperty("total_bet_coins")
    Integer totalBetCoins,

    @JsonProperty("created_at")
    Instant createdAt,

    CreatorInfo creator
) {
    public static GroupChallengeResponse from(GroupChallenge groupChallenge, Long creatorUserId) {
        return new GroupChallengeResponse(
            groupChallenge.getId(),
            groupChallenge.getGroupName(),
            groupChallenge.getInviteCode(),
            groupChallenge.getStartDate(),
            groupChallenge.getEndDate(),
            groupChallenge.getPeriod(),
            groupChallenge.getGoalDescription(),
            groupChallenge.getPenaltyDescription(),
            groupChallenge.getStatus(),
            groupChallenge.getTotalBetCoins(),
            groupChallenge.getTotalBetCoins(),
            groupChallenge.getCreatedAt(),
            new CreatorInfo(creatorUserId, "creator")
        );
    }
}
