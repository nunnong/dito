package com.ssafy.Dito.global.exception;

import org.springframework.http.HttpStatus;

public class PageNotFoundException extends ApiException {
    private static final String message = "페이지 찾을 수 없습니다";

    public PageNotFoundException() {
        super(HttpStatus.NOT_FOUND, message);
    }
}
