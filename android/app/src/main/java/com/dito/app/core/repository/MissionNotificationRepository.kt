package com.dito.app.core.repository

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

            val response = apiService.getMissionNotifications(
                token = "Bearer $accessToken",
                pageNumber = page
            )

            if (response.isSuccessful && response.body() != null) {
                if (response.body()!!.error == false) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception(response.body()!!.message ?: "미션 알림 로드 실패"))
                }
            } else {
                val errorMessage = "미션 알림 로드 실패 (code: ${response.code()})"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
