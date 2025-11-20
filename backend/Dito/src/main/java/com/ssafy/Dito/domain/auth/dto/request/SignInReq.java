package com.ssafy.Dito.domain.auth.dto.request;

import com.ssafy.Dito.domain.auth.constraint.password.ValidPassword;
import com.ssafy.Dito.domain.auth.constraint.personalId.ValidPersonalId;

public record SignInReq(
    @ValidPersonalId
    String personalId,
    @ValidPassword
    String password,
    String fcmToken  // FCM 토큰 (nullable)
) {

}