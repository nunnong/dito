package com.dito.app.core.background

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dito.app.core.data.RealmRepository
import com.dito.app.core.network.AIEvaluationRequest
import com.dito.app.core.network.AIService
import com.dito.app.core.network.BehaviorLogEntry
import com.dito.app.core.network.MissionInfo
import com.dito.app.core.service.Checker
import com.dito.app.core.service.mission.MissionTracker
import com.dito.app.core.storage.AuthTokenManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class MissionEvaluationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val aiService: AIService,
    private val authTokenManager: AuthTokenManager,
    private val missionTracker: MissionTracker
): CoroutineWorker(context, params){

    companion object{
        private const val TAG = "MissionEvalWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO){
        try {
            val missionId = inputData.getString("mission_id") ?: return@withContext Result.failure()
            val missionType = inputData.getString("mission_type") ?: "REST"
            val instruction = inputData.getString("instruction") ?: ""
            val durationSeconds = inputData.getInt("duration_seconds", 300)
            val targetAppsStr = inputData.getString("target_apps") ?: ""
            val targetApps = if (targetAppsStr.isNotEmpty()) targetAppsStr.split(",") else emptyList()
            val startTime = inputData.getString("start_time") ?: ""
            val endTime = inputData.getString("end_time") ?: ""

            Log.i(TAG, "📊 미션 평가 시작: $missionId")
            Log.d(TAG, "   타입: $missionType, 시간: ${durationSeconds}초")

            //1. realm에서 미션 추적 로그 수집
            val logs = RealmRepository.getMissionLogs(missionId)
            Log.d(TAG, "수집된 로그: ${logs.size}개")

            //2. behavior log 포맷으로 전환
            val behaviorLogs = logs.map { log ->
                BehaviorLogEntry(
                    logType = log.logType,
                    sequence = log.sequence,
                    timestamp = Checker.formatTimestamp(log.timestamp),
                    packageName = log.packageName,
                    appName = log.appName,
                    durationSeconds = log.durationSeconds,
                    isTargetApp = log.isTargetApp,
                    videoTitle = log.videoTitle,
                    channelName = log.channelName,
                    eventType = log.eventType,
                    watchTimeSeconds = log.watchTimeSeconds,
                    contentType = log.contentType
                )
            }

            //3. requestbody
            val request = AIEvaluationRequest(
                userId = authTokenManager.getPersonalId() ?: "",
                missionId = missionId,
                missionInfo = MissionInfo(
                    type = missionType,
                    instruction = instruction,
                    durationSeconds = durationSeconds,
                    targetApps = targetApps,
                    startTime = startTime,
                    endTime = endTime
                ),
                behaviorLogs = behaviorLogs
            )

            //4. 데이터 전송
            val response = aiService.evaluationMission(request)

            if(response.isSuccessful){
                RealmRepository.markMissionLogsSynced(missionId)
                missionTracker.stopTracking()
                Log.i(TAG, "✅ 미션 평가 전송 성공")
                Result.success()                                 // work 완료 처리
            } else {
                Log.e(TAG, "❌ 미션 평가 전송 실패: ${response.code()}")
                Result.retry()                                   // 실패 시 재시도 트리거
            }
        }catch (e: Exception){
            Log.e(TAG, "미션 평가 예외", e)
            Result.retry()
        }
    }


}