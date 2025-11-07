package com.ssafy.Dito.domain.fcm.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.ssafy.Dito.domain.fcm.dto.FcmSendRequest;
import com.ssafy.Dito.domain.mission.entity.Mission;
import com.ssafy.Dito.domain.mission.repository.MissionRepository;
import com.ssafy.Dito.domain.user.entity.User;
import com.ssafy.Dito.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FCM Service 테스트 (Data 타입 통일)")
class FcmServiceTest {

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MissionRepository missionRepository;

    @InjectMocks
    private FcmService fcmService;

    private User testUser;
    private Mission testMission;
    private String fcmToken = "test_fcm_token";
    private String personalId = "user123";

    @BeforeEach
    void setUp() {
        testUser = mock(User.class);
        when(testUser.getFcmToken()).thenReturn(fcmToken);
        when(testUser.getPersonalId()).thenReturn(personalId);
        when(testUser.getId()).thenReturn(1L);

        testMission = mock(Mission.class);
        when(testMission.getId()).thenReturn(42L);
        when(testMission.getMissionType()).thenReturn("REST");
        when(testMission.getDurationSeconds()).thenReturn(300);
        when(testMission.getCoinReward()).thenReturn(10);
    }

    @Test
    @DisplayName("mission_id가 있는 FCM 전송 - Mission DB 조회")
    void testSendWithMissionId() throws Exception {
        // given
        FcmSendRequest request = new FcmSendRequest(
            personalId,
            "디토",
            "잠시 휴식을 취해보세요",
            42L
        );

        when(userRepository.getByPersonalId(personalId)).thenReturn(testUser);
        when(missionRepository.findById(42L)).thenReturn(Optional.of(testMission));
        when(firebaseMessaging.send(any(Message.class))).thenReturn("msg_123");

        // when
        fcmService.sendInterventionNotification(request);

        // then
        verify(missionRepository).findById(42L);
        verify(firebaseMessaging).send(any(Message.class));
    }

    @Test
    @DisplayName("mission_id가 없는 FCM 전송 - 기본 정보만")
    void testSendWithoutMissionId() throws Exception {
        // given
        FcmSendRequest request = new FcmSendRequest(
            personalId,
            "디토",
            "잘하고 있어요!",
            null  // mission_id 없음
        );

        when(userRepository.getByPersonalId(personalId)).thenReturn(testUser);
        when(firebaseMessaging.send(any(Message.class))).thenReturn("msg_456");

        // when
        fcmService.sendInterventionNotification(request);

        // then
        verify(missionRepository, never()).findById(any());  // DB 조회 안함
        verify(firebaseMessaging).send(any(Message.class));
    }

    @Test
    @DisplayName("존재하지 않는 mission_id로 전송 시 예외")
    void testSendWithInvalidMissionId() {
        // given
        FcmSendRequest request = new FcmSendRequest(
            personalId, "디토", "메시지", 999L
        );

        when(userRepository.getByPersonalId(personalId)).thenReturn(testUser);
        when(missionRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fcmService.sendInterventionNotification(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Mission not found: 999");
    }

    @Test
    @DisplayName("FCM 토큰이 없는 사용자일 때 전송 스킵")
    void testNullFcmToken() throws Exception {
        // given
        FcmSendRequest request = new FcmSendRequest(
            personalId, "디토", "메시지", null
        );

        User userWithoutToken = mock(User.class);
        when(userWithoutToken.getFcmToken()).thenReturn(null);
        when(userWithoutToken.getPersonalId()).thenReturn(personalId);
        when(userRepository.getByPersonalId(personalId)).thenReturn(userWithoutToken);

        // when
        fcmService.sendInterventionNotification(request);

        // then
        verify(firebaseMessaging, never()).send(any(Message.class));
        verify(missionRepository, never()).findById(any());
    }

    @Test
    @DisplayName("FCM 전송 실패 시 예외 처리")
    void testFcmSendFailure() throws Exception {
        // given
        FcmSendRequest request = new FcmSendRequest(
            personalId, "디토", "메시지", null
        );

        FirebaseMessagingException exception = mock(FirebaseMessagingException.class);
        when(exception.getMessage()).thenReturn("FCM error");
        when(exception.getMessagingErrorCode()).thenReturn(MessagingErrorCode.INTERNAL);

        when(userRepository.getByPersonalId(personalId)).thenReturn(testUser);
        when(firebaseMessaging.send(any(Message.class))).thenThrow(exception);

        // when & then
        assertThatThrownBy(() -> fcmService.sendInterventionNotification(request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("FCM 전송 실패");
    }

    @Test
    @DisplayName("사용자를 찾을 수 없을 때 예외 발생")
    void testUserNotFound() {
        // given
        FcmSendRequest request = new FcmSendRequest(
            "unknown_user", "디토", "메시지", null
        );

        when(userRepository.getByPersonalId("unknown_user"))
            .thenThrow(new IllegalArgumentException("User not found"));

        // when & then
        assertThatThrownBy(() -> fcmService.sendInterventionNotification(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Deep link 생성 확인")
    void testDeepLinkGeneration() throws Exception {
        // given
        FcmSendRequest request = new FcmSendRequest(
            personalId, "디토", "미션 시작!", 123L
        );

        when(userRepository.getByPersonalId(personalId)).thenReturn(testUser);
        when(missionRepository.findById(123L)).thenReturn(Optional.of(testMission));
        when(testMission.getId()).thenReturn(123L);
        when(firebaseMessaging.send(any(Message.class))).thenReturn("msg_789");

        // when
        fcmService.sendInterventionNotification(request);

        // then
        verify(firebaseMessaging).send(any(Message.class));
        verify(missionRepository).findById(123L);
    }
}
