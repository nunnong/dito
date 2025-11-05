package com.ssafy.Dito.domain.weaklyGoal.exception;

import com.ssafy.Dito.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class NotFoundWeeklyGoalException extends ApiException {

    private static final String message = "주간 목표가 존재하지 않습니다.";

    public NotFoundWeeklyGoalException() {
        super(HttpStatus.NOT_FOUND, message);
    }
}
