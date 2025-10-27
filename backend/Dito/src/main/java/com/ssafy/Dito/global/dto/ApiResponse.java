package com.ssafy.Dito.global.dto;

import com.ssafy.Dito.global.dto.response.CreateRes;
import com.ssafy.Dito.global.exception.ApiException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ApiResponse {

    // ------
    // Success
    // ------

    public static ResponseEntity<CommonResult> of(HttpStatus status) {
        return ResponseEntity.status(status).body(new CommonResult(null));
    }

    public static <T> ResponseEntity<SingleResult<T>> of(HttpStatus status, T data) {
        return ResponseEntity.status(status).body(new SingleResult<>(null, data));
    }

    public static <T> ResponseEntity<ListResult<T>> of(HttpStatus status, List<T> data) {
        return ResponseEntity.status(status).body(new ListResult<>(null, data));
    }

    public static <T> ResponseEntity<PageResult<T>> of(HttpStatus status, Page<T> data) {
        return ResponseEntity.status(status).body(new PageResult<>(null, data));
    }

    public static ResponseEntity<CommonResult> of(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new CommonResult(message));
    }

    public static <T> ResponseEntity<SingleResult<T>> of(HttpStatus status, String message, T data) {
        return ResponseEntity.status(status).body(new SingleResult<>(message, data));
    }

    public static <T> ResponseEntity<ListResult<T>> of(HttpStatus status, String message, List<T> data) {
        return ResponseEntity.status(status).body(new ListResult<>(message, data));
    }

    public static <T> ResponseEntity<PageResult<T>> of(HttpStatus status, String message, Page<T> data) {
        return ResponseEntity.status(status).body(new PageResult<>(message, data));
    }

    // ------
    // Failed
    // ------

    public static ResponseEntity<CommonResult> failedOf(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new CommonResult(true, message));
    }

    public static ResponseEntity<CommonResult> failedOf(ApiException e) {
        return ResponseEntity.status(e.status).body(new CommonResult(true, e.getMessage()));
    }

    // ------
    // ETC
    // ------

    public static <T> ResponseEntity<CommonResult> ok() {
        return ApiResponse.of(HttpStatus.OK);
    }

    public static <T> ResponseEntity<SingleResult<T>> ok(T data) {
        return ApiResponse.of(HttpStatus.OK, data);
    }

    public static <T> ResponseEntity<ListResult<T>> ok(List<T> data) {
        return ApiResponse.of(HttpStatus.OK, data);
    }

    public static <T> ResponseEntity<PageResult<T>> ok(Page<T> data) {
        return ApiResponse.of(HttpStatus.OK, data);
    }

    @Deprecated
    public static <T> ResponseEntity<CommonResult> create() {
        return ApiResponse.of(HttpStatus.CREATED);
    }

    public static <T> ResponseEntity<SingleResult<T>> create(T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new SingleResult<>(null, data));
    }

    public static ResponseEntity<SingleResult<CreateRes>> create(Long id) {
        return create(new CreateRes(id));
    }
}
