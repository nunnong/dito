package com.dito.app.core.service

import android.content.Context
import android.util.Log
import com.dito.app.core.data.RealmRepository
import com.dito.app.core.network.*
import com.dito.app.core.storage.AuthTokenManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI 호출 통합 관리자
 */
@Singleton
class AIAgent @Inject constructor(
    @ApplicationContext private val context: Context,
    private val aiService: AIService,
    private val authTokenManager: AuthTokenManager
) {

    companion object {
        private const val TAG = "AIAgent"
        private const val MAX_RETRY = 1
        private const val RETRY_DELAY = 2000L
    }

    private val aiScope = CoroutineScope(Dispatchers.IO + SupervisorJob())


    /**
     * AI 개입 요청
     */
    fun requestIntervention(
        behaviorLog: BehaviorLog,
        eventIds: List<String>
    ) {

        if (!InterventionManager.canIntervene(context)) {
            Log.d(TAG, "개입 조건 미충족 - AI 호출 취소")
            return
        }

        Log.d(TAG, "🧪 테스트 모드 → 무조건 AI 호출 수행")

        aiScope.launch {
            callAIWithRetry(
                behaviorLog = behaviorLog,
                eventIds = eventIds,
                retryCount = 0
            )
        }
    }

    private suspend fun callAIWithRetry(
        behaviorLog: BehaviorLog,
        eventIds: List<String>,
        retryCount: Int
    ) {
        try {
            Log.i(TAG, "🚀 AI 호출: ${behaviorLog.appName} (${behaviorLog.durationSeconds}초)")

            val request = AIRunRequest(
                userId = getPersonalId(),
                behaviorLog = behaviorLog
            )

            val response = withTimeout(10000L) {
                aiService.sendToAI(request)
            }

            if (response.isSuccessful) {
                handleSuccess(response.body(), eventIds)
            } else {
                Log.e(TAG, "❌ AI 응답 실패: ${response.code()} - ${response.message()}")
                handleFailure(behaviorLog, eventIds, retryCount)
            }

        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "❌ AI 타임아웃 발생")
            handleFailure(behaviorLog, eventIds, retryCount)
        } catch (e: Exception) {
            Log.e(TAG, "❌ AI 호출 실패: ${e.message}", e)
            handleFailure(behaviorLog, eventIds, retryCount)
        }
    }

    private fun handleSuccess(
        response: AIRunResponse?,
        eventIds: List<String>
    ) {
        Log.d(TAG, "✅ AI 응답 성공: run_id=${response?.runId}, thread_id=${response?.threadId}, status=${response?.status}")

        eventIds.forEach { id ->
            RealmRepository.markAiCalled(id, success = true)
        }

        InterventionManager.recordIntervention(context)
    }

    private suspend fun handleFailure(
        behaviorLog: BehaviorLog,
        eventIds: List<String>,
        retryCount: Int
    ) {
        if (retryCount < MAX_RETRY) {
            Log.w(TAG, "🔄 AI 재시도 중... (${retryCount + 1}/$MAX_RETRY)")
            delay(RETRY_DELAY)
            callAIWithRetry(behaviorLog, eventIds, retryCount + 1)
        } else {
            Log.e(TAG, "❌ AI 호출 최종 실패")
            markAsFailed(eventIds)
        }
    }

    private fun markAsFailed(eventIds: List<String>) {
        eventIds.forEach { id ->
            RealmRepository.markAiCalled(id, success = false)
        }
    }

    private fun getPersonalId(): String {
        val personalId = authTokenManager.getPersonalId()
        if (personalId.isNullOrBlank()) {
            Log.e(TAG, "❌ 사용자 ID가 없습니다. AI 호출 중단")
            throw IllegalStateException("User not authenticated")
        }
        return personalId
    }

    fun shutdown() {
        aiScope.cancel()
        Log.d(TAG, "🛑 AIAgent 종료")
    }
}