package com.dito.app.core.data.screentime

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 로컬 Realm용 스크린타임 Document
 * MongoDB 동기화 대기 중인 데이터 임시 저장
 */
class ScreenTimeLocalLog : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()

    var groupId: Long = 0
    var userId: Long = 0
    var date: String = ""  // "2025-01-07"
    var totalMinutes: Int = 0
    var recordedAt: String = ""  // ISO-8601 format
    var synced: Boolean = false  // MongoDB 동기화 여부
}

/**
 * MongoDB Atlas 직접 저장용 데이터 클래스
 */
data class ScreenTimeDailySummaryMongo(
    val group_id: Long,
    val user_id: Long,
    val date: String,
    val total_minutes: Int,
    val last_updated_at: String  // ISO-8601
)

data class ScreenTimeSnapshotMongo(
    val group_id: Long,
    val user_id: Long,
    val date: String,
    val recorded_at: String,  // ISO-8601
    val screen_time_minutes: Int,
    val expire_at: String  // ISO-8601 (30일 후)
)

/**
 * Extension functions
 */
fun ScreenTimeLocalLog.toSummaryMongo(): ScreenTimeDailySummaryMongo {
    return ScreenTimeDailySummaryMongo(
        group_id = this.groupId,
        user_id = this.userId,
        date = this.date,
        total_minutes = this.totalMinutes,
        last_updated_at = this.recordedAt
    )
}

fun ScreenTimeLocalLog.toSnapshotMongo(): ScreenTimeSnapshotMongo {
    val expireAt = LocalDateTime.parse(this.recordedAt)
        .plusDays(30)
        .toString()

    return ScreenTimeSnapshotMongo(
        group_id = this.groupId,
        user_id = this.userId,
        date = this.date,
        recorded_at = this.recordedAt,
        screen_time_minutes = this.totalMinutes,
        expire_at = expireAt
    )
}
