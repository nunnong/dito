package com.dito.app.core.background

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.dito.app.core.data.screentime.ScreenTimeRepository
import com.dito.app.core.data.screentime.ScreenTimeUpdateRequest
import com.dito.app.core.network.ApiService
import com.dito.app.core.storage.AuthTokenManager
import com.dito.app.core.storage.GroupPreferenceManager
import com.dito.app.core.util.ScreenTimeCollector
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * 스크린타임 동기화 Worker
 *
 * 15분마다 실행:
 * 1. 로컬 Realm에 즉시 저장 (오프라인 대비)
 * 2. Backend API 호출 → MongoDB에 저장
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

            // 오늘 하루 스크린타임 수집 (전체 + YouTube)
            val today = LocalDate.now().toString()
            val totalMinutes = screenTimeCollector.getTodayScreenTimeMinutes()
            val youtubeMinutes = screenTimeCollector.getYouTubeUsageMinutes()

            Log.d(TAG, "📊 스크린타임 수집 완료")
            Log.d(TAG, "  - 전체: ${totalMinutes}분")
            Log.d(TAG, "  - YouTube: ${youtubeMinutes}분")

            // 1단계: 로컬 Realm 저장 (오프라인 대비)
            val localId = ScreenTimeRepository.saveScreenTimeLocal(
                groupId = groupId,
                userId = userId,
                date = today,
                totalMinutes = totalMinutes
            )
            Log.d(TAG, "✅ [1/2] 로컬 저장 완료: $localId")

            // 2단계 - Backend API 호출 (YouTube 시간 포함)
            val apiSuccess = uploadToBackendAPI(
                groupId = groupId,
                date = today,
                totalMinutes = totalMinutes,
                youtubeMinutes = youtubeMinutes
            )

            // 결과 처리
            if (apiSuccess) {
                ScreenTimeRepository.markAsSynced(listOf(localId))
                Log.d(TAG, "✅ [2/2] Backend API 전송 성공 → MongoDB 저장 완료")
                Log.i(TAG, "========== 동기화 성공 ==========")
                Result.success()
            } else {
                Log.e(TAG, "❌ Backend API 실패 → 재시도 예정")
                Result.retry()
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ 스크린타임 동기화 중 예외 발생", e)
            Result.retry()
        }
    }

    /**
     * 수정: Backend API로 전송 (YouTube 시간 포함)
     */
    private suspend fun uploadToBackendAPI(
        groupId: Long,
        date: String,
        totalMinutes: Int,
        youtubeMinutes: Int
    ): Boolean {
        return try {
            val request = ScreenTimeUpdateRequest(
                groupId = groupId,
                date = date,
                totalMinutes = totalMinutes,
                youtubeMinutes = youtubeMinutes  // ✅ YouTube 시간 포함
            )

            val token = authTokenManager.getAccessToken()
            if (token == null) {
                Log.w(TAG, "⚠️ 토큰이 없어 Backend 전송 불가")
                return false
            }

            Log.d(TAG, "📤 Backend API 전송 중...")
            Log.d(TAG, "  - groupId: $groupId")
            Log.d(TAG, "  - date: $date")
            Log.d(TAG, "  - totalMinutes: $totalMinutes")
            Log.d(TAG, "  - youtubeMinutes: $youtubeMinutes")

            val response = apiService.updateScreenTime(
                token = token,
                request = request
            )

            if (response.isSuccessful && response.body()?.error == false) {
                val data = response.body()?.data
                Log.d(TAG, "✅ Backend API 전송 성공")
                Log.d(TAG, "  - status: ${data?.status}")
                Log.d(TAG, "  - totalMinutes: ${data?.totalMinutes}")
                Log.d(TAG, "  - youtubeMinutes: ${data?.youtubeMinutes}")
                true
            } else {
                Log.e(TAG, "❌ Backend API 전송 실패: ${response.code()} ${response.message()}")
                false
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Backend API 전송 예외", e)
            false
        }
    }

    /**
     * 활성 그룹 ID 가져오기
     */
    private fun getActiveGroupId(): Long? {
        return GroupPreferenceManager.getActiveGroupId(applicationContext)
    }

    /**
     * 사용자 ID 가져오기
     */
    private fun getUserId(): Long? {
        // AuthTokenManager가 user_prefs에 Int로 저장하므로, 먼저 user_prefs 확인
        val userPrefs = applicationContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userIdInt = userPrefs.getInt("user_id", -1)
        if (userIdInt > 0) {
            return userIdInt.toLong()
        }

        // fallback: dito_prefs도 확인 (레거시 호환성)
        val ditoPrefs = applicationContext.getSharedPreferences("dito_prefs", Context.MODE_PRIVATE)
        val userIdLong = ditoPrefs.getLong("user_id", -1L)
        return if (userIdLong > 0) userIdLong else null
    }
}