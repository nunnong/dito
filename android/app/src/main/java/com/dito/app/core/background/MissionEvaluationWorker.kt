package com.dito.app.core.background

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dito.app.core.data.RealmRepository
import com.dito.app.core.network.AIEvaluationErrorResponse
import com.dito.app.core.network.AIEvaluationRequest
import com.dito.app.core.network.AIEvaluationResponse
import com.dito.app.core.network.AIService
import com.dito.app.core.network.BehaviorLogEntry
import com.dito.app.core.network.MissionInfo
import com.dito.app.core.notification.ProgressNotificationHelper
import com.dito.app.core.service.Checker
import com.dito.app.core.service.mission.MissionProgressService
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

            // MissionProgressService 중지 보장 (혹시 모를 경우 대비)
            try {
                applicationContext.stopService(Intent(applicationContext, MissionProgressService::class.java))
                Log.d(TAG, "🔔 MissionProgressService 중지 보장 완료")
            } catch (e: Exception) {
                Log.w(TAG, "MissionProgressService 중지 시도 중 에러 (이미 중지되었을 수 있음): ${e.message}")
            }

//            triggerFinalAppRecord()

            // 1. Realm에서 미션 추적 로그 수집
            val logs = RealmRepository.getMissionLogs(missionId)
            Log.d(TAG, "수집된 로그: ${logs.size}개")

            // 2. behavior log 포맷으로 변환
            val behaviorLogs = logs.map { log ->
                BehaviorLogEntry(
                    logType = log.logType,
                    sequence = log.sequence,
                    timestamp = Checker.formatTimestamp(log.timestamp),
                    packageName = log.packageName,
                    appName = log.appName,
                    durationSeconds = log.durationSeconds,
                    isTargetApp = if (log.logType == "APP_USAGE" || log.logType == "MEDIA_SESSION") log.isTargetApp else null,
                    videoTitle = log.videoTitle,
                    channelName = log.channelName,
                    eventType = log.eventType,
                    watchTimeSeconds = log.watchTimeSeconds,
                    contentType = log.contentType
                )
            }

            // 3. request body
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

            // 4. 데이터 전송
            val response = aiService.evaluationMission(request)

            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "서버 응답 코드: ${response.code()}")

            if (response.isSuccessful) {

                val responseBody = response.body()?.string()
                Log.d(TAG, "응답 body: $responseBody")

                try {
                    val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                    val successResponse = json.decodeFromString<AIEvaluationResponse>(responseBody ?: "{}")
                    Log.i(TAG, "✅ 미션 평가 성공: run_id=${successResponse.data.runId}, status=${successResponse.data.status}")
                } catch (e: Exception) {
                    Log.w(TAG, "응답 파싱 실패했지만 200이므로 성공 처리", e)
                }

                RealmRepository.markMissionLogsSynced(missionId)
                missionTracker.stopTracking()

                // ProgressStyle 알림 제거
                ProgressNotificationHelper.cancelNotification(applicationContext)

                Log.i(TAG, "✅ 미션 평가 전송 성공")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
                Result.success()

            } else {

                val errorBody = response.body()?.string()
                Log.e(TAG, "❌ 미션 평가 전송 실패: ${response.code()}")
                Log.e(TAG, "에러 응답: $errorBody")


                if (response.code() == 400 && errorBody != null) {
                    try {
                        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                        val errorResponse = json.decodeFromString<AIEvaluationErrorResponse>(errorBody)
                        Log.e(TAG, "서버 에러 메시지: ${errorResponse.message}")

                        // "이미 완료된 미션" 에러면 재시도 안 함
                        if (errorResponse.message.contains("이미 완료된") ||
                            errorResponse.message.contains("이미 진행중")) {
                            Log.w(TAG, "중복 미션으로 평가 종료")
                            RealmRepository.markMissionLogsSynced(missionId)
                            missionTracker.stopTracking()

                            // ProgressStyle 알림 제거
                            ProgressNotificationHelper.cancelNotification(applicationContext)

                            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
                            return@withContext Result.success()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "에러 응답 파싱 실패", e)
                    }
                }

                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
                Result.retry()
            }

        } catch (e: Exception) {
            Log.e(TAG, "미션 평가 예외: ${e.message}", e)
            Result.retry()
        }
    }

//    private fun triggerFinalAppRecord() {
//        try {
//            Log.d(TAG, "📌 미션 종료 - 마지막 앱 기록 시작")
//
//            // MissionTracker에게 현재 사용 중인 앱 강제 기록 요청
//            missionTracker.recordFinalApp()
//
//            // 기록 완료 대기 (Realm 쓰기 작업 완료 시간)
//            Thread.sleep(200)
//
//            Log.d(TAG, "✅ 마지막 앱 기록 완료")
//        } catch (e: Exception) {
//            Log.e(TAG, "마지막 앱 기록 트리거 실패", e)
//        }
//    }


}