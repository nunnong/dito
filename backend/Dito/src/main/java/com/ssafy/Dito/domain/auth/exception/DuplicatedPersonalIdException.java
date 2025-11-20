package com.ssafy.Dito.domain.auth.exception;

import com.ssafy.Dito.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class DuplicatedPersonalIdException extends ApiException {

    private static final String message = "이미 사용중인 아이디입니다.";

    public DuplicatedPersonalIdException() {
        super(HttpStatus.CONFLICT, message);
    }
}
