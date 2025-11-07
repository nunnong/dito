package com.dito.app.core.service.mission

import android.content.Context
import android.util.Log
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

        @Volatile
        private var currentMissionId: String? = null

        @Volatile
        private var currentMissionInfo: com.dito.app.core.network.MissionInfo? = null

        private var sequenceCounter = AtomicInteger(0) //로그 순서 카운트
    }

    fun startTracking(missionData: MissionData){
        // 20초 후 실제 추적 시작
        val DELAY_SECONDS = 20L
        val actualStartTime = System.currentTimeMillis() + (DELAY_SECONDS * 1000L)

        //이미 같은 미션 추적 중이면 무시
        if (currentMissionId == missionData.missionId) {
            Log.w(TAG, "⚠️ 이미 추적 중인 미션: ${missionData.missionId}")
            return
        }


        if (currentMissionId != null) {
            Log.w(TAG, "⚠️ 기존 미션($currentMissionId) 종료 후 새 미션 시작")

            // WorkManager 취소
            WorkManager.getInstance(context)
                .cancelUniqueWork("mission_eval_$currentMissionId")
            Log.d(TAG, "🚫 기존 WorkManager 취소: mission_eval_$currentMissionId")

            stopTracking()
        }



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

        Log.i(TAG, "🎯 미션 수신: ${missionData.missionId}")
        Log.d(TAG, "   ⏳ ${DELAY_SECONDS}초 후 추적 시작 예정")
        Log.d(TAG, "   타입: ${missionData.missionType}")
        Log.d(TAG, "   지시: ${missionData.instruction}")

        // WorkManager로 (20초 + 미션시간) 후 평가
        scheduleEvaluation(missionData, DELAY_SECONDS)
    }

    private fun scheduleEvaluation(missionData: MissionData, delaySeconds: Long) {
        // 총 대기 시간 = 20초 지연 + 미션 수행 시간
        val totalDelaySeconds = delaySeconds + missionData.durationSeconds

        val workRequest = OneTimeWorkRequestBuilder<com.dito.app.core.background.MissionEvaluationWorker>()
            .setInitialDelay(totalDelaySeconds, TimeUnit.SECONDS)  // 변경!
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

        Log.d(TAG, "⏰ WorkManager 스케줄: ${totalDelaySeconds}초 후 평가 (지연 ${delaySeconds}초 + 미션 ${missionData.durationSeconds}초)")
    }

    //앱 전환 기록
    fun onAppSwitch(packageName: String, appName: String, durationSeconds: Int) {
        val missionId = currentMissionId ?: return // 현재 미션 없으면 종료
        val targetApps = currentMissionInfo?.targetApps ?: emptyList() // 대상 앱 목록 확보

        val log = MissionTrackingLog().apply { // Realm 엔티티로 새 로그 생성
            this.missionId = missionId
            this.logType = "APP_USAGE"
            this.sequence = sequenceCounter.incrementAndGet()
            this.timestamp = System.currentTimeMillis()
            this.packageName = packageName
            this.appName = appName
            this.durationSeconds = durationSeconds
            this.isTargetApp = targetApps.contains(packageName)
        }

        RealmRepository.insertMissionLog(log)

        val targetFlag = if (log.isTargetApp) "⚠️ 타겟" else "일반"
        Log.d(TAG, "📱 앱 사용 기록: $appName (${durationSeconds}초) [$targetFlag]")
    }

    //media
    fun onMediaEvent(
        packageName: String,
        videoTitle: String,
        channelName: String,
        watchTimeSeconds: Int,
        eventType: String
    ) {
        val missionId = currentMissionId ?: return // 미션 없으면 종료

        // 영상 제목과 채널명 기반으로 콘텐츠 타입 결정
        val contentType = determineContentType(videoTitle, channelName)

        val log = MissionTrackingLog().apply { // Realm 엔티티로 새 로그 생성
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
        Log.d(TAG, "🎥 미디어 기록: $videoTitle ($contentType, ${watchTimeSeconds}초)") // 로그 결과 출력
    }

    //화면 상태 기록 -> 삭제 가능
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

        return when { // 콘텐츠 타입 판정 로직
            eduCount >= 2 -> "EDUCATIONAL"      // 교육 키워드 2개 이상: 교육 콘텐츠
            entCount >= 1 -> "ENTERTAINMENT"    // 엔터테인먼트 키워드 1개 이상: 엔터테인먼트 콘텐츠
            else -> "UNKNOWN"                   // 둘 다 해당 안 되면: 알 수 없음
        }
    }

    fun stopTracking(){
        Log.i(TAG, "미션 추적 종료: $currentMissionId")
        currentMissionId = null
        currentMissionInfo = null
        sequenceCounter.set(0)
    }

    fun isTracking(): Boolean = currentMissionId != null
    fun getCurrentMissionId(): String? = currentMissionId



}
