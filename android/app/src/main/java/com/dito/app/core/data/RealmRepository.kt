package com.dito.app.core.data

import android.util.Log
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import java.text.SimpleDateFormat
import java.util.*

/**
 * Realm 데이터 조회 헬퍼
 *
 * 사용처:
 * - MainActivity (테스트용)
 * - WorkManager (배치 전송용)
 * - 통계 화면 (나중에)
 */
object RealmRepository {

    private const val TAG = "RealmRepository"

    // 공유 Realm 객체 사용
    private val realm: Realm
        get() = RealmConfig.getInstance()

    /** 오늘 앱 사용 이벤트 전체 */
    fun getTodayAppEvents(): List<AppUsageEvent> {
        val today = getToday()
        return realm.query<AppUsageEvent>("date == $0", today).find()
    }

    /** 오늘 미디어 세션 이벤트 전체 */
    fun getTodayMediaEvents(): List<MediaSessionEvent> {
        val today = getToday()
        return realm.query<MediaSessionEvent>("date == $0", today).find()
    }

    /** 동기화 대기 중 앱 이벤트 */
    fun getUnsyncedAppEvents(): List<AppUsageEvent> {
        return realm.query<AppUsageEvent>("synced == false").find()
    }

    /** 동기화 대기 중 미디어 이벤트 */
    fun getUnsyncedMediaEvents(): List<MediaSessionEvent> {
        return realm.query<MediaSessionEvent>("synced == false").find()
    }

    /** 이벤트를 synced=true로 마킹 (서버 전송 성공 후) */
    fun markAsSynced(eventIds: List<String>) {
        realm.writeBlocking {
            eventIds.forEach { id ->
                // AppUsageEvent
                query<AppUsageEvent>("_id == $0", id).first().find()?.let { event ->
                    event.synced = true
                    event.syncedAt = System.currentTimeMillis()
                }
                // MediaSessionEvent
                query<MediaSessionEvent>("_id == $0", id).first().find()?.let { event ->
                    event.synced = true
                    event.syncedAt = System.currentTimeMillis()
                }
            }
        }
        Log.d(TAG, "✅ ${eventIds.size}개 이벤트 synced 완료")
    }

    /** 전체 데이터 삭제 (테스트용) */
    fun clearAll() {
        realm.writeBlocking {
            delete(query<AppUsageEvent>())
            delete(query<MediaSessionEvent>())
        }
        Log.d(TAG, "🗑️ 전체 데이터 삭제 완료")
    }

    /** 30일 이상 된 데이터 삭제 (정기 정리용) */
    fun deleteOldData() {
        val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        realm.writeBlocking {
            delete(query<AppUsageEvent>("createdAt < $0", thirtyDaysAgo))
            delete(query<MediaSessionEvent>("createdAt < $0", thirtyDaysAgo))
        }
        Log.d(TAG, "🗑️ 30일 이상 된 데이터 삭제 완료")
    }

    private fun getToday(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}
