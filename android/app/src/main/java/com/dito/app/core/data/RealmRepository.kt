package com.dito.app.core.data

import android.util.Log
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId
import java.text.SimpleDateFormat
import java.util.*

object RealmRepository {

    private const val TAG = "RealmRepository"

    private val realm: Realm
        get() = RealmConfig.getInstance()

    fun getTodayAppEvents(): List<AppUsageEvent> {
        val today = getToday()
        return realm.query<AppUsageEvent>("date == $0", today).find()
    }

    fun getTodayMediaEvents(): List<MediaSessionEvent> {
        val today = getToday()
        return realm.query<MediaSessionEvent>("date == $0", today).find()
    }

    fun getUnsyncedAppEvents(): List<AppUsageEvent> {
        return realm.query<AppUsageEvent>("synced == false").find()
    }

    fun getUnsyncedMediaEvents(): List<MediaSessionEvent> {
        return realm.query<MediaSessionEvent>("synced == false").find()
    }


    fun markAsSynced(eventIds: List<String>) {
        realm.writeBlocking {
            eventIds.forEach { hexId ->
                try {
                    // String(HexString)을 ObjectId로 변환
                    val objectId = BsonObjectId(hexId)

                    // AppUsageEvent 찾기
                    query<AppUsageEvent>("_id == $0", objectId).first().find()?.let { event ->
                        event.synced = true
                        event.syncedAt = System.currentTimeMillis()
                    }

                    // MediaSessionEvent 찾기
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
        Log.d(TAG, "🗑️ 30일 이상 된 데이터 삭제 완료")
    }

    private fun getToday(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}