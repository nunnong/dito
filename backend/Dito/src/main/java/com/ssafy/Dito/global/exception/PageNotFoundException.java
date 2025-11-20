package com.ssafy.Dito.global.exception;

import org.springframework.http.HttpStatus;

public class PageNotFoundException extends ApiException {
    private static final String DEFAULT_MESSAGE = "페이지 찾을 수 없습니다";

    public PageNotFoundException() {
        super(HttpStatus.NOT_FOUND, DEFAULT_MESSAGE);
    }

    public PageNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
