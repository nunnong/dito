package com.ssafy.Dito.domain.groups.exception;

import com.ssafy.Dito.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidInviteCodeException extends ApiException {

    private static final String MESSAGE = "유효하지 않은 초대코드입니다";

    public InvalidInviteCodeException() {
        super(HttpStatus.NOT_FOUND, MESSAGE);
    }
}
