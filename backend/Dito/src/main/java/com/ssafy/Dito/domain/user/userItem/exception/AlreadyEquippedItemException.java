package com.ssafy.Dito.domain.user.userItem.exception;

import com.ssafy.Dito.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class AlreadyEquippedItemException extends ApiException {

    private static final String message = "이미 착용 중인 아이템입니다.";

    public AlreadyEquippedItemException() {
        super(HttpStatus.CONFLICT, message);
    }
}

