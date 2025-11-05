package com.ssafy.Dito.domain.groups.exception;

import com.ssafy.Dito.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class GroupNotFoundException extends ApiException {

    private static final String MESSAGE = "그룹 챌린지를 찾을 수 없습니다";

    public GroupNotFoundException() {
        super(HttpStatus.NOT_FOUND, MESSAGE);
    }
}