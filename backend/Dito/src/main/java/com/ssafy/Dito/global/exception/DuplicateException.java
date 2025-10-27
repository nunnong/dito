package com.ssafy.Dito.global.exception;

import org.springframework.http.HttpStatus;

public class DuplicateException extends ApiException {
    private static final String FORMAT            = "이미 존재하는 %s입니다.";
    private static final String FORMAT_WITH_VALUE = "'%s'는 이미 존재하는 %s입니다.";

    public DuplicateException(String name) {
        super(HttpStatus.NOT_FOUND, String.format(FORMAT, name));
    }

    public DuplicateException(String name, String value) {
        super(HttpStatus.NOT_FOUND, String.format(FORMAT_WITH_VALUE, value, name));
    }

}