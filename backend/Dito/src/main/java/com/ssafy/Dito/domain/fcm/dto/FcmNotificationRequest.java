package com.ssafy.Dito.domain.fcm.dto;

import java.util.Map;

/**
 * 내부 FCM 알림 전송 요청
 */
public record FcmNotificationRequest(
        String title,
        String body,
        Map<String, String> data,
        String priority,  // "high", "normal"
        Integer timeToLive  // seconds (TTL)
) {
    /**
     * 기본값을 가진 생성자
     */
    public FcmNotificationRequest(String title, String body, Map<String, String> data) {
        this(title, body, data, "high", 3600);  // 기본 TTL: 1시간
    }

    /**
     * 우선순위를 지정한 생성자
     */
    public FcmNotificationRequest(String title, String body, Map<String, String> data, String priority) {
        this(title, body, data, priority, 3600);
    }
}
