package com.ssafy.Dito.global.handler;

import com.ssafy.Dito.global.dto.ApiResponse;
import com.ssafy.Dito.global.dto.CommonResult;
import com.ssafy.Dito.global.exception.ApiException;
import com.ssafy.Dito.global.exception.ExternalApiException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    // Custom Api Exception
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<CommonResult> handleApiException(ApiException e) {
        e.printStackTrace();
        log.info(e.getMessage());
        return ApiResponse.failedOf(e);
    }

    // ValidException Parsing
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResult> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(
                        fieldError -> fieldError.getField() + ": " + (fieldError
                                .getDefaultMessage() != null ? fieldError.getDefaultMessage() : "")
                )
                .collect(Collectors.joining(", "));

        log.info(e.getMessage());
        return ApiResponse.failedOf(HttpStatus.BAD_REQUEST, message);
    }

    // Request Not Readable
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CommonResult> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.info(e.getMessage());
        return ApiResponse.failedOf(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // 외부 API 호출 에러
    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<CommonResult> handleExternalApiException(ExternalApiException e) {
        log.warn(e.getMessage());
        return ApiResponse.failedOf(e);
    }

    // UnHandled Exception
    @Order
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResult> unHandleException(Exception e) {
        log.error("unhandled exception", e);
        return ApiResponse.failedOf(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
}
