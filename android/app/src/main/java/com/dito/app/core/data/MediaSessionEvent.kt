package com.dito.app.core.data

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

/**
 * 미디어 세션 이벤트 (유튜브 영상 시청 기록)
 *
 * 저장 위치: SessionStateManager.saveSession()
 * 사용 시점: WorkManager가 30분마다 서버로 배치 전송
 */
class MediaSessionEvent : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()

    // 이벤트 타입
    var eventType: String = ""              // "VIDEO_START", "VIDEO_PAUSE", "VIDEO_END"

    // 콘텐츠 정보
    var title: String = ""                  // 영상 제목
    var channel: String = ""                // 채널명
    var appPackage: String = ""             // com.google.android.youtube

    // 시간 정보
    var timestamp: Long = 0L                // 이벤트 발생 시각
    var videoDuration: Long = 0L            // 영상 전체 길이
    var watchTime: Long = 0L                // 실제 시청 시간
    var pauseTime: Long = 0L                // 일시정지 시간
    var date: String = ""                   // yyyy-MM-dd

    // 감지 방법
    var detectionMethod: String = ""        // "resource-id", "media-session" 등

    // 동기화 플래그
    var synced: Boolean = false             // 서버 전송 완료 여부
    var syncedAt: Long = 0L                 // 전송 완료 시각

    // 메타데이터
    var createdAt: Long = System.currentTimeMillis()
}