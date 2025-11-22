package com.ssafy.Dito.domain.ai.report.controller;

import com.ssafy.Dito.domain.ai.report.dto.RealtimeActivityReq;
import com.ssafy.Dito.domain.ai.report.dto.RealtimeUsageReq;
import com.ssafy.Dito.domain.ai.report.service.RealtimeActivityService;
import com.ssafy.Dito.global.jwt.util.JwtAuthentication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
        summary = "실시간 활동 상태 업데이트 (Heartbeat)",
        description = """
            사용자의 현재 미디어 시청 상태를 5초 주기로 업데이트합니다.
            MongoDB의 user_realtime_status 컬렉션에 저장됩니다.
            """
    )
    public ResponseEntity<String> heartbeat(
            @RequestBody RealtimeActivityReq req) {
        Long userId = JwtAuthentication.getUserId();
        realtimeActivityService.updateRealtimeStatus(userId, req);
        return ResponseEntity.ok("Heartbeat received");
    }

    @PostMapping("/usage/heartbeat")
    public ResponseEntity<String> usageHeartbeat(
            @RequestBody RealtimeUsageReq req) {
        Long userId = JwtAuthentication.getUserId();
        realtimeActivityService.updateRealtimeUsage(userId, req);
        return ResponseEntity.ok("Usage heartbeat received");
    }
}
