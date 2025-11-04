package com.dito.app.core.data

import android.util.Log
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import org.mongodb.kbson.BsonObjectId
import java.text.SimpleDateFormat
import java.util.*

object RealmRepository {

    private const val TAG = "RealmRepository"

    private val realm: Realm
        get() = RealmConfig.getInstance()

    // Track 2 전용 쿼리 (배치 전송용)
    fun getUnsyncedAppEvents(): List<AppUsageEvent> {
        return realm.query<AppUsageEvent>(
            "trackType == $0 AND synced == false",
            "TRACK_2"
        ).find()
    }

    fun getUnsyncedMediaEvents(): List<MediaSessionEvent> {
        return realm.query<MediaSessionEvent>(
            "trackType == $0 AND synced == false",
            "TRACK_2"
        ).find()
    }


    fun deleteAiProcessedEvents() {
        realm.writeBlocking {
            val appEvents = query<AppUsageEvent>(
                "trackType == $0 AND aiCalled == true",
                "TRACK_1"
            ).find()

            val mediaEvents = query<MediaSessionEvent>(
                "trackType == $0 AND aiCalled == true",
                "TRACK_1"
            ).find()

            // 실제 삭제 안 함, 유지만 로깅
            Log.d(TAG, "✅ Track1 AI 호출 완료 이벤트 ${appEvents.size + mediaEvents.size}개 유지 (삭제하지 않음)")
        }
    }

    fun getTodayAppEvents(): List<AppUsageEvent> {
        val today = getToday()
        return realm.query<AppUsageEvent>("date == $0", today).find()
    }

    fun getTodayMediaEvents(): List<MediaSessionEvent> {
        val today = getToday()
        return realm.query<MediaSessionEvent>("date == $0", today).find()
    }

    fun markAsSynced(eventIds: List<String>) {
        realm.writeBlocking {
            eventIds.forEach { hexId ->
                try {
                    val objectId = BsonObjectId(hexId)

                    query<AppUsageEvent>("_id == $0", objectId).first().find()?.let { event ->
                        event.synced = true
                        event.syncedAt = System.currentTimeMillis()
                    }

                    query<MediaSessionEvent>("_id == $0", objectId).first().find()?.let { event ->
                        event.synced = true
                        event.syncedAt = System.currentTimeMillis()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ ObjectId 변환 실패: $hexId", e)
                }
            }
        }
        Log.d(TAG, "✅ ${eventIds.size}개 이벤트 synced 완료")
    }

    // Track 1: AI 호출 성공 표시
    fun markAiCalled(eventId: String, success: Boolean) {
        realm.writeBlocking {
            try {
                val objectId = BsonObjectId(eventId)

                query<AppUsageEvent>("_id == $0", objectId).first().find()?.let { event ->
                    event.aiCalled = success
                    event.aiCalledAt = System.currentTimeMillis()
                    if (!success) event.aiRetryCount += 1
                }

                query<MediaSessionEvent>("_id == $0", objectId).first().find()?.let { event ->
                    event.aiCalled = success
                    event.aiCalledAt = System.currentTimeMillis()
                    if (!success) event.aiRetryCount += 1
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ markAiCalled 실패: $eventId", e)
            }
        }
    }

    fun clearAll() {
        realm.writeBlocking {
            delete(query<AppUsageEvent>())
            delete(query<MediaSessionEvent>())
        }
        Log.d(TAG, "🗑️ 전체 데이터 삭제 완료")
    }

    fun deleteOldData() {
        val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        realm.writeBlocking {
            delete(query<AppUsageEvent>("createdAt < $0", thirtyDaysAgo))
            delete(query<MediaSessionEvent>("createdAt < $0", thirtyDaysAgo))
        }
        Log.d(TAG, "🗑️ 30일 이상 데이터 삭제 완료")
    }

    private fun getToday(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

}