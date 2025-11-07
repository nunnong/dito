package com.dito.app.core.background

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.dito.app.core.data.screentime.MongoDBDirectClient
import com.dito.app.core.data.screentime.ScreenTimeRepository
import com.dito.app.core.data.screentime.ScreenTimeUpdateRequest
import com.dito.app.core.data.screentime.toSnapshotMongo
import com.dito.app.core.data.screentime.toSummaryMongo
import com.dito.app.core.network.ApiService
import com.dito.app.core.storage.AuthTokenManager
import com.dito.app.core.util.ScreenTimeCollector
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * 스크린타임 동기화 Worker (하이브리드 방식)
 *
 * 5분마다 실행:
 * 1. 로컬 Realm에 즉시 저장
 * 2. MongoDB Atlas에 직접 저장 (빠른 저장)
 * 3. Backend API 호출 (검증 & Summary 생성)
 */
@HiltWorker
class ScreenTimeSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val apiService: ApiService,
    private val authTokenManager: AuthTokenManager
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "ScreenTimeSyncWorker"
        private const val WORK_NAME = "screen_time_sync"

        /**
         * 5분 주기 스크린타임 동기화 설정
         */
        fun setupPeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<ScreenTimeSyncWorker>(
                15, TimeUnit.MINUTES  // 최소 15분 (Android 제한)
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    5, TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
            Log.i(TAG, "✅ 스크린타임 동기화 WorkManager 등록 완료 (15분 주기)")
        }

        /**
         * 즉시 한번 실행
         */
        fun triggerImmediateSync(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<ScreenTimeSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context)
                .enqueue(workRequest)
            Log.i(TAG, "📤 스크린타임 즉시 동기화 요청")
        }
    }

    private val screenTimeCollector = ScreenTimeCollector(applicationContext)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "========== 스크린타임 동기화 시작 ==========")

            // 현재 참여 중인 그룹 ID 가져오기
            val groupId = getActiveGroupId()
            if (groupId == null) {
                Log.w(TAG, "⚠️ 활성 그룹이 없어 스크린타임 동기화 스킵")
                return@withContext Result.success()
            }

            // 현재 사용자 ID 가져오기
            val userId = getUserId()
            if (userId == null) {
                Log.w(TAG, "⚠️ 사용자 ID가 없어 스크린타임 동기화 스킵")
                return@withContext Result.failure()
            }

            // 권한 확인
            if (!screenTimeCollector.hasUsageStatsPermission()) {
                Log.w(TAG, "⚠️ PACKAGE_USAGE_STATS 권한이 없습니다")
                return@withContext Result.failure()
            }

            // 오늘 하루 스크린타임 수집
            val todayScreenTime = screenTimeCollector.getTodayScreenTimeMinutes()
            val today = LocalDate.now().toString()

            Log.d(TAG, "📊 오늘 스크린타임: ${todayScreenTime}분 (그룹 ID: $groupId)")

            // === 하이브리드 동기화 시작 ===

            // 1단계: 로컬 Realm 저장 (즉시)
            val localId = ScreenTimeRepository.saveScreenTimeLocal(
                groupId = groupId,
                userId = userId,
                date = today,
                totalMinutes = todayScreenTime
            )
            Log.d(TAG, "✅ [1/3] 로컬 저장 완료: $localId")

            // 2단계: MongoDB Atlas 직접 저장 (병렬)
            val mongoSuccess = async {
                saveToMongoDirectly(groupId, userId, today, todayScreenTime)
            }

            // 3단계: Backend API 호출 (병렬)
            val apiSuccess = async {
                uploadToBackendAPI(groupId, today, todayScreenTime)
            }

            val mongoResult = mongoSuccess.await()
            val apiResult = apiSuccess.await()

            // 결과 처리
            when {
                mongoResult && apiResult -> {
                    ScreenTimeRepository.markAsSynced(listOf(localId))
                    Log.d(TAG, "✅ [2/3] MongoDB 직접 저장 성공")
                    Log.d(TAG, "✅ [3/3] Backend API 전송 성공")
                    Log.i(TAG, "========== 전체 동기화 성공 ==========")
                    Result.success()
                }
                mongoResult && !apiResult -> {
                    Log.w(TAG, "⚠️ Backend API 실패 (MongoDB 저장 성공)")
                    Result.retry()
                }
                !mongoResult && apiResult -> {
                    ScreenTimeRepository.markAsSynced(listOf(localId))
                    Log.w(TAG, "⚠️ MongoDB 실패 (Backend 저장 성공)")
                    Result.success()
                }
                else -> {
                    Log.e(TAG, "❌ MongoDB & Backend 모두 실패 → 재시도 예정")
                    Result.retry()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ 스크린타임 동기화 중 예외 발생", e)
            Result.retry()
        }
    }

    /**
     * MongoDB Atlas에 직접 저장
     */
    private suspend fun saveToMongoDirectly(
        groupId: Long,
        userId: Long,
        date: String,
        totalMinutes: Int
    ): Boolean {
        return try {
            val logs = ScreenTimeRepository.getUnsyncedLogs()
            if (logs.isEmpty()) {
                Log.d(TAG, "MongoDB 저장할 로그 없음")
                return true
            }

            var successCount = 0
            logs.forEach { log ->
                // Summary upsert
                val summarySuccess = MongoDBDirectClient.upsertSummary(log.toSummaryMongo())
                // Snapshot insert
                val snapshotSuccess = MongoDBDirectClient.insertSnapshot(log.toSnapshotMongo())

                if (summarySuccess && snapshotSuccess) {
                    successCount++
                }
            }

            Log.d(TAG, "MongoDB 직접 저장: $successCount/${logs.size}건 성공")
            successCount > 0

        } catch (e: Exception) {
            Log.e(TAG, "MongoDB 직접 저장 중 예외", e)
            false
        }
    }

    /**
     * Backend API로 전송
     */
    private suspend fun uploadToBackendAPI(groupId: Long, date: String, totalMinutes: Int): Boolean {
        return try {
            val request = ScreenTimeUpdateRequest(
                groupId = groupId,
                date = date,
                totalMinutes = totalMinutes
            )

            val token = authTokenManager.getBearerToken()
            if (token == null) {
                Log.w(TAG, "⚠️ 토큰이 없어 Backend 전송 불가")
                return false
            }

            val response = apiService.updateScreenTime(
                token = token,
                request = request
            )

            if (response.isSuccessful && response.body()?.error == false) {
                val data = response.body()?.data
                Log.d(TAG, "Backend API 전송 성공: ${data?.status}")
                true
            } else {
                Log.e(TAG, "Backend API 전송 실패: ${response.code()} ${response.message()}")
                false
            }

        } catch (e: Exception) {
            Log.e(TAG, "Backend API 전송 예외", e)
            false
        }
    }

    /**
     * 활성 그룹 ID 가져오기
     */
    private fun getActiveGroupId(): Long? {
        val sharedPref = applicationContext.getSharedPreferences("dito_prefs", Context.MODE_PRIVATE)
        val groupId = sharedPref.getLong("active_group_id", -1L)
        return if (groupId > 0) groupId else null
    }

    /**
     * 사용자 ID 가져오기
     */
    private fun getUserId(): Long? {
        val sharedPref = applicationContext.getSharedPreferences("dito_prefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getLong("user_id", -1L)
        return if (userId > 0) userId else null
    }
}
