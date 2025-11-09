package com.dito.app.core.repository

import android.util.Log
import com.dito.app.core.data.home.HomeData
import com.dito.app.core.data.home.UpdateWeeklyGoalRequest
import com.dito.app.core.network.ApiService
import com.dito.app.core.storage.AuthTokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
    private val apiService: ApiService,
    private val authTokenManager: AuthTokenManager
){
    suspend fun getHomeData(): Result<HomeData> = withContext(Dispatchers.IO){
        try {
            // 1. 액세스 토큰 가져오기
            val accessToken = authTokenManager.getAccessToken()
            if (accessToken == null){
                return@withContext Result.failure(Exception("로그인이 필요합니다"))
            }

            // 2. API 호출
            val response = apiService.getHomeData("Bearer $accessToken")

            // 3. 응답 처리
            if (response.isSuccessful && response.body()?.error == false){
                val homeData = response.body()?.data
                if (homeData != null){
                    Result.success(homeData)
                } else {
                    Result.failure(Exception("홈 데이터가 없습니다"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "홈 데이터 로드 실패"
                Result.failure(Exception(errorMessage))
            }

        } catch (e: Exception){
            Result.failure(e)
        }
    }

    // 주간목표 post
    suspend fun updateWeeklyGoal(goal: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val accessToken = authTokenManager.getAccessToken()
            if (accessToken == null) {
                return@withContext Result.failure(Exception("로그인이 필요합니다"))
            }

            val request = UpdateWeeklyGoalRequest(goal = goal)
            val response = apiService.updateWeeklyGoal(
                token = "Bearer $accessToken",
                request = request
            )

            if (response.isSuccessful && response.body()?.error == false) {
                Result.success(Unit)
            } else {
                val errorMessage = response.body()?.message ?: "주간 목표 업데이트 실패"
                Result.failure(Exception(errorMessage))
            }

        } catch (e: Exception) {
            Log.e("HomeRepository", "주간 목표 업데이트 중 오류 발생", e)
            Result.failure(e)
        }
    }
}