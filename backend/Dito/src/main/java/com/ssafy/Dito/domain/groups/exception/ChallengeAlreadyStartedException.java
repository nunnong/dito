package com.ssafy.Dito.domain.groups.exception;

import com.ssafy.Dito.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ChallengeAlreadyStartedException extends ApiException {

    private static final String MESSAGE = "이미 시작된 챌린지입니다";

    public ChallengeAlreadyStartedException() {
        super(HttpStatus.BAD_REQUEST, MESSAGE);
    }
}
