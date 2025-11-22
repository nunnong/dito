package com.ssafy.Dito.domain.ai.report.controller;

import com.ssafy.Dito.domain.ai.report.dto.HeartbeatReq;
import com.ssafy.Dito.domain.ai.report.service.RealtimeActivityService;
import com.ssafy.Dito.global.jwt.util.JwtAuthentication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/ai/activity")
@RequiredArgsConstructor
@Tag(name = "Realtime Activity", description = "실시간 활동 상태 관리 API")
public class RealtimeActivityController {

    private final RealtimeActivityService realtimeActivityService;

    @PostMapping("/heartbeat")
    @Operation(
        summary = "통합 Heartbeat - 실시간 활동 상태 업데이트",
        description = """
            클라이언트는 5초마다 현재 상태만 전송합니다.
            서버에서 마지막 heartbeat와 비교하여 자동으로 사용 시간을 계산합니다.

            - 미디어 세션 정보 (Optional): 미디어 재생 중일 때 포함
            - 현재 앱 정보 (Optional): 포그라운드 앱 정보
            - 타임스탬프 (Required): 현재 시간 (Unix timestamp, milliseconds)

            저장 위치:
            - user_realtime_status: 사용자의 현재 상태 스냅샷
            - daily_user_activities: 일일 활동 집계 데이터 (자동 누적)
            """
    )
    public ResponseEntity<String> heartbeat(
            @Valid @RequestBody HeartbeatReq req) {
        Long userId = JwtAuthentication.getUserId();
        realtimeActivityService.processHeartbeat(userId, req);
        return ResponseEntity.ok("Heartbeat received");
    }
}
