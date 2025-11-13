package com.ssafy.Dito.domain.groups.exception;

import com.ssafy.Dito.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class AlreadyJoinedGroupException extends ApiException {

    private static final String MESSAGE = "이미 참여한 그룹입니다";

    public AlreadyJoinedGroupException() {
        super(HttpStatus.CONFLICT, MESSAGE);
    }
}
