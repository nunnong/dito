package com.ssafy.Dito.domain.auth.controller;

import com.ssafy.Dito.domain.auth.dto.response.SignInRes;
import com.ssafy.Dito.domain.auth.service.AuthService;
import com.ssafy.Dito.domain.auth.dto.request.SignInReq;
import com.ssafy.Dito.domain.auth.dto.request.SignUpReq;
import com.ssafy.Dito.global.dto.ApiResponse;
import com.ssafy.Dito.global.dto.CommonResult;
import com.ssafy.Dito.global.dto.SingleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "auth", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;


    @Operation(summary = "회원가입", description = "회원가입합니다.")
    @PostMapping("/auth/sign-up")
    public ResponseEntity<CommonResult> signUp(
            @Valid @RequestBody SignUpReq req) {
        authService.signUp(req);
        return ApiResponse.ok();
    }

    @Operation(summary = "아이디 중복 확인", description = "아이디가 중복되는지 확인합니다.")
    @GetMapping("/auth/check/personal-id")
    public ResponseEntity<SingleResult<Boolean>> checkPersonalId(
            @Valid @RequestParam String personalId) {
        boolean exists = authService.checkPersonalId(personalId);
        return ApiResponse.ok(exists);
    }

    @Operation(summary = "로그인", description = "로그인합니다.")
    @PostMapping("/auth/sign-in")
    public ResponseEntity<SingleResult<SignInRes>> signIn(
            @Valid @RequestBody SignInReq req){
        SignInRes res = authService.signIn(req);
        return ApiResponse.ok(res);
    }

    @Operation(summary = "로그아웃", description = "로그아웃합니다.")
    @PostMapping("/logout")
    public ResponseEntity<CommonResult> logout(
            @RequestHeader("Authorization") String accessToken
    ) {
        authService.logout(accessToken);
        return ApiResponse.ok();
    }

    @Operation(summary = "토큰 재발급", description = "액세스 토큰과 리프레시 토큰을 재발급합니다.")
    @PostMapping("/auth/refresh")
    public ResponseEntity<SingleResult<SignInRes>> refresh(
            @RequestHeader("refreshToken") String refreshToken
    ) {
        SignInRes res = authService.refresh(refreshToken);
        return ApiResponse.ok(res);
    }

    @Operation(summary = "회원 탈퇴", description = "회원을 탈퇴합니다.")
    @DeleteMapping("/sign-out")
    public ResponseEntity<CommonResult> deleteUser(
            @RequestHeader("Authorization") String accessToken
    ) {
        authService.deleteUser(accessToken);
        return ApiResponse.ok();
    }
}
