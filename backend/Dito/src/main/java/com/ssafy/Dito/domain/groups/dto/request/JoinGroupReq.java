package com.ssafy.Dito.domain.groups.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record JoinGroupReq(
    @JsonProperty("invite_code")
    @NotBlank(message = "초대 코드는 필수입니다")
    @Size(min = 4, max = 4, message = "초대 코드는 4자리여야 합니다")
    String inviteCode,

    @JsonProperty("bet_coins")
    @NotNull(message = "베팅 코인은 필수입니다")
    @Min(value = 0, message = "베팅 코인은 0 이상이어야 합니다")
    Integer betCoins
) {
}
