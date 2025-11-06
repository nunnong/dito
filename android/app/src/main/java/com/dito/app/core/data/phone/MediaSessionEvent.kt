package com.dito.app.core.data.phone

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 미디어 세션 이벤트 (유튜브 영상 시청 기록)
 */
class MediaSessionEvent : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()

    var trackType: String = "TRACK_2"

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

    var aiCalled: Boolean = false
    var aiCalledAt:Long = 0L
    var aiRetryCount: Int = 0

    // 메타데이터
    var createdAt: Long = System.currentTimeMillis()
}


fun MediaSessionEvent.toDto(): MediaSessionEventDto {
    return MediaSessionEventDto(
        event_id = try {
            this._id.toHexString()
        } catch (e: Exception) {
            UUID.randomUUID().toString()
        },
        event_type = this.eventType.ifBlank { "UNKNOWN" },
        package_name = this.appPackage.ifBlank { "unknown.package" },
        app_name = getAppNameFromPackage(this.appPackage),
        title = this.title.takeIf { it.isNotBlank() },
        channel = this.channel.takeIf { it.isNotBlank() },
        event_timestamp = this.timestamp,
        video_duration = if (this.videoDuration > 0) this.videoDuration else null,
        watch_time = if (this.watchTime > 0) this.watchTime else null,
        pause_time = if (this.pauseTime > 0) this.pauseTime else null,
        event_date = this.date.ifBlank { getTodayDateString() }
    )
}

private fun getAppNameFromPackage(packageName: String): String {
    return when (packageName) {
        "com.google.android.youtube" -> "YouTube"
        "com.google.android.youtube.music" -> "YouTube Music"
        "com.samsung.android.app.music" -> "Samsung Music"
        "com.android.chrome" -> "Chrome"
        else -> {
            // 패키지명에서 앱 이름 추출 (마지막 부분)
            packageName.split(".").lastOrNull()?.capitalize(Locale.ROOT) ?: packageName
        }
    }
}


private fun getTodayDateString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}