package com.ssafy.Dito.domain.log.mediaSessionEvent.service;

import com.ssafy.Dito.domain.log.mediaSessionEvent.document.MediaSessionEventDocument;
import com.ssafy.Dito.domain.log.mediaSessionEvent.dto.request.MediaSessionEventBatchReq;
import com.ssafy.Dito.domain.log.mediaSessionEvent.dto.response.MediaSessionEventRes;
import com.ssafy.Dito.domain.log.mediaSessionEvent.repository.MediaSessionLogRepository;
import com.ssafy.Dito.global.jwt.util.JwtAuthentication;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Media session event service - MongoDB implementation
 * Stores media playback logs in MongoDB for behavioral analysis and AI intervention
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaSessionEventService {

    private final MediaSessionLogRepository mediaSessionLogRepository;

    /**
     * Save media session events to MongoDB
     * @param req Batch request containing multiple media session events
     * @return Response with count of saved events
     */
    public MediaSessionEventRes saveMediaSessionEvent(MediaSessionEventBatchReq req) {
        long userId = JwtAuthentication.getUserId();

        // Convert DTOs to MongoDB documents
        List<MediaSessionEventDocument> documents = req.mediaSessionEvent().stream()
            .map(e -> MediaSessionEventDocument.of(e, userId))
            .collect(Collectors.toList());

        // Save to MongoDB
        if (!documents.isEmpty()) {
            mediaSessionLogRepository.saveAll(documents);
            log.debug("Saved {} media session events to MongoDB for user {}", documents.size(), userId);
        }

        return new MediaSessionEventRes(documents.size());
    }
}
