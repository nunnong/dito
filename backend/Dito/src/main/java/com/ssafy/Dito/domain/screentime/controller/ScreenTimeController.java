package com.ssafy.Dito.domain.screentime.controller;

import com.ssafy.Dito.domain.screentime.dto.request.ScreenTimeUpdateReq;
import com.ssafy.Dito.domain.screentime.dto.response.ScreenTimeUpdateRes;
import com.ssafy.Dito.domain.screentime.service.ScreenTimeService;
import com.ssafy.Dito.global.dto.ApiResponse;
import com.ssafy.Dito.global.dto.SingleResult;
import com.ssafy.Dito.global.jwt.util.JwtAuthentication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 스크린타임 관리 컨트롤러
 * - 스크린타임 갱신 (앱에서 5분마다 호출)
 */
@Tag(name = "Screen Time", description = "스크린타임 관리 API")
@RestController
@RequestMapping("/screen-time")
@RequiredArgsConstructor
public class ScreenTimeController {

    private final ScreenTimeService screenTimeService;

    @Operation(
        summary = "스크린타임 갱신",
        description = "사용자의 스크린타임을 갱신합니다. 앱에서 5분마다 호출됩니다.\n\n" +
            "- Summary: 날짜별 집계 데이터 갱신 (upsert)\n" +
            "- Snapshot: 시간대별 기록 저장 (insert)\n" +
            "- 동일 날짜 갱신 시 기존 데이터 업데이트"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "스크린타임 갱신 성공",
            content = @Content(schema = @Schema(implementation = ScreenTimeUpdateRes.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "그룹을 찾을 수 없음"
        )
    })
    @PostMapping("/update")
    public ResponseEntity<SingleResult<ScreenTimeUpdateRes>> updateScreenTime(
        @Valid @RequestBody ScreenTimeUpdateReq request
    ) {
        Long userId = JwtAuthentication.getUserId();
        ScreenTimeUpdateRes response = screenTimeService.updateScreenTime(request, userId);

        return ApiResponse.ok(response);
    }
}
