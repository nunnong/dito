package com.ssafy.Dito.domain.screentime.document;

import com.ssafy.Dito.domain.log.common.MongoBaseDocument;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * MongoDB document for current app usage
 * 사용자가 현재 사용 중인 앱 정보를 실시간으로 저장
 *
 * 특징:
 * - 그룹 챌린지 랭킹 화면에서 각 참가자의 현재 사용 앱 표시
 * - upsert 방식으로 동작 (한 사용자당 하나의 현재 앱 정보만 유지)
 * - 앱 전환 시마다 실시간 업데이트
 */
@Document(collection = "current_app_usage")
@CompoundIndexes({
    @CompoundIndex(
        name = "group_user_unique_idx",
        def = "{'group_id': 1, 'user_id': 1}",
        unique = true
    ),
    @CompoundIndex(
        name = "group_idx",
        def = "{'group_id': 1}"
    )
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CurrentAppUsage extends MongoBaseDocument {

    @Field("group_id")
    private Long groupId;

    @Field("user_id")
    private Long userId;

    @Field("app_package")
    private String appPackage;

    @Field("app_name")
    private String appName;

    @Field("usage_duration")
    private Long usageDuration; // in seconds

    @Field("last_media_event_id")
    private String lastMediaEventId;

    @Field("last_media_timestamp")
    private Long lastMediaTimestamp;

    @Field("last_is_educational")
    private Boolean lastIsEducational;

    @Field("last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @Builder
    private CurrentAppUsage(
        Long groupId,
        Long userId,
        String appPackage,
        String appName,
        Long usageDuration,
        String lastMediaEventId,
        Long lastMediaTimestamp,
        Boolean lastIsEducational,
        LocalDateTime lastUpdatedAt
    ) {
        this.groupId = groupId;
        this.userId = userId;
        this.appPackage = appPackage;
        this.appName = appName;
        this.usageDuration = usageDuration;
        this.lastMediaEventId = lastMediaEventId;
        this.lastMediaTimestamp = lastMediaTimestamp;
        this.lastIsEducational = lastIsEducational;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    /**
     * 새로운 현재 앱 정보 생성
     */
    public static CurrentAppUsage create(
        Long groupId,
        Long userId,
        String appPackage,
        String appName,
        Long usageDuration,
        String mediaEventId,
        Long mediaEventTimestamp,
        Boolean isEducational
    ) {
        return CurrentAppUsage.builder()
            .groupId(groupId)
            .userId(userId)
            .appPackage(appPackage)
            .appName(appName)
            .usageDuration(usageDuration)
            .lastMediaEventId(mediaEventId)
            .lastMediaTimestamp(mediaEventTimestamp)
            .lastIsEducational(isEducational)
            .lastUpdatedAt(LocalDateTime.now())
            .build();
    }

    /**
     * 현재 앱 정보 업데이트
     */
    public void update(String appPackage, String appName, Long usageDuration,
                       String mediaEventId, Long mediaEventTimestamp, Boolean isEducational) {
        this.appPackage = appPackage;
        this.appName = appName;
        this.usageDuration = usageDuration;
        if (mediaEventId != null) {
            this.lastMediaEventId = mediaEventId;
        }
        if (mediaEventTimestamp != null) {
            this.lastMediaTimestamp = mediaEventTimestamp;
        }
        if (isEducational != null) {
            this.lastIsEducational = isEducational;
        }
        this.lastUpdatedAt = LocalDateTime.now();
    }
}
