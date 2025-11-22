package com.ssafy.Dito.domain.ai.report.document;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder(toBuilder = true)
@Document(collection = "user_realtime_status")
public class UserRealtimeStatusDocument {
    @Id
    private String id;  // MongoDB가 자동생성하는 ObjectId

    private Long userId;

    private String videoId;
    private String title;
    private String channel;
    private String appPackage;
    private String thumbnailUri;
    private String status; // PLAYING, PAUSED, STOPPED
    private Long watchTime;
    private Long videoDuration;
    private Long pauseTime;
    private Long timestamp;
    private Long lastUpdatedAt;

    // Usage Stats Heartbeat fields
    private String currentAppPackage;
    private String currentAppName;

    // Heartbeat tracking
    private Long lastHeartbeatTimestamp;
}
