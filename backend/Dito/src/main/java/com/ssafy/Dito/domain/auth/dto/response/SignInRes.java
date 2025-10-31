package com.ssafy.Dito.domain.auth.dto.response;

public record SignInRes (
        String accessToken,
        String refreshToken)
{ }
