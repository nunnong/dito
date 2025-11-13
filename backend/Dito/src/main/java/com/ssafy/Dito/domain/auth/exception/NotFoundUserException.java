package com.ssafy.Dito.domain.auth.exception;

import com.ssafy.Dito.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class NotFoundUserException extends ApiException {
    private static final String message = "사용자를 찾을 수 없습니다";

    public NotFoundUserException() {
        super(HttpStatus.NOT_FOUND, message);
    }
}
