package com.ssafy.Dito.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials.path:}")
    private String credentialsPath;

    @Value("${firebase.credentials-json:}")
    private String credentialsJson;

    @Value("${firebase.credentials.path:}")
    private Resource credentialsResource;

    @PostConstruct
    public void initialize() {
        try {
            // FirebaseApp이 이미 초기화되었는지 확인
            if (!FirebaseApp.getApps().isEmpty()) {
                log.info("FirebaseApp already initialized");
                return;
            }

            GoogleCredentials credentials;

            // 환경 변수로 전달된 JSON 문자열이 있는 경우 우선 사용 (프로덕션 환경)
            if (credentialsJson != null && !credentialsJson.isBlank()) {
                log.info("Loading Firebase credentials from environment variable");
                InputStream credentialsStream = new ByteArrayInputStream(credentialsJson.getBytes());
                credentials = GoogleCredentials.fromStream(credentialsStream);
            }
            // 파일 경로가 설정된 경우 (로컬 개발 환경)
            else if (credentialsPath != null && !credentialsPath.isBlank() && credentialsResource.exists()) {
                log.info("Loading Firebase credentials from file: {}", credentialsPath);
                credentials = GoogleCredentials.fromStream(credentialsResource.getInputStream());
            }
            // Firebase 설정이 없는 경우
            else {
                log.warn("Firebase credentials not configured. FCM features will be disabled.");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("FirebaseApp initialized successfully");

        } catch (IOException e) {
            log.error("Failed to initialize Firebase", e);
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        // FirebaseApp이 초기화되지 않은 경우 null 반환
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("FirebaseApp not initialized. FirebaseMessaging bean will not be created.");
            return null;
        }
        return FirebaseMessaging.getInstance();
    }
}
