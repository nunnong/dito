package com.ssafy.Dito.global.exception;

import org.springframework.http.HttpStatus;

public class ExternalApiException extends ApiException {

    public ExternalApiException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public ExternalApiException(HttpStatus status, String message) {
        super(status, message);
    }

}
