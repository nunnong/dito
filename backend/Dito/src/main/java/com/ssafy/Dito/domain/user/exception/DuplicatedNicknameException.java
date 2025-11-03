package com.ssafy.Dito.domain.user.exception;

import com.ssafy.Dito.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class DuplicatedNicknameException extends ApiException {

    private static final String message = "현재 사용하고 있는 닉네임입니다.";

    public DuplicatedNicknameException() {
        super(HttpStatus.CONFLICT, message);
    }
}
