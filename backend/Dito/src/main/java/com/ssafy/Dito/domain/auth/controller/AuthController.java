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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 회원가입
    @PostMapping("/auth/sign-up")
    public ResponseEntity<CommonResult> signUp(
            @Valid @RequestBody SignUpReq req) {
        authService.signUp(req);
        return ApiResponse.ok();
    }

    // 개인 아이디 중복 확인
    @GetMapping("/auth/check/personal-id")
    public ResponseEntity<SingleResult<Boolean>> checkPersonalId(
            @Valid @RequestParam String personalId) {
        boolean exists = authService.checkPersonalId(personalId);
        return ApiResponse.ok(exists);
    }

    // 로그인
    @PostMapping("/auth/sign-in")
    public ResponseEntity<SingleResult<SignInRes>> signIn(
            @Valid @RequestBody SignInReq req){
        SignInRes response = authService.signIn(req);
        return ApiResponse.ok(response);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<CommonResult> logout(
            @RequestHeader("Authorization") String accessToken
    ) {
        authService.logout(accessToken);
        return ApiResponse.ok();
    }

    // 토큰 재발급
    @PostMapping("/auth/refresh")
    public ResponseEntity<SingleResult<SignInRes>> refresh(
            @RequestHeader("refreshToken") String refreshToken
    ) {
        SignInRes response = authService.refresh(refreshToken);
        return ApiResponse.ok(response);
    }

    // 회원 탈퇴
    @DeleteMapping("/sign-out")
    public ResponseEntity<CommonResult> deleteUser(
            @RequestHeader("Authorization") String accessToken
    ) {
        authService.deleteUser(accessToken);
        return ApiResponse.ok();
    }
}
