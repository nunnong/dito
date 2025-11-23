package com.ssafy.Dito.domain.report.service;

import com.ssafy.Dito.domain.ai.report.document.DailyUserActivityDocument;
import com.ssafy.Dito.domain.ai.report.repository.DailyUserActivityRepository;
import com.ssafy.Dito.domain.report.dto.request.ReportReq;
import com.ssafy.Dito.domain.report.dto.request.ReportUpdateReq;
import com.ssafy.Dito.domain.report.dto.response.ReportRes;
import com.ssafy.Dito.domain.report.dto.response.VideoFeedbackItem;
import com.ssafy.Dito.domain.report.entity.Report;
import com.ssafy.Dito.domain.report.repository.ReportRepository;
import com.ssafy.Dito.domain.user.entity.User;
import com.ssafy.Dito.domain.user.repository.UserRepository;
import com.ssafy.Dito.domain.youtube.entity.YoutubeVideo;
import com.ssafy.Dito.domain.youtube.service.YoutubeVideoService;
import com.ssafy.Dito.global.jwt.util.JwtAuthentication;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final DailyUserActivityRepository dailyUserActivityRepository;
    private final YoutubeVideoService youtubeVideoService;

    @Transactional(readOnly = true)
    public ReportRes getLatestReport() {
        long userId = JwtAuthentication.getUserId();
        Report report = reportRepository.getLatestByUserId(userId);
        return ReportRes.from(report);
    }

    @Transactional
    public ReportRes createReportForAi(ReportReq req) {
        User user = userRepository.getById(req.userId());

        Report report = Report.of(
            user,
            req.reportOverview(),
            req.insights(),
            req.strategy(),
            req.advice(),
            req.missionSuccessRate(),
            req.reportDate(),
            req.status()
        );

        Report savedReport = reportRepository.save(report);
        return ReportRes.from(savedReport);
    }

    /**
     * Update existing report (partial update)
     * Used by AI server after async report generation completes
     *
     * @param reportId Report ID to update
     * @param req Update request with optional fields
     * @return Updated report response
     */
    @Transactional
    public ReportRes updateReportForAi(Long reportId, ReportUpdateReq req) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + reportId));

        report.update(
            req.reportOverview(),
            req.insights(),
            req.strategy(),
            req.advice(),
            req.missionSuccessRate(),
            req.status()
        );

        return ReportRes.from(report);
    }

    /**
     * Get videos that need user feedback
     * Retrieves media sessions from recent daily activities
     * Enriches with video information from PostgreSQL youtube_video table
     *
     * @return List of videos for feedback
     */
    @Transactional(readOnly = true)
    public List<VideoFeedbackItem> getVideosForFeedback() {
        long userId = JwtAuthentication.getUserId();

        // Get recent daily activities for the user
        List<DailyUserActivityDocument> activities = dailyUserActivityRepository
            .findByUserIdOrderByDateDesc(userId);

        if (activities.isEmpty()) {
            // Return mock data if no activities found
            return getMockVideosForFeedback();
        }

        // Extract all media sessions
        List<DailyUserActivityDocument.MediaSession> allSessions = activities.stream()
            .filter(activity -> activity.getMediaSessions() != null)
            .flatMap(activity -> activity.getMediaSessions().stream())
            .collect(Collectors.toList());

        if (allSessions.isEmpty()) {
            return getMockVideosForFeedback();
        }

        // Extract youtube_video IDs (filter out null IDs)
        List<Long> youtubeVideoIds = allSessions.stream()
            .map(DailyUserActivityDocument.MediaSession::getYoutubeVideoId)
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());

        // Batch query: get all YouTube videos from PostgreSQL
        Map<Long, YoutubeVideo> videoMap = youtubeVideoService.getVideosByIds(youtubeVideoIds);

        // Convert media sessions to VideoFeedbackItem
        List<VideoFeedbackItem> videos = new ArrayList<>();
        for (DailyUserActivityDocument.MediaSession session : allSessions) {
            // Skip if no youtube_video_id
            if (session.getYoutubeVideoId() == null) {
                continue;
            }

            // Get video information from PostgreSQL
            YoutubeVideo video = videoMap.get(session.getYoutubeVideoId());
            if (video == null) {
                // Video not found in PostgreSQL, skip this session
                continue;
            }

            // Convert watch time from seconds to minutes
            int watchTimeMinutes = (int) Math.ceil(session.getWatchTime() / 60.0);

            // Build VideoFeedbackItem using PostgreSQL data
            VideoFeedbackItem item = VideoFeedbackItem.builder()
                .id(String.valueOf(session.getYoutubeVideoId()))
                .title(video.getTitle())
                .channel(video.getChannel())
                .thumbnailBase64(video.getThumbnailBase64())
                .watchTimeMinutes(watchTimeMinutes)
                .build();

            videos.add(item);
        }

        return videos.isEmpty() ? getMockVideosForFeedback() : videos;
    }

    /**
     * Convert thumbnail URI to Base64 encoded string
     * TODO: Implement actual conversion logic based on thumbnail storage mechanism
     *
     * @param thumbnailUri Thumbnail URI from database
     * @return Base64 encoded thumbnail
     */
    private String convertThumbnailToBase64(String thumbnailUri) {
        if (thumbnailUri == null || thumbnailUri.isEmpty()) {
            return generateMockThumbnail();
        }

        // TODO: If thumbnailUri is a file path, read and convert to Base64
        // TODO: If thumbnailUri is a URL, fetch and convert to Base64
        // For now, check if it's already Base64 or return mock
        if (thumbnailUri.startsWith("data:image") || thumbnailUri.length() > 100) {
            return thumbnailUri;
        }

        return generateMockThumbnail();
    }

    /**
     * Generate mock thumbnail (1x1 transparent PNG)
     *
     * @return Base64 encoded transparent PNG
     */
    private String generateMockThumbnail() {
        return "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
    }

    /**
     * Get mock videos for testing when no real data is available
     *
     * @return Mock list of VideoFeedbackItem
     */
    private List<VideoFeedbackItem> getMockVideosForFeedback() {
        return List.of(
            VideoFeedbackItem.builder()
                .id("video_1")
                .title("Kotlin 코루틴 완벽 가이드 - 비동기 프로그래밍 마스터하기")
                .channel("개발자 튜토리얼")
                .thumbnailBase64(generateMockThumbnail())
                .watchTimeMinutes(45)
                .build(),
            VideoFeedbackItem.builder()
                .id("video_2")
                .title("재미있는 쇼츠 모음 😂 웃음 참기 챌린지")
                .channel("엔터테인먼트 채널")
                .thumbnailBase64(generateMockThumbnail())
                .watchTimeMinutes(23)
                .build()
        );
    }
}
