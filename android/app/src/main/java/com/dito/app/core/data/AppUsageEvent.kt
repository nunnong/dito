package com.dito.app.core.data

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 앱 사용 이벤트 (Track 2 배치 전송용)
 *
 * 저장 위치: AppMonitoringService.saveAppUsage()
 * 사용 시점: WorkManager가 30분마다 서버로 배치 전송
 */
class AppUsageEvent : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()

    // 이벤트 타입
    var eventType: String = ""              // "APP_OPEN" or "APP_CLOSE"

    // 앱 정보
    var packageName: String = ""            // com.google.android.youtube
    var appName: String = ""                // YouTube

    // 시간 정보
    var timestamp: Long = 0L                // 이벤트 발생 시각 (밀리초)
    var duration: Long = 0L                 // 사용 시간 (밀리초) - CLOSE만
    var date: String = ""                   // yyyy-MM-dd

    // 동기화 플래그
    var synced: Boolean = false             // 서버 전송 완료 여부
    var syncedAt: Long = 0L                 // 전송 완료 시각

    // 메타데이터
    var createdAt: Long = System.currentTimeMillis()
}

fun AppUsageEvent.toDto(): AppUsageEventDto {
    return AppUsageEventDto(
        event_id = try {
            this._id.toHexString()
        } catch (e: Exception) {
            // detach 또는 null 방지용
            UUID.randomUUID().toString()
        },
        event_type = this.eventType.ifBlank { "UNKNOWN" },
        package_name = this.packageName.ifBlank { "unknown.package" },
        app_name = this.appName.takeIf { it.isNotBlank() },
        event_timestamp = this.timestamp,
        duration = if (this.duration > 0) this.duration else null,
        event_date = this.date.ifBlank { getTodayDateString() }
    )
}

private fun getTodayDateString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}
