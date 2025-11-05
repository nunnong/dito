package com.ssafy.Dito.domain.status.exception;

import com.ssafy.Dito.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class NotFoundUserStatusException extends ApiException {
    private static final String message = "유저 스탯 정보를 찾을 수 없습니다.";

    public NotFoundUserStatusException() {
        super(HttpStatus.NOT_FOUND, message);
    }
}
