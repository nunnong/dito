package com.dito.app.core.data.screentime

import android.util.Log
import com.dito.app.core.data.RealmConfig
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import java.time.LocalDateTime

/**
 * 스크린타임 로컬 저장 Repository
 * Realm을 사용한 임시 저장 및 Backend API 동기화 관리
 */
object ScreenTimeRepository {

    private const val TAG = "ScreenTimeRepository"

    /**
     * 스크린타임 로컬 저장
     */
    suspend fun saveScreenTimeLocal(
        groupId: Long,
        userId: Long,
        date: String,
        totalMinutes: Int
    ): String = withContext(Dispatchers.IO)  {
        try {
            val realm = RealmConfig.getInstance()
            val recordedAt = LocalDateTime.now().toString()

            val log = ScreenTimeLocalLog().apply {
                this._id = ObjectId()
                this.groupId = groupId
                this.userId = userId
                this.date = date
                this.totalMinutes = totalMinutes
                this.recordedAt = recordedAt
                this.synced = false
            }

            realm.write {
                copyToRealm(log)
            }

            Log.d(TAG, "✅ 로컬 저장 성공: ${log._id} (${totalMinutes}분)")
            log._id.toHexString()

        } catch (e: Exception) {
            Log.e(TAG, "❌ 로컬 저장 실패", e)
            throw e
        }
    }

    /**
     * 동기화되지 않은 스크린타임 로그 조회
     */
    suspend fun getUnsyncedLogs(): List<ScreenTimeLocalLog> = withContext(Dispatchers.IO) {
        try {
            val realm = RealmConfig.getInstance()
            val results = realm.query<ScreenTimeLocalLog>("synced == false").find()

            // Realm 객체를 일반 객체로 복사
            results.map { log ->
                ScreenTimeLocalLog().apply {
                    this._id = log._id
                    this.groupId = log.groupId
                    this.userId = log.userId
                    this.date = log.date
                    this.totalMinutes = log.totalMinutes
                    this.recordedAt = log.recordedAt
                    this.synced = log.synced
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 미동기화 로그 조회 실패", e)
            emptyList()
        }
    }

    /**
     * 동기화 완료 표시
     */
    suspend fun markAsSynced(ids: List<String>) = withContext(Dispatchers.IO) {
        try {
            val realm = RealmConfig.getInstance()

            realm.write {
                ids.forEach { hexId ->
                    val objectId = ObjectId(hexId)
                    val log = query<ScreenTimeLocalLog>("_id == $0", objectId).first().find()
                    log?.synced = true
                }
            }

            Log.d(TAG, "✅ 동기화 완료 표시: ${ids.size}건")
        } catch (e: Exception) {
            Log.e(TAG, "❌ 동기화 완료 표시 실패", e)
        }
    }

    /**
     * 오래된 동기화 완료 로그 삭제 (7일 이상)
     */
    suspend fun cleanupOldSyncedLogs() = withContext(Dispatchers.IO) {
        try {
            val realm = RealmConfig.getInstance()
            val sevenDaysAgo = LocalDateTime.now().minusDays(7).toString()

            realm.write {
                val oldLogs = query<ScreenTimeLocalLog>(
                    "synced == true AND recordedAt < $0",
                    sevenDaysAgo
                ).find()

                delete(oldLogs)
                Log.d(TAG, "🧹 오래된 로그 삭제: ${oldLogs.size}건")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 로그 정리 실패", e)
        }
    }
}
