package com.ssafy.Dito.domain.log.mediaSessionEvent.service;

import com.ssafy.Dito.domain.log.mediaSessionEvent.document.MediaSessionEventDocument;
import com.ssafy.Dito.domain.log.mediaSessionEvent.dto.request.MediaSessionEventBatchReq;
import com.ssafy.Dito.domain.log.mediaSessionEvent.dto.request.MediaSessionEventReq;
import com.ssafy.Dito.domain.log.mediaSessionEvent.dto.response.MediaSessionEventRes;
import com.ssafy.Dito.domain.log.mediaSessionEvent.entity.EventType;
import com.ssafy.Dito.domain.log.mediaSessionEvent.repository.MediaSessionLogRepository;
import com.ssafy.Dito.global.jwt.util.JwtAuthentication;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
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

        List<MediaSessionEventDocument> documentsToInsert = new ArrayList<>();
        int processedCount = 0;

        for (MediaSessionEventReq eventReq : req.mediaSessionEvent()) {
            boolean handled = false;

            if (shouldUpdateWatchTime(eventReq)) {
                MediaSessionEventDocument pendingSession = findPendingSession(documentsToInsert, eventReq);
                if (pendingSession != null) {
                    pendingSession.updateWatchTime(eventReq.watchTime(), eventReq.eventTimestamp());
                    processedCount++;
                    handled = true;
                } else {
                    Optional<MediaSessionEventDocument> existingSession = mediaSessionLogRepository
                        .findFirstByUserIdAndTitleAndEventDateAndEventTypeOrderByEventTimestampDesc(
                            userId, eventReq.title(), eventReq.eventDate(), eventReq.eventType()
                        );
                    if (existingSession.isPresent()) {
                        MediaSessionEventDocument document = existingSession.get();
                        document.updateWatchTime(eventReq.watchTime(), eventReq.eventTimestamp());
                        mediaSessionLogRepository.save(document);
                        log.debug("Updated watch time to {} for '{}' on {}", eventReq.watchTime(),
                            eventReq.title(), eventReq.eventDate());
                        processedCount++;
                        handled = true;
                    }
                }
            }

            if (!handled) {
                documentsToInsert.add(MediaSessionEventDocument.of(eventReq, userId));
            }
        }

        // Save to MongoDB
        if (!documentsToInsert.isEmpty()) {
            mediaSessionLogRepository.saveAll(documentsToInsert);
            log.debug("Saved {} media session events to MongoDB for user {}", documentsToInsert.size(), userId);
            processedCount += documentsToInsert.size();
        }

        return new MediaSessionEventRes(processedCount);
    }

    private boolean shouldUpdateWatchTime(MediaSessionEventReq eventReq) {
        return eventReq != null
            && EventType.VIDEO_START.equals(eventReq.eventType())
            && eventReq.watchTime() != null
            && eventReq.title() != null
            && eventReq.eventDate() != null;
    }

    private MediaSessionEventDocument findPendingSession(List<MediaSessionEventDocument> pendingDocuments,
                                                         MediaSessionEventReq eventReq) {
        return pendingDocuments.stream()
            .filter(doc -> doc.getEventType() == eventReq.eventType())
            .filter(doc -> Objects.equals(doc.getTitle(), eventReq.title()))
            .filter(doc -> Objects.equals(doc.getEventDate(), eventReq.eventDate()))
            .findFirst()
            .orElse(null);
    }
}
