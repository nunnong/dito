package com.ssafy.Dito.domain.log.appUsageEvent.service;

import com.ssafy.Dito.domain.log.appUsageEvent.document.AppUsageLogDocument;
import com.ssafy.Dito.domain.log.appUsageEvent.dto.request.AppUsageEventBatchReq;
import com.ssafy.Dito.domain.log.appUsageEvent.dto.response.AppUsageEventRes;
import com.ssafy.Dito.domain.log.appUsageEvent.repository.AppUsageLogRepository;
import com.ssafy.Dito.global.jwt.util.JwtAuthentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * App usage event service - MongoDB implementation
 * Stores app usage logs in MongoDB for time-series analytics and AI agent consumption
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppUsageEventService {

    private final AppUsageLogRepository appUsageLogRepository;

    /**
     * Save app usage events to MongoDB
     * @param req Batch request containing multiple app usage events
     * @return Response with count of saved events
     */
    public AppUsageEventRes saveAppUsageEvent(AppUsageEventBatchReq req) {
        long userId = JwtAuthentication.getUserId();

        // Convert DTOs to MongoDB documents
        List<AppUsageLogDocument> documents = req.appUsageEvent().stream()
            .map(e -> AppUsageLogDocument.of(e, userId))
            .collect(Collectors.toList());

        // Save to MongoDB
        if (!documents.isEmpty()) {
            appUsageLogRepository.saveAll(documents);
            log.debug("Saved {} app usage events to MongoDB for user {}", documents.size(), userId);
        }

        return new AppUsageEventRes(documents.size());
    }
}
