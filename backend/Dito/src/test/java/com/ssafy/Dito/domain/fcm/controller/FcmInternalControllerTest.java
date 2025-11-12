package com.ssafy.Dito.domain.fcm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.Dito.domain.fcm.dto.FcmSendRequest;
import com.ssafy.Dito.domain.fcm.service.FcmService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(FcmInternalController.class)
@DisplayName("FCM Internal Controller 테스트 (Data 타입 통일)")
class FcmInternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FcmService fcmService;

    private String apiKey = "test-api-key";

    @BeforeEach
    void setUp() {
        doNothing().when(fcmService).sendInterventionNotification(any(FcmSendRequest.class));
    }

    @Test
    @DisplayName("mission_id가 있는 FCM 전송 요청 - 성공")
    void testSendWithMissionIdSuccess() throws Exception {
        // given
        Map<String, Object> requestBody = Map.of(
            "user_id", "user123",
            "title", "디토",
            "message", "잠시 휴식을 취해보세요",
            "mission_id", 42
        );

        // when & then
        mockMvc.perform(post("/api/fcm/send")
                .header("X-API-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Notification sent successfully"))
            .andExpect(jsonPath("$.personalId").value("user123"))
            .andExpect(jsonPath("$.missionId").value(42))
            .andExpect(jsonPath("$.hasMission").value(true));

        verify(fcmService, times(1)).sendInterventionNotification(any(FcmSendRequest.class));
    }

    @Test
    @DisplayName("mission_id가 없는 FCM 전송 요청 - 성공")
    void testSendWithoutMissionIdSuccess() throws Exception {
        // given
        Map<String, Object> requestBody = Map.of(
            "user_id", "user123",
            "title", "디토",
            "message", "잘하고 있어요!"
        );

        // when & then
        mockMvc.perform(post("/api/fcm/send")
                .header("X-API-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.personalId").value("user123"))
            .andExpect(jsonPath("$.missionId").value("none"))
            .andExpect(jsonPath("$.hasMission").value(false));

        verify(fcmService, times(1)).sendInterventionNotification(any(FcmSendRequest.class));
    }

    @Test
    @DisplayName("필수 필드 누락 시 400 Bad Request")
    void testMissingRequiredFields() throws Exception {
        // given - title 누락
        Map<String, Object> requestBody = Map.of(
            "user_id", "user123",
            "message", "메시지만 있음"
        );

        // when & then
        mockMvc.perform(post("/api/fcm/send")
                .header("X-API-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isBadRequest());

        verify(fcmService, never()).sendInterventionNotification(any(FcmSendRequest.class));
    }

    @Test
    @DisplayName("Service에서 IllegalArgumentException 발생 시 400 반환")
    void testServiceThrowsIllegalArgumentException() throws Exception {
        // given
        Map<String, Object> requestBody = Map.of(
            "user_id", "user123",
            "title", "디토",
            "message", "메시지",
            "mission_id", 999
        );

        doThrow(new IllegalArgumentException("Mission not found: 999"))
            .when(fcmService).sendInterventionNotification(any(FcmSendRequest.class));

        // when & then
        mockMvc.perform(post("/api/fcm/send")
                .header("X-API-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value(containsString("Mission not found")));
    }

    @Test
    @DisplayName("Service에서 RuntimeException 발생 시 500 반환")
    void testServiceThrowsRuntimeException() throws Exception {
        // given
        Map<String, Object> requestBody = Map.of(
            "user_id", "user123",
            "title", "디토",
            "message", "메시지"
        );

        doThrow(new RuntimeException("FCM 전송 실패: Internal error"))
            .when(fcmService).sendInterventionNotification(any(FcmSendRequest.class));

        // when & then
        mockMvc.perform(post("/api/fcm/send")
                .header("X-API-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value(containsString("Failed to send notification")));
    }

    @Test
    @DisplayName("user_id null 시 400 Bad Request")
    void testNullUserId() throws Exception {
        // given
        Map<String, Object> requestBody = Map.of(
            "title", "디토",
            "message", "메시지"
        );

        // when & then
        mockMvc.perform(post("/api/fcm/send")
                .header("X-API-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isBadRequest());

        verify(fcmService, never()).sendInterventionNotification(any(FcmSendRequest.class));
    }

    @Test
    @DisplayName("빈 title 시 400 Bad Request")
    void testBlankTitle() throws Exception {
        // given
        Map<String, Object> requestBody = Map.of(
            "user_id", "user123",
            "title", "",
            "message", "메시지"
        );

        // when & then
        mockMvc.perform(post("/api/fcm/send")
                .header("X-API-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isBadRequest());

        verify(fcmService, never()).sendInterventionNotification(any(FcmSendRequest.class));
    }

    @Test
    @DisplayName("빈 message 시 400 Bad Request")
    void testBlankMessage() throws Exception {
        // given
        Map<String, Object> requestBody = Map.of(
            "user_id", "user123",
            "title", "디토",
            "message", "   "
        );

        // when & then
        mockMvc.perform(post("/api/fcm/send")
                .header("X-API-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isBadRequest());

        verify(fcmService, never()).sendInterventionNotification(any(FcmSendRequest.class));
    }
}
