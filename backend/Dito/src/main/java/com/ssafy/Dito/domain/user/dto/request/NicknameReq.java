package com.ssafy.Dito.domain.user.dto.request;

import com.ssafy.Dito.domain.auth.constraint.nickname.ValidNickname;

public record NicknameReq(
    @ValidNickname
    String nickname
) {

}
