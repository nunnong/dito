package com.ssafy.Dito.domain.groups.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ssafy.Dito.domain.groups.entity.GroupChallenge;

public record JoinGroupRes(
    @JsonProperty("group_id")
    Long groupId,

    @JsonProperty("group_name")
    String groupName
) {
    public static JoinGroupRes from(GroupChallenge groupChallenge) {
        return new JoinGroupRes(
            groupChallenge.getId(),
            groupChallenge.getGroupName() //참여한 그룹 Name return 하면 프론트에서 쓸 수 있도록.
        );
    }
}
