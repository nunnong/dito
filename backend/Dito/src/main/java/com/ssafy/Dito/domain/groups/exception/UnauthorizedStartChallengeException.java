package com.ssafy.Dito.domain.groups.exception;

import com.ssafy.Dito.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class UnauthorizedStartChallengeException extends ApiException {

    private static final String MESSAGE = "챌린지를 시작할 권한이 없습니다. 호스트만 시작할 수 있습니다";

    public UnauthorizedStartChallengeException() {
        super(HttpStatus.FORBIDDEN, MESSAGE);
    }
}
