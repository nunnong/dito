package com.ssafy.Dito.global.dto;

import lombok.Getter;

@Getter
public class CommonResult {
    private final boolean success;
    private final boolean error;
    private final String  message;

    public CommonResult(boolean error, String message) {
        this.success = !error;
        this.error = error;
        this.message = message;
    }

    public CommonResult(String message) {
        this.success = true;
        this.error = false;
        this.message = message;
    }
}
