package com.ssafy.Dito.domain.groups.exception;

import com.ssafy.Dito.global.exception.BadRequestException;

public class InsufficientCoinsException extends BadRequestException {

    public InsufficientCoinsException(int required, int available) {
        super(String.format("코인이 부족합니다. 필요: %d, 보유: %d", required, available));
    }
}
