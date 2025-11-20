package com.ssafy.Dito.domain.report.exception;

import com.ssafy.Dito.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ReportNotFoundException extends ApiException {

    private static final String message = "리포트를 찾을 수 없습니다.";

    public ReportNotFoundException() {
        super(HttpStatus.NOT_FOUND, message);
    }
}
