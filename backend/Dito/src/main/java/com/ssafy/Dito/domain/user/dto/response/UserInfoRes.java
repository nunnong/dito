package com.ssafy.Dito.domain.user.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import com.ssafy.Dito.domain.status.dto.response.StatusRes;

public record UserInfoRes(
    ProfileRes profile,
    StatusRes statusRes
) {
    @QueryProjection
    public UserInfoRes {
    }
}
