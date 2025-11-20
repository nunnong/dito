package com.ssafy.Dito.domain.groups.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ssafy.Dito.domain.groups.entity.GroupChallenge;
import java.time.LocalDate;

public record JoinGroupRes(
    @JsonProperty("group_id")
    Long groupId,
    @JsonProperty("group_name")
    String groupName,
    int period,
    String goalDescription,
    String penaltyDescription

) {

}
