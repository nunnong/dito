package com.ssafy.Dito.domain.user.controller;

import com.ssafy.Dito.domain.user.dto.request.FrequencyReq;
import com.ssafy.Dito.domain.user.dto.request.NicknameReq;
import com.ssafy.Dito.domain.user.dto.response.ProfileRes;
import com.ssafy.Dito.domain.user.service.UserService;
import com.ssafy.Dito.global.dto.ApiResponse;
import com.ssafy.Dito.global.dto.CommonResult;
import com.ssafy.Dito.global.dto.SingleResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    // 프로필 조회
    @GetMapping
    public ResponseEntity<SingleResult<ProfileRes>> getProfile() {
        ProfileRes res = userService.getProfile();
        return ApiResponse.ok(res);
    }

    // 닉네임 수정
    @PatchMapping
    public ResponseEntity<CommonResult> updateNickname(
        @Valid @RequestBody NicknameReq req
    ){
        userService.updateNickname(req);
        return ApiResponse.ok();
    }

    // 미션 빈도 수정
    @PatchMapping("/frequency")
    public ResponseEntity<CommonResult> updateFrequency(
        @RequestBody FrequencyReq req
    ){
        userService.updateFrequency(req);
        return ApiResponse.ok();
    }

    // 회원 탈퇴
    @DeleteMapping("/delete")
    public ResponseEntity<CommonResult> deleteUser() {
        userService.deleteUser();
        return ApiResponse.ok();
    }
}
