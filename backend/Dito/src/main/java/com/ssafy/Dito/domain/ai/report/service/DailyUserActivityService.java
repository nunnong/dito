package com.ssafy.Dito.domain.ai.report.service;

import com.ssafy.Dito.domain.ai.report.document.DailyUserActivityDocument;
import com.ssafy.Dito.domain.ai.report.dto.DailyActivityQueryRes;
import com.ssafy.Dito.domain.ai.report.dto.DailyActivityReq;
import com.ssafy.Dito.domain.ai.report.dto.DailyActivityRes;
import com.ssafy.Dito.domain.ai.report.dto.ReportRequestReq;
import com.ssafy.Dito.domain.ai.report.dto.ReportRequestRes;
import com.ssafy.Dito.domain.ai.report.repository.DailyUserActivityRepository;
import com.ssafy.Dito.domain.user.entity.User;
import com.ssafy.Dito.domain.user.repository.UserRepository;
import com.ssafy.Dito.global.exception.PageNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing daily user activities
 * Handles MongoDB storage and AI server requests
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyUserActivityService {

    private final DailyUserActivityRepository dailyUserActivityRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${ai.server.url:http://52.78.96.102:8000}")
    private String aiServerUrl;

    private static final DateTimeFormatter ID_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Save daily user activity to MongoDB
     * Creates custom ID format: "YYYYMMDD_userId"
     * Overwrites existing activity if one exists for the same date and user
     *
     * @param req Daily activity request
     * @return Response with activity ID
     */
    public DailyActivityRes saveActivity(DailyActivityReq req) {
        // Generate custom ID: "20251117_23"
        // Convert "yyyy-MM-dd" to "yyyyMMdd" format for ID
        String dateForId = req.date().replace("-", "");
        String activityId = generateActivityId(dateForId, req.userId());

        // Convert request DTO to MongoDB document
        DailyUserActivityDocument document = DailyUserActivityDocument.builder()
            .id(activityId)
            .date(req.date())  // Already String format "yyyy-MM-dd"
            .userId(req.userId())
            .summary(convertSummary(req.summary()))
            .appUsageStats(convertAppUsageStats(req.appUsageStats()))
            .mediaSessions(convertMediaSessions(req.mediaSessions()))
            .build();

        // Save to MongoDB (overwrites if exists)
        DailyUserActivityDocument saved = dailyUserActivityRepository.save(document);

        log.info("Saved daily activity for user {} on date {}: {}", req.userId(), req.date(), activityId);

        return DailyActivityRes.of(saved.getId());
    }

    /**
     * Generate activity ID in format "YYYYMMDD_userId"
     */
    private String generateActivityId(String dateStr, Long userId) {
        return String.format("%s_%d", dateStr, userId);
    }

    /**
     * Convert Summary DTO to Document entity
     */
    private DailyUserActivityDocument.Summary convertSummary(DailyActivityReq.SummaryReq req) {
        if (req == null) {
            return null;
        }
        return DailyUserActivityDocument.Summary.builder()
            .totalAppUsageTime(req.totalAppUsageTime())
            .totalMediaWatchTime(req.totalMediaWatchTime())
            .mostUsedApp(req.mostUsedApp())
            .build();
    }

    /**
     * Convert AppUsageStat DTO list to Document entity list
     */
    private List<DailyUserActivityDocument.AppUsageStat> convertAppUsageStats(List<DailyActivityReq.AppUsageStatReq> reqList) {
        if (reqList == null || reqList.isEmpty()) {
            return List.of();
        }
        return reqList.stream()
            .map(req -> DailyUserActivityDocument.AppUsageStat.builder()
                .appName(req.appName())
                .packageName(req.packageName())
                .totalDuration(req.totalDuration())
                .sessionCount(req.sessionCount())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * Convert MediaSession DTO list to Document entity list
     */
    private List<DailyUserActivityDocument.MediaSession> convertMediaSessions(List<DailyActivityReq.MediaSessionReq> reqList) {
        if (reqList == null || reqList.isEmpty()) {
            return List.of();
        }
        return reqList.stream()
            .map(req -> DailyUserActivityDocument.MediaSession.builder()
                .platform(req.platform())
                .title(req.title())
                .channel(req.channel())
                .timestamp(req.timestamp())
                .watchTime(req.watchTime())
                .videoType(req.videoType())
                .keywords(req.keywords())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * Request AI report generation
     * Sends user_id and date to AI server for report generation
     * AI server will:
     * 1. Create PostgreSQL Report (status: IN_PROGRESS)
     * 2. Fetch data from MongoDB
     * 3. Generate report analysis
     * 4. Update PostgreSQL Report with results
     *
     * @param req Report request (personalId, date)
     * @return AI server response with run_id, thread_id, status
     */
    public ReportRequestRes requestAiReport(ReportRequestReq req) {
        log.info("Requesting AI report for personalId {} on date {}", req.userId(), req.date());

        // Convert personalId to User entity
        User user = userRepository.getByPersonalId(req.userId());
        log.info("Found user with DB ID {} for personalId {}", user.getId(), req.userId());

        // Prepare AI request payload (user DB ID and date)
        Map<String, Object> inputData = Map.of(
            "user_id", user.getId(),
            "date", req.date()
        );

        Map<String, Object> aiRequest = Map.of(
            "assistant_id", "report",
            "input", inputData
        );

        try {
            // Call AI server
            String aiEndpoint = aiServerUrl + "/runs";
            log.info("Calling AI server: {}", aiEndpoint);

            ResponseEntity<Map> aiResponse = restTemplate.postForEntity(
                aiEndpoint,
                aiRequest,
                Map.class
            );

            if (!aiResponse.getStatusCode().is2xxSuccessful()) {
                log.error("AI server error: {}", aiResponse.getStatusCode());
                throw new RuntimeException("AI server error");
            }

            Map<String, Object> aiResult = aiResponse.getBody();
            String runId = (String) aiResult.get("run_id");
            String threadId = (String) aiResult.get("thread_id");
            String status = (String) aiResult.get("status");

            log.info("AI report request initiated - runId: {}, threadId: {}", runId, threadId);

            // Return response
            return ReportRequestRes.of(runId, threadId, status);

        } catch (Exception e) {
            log.error("Failed to call AI server", e);
            throw new RuntimeException("Failed to request AI report: " + e.getMessage());
        }
    }

    /**
     * Get daily user activity by userId and date
     * Retrieves activity data from MongoDB including summary, app usage stats, and media sessions
     *
     * @param userId User ID
     * @param date Activity date
     * @return Daily activity response with complete data
     * @throws PageNotFoundException if activity data not found
     */
    public DailyActivityQueryRes getActivity(Long userId, LocalDate date) {
        log.info("Querying daily activity for user {} on date {}", userId, date);

        return dailyUserActivityRepository.findByUserIdAndDate(userId, date.toString())
            .map(DailyActivityQueryRes::from)
            .orElseThrow(() -> {
                log.warn("Activity data not found for user {} on date {}", userId, date);
                return new PageNotFoundException("활동 데이터를 찾을 수 없습니다");
            });
    }
}
