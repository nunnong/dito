package com.dito.app.core.repository

import android.util.Log
import com.dito.app.core.data.ApiResponse
import com.dito.app.core.data.missionNotification.MissionNotificationResponse
import com.dito.app.core.network.ApiService
import com.dito.app.core.storage.AuthTokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MissionNotificationRepository @Inject constructor(
    private val apiService: ApiService,
    private val authTokenManager: AuthTokenManager
) {

    suspend fun getMissionNotifications(page: Int): Result<MissionNotificationResponse> = withContext(Dispatchers.IO) {
        try {
            val accessToken = authTokenManager.getAccessToken()
                ?: return@withContext Result.failure(Exception("로그인이 필요합니다"))

            Log.d("MissionNotification", "=== API 요청 시작 ===")
            Log.d("MissionNotification", "페이지: $page")

            val response = apiService.getMissionNotifications(
                token = "Bearer $accessToken",
                pageNumber = page
            )

            Log.d("MissionNotification", "응답 코드: ${response.code()}")
            Log.d("MissionNotification", "응답 성공 여부: ${response.isSuccessful}")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d("MissionNotification", "=== 응답 바디 ===")
                Log.d("MissionNotification", "error: ${body.error}")
                Log.d("MissionNotification", "message: ${body.message}")
                Log.d("MissionNotification", "데이터 개수: ${body.data.size}")

                body.data.forEachIndexed { index, notification ->
                    Log.d("MissionNotification", "--- 알림 #$index ---")
                    Log.d("MissionNotification", "  id: ${notification.id}")
                    Log.d("MissionNotification", "  missionType: ${notification.missionType}")
                    Log.d("MissionNotification", "  title: ${notification.title}")
                    Log.d("MissionNotification", "  coinReward: ${notification.coinReward}")
                    Log.d("MissionNotification", "  status: ${notification.status}")
                    Log.d("MissionNotification", "  result: ${notification.result}")
                }

                Log.d("MissionNotification", "=== 페이지 정보 ===")
                Log.d("MissionNotification", "현재 페이지: ${body.pageInfo.page}")
                Log.d("MissionNotification", "다음 페이지 존재: ${body.pageInfo.hasNext}")

                if (!body.error) {
                    Result.success(body)
                } else {
                    Log.e("MissionNotification", "API 에러: ${body.message}")
                    Result.failure(Exception(body.message ?: "미션 알림 로드 실패"))
                }
            } else {
                val errorMessage = "미션 알림 로드 실패 (code: ${response.code()})"
                Log.e("MissionNotification", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("MissionNotification", "예외 발생: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun claimMissionReward(missionId: Long): Result<ApiResponse<Unit>> = withContext(Dispatchers.IO) {
        try {
            val accessToken = authTokenManager.getAccessToken()
                ?: return@withContext Result.failure(Exception("로그인이 필요합니다"))

            Log.d("MissionNotification", "=== 레몬 획득 API 요청 ===")
            Log.d("MissionNotification", "미션 ID: $missionId")

            val response = apiService.claimMissionReward(
                missionId = missionId,
                token = "Bearer $accessToken"
            )

            Log.d("MissionNotification", "응답 코드: ${response.code()}")
            Log.d("MissionNotification", "응답 성공 여부: ${response.isSuccessful}")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d("MissionNotification", "레몬 획득 성공")
                Result.success(body)
            } else {
                val errorMessage = "레몬 획득 실패 (code: ${response.code()})"
                Log.e("MissionNotification", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("MissionNotification", "레몬 획득 예외 발생: ${e.message}", e)
            Result.failure(e)
        }
    }
}
