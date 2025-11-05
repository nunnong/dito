package com.ssafy.Dito.domain.user.userItem.exception;

import com.ssafy.Dito.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class NotFoundUserItemException extends ApiException {

    private static final String message = "보유하고 있는 아이탬이 아닙니다.";

    public NotFoundUserItemException() {
        super(HttpStatus.NOT_FOUND, message);
    }
}
