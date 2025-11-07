package com.ssafy.Dito.domain.fcm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ssafy.Dito.domain.fcm.constraint.ValidFcmMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * AI 서버에서 전달받는 FCM 알림 요청 (V2)
 * - intervention_id 제거, mission_id 사용
 * - fcm_type을 통해 notification/data/mixed 메시지 타입 지원
 * - AI 서버는 미션 생성 후 mission_id를 포함하여 FCM 전송 요청
 */
@ValidFcmMessage
@Schema(description = "AI 서버 FCM 알림 요청", example = """
{
  "user_id": "user123",
  "message": "잠시 휴식을 취해보는 건 어떨까요?",
  "mission_id": 42,
  "type": "intervention",
  "fcm_type": "mixed",
  "title": "디토",
  "data": {
    "mission_type": "REST",
    "duration": "300",
    "coin_reward": "10",
    "instruction": "잠시 휴식을 취해보세요"
  }
}
""")
public record FcmSendRequest(
        @NotNull(message = "personalId는 필수입니다")
        @JsonProperty("user_id")
        @Schema(description = "사용자 Personal ID", example = "user123")
        String personalId,

        @NotBlank(message = "message는 필수입니다")
        @Schema(description = "FCM 메시지 본문", example = "잠시 휴식을 취해보는 건 어떨까요?")
        String message,

<<<<<<< Updated upstream
        @NotBlank(message = "interventionId는 필수입니다")
        String interventionId,
=======
        @JsonProperty("mission_id")
        @Schema(description = "AI 서버가 생성한 미션 ID (선택)", example = "42")
        Long missionId,
>>>>>>> Stashed changes

        @NotBlank(message = "type은 필수입니다")
        @Schema(description = "메시지 타입", example = "intervention")
        String type,  // "intervention"

        @NotBlank(message = "fcm_type은 필수입니다")
        @JsonProperty("fcm_type")
        @Schema(description = "FCM 메시지 타입 (notification, data, mixed)", example = "mixed")
        String fcmType,

        @Schema(description = "Notification 제목 (fcm_type이 notification 또는 mixed일 때 필수)", example = "디토")
        String title,

        @Schema(description = "추가 데이터 페이로드 (선택)", example = """
        {
          "mission_type": "REST",
          "duration": "300",
          "coin_reward": "10"
        }
        """)
        Map<String, String> data
) {
    /**
     * FCM 메시지 타입 상수
     */
    public static final String TYPE_NOTIFICATION = "notification";
    public static final String TYPE_DATA = "data";
    public static final String TYPE_MIXED = "mixed";

    /**
     * fcm_type이 notification 또는 mixed 타입인지 확인
     */
    public boolean isNotificationType() {
        return TYPE_NOTIFICATION.equals(fcmType) || TYPE_MIXED.equals(fcmType);
    }

    /**
     * fcm_type이 data 또는 mixed 타입인지 확인
     */
    public boolean isDataType() {
        return TYPE_DATA.equals(fcmType) || TYPE_MIXED.equals(fcmType);
    }
}
