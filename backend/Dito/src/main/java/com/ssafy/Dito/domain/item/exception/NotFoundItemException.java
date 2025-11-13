package com.ssafy.Dito.domain.item.exception;

import com.ssafy.Dito.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class NotFoundItemException extends ApiException {
    private static final String message = "아이탬을 찾을 수 없습니다";

    public NotFoundItemException() {
        super(HttpStatus.NOT_FOUND, message);
    }
}
