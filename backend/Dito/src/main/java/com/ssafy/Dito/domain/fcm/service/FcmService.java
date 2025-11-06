package com.ssafy.Dito.domain.fcm.service;

import com.google.firebase.messaging.*;
import com.ssafy.Dito.domain.fcm.dto.FcmNotificationRequest;
import com.ssafy.Dito.domain.fcm.dto.FcmSendRequest;
import com.ssafy.Dito.domain.mission.dto.request.MissionReq;
import com.ssafy.Dito.domain.mission.entity.Mission;
import com.ssafy.Dito.domain.mission.repository.MissionRepository;
import com.ssafy.Dito.domain.user.entity.User;
import com.ssafy.Dito.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * AI 서버에서 요청하는 개입 알림 전송
     * TECH_SPEC.md:882 - POST /fcm/send 구현
     * TECH_SPEC.md:2136-2156 - FCM 푸시 알림 구조
     *
     * @param request AI 서버 요청 (personalId, message, interventionId, type)
     */
    public void sendInterventionNotification(FcmSendRequest request) {
        // personalId로 User 조회
        User user = userRepository.getByPersonalId(request.personalId());

        // 1. 항상 FCM 알림 전송
        Map<String, String> data = new HashMap<>();
        data.put("type", request.type());
        data.put("intervention_id", request.interventionId());
        data.put("intervention_needed", String.valueOf(request.interventionNeeded()));
        data.put("action", "rest_suggestion");
        data.put("deep_link", "dito://intervention/" + request.interventionId());

        FcmNotificationRequest notificationRequest = new FcmNotificationRequest(
                "디토",  // title
                request.message(),  // body (AI가 보낸 메시지)
                data,
                "high",  // priority
                300  // timeToLive: 5분 (TECH_SPEC.md 참조)
        );

        // FCM 전송 (실패해도 Mission 생성에 영향 없음)
        try {
            sendNotificationToUser(user.getId(), notificationRequest);
        } catch (Exception e) {
            log.error("FCM send failed for intervention {}, but will still create mission if needed: {}",
                    request.interventionId(), e.getMessage());
        }

        // 2. intervention_needed=true일 때만 Mission 생성 (FCM 전송 결과와 무관)
        if (request.interventionNeeded()) {
            createInterventionMission(user, request);
            log.info("Mission created for intervention {}", request.interventionId());
        } else {
            log.info("No mission created - intervention not needed for {}", request.interventionId());
        }
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

    /**
     * 개입에 대한 Mission 생성
     */
    @Transactional
    private void createInterventionMission(User user, FcmSendRequest request) {
        String missionType = determineMissionType(request.interventionType());
        int durationSeconds = 600;  // 10분 기본값
        int coinReward = 10;
        String targetApp = determineTargetApp(request.interventionType());

        MissionReq missionReq = new MissionReq(
                missionType,
                request.message(),  // AI 생성 메시지 (최대 100자)
                coinReward,
                durationSeconds,
                targetApp,
                1, 1, 1,  // stat changes (체력, 정신력, 집중력)
                "AI 개입"
        );

        Mission mission = Mission.of(missionReq, user);
        missionRepository.save(mission);

        log.info("Created mission {} for user {}", mission.getId(), user.getId());
    }

    /**
     * 개입 유형에 따른 미션 타입 결정
     */
    private String determineMissionType(String interventionType) {
        if (interventionType == null) return "REST";

        return switch (interventionType) {
            case "short-form-overuse" -> "REST";
            case "bedtime-usage" -> "SLEEP";
            case "focus-break" -> "FOCUS";
            case "app-switching" -> "FOCUS";
            default -> "REST";
        };
    }

    /**
     * 개입 유형에 따른 타겟 앱 결정
     */
    private String determineTargetApp(String interventionType) {
        // TODO: behavior_log에서 실제 앱 이름 가져오기
        return switch (interventionType) {
            case "short-form-overuse" -> "YouTube Shorts";
            case "bedtime-usage" -> "All Apps";
            default -> "All Apps";
        };
    }
}
