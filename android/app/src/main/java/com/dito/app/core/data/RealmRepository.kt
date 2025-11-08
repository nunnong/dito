package com.dito.app.core.data

import android.util.Log
import com.dito.app.core.data.mission.MissionTrackingLog
import com.dito.app.core.data.phone.AppUsageEvent
import com.dito.app.core.data.phone.MediaSessionEvent
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
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
            "synced == false AND eventType == $0",
            "APP_CLOSE"
        ).find()
    }

    fun getUnsyncedMediaEvents(): List<MediaSessionEvent> {
        return realm.query<MediaSessionEvent>(
            "synced == false AND eventType == $0",
            "VIDEO_END"
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

    fun insertMissionLog(log: MissionTrackingLog){
        realm.writeBlocking {
            copyToRealm(log)
        }
        Log.d(TAG, "✅ 미션 로그 저장 완료: missionId=${log.missionId}, seq=${log.sequence}, type=${log.logType}, app=${log.appName}")
    }

    fun getMissionLogs(missionId: String): List<MissionTrackingLog>{
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "getMissionLogs 호출: missionId='$missionId'")

        // 전체 미션 로그 수 확인
        val allLogs = realm.query<MissionTrackingLog>().find()
        Log.d(TAG, "  전체 MissionTrackingLog 수: ${allLogs.size}")

        // 해당 missionId를 가진 모든 로그 (synced 무관)
        val allMissionLogs = realm.query<MissionTrackingLog>(
            "missionId == $0",
            missionId
        ).find()
        Log.d(TAG, "  missionId='$missionId'인 전체 로그: ${allMissionLogs.size}개")

        allMissionLogs.forEachIndexed { index, log ->
            Log.d(TAG, "    [$index] seq=${log.sequence}, type=${log.logType}, synced=${log.synced}, app=${log.appName}")
        }

        val unsyncedLogs = realm.query<MissionTrackingLog>(
            "missionId == $0 AND synced == false",
            missionId
        )
            .sort("sequence", Sort.ASCENDING)
            .find()

        Log.d(TAG, "  synced=false인 로그: ${unsyncedLogs.size}개")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")

        return unsyncedLogs
    }

    fun markMissionLogsSynced(missionId: String) {
        var syncedCount = 0
        realm.writeBlocking {
            val logs = query<MissionTrackingLog>("missionId == $0", missionId).find()
            logs.forEach {
                it.synced = true
                it.syncedAt = System.currentTimeMillis()
            }
            syncedCount = logs.size
        }
        Log.d(TAG, "✅ 미션 로그 ${syncedCount}개 synced 완료")
    }


}