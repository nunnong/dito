package com.dito.app.core.background

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.dito.app.core.data.*
import com.dito.app.core.network.ApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@HiltWorker
class EventSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val apiService: ApiService
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "EventSyncWorker"
        private const val WORK_NAME = "event_batch_sync"

        fun setupPeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<EventSyncWorker>(
                30, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    15, TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
            Log.i(TAG, "WorkManager 등록 완료 (30분 주기)")
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "배치 전송 시작")

            val appEvents = RealmRepository.getUnsyncedAppEvents()
            val mediaEvents = RealmRepository.getUnsyncedMediaEvents()

            Log.d(TAG, "전송 대상: 앱 사용=${appEvents.size}, 미디어=${mediaEvents.size}")

            if (appEvents.isEmpty() && mediaEvents.isEmpty()) {
                return@withContext Result.success()
            }

            val appSuccess = uploadAppUsageEvents(appEvents.map { it.toDto() })
            val mediaSuccess = uploadMediaSessionEvents(mediaEvents.map { it.toDto() })

            when {
                appSuccess && mediaSuccess -> {
                    val allIds = appEvents.map { it._id.toHexString() } +
                            mediaEvents.map { it._id.toHexString() }
                    RealmRepository.markAsSynced(allIds)
                    Log.d(TAG, "✅ 전체 전송 성공 (${allIds.size}건)")
                    Result.success()
                }

                appSuccess && !mediaSuccess -> {
                    RealmRepository.markAsSynced(appEvents.map { it._id.toHexString() })
                    Log.w(TAG, "⚠️ 미디어 이벤트 실패 → 재시도 예정")
                    Result.retry()
                }

                !appSuccess && mediaSuccess -> {
                    RealmRepository.markAsSynced(mediaEvents.map { it._id.toHexString() })
                    Log.w(TAG, "⚠️ 앱 사용 이벤트 실패 → 재시도 예정")
                    Result.retry()
                }

                else -> {
                    Log.e(TAG, "❌ 전체 전송 실패 → 재시도 예정")
                    Result.retry()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "배치 전송 중 예외 발생", e)
            Result.retry()
        }
    }

    private suspend fun uploadAppUsageEvents(events: List<AppUsageEventDto>): Boolean {
        if (events.isEmpty()) return true

        val safeEvents = events.map { event ->
            val safeDuration = event.duration ?: 0L
            val safeDate = try {
                LocalDate.parse(event.event_date, DateTimeFormatter.ISO_DATE).toString()
            } catch (e: Exception) {
                LocalDate.now().toString()
            }
            event.copy(duration = safeDuration, event_date = safeDate)
        }

        return try {
            val request = AppUsageBatchRequest(safeEvents)
            val jwt = getJwtToken()

            val response = apiService.uploadAppUsageEvents(
                token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI3IiwidXNlcklkIjo3LCJpYXQiOjE3NjIyMzEwOTQsImV4cCI6MTc2MjIzNDY5NH0.J5V8GHouwmZeWXPzyRhfMJ73SnkyCwF-A3YVD8eo94c",
                request = request
            )

            if (response.isSuccessful && response.body()?.error == false) {
                Log.d(TAG, "✅ 앱 사용 이벤트 전송 성공 (${events.size}건)")
                true
            } else {
                Log.e(TAG, "❌ 앱 사용 이벤트 전송 실패: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 앱 사용 이벤트 전송 예외", e)
            false
        }
    }

    private suspend fun uploadMediaSessionEvents(events: List<MediaSessionEventDto>): Boolean {
        if (events.isEmpty()) return true

        val safeEvents = events.map { event ->
            val safeVideoDuration = event.video_duration ?: 0L
            val safeWatchTime = event.watch_time ?: 0L
            val safePauseTime = event.pause_time ?: 0L
            val safeDate = try {
                LocalDate.parse(event.event_date, DateTimeFormatter.ISO_DATE).toString()
            } catch (e: Exception) {
                LocalDate.now().toString()
            }
            event.copy(
                video_duration = safeVideoDuration,
                watch_time = safeWatchTime,
                pause_time = safePauseTime,
                event_date = safeDate
            )
        }

        return try {
            val request = MediaSessionBatchRequest(safeEvents)
            val jwt = getJwtToken()

            val response = apiService.uploadMediaSessionEvents(
                token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI3IiwidXNlcklkIjo3LCJpYXQiOjE3NjIyMzEwOTQsImV4cCI6MTc2MjIzNDY5NH0.J5V8GHouwmZeWXPzyRhfMJ73SnkyCwF-A3YVD8eo94c",
                request = request
            )

            if (response.isSuccessful && response.body()?.error == false) {
                Log.d(TAG, "✅ 미디어 이벤트 전송 성공 (${events.size}건)")
                true
            } else {
                Log.e(TAG, "❌ 미디어 이벤트 전송 실패: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌미디어 이벤트 전송 예외", e)
            false
        }
    }

    private fun getUserId(): Int {
        return 7
    }

    private fun getJwtToken(): String {
        val prefs = applicationContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getString("access_token", "") ?: ""
    }
}