package com.ssafy.Dito.domain.fcm.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.ssafy.Dito.domain.fcm.dto.FcmSendRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FCM Service 테스트")
class FcmServiceTest {

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FcmService fcmService;

    private User testUser;
    private String fcmToken = "test_fcm_token";
    private String personalId = "user123";

    @BeforeEach
    void setUp() {
        testUser = mock(User.class);
        lenient().when(testUser.getFcmToken()).thenReturn(fcmToken);
        lenient().when(testUser.getPersonalId()).thenReturn(personalId);
        lenient().when(testUser.getId()).thenReturn(1L);
    }

    @Test
    @DisplayName("notification 타입 FCM 전송 성공")
    void testSendNotificationOnly() throws Exception {
        // given
        FcmSendRequest request = new FcmSendRequest(
            personalId,
            "잠시 휴식을 취해보세요",
            null,  // missionId
            "intervention",
            FcmSendRequest.TYPE_NOTIFICATION,
            "디토",  // title
            null   // data
        );

        when(userRepository.getByPersonalId(personalId)).thenReturn(testUser);
        when(firebaseMessaging.send(any(Message.class))).thenReturn("message_id_123");

        // when
        fcmService.sendInterventionNotification(request);

        // then
        verify(firebaseMessaging).send(any(Message.class));
        verify(userRepository).getByPersonalId(personalId);
    }

    @Test
    @DisplayName("data 타입 FCM 전송 성공")
    void testSendDataOnly() throws Exception {
        // given
        Map<String, String> data = Map.of(
            "mission_type", "REST",
            "duration", "300",
            "coin_reward", "10"
        );

        FcmSendRequest request = new FcmSendRequest(
            personalId,
            "잠시 휴식을 취해보세요",
            42L,  // missionId
            "intervention",
            FcmSendRequest.TYPE_DATA,
            null,  // title (data 타입은 title 불필요)
            data
        );

        when(userRepository.getByPersonalId(personalId)).thenReturn(testUser);
        when(firebaseMessaging.send(any(Message.class))).thenReturn("message_id_456");

        // when
        fcmService.sendInterventionNotification(request);

        // then
        verify(firebaseMessaging).send(any(Message.class));
        verify(userRepository).getByPersonalId(personalId);
    }

    @Test
    @DisplayName("mixed 타입 FCM 전송 성공")
    void testSendMixedMessage() throws Exception {
        // given
        Map<String, String> data = Map.of(
            "mission_type", "REST",
            "duration", "300"
        );

        FcmSendRequest request = new FcmSendRequest(
            personalId,
            "잠시 휴식을 취해보세요",
            42L,  // missionId
            "intervention",
            FcmSendRequest.TYPE_MIXED,
            "디토",  // title
            data
        );

        when(userRepository.getByPersonalId(personalId)).thenReturn(testUser);
        when(firebaseMessaging.send(any(Message.class))).thenReturn("message_id_789");

        // when
        fcmService.sendInterventionNotification(request);

        // then
        verify(firebaseMessaging).send(any(Message.class));
        verify(userRepository).getByPersonalId(personalId);
    }

    @Test
    @DisplayName("잘못된 fcm_type으로 전송 시 예외 발생")
    void testInvalidFcmType() {
        // given
        FcmSendRequest request = new FcmSendRequest(
            personalId,
            "메시지",
            null,
            "intervention",
            "invalid_type",  // 잘못된 타입
            "디토",
            null
        );

        when(userRepository.getByPersonalId(personalId)).thenReturn(testUser);

        // when & then
        assertThatThrownBy(() -> fcmService.sendInterventionNotification(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid fcm_type: invalid_type");
    }

    @Test
    @DisplayName("FCM 토큰이 없는 사용자일 때 전송 스킵")
    void testNullFcmToken() throws Exception {
        // given
        FcmSendRequest request = new FcmSendRequest(
            personalId,
            "메시지",
            null,
            "intervention",
            FcmSendRequest.TYPE_NOTIFICATION,
            "디토",
            null
        );

        User userWithoutToken = mock(User.class);
        when(userWithoutToken.getFcmToken()).thenReturn(null);
        when(userWithoutToken.getPersonalId()).thenReturn(personalId);
        when(userRepository.getByPersonalId(personalId)).thenReturn(userWithoutToken);

        // when
        fcmService.sendInterventionNotification(request);

        // then
        verify(firebaseMessaging, never()).send(any(Message.class));
    }

    @Test
    @DisplayName("FCM 전송 실패 시 예외 처리")
    void testFcmSendFailure() throws Exception {
        // given
        FcmSendRequest request = new FcmSendRequest(
            personalId,
            "메시지",
            null,
            "intervention",
            FcmSendRequest.TYPE_NOTIFICATION,
            "디토",
            null
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
            "unknown_user",
            "메시지",
            null,
            "intervention",
            FcmSendRequest.TYPE_NOTIFICATION,
            "디토",
            null
        );

        when(userRepository.getByPersonalId("unknown_user"))
            .thenThrow(new IllegalArgumentException("User not found"));

        // when & then
        assertThatThrownBy(() -> fcmService.sendInterventionNotification(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User not found");
    }
}