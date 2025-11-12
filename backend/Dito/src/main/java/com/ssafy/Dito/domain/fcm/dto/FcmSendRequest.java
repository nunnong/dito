package com.ssafy.Dito.domain.fcm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * AI 서버에서 전달받는 FCM 알림 요청 (Data 타입 통일)
 * - FCM 타입은 항상 data로 전송
 * - mission_id가 있으면: Mission 테이블 조회 후 풍부한 정보 전송
 * - mission_id가 없으면: title, message만 전송 (격려 메시지 등)
 */
@Schema(description = "AI 서버 FCM 알림 요청", example = """
{
  "user_id": 123,
  "title": "디토",
  "message": "잠시 휴식을 취해보는 건 어떨까요?",
  "mission_id": 42
}
""")
public record FcmSendRequest(
        @NotNull(message = "user_id는 필수입니다")
        @JsonProperty("user_id")
        @Schema(description = "사용자 DB ID", example = "123")
        Long userId,

        @NotBlank(message = "title은 필수입니다")
        @Schema(description = "알림 제목", example = "디토")
        String title,

        @NotBlank(message = "message는 필수입니다")
        @Schema(description = "알림 메시지 본문", example = "잠시 휴식을 취해보는 건 어떨까요?")
        String message,

        @JsonProperty("mission_id")
        @Schema(description = "미션 ID (선택) - 있으면 Mission 테이블 조회", example = "42")
        Long missionId
) {}
