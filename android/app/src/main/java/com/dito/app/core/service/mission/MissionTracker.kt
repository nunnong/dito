package com.dito.app.core.service.mission

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.dito.app.core.data.RealmRepository
import com.dito.app.core.data.mission.MissionTrackingLog
import com.dito.app.core.service.Checker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MissionTracker @Inject constructor(
    @ApplicationContext private val context: Context
){
    companion object{
        private const val TAG = "MissionTracker"
        private const val START_DELAY_SECONDS = 20

        @Volatile
        private var currentMissionId: String? = null

        @Volatile
        private var currentMissionInfo: com.dito.app.core.network.MissionInfo? = null

        private var sequenceCounter = AtomicInteger(0)

        //미션 시작 시점의 앱 정보 저장
        @Volatile
        private var missionStartAppPackage: String? = null

        @Volatile
        private var missionStartTime: Long = 0L
    }

    private val handler = Handler(Looper.getMainLooper())
    private var startTrackingRunnable: Runnable? = null

    fun startTracking(missionData: MissionData){
        if (currentMissionId == missionData.missionId) {
            Log.w(TAG, "⚠️ 이미 추적 중인 미션: ${missionData.missionId}")
            return
        }

        if (currentMissionId != null) {
            Log.w(TAG, "⚠️ 기존 미션($currentMissionId) 종료 후 새 미션 시작")
            startTrackingRunnable?.let { handler.removeCallbacks(it) }
            WorkManager.getInstance(context)
                .cancelUniqueWork("mission_eval_$currentMissionId")
            stopTracking()
        }

        Log.i(TAG, "🎯 미션 수신: ${missionData.missionId}")
        Log.d(TAG, "   타입: ${missionData.missionType}")
        Log.d(TAG, "   지시: ${missionData.instruction}")
        Log.d(TAG, "   ${START_DELAY_SECONDS}초 후 시작 예정")

        // 20초 후 추적 시작 (Progress 알림 표시)
        startTrackingRunnable = Runnable {
            Log.i(TAG, "⏰ ${START_DELAY_SECONDS}초 대기 완료 - 미션 시작")
            actualStartTracking(missionData)
        }
        handler.postDelayed(startTrackingRunnable!!, START_DELAY_SECONDS * 1000L)
    }

    private fun actualStartTracking(missionData: MissionData) {
        val actualStartTime = System.currentTimeMillis()
        missionStartTime = actualStartTime

        currentMissionId = missionData.missionId
        currentMissionInfo = com.dito.app.core.network.MissionInfo(
            type = missionData.missionType,
            instruction = missionData.instruction,
            durationSeconds = missionData.durationSeconds,
            targetApps = missionData.targetApps,
            startTime = Checker.formatTimestamp(actualStartTime),
            endTime = Checker.formatTimestamp(actualStartTime + missionData.durationSeconds * 1000L)
        )

        sequenceCounter.set(0)

        Log.i(TAG, "✅ 미션 추적 실제 시작: ${missionData.missionId}")
        Log.d(TAG, "   시작 시간: ${Checker.formatTimestamp(actualStartTime)}")
        Log.d(TAG, "   종료 예정: ${Checker.formatTimestamp(actualStartTime + missionData.durationSeconds * 1000L)}")

        // MissionProgressService 시작 (Progress 알림 표시)
        val serviceIntent = Intent(context, MissionProgressService::class.java).apply {
            putExtra("mission_id", missionData.missionId)
            putExtra("mission_type", missionData.missionType)
            putExtra("instruction", missionData.instruction)
            putExtra("duration_seconds", missionData.durationSeconds)
            putExtra("coin_reward", missionData.coinReward)
            putExtra("deep_link", missionData.deepLink)
            putExtra("start_time_ms", actualStartTime)
            putExtra("delay_seconds", START_DELAY_SECONDS)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
        Log.d(TAG, "🔔 MissionProgressService 시작")

        // 미션 시작 시 현재 사용 중인 앱 기록
        recordCurrentApp()

        // 미션 시간 + 딜레이 후에 평가 예약
        scheduleEvaluation(missionData, START_DELAY_SECONDS.toLong())
    }

    // 미션 시작 시 현재 앱 기록
    private fun recordCurrentApp() {
        try {
            // AppMonitoringService에서 현재 앱 정보 가져오기
            val appInfo = com.dito.app.core.service.phone.AppMonitoringService.getCurrentAppInfo()

            if (appInfo != null) {
                val (packageName, _) = appInfo

                val packageManager = context.packageManager
                val appName = try {
                    val appInfoObj = packageManager.getApplicationInfo(packageName, 0)
                    packageManager.getApplicationLabel(appInfoObj).toString()
                } catch (e: Exception) {
                    packageName
                }

                // 시작 앱 정보 저장
                missionStartAppPackage = packageName

                Log.d(TAG, "📱 미션 시작 시점의 앱: $appName")

                // 시작 마커 기록 (duration=0)
                val targetApps = currentMissionInfo?.targetApps ?: emptyList()
                val log = MissionTrackingLog().apply {
                    this.missionId = currentMissionId!!
                    this.logType = "APP_USAGE"
                    this.sequence = sequenceCounter.incrementAndGet()
                    this.timestamp = System.currentTimeMillis()
                    this.packageName = packageName
                    this.appName = appName
                    this.durationSeconds = 0  // 시작 마커
                    this.isTargetApp = targetApps.contains(packageName)
                }

                RealmRepository.insertMissionLog(log)

                val targetFlag = if (log.isTargetApp == true) "⚠️ 타겟" else "일반"
                Log.d(TAG, "📌 미션 시작 앱 마킹: $appName (0초) [$targetFlag]")
            } else {
                Log.w(TAG, "⚠️ AppMonitoringService에서 현재 앱 정보를 가져올 수 없음")
            }
        } catch (e: Exception) {
            Log.e(TAG, "현재 앱 기록 실패", e)
        }
    }

    private fun scheduleEvaluation(missionData: MissionData, delaySeconds: Long) {
        val totalDelaySeconds = delaySeconds + missionData.durationSeconds

        val workRequest = OneTimeWorkRequestBuilder<com.dito.app.core.background.MissionEvaluationWorker>()
            .setInitialDelay(totalDelaySeconds, TimeUnit.SECONDS)
            .setInputData(
                workDataOf(
                    "mission_id" to missionData.missionId,
                    "mission_type" to missionData.missionType,
                    "instruction" to missionData.instruction,
                    "duration_seconds" to missionData.durationSeconds,
                    "target_apps" to missionData.targetApps.joinToString(","),
                    "start_time" to currentMissionInfo!!.startTime,
                    "end_time" to currentMissionInfo!!.endTime
                )
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                15, TimeUnit.MINUTES
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "mission_eval_${missionData.missionId}",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

        Log.d(TAG, "⏰ WorkManager 스케줄: ${totalDelaySeconds}초 후 평가")
    }

    fun onAppSwitch(packageName: String, appName: String, durationSeconds: Int) {
        val missionId = currentMissionId ?: return
        val targetApps = currentMissionInfo?.targetApps ?: emptyList()

        // 시작 앱이 처음 전환될 때 실제 사용 시간 계산
        val actualDuration = if (packageName == missionStartAppPackage && missionStartAppPackage != null) {
            // 미션 시작부터 지금까지의 실제 시간
            val elapsedTime = (System.currentTimeMillis() - missionStartTime) / 1000
            Log.d(TAG, "📊 시작 앱($appName) 실제 사용 시간: ${elapsedTime}초 (입력값: ${durationSeconds}초 무시)")
            elapsedTime.toInt()
        } else {
            durationSeconds
        }

        val log = MissionTrackingLog().apply {
            this.missionId = missionId
            this.logType = "APP_USAGE"
            this.sequence = sequenceCounter.incrementAndGet()
            this.timestamp = System.currentTimeMillis()
            this.packageName = packageName
            this.appName = appName
            this.durationSeconds = actualDuration
            this.isTargetApp = targetApps.contains(packageName)
        }

        RealmRepository.insertMissionLog(log)

        val targetFlag = if (log.isTargetApp == true) "⚠️ 타겟" else "일반"
        Log.d(TAG, "📱 앱 사용 기록: $appName (${actualDuration}초) [$targetFlag]")

        // 시작 앱 기록 후 초기화
        if (packageName == missionStartAppPackage) {
            missionStartAppPackage = null
        }
    }

    fun onMediaEvent(
        packageName: String,
        videoTitle: String,
        channelName: String,
        watchTimeSeconds: Int,
        eventType: String
    ) {
        val missionId = currentMissionId ?: return
        val contentType = determineContentType(videoTitle, channelName)

        val log = MissionTrackingLog().apply {
            this.missionId = missionId
            this.logType = "MEDIA_SESSION"
            this.sequence = sequenceCounter.incrementAndGet()
            this.timestamp = System.currentTimeMillis()
            this.packageName = packageName
            this.videoTitle = videoTitle
            this.channelName = channelName
            this.eventType = eventType
            this.watchTimeSeconds = watchTimeSeconds
            this.contentType = contentType.toString()
        }

        RealmRepository.insertMissionLog(log)
        Log.d(TAG, "🎥 미디어 기록: $videoTitle ($contentType, ${watchTimeSeconds}초)")
    }

    fun onScreenEvent(isScreenOn: Boolean){
        val missionId = currentMissionId ?: return

        val log = MissionTrackingLog().apply {
            this.missionId = missionId
            this.logType = if(isScreenOn) "SCREEN_ON" else "SCREEN_OFF"
            this.sequence = sequenceCounter.incrementAndGet()
            this.timestamp = System.currentTimeMillis()
        }

        RealmRepository.insertMissionLog(log)
        Log.d(TAG, "📱 화면 상태: ${if (isScreenOn) "ON" else "OFF"}")
    }

    private fun determineContentType(title: String, channel: String): String {
        val educationalKeywords = setOf(
            "강의", "lecture", "tutorial", "강좌", "공부", "study",
            "배우기", "learn", "교육", "education", "수업", "class",
            "코딩", "programming", "개발", "development"
        )

        val entertainmentKeywords = setOf(
            "브이로그", "vlog", "먹방", "mukbang", "게임", "game",
            "예능", "entertainment", "리액션", "reaction", "쇼츠", "shorts"
        )

        val lowerTitle = title.lowercase()
        val lowerChannel = channel.lowercase()

        val eduCount = educationalKeywords.count { keyword ->
            lowerTitle.contains(keyword) || lowerChannel.contains(keyword)
        }

        val entCount = entertainmentKeywords.count { keyword ->
            lowerTitle.contains(keyword) || lowerChannel.contains(keyword)
        }

        return when {
            eduCount >= 3 -> "EDUCATIONAL"
            entCount >= 1 -> "ENTERTAINMENT"
            else -> "UNKNOWN"
        }
    }

    fun stopTracking(){
        Log.i(TAG, "미션 추적 종료: $currentMissionId")

        // MissionProgressService 중지
        try {
            context.stopService(Intent(context, MissionProgressService::class.java))
            Log.d(TAG, "🔔 MissionProgressService 중지")
        } catch (e: Exception) {
            Log.e(TAG, "MissionProgressService 중지 실패", e)
        }

        startTrackingRunnable?.let { handler.removeCallbacks(it) }
        startTrackingRunnable = null
        currentMissionId = null
        currentMissionInfo = null
        sequenceCounter.set(0)
        missionStartAppPackage = null
        missionStartTime = 0L
    }

    fun isTracking(): Boolean = currentMissionId != null
    fun getCurrentMissionId(): String? = currentMissionId
    fun getCurrentMissionStartTime(): Long = missionStartTime
    fun getCurrentMissionDuration(): Int? = currentMissionInfo?.durationSeconds

    /**
     * 미션 종료 시점에 현재 사용 중인 앱을 강제로 기록
     */
//    fun recordFinalApp() {
//        val missionId = currentMissionId ?: return
//
//        try {
//            val currentTime = System.currentTimeMillis()
//
//            // AppMonitoringService에서 현재 앱 정보 가져오기
//            val appInfo = com.dito.app.core.service.phone.AppMonitoringService.getCurrentAppInfo()
//
//            if (appInfo != null) {
//                val (packageName, startTime) = appInfo
//
//                val packageManager = context.packageManager
//                val appName = try {
//                    val appInfoObj = packageManager.getApplicationInfo(packageName, 0)
//                    packageManager.getApplicationLabel(appInfoObj).toString()
//                } catch (e: Exception) {
//                    packageName
//                }
//
//                // 미션 시작부터 종료까지의 시간 계산
//                val elapsedSeconds = ((currentTime - missionStartTime) / 1000).toInt()
//
//                val targetApps = currentMissionInfo?.targetApps ?: emptyList()
//                val log = MissionTrackingLog().apply {
//                    this.missionId = missionId
//                    this.logType = "APP_USAGE"
//                    this.sequence = sequenceCounter.incrementAndGet()
//                    this.timestamp = currentTime
//                    this.packageName = packageName
//                    this.appName = appName
//                    this.durationSeconds = elapsedSeconds
//                    this.isTargetApp = targetApps.contains(packageName)
//                }
//
//                RealmRepository.insertMissionLog(log)
//
//                val targetFlag = if (log.isTargetApp == true) "⚠️ 타겟" else "일반"
//                Log.d(TAG, "🏁 미션 종료 시점 앱 기록: $appName (${elapsedSeconds}초) [$targetFlag]")
//            } else {
//                // AppMonitoringService가 앱 정보를 제공하지 못한 경우,
//                // 시작 마커로 저장한 앱 정보 활용
//                if (missionStartAppPackage != null) {
//                    val packageManager = context.packageManager
//                    val appName = try {
//                        val appInfoObj = packageManager.getApplicationInfo(missionStartAppPackage!!, 0)
//                        packageManager.getApplicationLabel(appInfoObj).toString()
//                    } catch (e: Exception) {
//                        missionStartAppPackage!!
//                    }
//
//                    val elapsedSeconds = ((currentTime - missionStartTime) / 1000).toInt()
//                    val targetApps = currentMissionInfo?.targetApps ?: emptyList()
//                    val log = MissionTrackingLog().apply {
//                        this.missionId = missionId
//                        this.logType = "APP_USAGE"
//                        this.sequence = sequenceCounter.incrementAndGet()
//                        this.timestamp = currentTime
//                        this.packageName = missionStartAppPackage!!
//                        this.appName = appName
//                        this.durationSeconds = elapsedSeconds
//                        this.isTargetApp = targetApps.contains(missionStartAppPackage!!)
//                    }
//
//                    RealmRepository.insertMissionLog(log)
//
//                    val targetFlag = if (log.isTargetApp == true) "⚠️ 타겟" else "일반"
//                    Log.d(TAG, "🏁 미션 종료 시점 앱 기록 (백업): $appName (${elapsedSeconds}초) [$targetFlag]")
//                } else {
//                    Log.w(TAG, "⚠️ 미션 종료 시점에 사용 중인 앱 정보를 가져올 수 없음")
//                }
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "미션 종료 시점 앱 기록 실패", e)
//        }
//    }
}