package com.ssafy.Dito.domain.ai.report.service;

import com.ssafy.Dito.domain.ai.report.document.UserRealtimeStatusDocument;
import com.ssafy.Dito.domain.ai.report.dto.HeartbeatReq;
import com.ssafy.Dito.domain.ai.report.repository.UserRealtimeStatusRepository;
import com.ssafy.Dito.domain.youtube.entity.YoutubeVideo;
import com.ssafy.Dito.domain.youtube.repository.YoutubeVideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.ssafy.Dito.domain.ai.report.document.DailyUserActivityDocument;
import com.ssafy.Dito.domain.ai.report.repository.DailyUserActivityRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealtimeActivityService {

    private final UserRealtimeStatusRepository userRealtimeStatusRepository;
    private final DailyUserActivityRepository dailyUserActivityRepository;
    private final YoutubeVideoRepository youtubeVideoRepository;

    private static final long MAX_ELAPSED_SECONDS = 10; // 최대 10초까지만 인정

    /**
     * 통합 Heartbeat 처리
     * - 클라이언트는 5초마다 현재 상태만 전송
     * - 서버에서 마지막 heartbeat와 비교하여 경과 시간 자동 계산
     */
    public void processHeartbeat(Long userId, HeartbeatReq req) {
        // 1. 마지막 heartbeat 시간 조회 및 경과 시간 계산
        UserRealtimeStatusDocument lastStatus = userRealtimeStatusRepository.findByUserId(userId).orElse(null);

        long elapsedSeconds = 0;
        if (lastStatus != null && lastStatus.getLastHeartbeatTimestamp() != null) {
            elapsedSeconds = (req.timestamp() - lastStatus.getLastHeartbeatTimestamp()) / 1000;

            // 검증: 시간 역행 방지
            if (elapsedSeconds < 0) {
                log.warn("Time went backwards for user {}: {} -> {}", userId,
                    lastStatus.getLastHeartbeatTimestamp(), req.timestamp());
                return;
            }

            // 검증: 너무 긴 간격은 제한 (앱 백그라운드 또는 네트워크 끊김)
            if (elapsedSeconds > MAX_ELAPSED_SECONDS) {
                log.debug("Elapsed time too long for user {}: {}s, capping to {}s",
                    userId, elapsedSeconds, MAX_ELAPSED_SECONDS);
                elapsedSeconds = MAX_ELAPSED_SECONDS;
            }
        }

        // 2. 실시간 상태 업데이트
        updateRealtimeStatus(userId, req, lastStatus);

        // 3. 일일 활동 집계 (첫 heartbeat가 아닐 때만)
        // TODO: 테스트를 위해 임시 주석처리 - 수동으로 데이터 insert 후 조회 테스트용
        /*
        if (elapsedSeconds > 0) {
            aggregateDailyActivity(userId, req, elapsedSeconds);
        }
        */
    }

    /**
     * 실시간 상태 업데이트 (user_realtime_status 컬렉션)
     */
    private void updateRealtimeStatus(Long userId, HeartbeatReq req, UserRealtimeStatusDocument lastStatus) {
        UserRealtimeStatusDocument.UserRealtimeStatusDocumentBuilder builder;

        if (lastStatus != null) {
            // 기존 상태 유지하면서 업데이트
            builder = lastStatus.toBuilder();
        } else {
            // 첫 heartbeat
            builder = UserRealtimeStatusDocument.builder().userId(userId);
        }

        // 미디어 세션 정보 업데이트
        if (req.mediaSession() != null) {
            HeartbeatReq.MediaSessionInfo media = req.mediaSession();
            builder.videoId(media.videoId())
                   .title(media.title())
                   .channel(media.channel())
                   .appPackage(media.appPackage())
                   .thumbnailUri(media.thumbnailUri())
                   .status(media.status())
                   .watchTime(media.watchTime())
                   .videoDuration(media.videoDuration())
                   .pauseTime(media.pauseTime())
                   .timestamp(req.timestamp());
        } else {
            // 미디어 세션 없으면 상태 초기화
            builder.videoId(null)
                   .title(null)
                   .channel(null)
                   .appPackage(null)
                   .thumbnailUri(null)
                   .status(null)
                   .watchTime(null)
                   .videoDuration(null)
                   .pauseTime(null)
                   .timestamp(null);
        }

        // 앱 정보 업데이트
        if (req.currentApp() != null) {
            HeartbeatReq.CurrentAppInfo app = req.currentApp();
            builder.currentAppPackage(app.packageName())
                   .currentAppName(app.appName());
        } else {
            builder.currentAppPackage(null)
                   .currentAppName(null);
        }

        // Heartbeat 추적 정보 업데이트
        builder.lastHeartbeatTimestamp(req.timestamp())
               .lastUpdatedAt(System.currentTimeMillis());

        UserRealtimeStatusDocument document = builder.build();
        userRealtimeStatusRepository.save(document);

        log.debug("Updated realtime status for user {}", userId);
    }

    /**
     * 일일 활동 집계 (daily_user_activities 컬렉션)
     */
    private void aggregateDailyActivity(Long userId, HeartbeatReq req, long elapsedSeconds) {
        LocalDate today = LocalDate.now();
        DailyUserActivityDocument dailyDoc = getOrCreateDailyDocument(userId, today);

        boolean updated = false;

        // 미디어 시청 시간 누적 (PLAYING 상태일 때만)
        if (req.mediaSession() != null && "PLAYING".equals(req.mediaSession().status())) {
            dailyDoc = updateMediaSession(dailyDoc, req.mediaSession(), elapsedSeconds);
            updated = true;
        }

        // 앱 사용 시간 누적
        if (req.currentApp() != null) {
            dailyDoc = updateAppUsage(dailyDoc, req.currentApp(), elapsedSeconds);
            updated = true;
        }

        if (updated) {
            dailyUserActivityRepository.save(dailyDoc);
            log.debug("Updated daily activity for user {}: +{}s", userId, elapsedSeconds);
        }
    }

    /**
     * 미디어 세션 시청 시간 업데이트
     */
    private DailyUserActivityDocument updateMediaSession(
            DailyUserActivityDocument dailyDoc,
            HeartbeatReq.MediaSessionInfo mediaInfo,
            long elapsedSeconds) {

        List<DailyUserActivityDocument.MediaSession> sessions = dailyDoc.getMediaSessions();
        if (sessions == null) sessions = new ArrayList<>();

        // PostgreSQL에 영상 정보 캐싱 및 ID 가져오기
        String platform = extractPlatform(mediaInfo.appPackage());
        Long youtubeVideoId = cacheYoutubeVideoToPostgres(mediaInfo, platform);

        // 동일 영상 찾기 (youtube_video_id로 식별)
        Optional<DailyUserActivityDocument.MediaSession> existingSession = sessions.stream()
            .filter(s -> s.getYoutubeVideoId() != null && youtubeVideoId != null
                && s.getYoutubeVideoId().equals(youtubeVideoId))
            .findFirst();

        List<DailyUserActivityDocument.MediaSession> newSessions = new ArrayList<>(sessions);

        if (existingSession.isPresent()) {
            // 기존 세션: 시청 시간만 증가
            DailyUserActivityDocument.MediaSession oldSession = existingSession.get();
            newSessions.remove(oldSession);
            newSessions.add(oldSession.toBuilder()
                .watchTime(oldSession.getWatchTime() + elapsedSeconds)
                .youtubeVideoId(youtubeVideoId != null ? youtubeVideoId : oldSession.getYoutubeVideoId())
                .build());
        } else {
            // 새 세션: youtube_video_id 포함하여 생성
            newSessions.add(DailyUserActivityDocument.MediaSession.builder()
                .youtubeVideoId(youtubeVideoId)  // PostgreSQL youtube_video.id
                .platform(platform)              // Deprecated (호환성 유지)
                .title(mediaInfo.title())        // Deprecated (호환성 유지)
                .channel(mediaInfo.channel())    // Deprecated (호환성 유지)
                .thumbnailUri(null)              // 더 이상 저장하지 않음
                .timestamp(System.currentTimeMillis())
                .watchTime(elapsedSeconds)
                .videoType("VIDEO")
                .keywords(new ArrayList<>())
                .build());
        }

        // Summary 업데이트 (분 단위)
        double addedMinutes = elapsedSeconds / 60.0;
        DailyUserActivityDocument.Summary oldSummary = dailyDoc.getSummary();
        double newTotalMediaTime = (oldSummary != null && oldSummary.getTotalMediaWatchTime() != null)
            ? oldSummary.getTotalMediaWatchTime() + addedMinutes
            : addedMinutes;

        DailyUserActivityDocument.Summary newSummary = DailyUserActivityDocument.Summary.builder()
            .totalAppUsageTime(oldSummary != null ? oldSummary.getTotalAppUsageTime() : 0)
            .totalMediaWatchTime(newTotalMediaTime)
            .mostUsedApp(oldSummary != null ? oldSummary.getMostUsedApp() : "")
            .build();

        return DailyUserActivityDocument.builder()
            .id(dailyDoc.getId())
            .date(dailyDoc.getDate())
            .userId(dailyDoc.getUserId())
            .summary(newSummary)
            .appUsageStats(dailyDoc.getAppUsageStats())
            .mediaSessions(newSessions)
            .build();
    }

    /**
     * 앱 사용 시간 업데이트
     */
    private DailyUserActivityDocument updateAppUsage(
            DailyUserActivityDocument dailyDoc,
            HeartbeatReq.CurrentAppInfo appInfo,
            long elapsedSeconds) {

        List<DailyUserActivityDocument.AppUsageStat> stats = dailyDoc.getAppUsageStats();
        if (stats == null) stats = new ArrayList<>();

        // 동일 앱 찾기
        Optional<DailyUserActivityDocument.AppUsageStat> existingStat = stats.stream()
            .filter(s -> appInfo.packageName().equals(s.getPackageName()))
            .findFirst();

        List<DailyUserActivityDocument.AppUsageStat> newStats = new ArrayList<>(stats);

        if (existingStat.isPresent()) {
            // 기존 앱 사용 시간 증가
            DailyUserActivityDocument.AppUsageStat oldStat = existingStat.get();
            newStats.remove(oldStat);
            newStats.add(oldStat.toBuilder()
                .totalDuration(oldStat.getTotalDuration() + elapsedSeconds)
                .build());
        } else {
            // 새 앱 추가
            newStats.add(DailyUserActivityDocument.AppUsageStat.builder()
                .appName(appInfo.appName())
                .packageName(appInfo.packageName())
                .totalDuration(elapsedSeconds)
                .sessionCount(1)
                .build());
        }

        // Summary 업데이트
        long totalSeconds = newStats.stream()
            .mapToLong(DailyUserActivityDocument.AppUsageStat::getTotalDuration)
            .sum();
        int totalMinutes = (int) (totalSeconds / 60);

        String mostUsedApp = newStats.stream()
            .max((a, b) -> Long.compare(a.getTotalDuration(), b.getTotalDuration()))
            .map(DailyUserActivityDocument.AppUsageStat::getPackageName)
            .orElse("");

        DailyUserActivityDocument.Summary oldSummary = dailyDoc.getSummary();
        DailyUserActivityDocument.Summary newSummary = DailyUserActivityDocument.Summary.builder()
            .totalAppUsageTime(totalMinutes)
            .totalMediaWatchTime(oldSummary != null ? oldSummary.getTotalMediaWatchTime() : 0.0)
            .mostUsedApp(mostUsedApp)
            .build();

        return DailyUserActivityDocument.builder()
            .id(dailyDoc.getId())
            .date(dailyDoc.getDate())
            .userId(dailyDoc.getUserId())
            .summary(newSummary)
            .appUsageStats(newStats)
            .mediaSessions(dailyDoc.getMediaSessions())
            .build();
    }

    /**
     * 일일 활동 Document 조회 또는 생성 (날짜별 자동 생성)
     */
    private DailyUserActivityDocument getOrCreateDailyDocument(Long userId, LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ISO_DATE); // yyyy-MM-dd
        String id = date.format(DateTimeFormatter.BASIC_ISO_DATE) + "_" + userId; // yyyyMMdd_userId

        return dailyUserActivityRepository.findById(id)
            .orElse(DailyUserActivityDocument.builder()
                .id(id)
                .date(dateStr)
                .userId(userId)
                .summary(DailyUserActivityDocument.Summary.builder()
                    .totalAppUsageTime(0)
                    .totalMediaWatchTime(0.0)
                    .mostUsedApp("")
                    .build())
                .appUsageStats(new ArrayList<>())
                .mediaSessions(new ArrayList<>())
                .build());
    }

    /**
     * 앱 패키지명에서 플랫폼 이름 추출
     */
    private String extractPlatform(String appPackage) {
        if (appPackage == null) return "Unknown";

        if (appPackage.contains("youtube")) return "YouTube";
        if (appPackage.contains("netflix")) return "Netflix";
        if (appPackage.contains("spotify")) return "Spotify";

        return appPackage;
    }

    /**
     * YouTube 영상 정보를 PostgreSQL에 캐싱
     * - channel + title로 중복 체크
     * - 존재하지 않으면 새로 저장, 존재하면 기존 ID 반환
     * @return YouTube video ID (null if failed)
     */
    private Long cacheYoutubeVideoToPostgres(HeartbeatReq.MediaSessionInfo mediaInfo, String platform) {
        try {
            // Null 체크
            if (mediaInfo.title() == null || mediaInfo.channel() == null) {
                log.debug("Skipping PostgreSQL cache: title or channel is null");
                return null;
            }

            // 중복 체크: channel + title 조합으로 이미 저장되어 있는지 확인
            Optional<YoutubeVideo> existingVideo = youtubeVideoRepository.findByChannelAndTitle(
                mediaInfo.channel(),
                mediaInfo.title()
            );

            if (existingVideo.isPresent()) {
                log.debug("Video already cached in PostgreSQL: channel={}, title={}, id={}",
                    mediaInfo.channel(), mediaInfo.title(), existingVideo.get().getId());
                return existingVideo.get().getId();
            }

            // 새 영상 정보 저장
            YoutubeVideo video = YoutubeVideo.of(
                mediaInfo.title(),
                mediaInfo.channel(),
                mediaInfo.thumbnailUri(), // Base64 썸네일
                mediaInfo.appPackage(),
                platform,
                "VIDEO", // videoType (기본값)
                null     // keywords (나중에 확장)
            );

            YoutubeVideo savedVideo = youtubeVideoRepository.save(video);
            log.info("Cached new video to PostgreSQL: channel={}, title={}, id={}",
                mediaInfo.channel(), mediaInfo.title(), savedVideo.getId());

            return savedVideo.getId();

        } catch (Exception e) {
            // PostgreSQL 캐싱 실패해도 MongoDB 저장에는 영향 없도록 예외 처리
            log.error("Failed to cache video to PostgreSQL: channel={}, title={}",
                mediaInfo.channel(), mediaInfo.title(), e);
            return null;
        }
    }
}
