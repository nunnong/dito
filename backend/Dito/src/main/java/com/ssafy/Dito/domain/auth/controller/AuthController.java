package com.ssafy.Dito.domain.auth.controller;

import com.ssafy.Dito.domain.auth.dto.response.SignInRes;
import com.ssafy.Dito.domain.auth.service.AuthService;
import com.ssafy.Dito.domain.auth.dto.request.SignInReq;
import com.ssafy.Dito.domain.auth.dto.request.SignUpReq;
import com.ssafy.Dito.global.dto.ApiResponse;
import com.ssafy.Dito.global.dto.CommonResult;
import com.ssafy.Dito.global.dto.SingleResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 회원가입
    @PostMapping("/sign-up")
    public ResponseEntity<CommonResult> signUp(
        @Valid @RequestBody SignUpReq req
    ) {
        authService.signUp(req);
        return ApiResponse.ok();
    }

    // 개인 아이디 중복 확인
    @GetMapping("/check/personal-id")
    public ResponseEntity<CommonResult> checkPersonalId(
        @Valid @RequestParam String personalId) {
        boolean exists = authService.checkPersonalId(personalId);

        if (exists) {
            return ApiResponse.failedOf(HttpStatus.BAD_REQUEST,"이미 존재하는 ID입니다.");
        } else {
            return ApiResponse.ok();
        }
    }

    // 로그인
    @PostMapping("/sign-in")
    public ResponseEntity<SingleResult<SignInRes>> signIn(
        @Valid @RequestBody SignInReq req){

        SignInRes response = authService.signIn(req);
        return ApiResponse.ok(response);
    }

    // 로그아웃


    // 토큰 재발급

}
