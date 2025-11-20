package com.ssafy.Dito.domain.groups.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateGroupChallengeReq(
    @JsonProperty("group_name")
    @NotBlank(message = "그룹 이름은 필수입니다")
    @Size(max = 100, message = "그룹 이름은 100자 이하여야 합니다")
    String groupName,

    @JsonProperty("goal_description")
    @NotBlank(message = "목표 설명은 필수입니다")
    String goalDescription,

    @JsonProperty("penalty_description")
    @NotBlank(message = "페널티 설명은 필수입니다")
    String penaltyDescription,

    @NotNull(message = "기간은 필수입니다")
    @Min(value = 1, message = "기간은 1일 이상이어야 합니다")
    Integer period,

    @JsonProperty("bet_coins")
    @NotNull(message = "베팅 코인은 필수입니다")
    @Min(value = 0, message = "베팅 코인은 0 이상이어야 합니다")
    Integer betCoins
) {
}
