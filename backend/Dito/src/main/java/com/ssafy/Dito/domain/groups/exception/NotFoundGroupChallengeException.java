package com.ssafy.Dito.domain.groups.exception;

import com.ssafy.Dito.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class NotFoundGroupChallengeException extends ApiException {
    private static final String MESSAGE = "챌린지를 찾을 수 없습니다.";

    public NotFoundGroupChallengeException() {
        super(HttpStatus.FORBIDDEN, MESSAGE);
    }
}
