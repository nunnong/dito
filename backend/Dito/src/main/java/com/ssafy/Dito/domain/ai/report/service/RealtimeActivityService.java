package com.ssafy.Dito.domain.ai.report.service;

import com.ssafy.Dito.domain.ai.report.document.UserRealtimeStatusDocument;
import com.ssafy.Dito.domain.ai.report.dto.RealtimeActivityReq;
import com.ssafy.Dito.domain.ai.report.dto.RealtimeUsageReq;
import com.ssafy.Dito.domain.ai.report.repository.UserRealtimeStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealtimeActivityService {

    private final UserRealtimeStatusRepository userRealtimeStatusRepository;

    public void updateRealtimeStatus(Long userId, RealtimeActivityReq req) {
        UserRealtimeStatusDocument document = UserRealtimeStatusDocument.builder()
            .userId(userId)
            .videoId(req.videoId())
            .title(req.title())
            .channel(req.channel())
            .appPackage(req.appPackage())
            .thumbnailUri(req.thumbnailUri())
            .status(req.status())
            .watchTime(req.watchTime())
            .videoDuration(req.videoDuration())
            .pauseTime(req.pauseTime())
            .timestamp(req.timestamp())
            .lastUpdatedAt(System.currentTimeMillis())
            .build();

        userRealtimeStatusRepository.save(document);
        log.debug("Updated realtime status for user {}: {}", userId, req.status());
    }

    public void updateRealtimeUsage(Long userId, RealtimeUsageReq req) {
        // 기존 문서가 있으면 가져와서 업데이트, 없으면 새로 생성
        UserRealtimeStatusDocument document = userRealtimeStatusRepository.findByUserId(userId)
            .orElse(UserRealtimeStatusDocument.builder()
                .userId(userId)
                .build());

        // Builder를 사용하여 기존 값 유지하면서 새로운 값 업데이트 (Lombok @Builder.toBuilder가 없으므로 수동 처리 필요하거나, 
        // 여기서는 간단히 Repository의 save가 덮어쓰기이므로 기존 필드를 유지하려면 조회가 필요함.
        // 하지만 위에서 findById로 가져왔으니, 객체의 필드를 수정해서 저장해야 함.
        // UserRealtimeStatusDocument가 @Setter가 없으면 새로 빌드해야 함.
        // 편의상 여기서는 기존 미디어 정보는 유지하고 앱 정보만 업데이트하는 식으로 구현하려면
        // Document에 @Setter를 추가하거나 toBuilder=true를 쓰는게 좋음.
        // 현재 @Builder만 있으므로 전체를 다시 빌드해야 하는데, 기존 필드들이 null이 될 수 있음.
        // 따라서 Repository에서 조회한 데이터를 기반으로 다시 빌드.
        
        UserRealtimeStatusDocument updatedDocument = UserRealtimeStatusDocument.builder()
            .id(document.getId())  // 기존 ID 유지 (있으면 업데이트, 없으면 새로 생성)
            .userId(userId)
            .videoId(document.getVideoId())
            .title(document.getTitle())
            .channel(document.getChannel())
            .appPackage(document.getAppPackage())
            .thumbnailUri(document.getThumbnailUri())
            .status(document.getStatus())
            .watchTime(document.getWatchTime())
            .videoDuration(document.getVideoDuration())
            .pauseTime(document.getPauseTime())
            .timestamp(document.getTimestamp()) // 미디어 타임스탬프 유지
            .lastUpdatedAt(System.currentTimeMillis())
            .currentAppPackage(req.packageName())
            .currentAppName(req.appName())
            .build();

        userRealtimeStatusRepository.save(updatedDocument);
        log.debug("Updated realtime usage for user {}: {}", userId, req.packageName());
    }
}
