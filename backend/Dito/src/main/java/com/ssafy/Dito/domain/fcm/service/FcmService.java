package com.ssafy.Dito.domain.fcm.service;

import com.google.firebase.messaging.*;
import com.ssafy.Dito.domain.fcm.dto.FcmNotificationRequest;
import com.ssafy.Dito.domain.fcm.dto.FcmSendRequest;
import com.ssafy.Dito.domain.log.fcm.document.FcmLogDocument;
import com.ssafy.Dito.domain.log.fcm.repository.FcmLogRepository;
import com.ssafy.Dito.domain.mission.entity.Mission;
import com.ssafy.Dito.domain.mission.repository.MissionRepository;
import com.ssafy.Dito.domain.user.entity.User;
import com.ssafy.Dito.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final FirebaseMessaging firebaseMessaging;
    private final UserRepository userRepository;
    private final MissionRepository missionRepository;
    private final FcmLogRepository fcmLogRepository;

    /**
     * 특정 사용자에게 FCM 알림 전송
     *
     * @param userId  사용자 ID
     * @param request 알림 요청 정보
     */
    @Transactional
    public void sendNotificationToUser(Long userId, FcmNotificationRequest request) {
        // FirebaseMessaging이 초기화되지 않은 경우
        if (firebaseMessaging == null) {
            log.warn("FirebaseMessaging is not initialized. Skipping notification.");
            return;
        }

        // 1. User 조회 및 fcmToken 가져오기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        String fcmToken = user.getFcmToken();
        if (fcmToken == null || fcmToken.isBlank()) {
            log.warn("User {} has no FCM token. Skipping notification.", userId);
            return;
        }

        try {
            // 2. Message 객체 생성
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(request.title())
                            .setBody(request.body())
                            .build())
                    .putAllData(request.data())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(getPriority(request.priority()))
                            .setTtl(request.timeToLive() * 1000L)  // 초를 밀리초로 변환
                            .build())
                    .build();

            // 3. firebaseMessaging.send(message)
            String response = firebaseMessaging.send(message);
            log.info("Successfully sent notification to user {}. Response: {}", userId, response);

        } catch (FirebaseMessagingException e) {
            // 4. 실패 처리 (토큰 무효화 등)
            handleMessagingException(e, user);
        }
    }

    /**
     * AI 서버에서 요청하는 개입 알림 전송 (Data 타입 통일)
     * - FCM 타입은 항상 data로 전송
     * - mission_id가 있으면: Mission 테이블 조회 후 풍부한 정보 전송
     * - mission_id가 없으면: title, message만 전송
     *
     * @param request AI 서버 요청 (personalId, title, message, missionId)
     */
    public void sendInterventionNotification(FcmSendRequest request) {
        // 1. 사용자 조회
        User user = userRepository.getById(request.userId());

        if (user.getFcmToken() == null || user.getFcmToken().isBlank()) {
            log.warn("User {} (ID: {}) has no FCM token. Skipping notification.",
                    user.getPersonalId(), user.getId());

            // Log failure - no FCM token
            FcmLogDocument fcmLog = FcmLogDocument.builder()
                    .userId(user.getId())
                    .personalId(user.getPersonalId())
                    .title(request.title())
                    .message(request.message())
                    .missionId(request.missionId())
                    .hasMission(request.missionId() != null)
                    .fcmToken(null)
                    .success(false)
                    .errorCode("NO_FCM_TOKEN")
                    .errorMessage("User has no FCM token")
                    .sentAt(LocalDateTime.now())
                    .build();
            fcmLogRepository.save(fcmLog);
            return;
        }

        // 2. Data payload 구성
        Map<String, String> data = buildDataPayload(request);

        // 3. Create log document before sending
        FcmLogDocument fcmLog = FcmLogDocument.builder()
                .userId(user.getId())
                .personalId(user.getPersonalId())
                .title(request.title())
                .message(request.message())
                .missionId(request.missionId())
                .hasMission(request.missionId() != null)
                .fcmToken(user.getFcmToken())
                .success(false)  // Default to false, will update on success
                .sentAt(LocalDateTime.now())
                .build();

        // 4. FCM 전송
        try {
            String response = sendDataMessage(user, data);

            // Mark as success
            fcmLog.markSuccess(response);
            fcmLogRepository.save(fcmLog);

            log.info("FCM sent successfully - user: {} (ID: {}), missionId: {}, hasMission: {}, response: {}",
                    user.getPersonalId(),
                    request.userId(),
                    request.missionId() != null ? request.missionId() : "none",
                    request.missionId() != null,
                    response);

        } catch (FirebaseMessagingException e) {
            // Mark as failure
            String errorCode = e.getMessagingErrorCode() != null
                    ? e.getMessagingErrorCode().name()
                    : "UNKNOWN";
            fcmLog.markFailure(errorCode, e.getMessage());
            fcmLogRepository.save(fcmLog);

            log.error("FCM send failed - user: {} (ID: {}), missionId: {}, error: {}",
                    user.getPersonalId(),
                    request.userId(),
                    request.missionId() != null ? request.missionId() : "none",
                    e.getMessage(), e);
            handleMessagingException(e, user);
            throw new RuntimeException("FCM 전송 실패: " + e.getMessage(), e);
        }
    }

    /**
     * FCM data payload 구성
     * - mission_id가 있으면: Mission 테이블 조회해서 미션 정보 추가
     * - mission_id가 없으면: title, message만 포함
     */
    private Map<String, String> buildDataPayload(FcmSendRequest request) {
        Map<String, String> data = new HashMap<>();

        // 기본 정보 (항상 포함)
        data.put("title", request.title());
        data.put("message", request.message());

        // FCM 타입 추가 (intervention | evaluation)
        if (request.type() != null && !request.type().isBlank()) {
            data.put("type", request.type());
        }

        // 미션 ID가 있으면 Mission 정보 조회 및 추가
        if (request.missionId() != null) {
            Mission mission = missionRepository.findById(request.missionId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Mission not found: " + request.missionId()));

            data.put("mission_id", String.valueOf(mission.getId()));
            data.put("mission_type", mission.getMissionType());
            data.put("duration_seconds", String.valueOf(mission.getDurationSeconds()));
            data.put("coin_reward", String.valueOf(mission.getCoinReward()));
            data.put("deep_link", "dito://mission/" + mission.getId());
        }
        // mission_id가 없으면 미션 관련 필드는 아예 포함하지 않음

        return data;
    }

    /**
     * Data 타입 FCM 전송
     */
    private String sendDataMessage(User user, Map<String, String> data)
            throws FirebaseMessagingException {

        log.info("=== FCM Data Message ===");
        log.info("User: {} (ID: {})", user.getPersonalId(), user.getId());
        log.info("Data Payload: {}", data);
        log.info("Has Mission: {}", data.containsKey("mission_id"));
        log.info("Android Config - Priority: HIGH, TTL: 300s");

        Message message = Message.builder()
                .setToken(user.getFcmToken())
                .putAllData(data)
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setTtl(300 * 1000L)  // 5분 TTL
                        .build())
                .build();

        String response = firebaseMessaging.send(message);
        log.info("Firebase Response - Message ID: {}", response);
        log.info("========================");
        return response;
    }

    /**
     * 다중 사용자에게 알림 전송 (향후 확장)
     *
     * @param userIds 사용자 ID 목록
     * @param request 알림 요청 정보
     */
    @Transactional
    public void sendNotificationToMultipleUsers(List<Long> userIds, FcmNotificationRequest request) {
        if (firebaseMessaging == null) {
            log.warn("FirebaseMessaging is not initialized. Skipping notification.");
            return;
        }

        // 사용자들의 FCM 토큰 수집
        List<String> tokens = userRepository.findAllById(userIds).stream()
                .map(User::getFcmToken)
                .filter(token -> token != null && !token.isBlank())
                .toList();

        if (tokens.isEmpty()) {
            log.warn("No valid FCM tokens found for users: {}", userIds);
            return;
        }

        try {
            // MulticastMessage 사용 (최대 500개 토큰)
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(Notification.builder()
                            .setTitle(request.title())
                            .setBody(request.body())
                            .build())
                    .putAllData(request.data())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(getPriority(request.priority()))
                            .setTtl(request.timeToLive() * 1000L)  // 초를 밀리초로 변환
                            .build())
                    .build();

            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
            log.info("Successfully sent {} notifications. {} failures.",
                    response.getSuccessCount(), response.getFailureCount());

            // 실패한 토큰 처리
            if (response.getFailureCount() > 0) {
                handleBatchFailures(response, tokens, userIds);
            }

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send multicast notification", e);
        }
    }

    /**
     * 토픽 기반 알림 전송 (향후 확장)
     *
     * @param topic   토픽 이름
     * @param request 알림 요청 정보
     */
    public void sendNotificationToTopic(String topic, FcmNotificationRequest request) {
        if (firebaseMessaging == null) {
            log.warn("FirebaseMessaging is not initialized. Skipping notification.");
            return;
        }

        try {
            Message message = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder()
                            .setTitle(request.title())
                            .setBody(request.body())
                            .build())
                    .putAllData(request.data())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(getPriority(request.priority()))
                            .setTtl(request.timeToLive() * 1000L)  // 초를 밀리초로 변환
                            .build())
                    .build();

            String response = firebaseMessaging.send(message);
            log.info("Successfully sent notification to topic {}. Response: {}", topic, response);

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send notification to topic {}", topic, e);
        }
    }

    /**
     * 우선순위 문자열을 AndroidConfig.Priority로 변환
     */
    private AndroidConfig.Priority getPriority(String priority) {
        return "high".equalsIgnoreCase(priority)
                ? AndroidConfig.Priority.HIGH
                : AndroidConfig.Priority.NORMAL;
    }

    /**
     * FCM 전송 실패 처리
     * - 잘못된 토큰 감지 시 DB에서 제거
     */
    @Transactional
    protected void handleMessagingException(FirebaseMessagingException e, User user) {
        String errorCode = e.getMessagingErrorCode().name();
        log.error("FCM messaging error for user {}: {}", user.getId(), errorCode, e);

        // 토큰이 유효하지 않거나 등록되지 않은 경우
        if (errorCode.equals("INVALID_ARGUMENT") ||
                errorCode.equals("UNREGISTERED") ||
                errorCode.equals("SENDER_ID_MISMATCH")) {

            log.warn("Invalidating FCM token for user {}", user.getId());
            user.updateFcmToken(null);  // 토큰 무효화
            userRepository.save(user);
        }
    }

    /**
     * 배치 전송 실패 처리
     */
    @Transactional
    protected void handleBatchFailures(BatchResponse response, List<String> tokens, List<Long> userIds) {
        for (int i = 0; i < response.getResponses().size(); i++) {
            SendResponse sendResponse = response.getResponses().get(i);
            if (!sendResponse.isSuccessful()) {
                String token = tokens.get(i);
                Long userId = userIds.get(i);

                FirebaseMessagingException exception = sendResponse.getException();
                if (exception != null) {
                    log.error("Failed to send notification to user {}: {}",
                            userId, exception.getMessagingErrorCode());

                    // 토큰 무효화
                    userRepository.findById(userId).ifPresent(user -> {
                        if (token.equals(user.getFcmToken())) {
                            user.updateFcmToken(null);
                            userRepository.save(user);
                        }
                    });
                }
            }
        }
    }
}
