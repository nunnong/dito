package com.dito.app.core.data.phone

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
* 앱 사용 이벤트
*
* - "TRACK_1": AI 즉시 호출용
* - "TRACK_2": 배치 전송용 (synced 관리)
*/
class AppUsageEvent : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()

    //Track 1,2 구분
    var trackType: String = ""              // "TRACK_1" or "TRACK_2"

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

    //AI 호출
    var aiCalled: Boolean = false           // AI 호출 완료 여부
    var aiCalledAt: Long = 0L               // AI 호출 시각
    var aiRetryCount: Int = 0


    // 메타데이터
    var createdAt: Long = System.currentTimeMillis()
}

fun AppUsageEvent.toDto(): AppUsageEventDto {
    return AppUsageEventDto(
        event_id = try {
            this._id.toHexString()
        } catch (e: Exception) {
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
