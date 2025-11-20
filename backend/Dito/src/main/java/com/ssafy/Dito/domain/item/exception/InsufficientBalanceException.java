package com.ssafy.Dito.domain.item.exception;

import com.ssafy.Dito.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends ApiException {

    private static final String message = "보유 코인이 부족하여 구매할 수 없습니다.";

    public InsufficientBalanceException() {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
