package com.ssafy.Dito.domain.mission.exception;

import com.ssafy.Dito.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class NotFoundMissionException extends ApiException {
    private static final String message = "미션을 찾을 수 없습니다";

    public NotFoundMissionException() {
        super(HttpStatus.NOT_FOUND, message);
    }
}
