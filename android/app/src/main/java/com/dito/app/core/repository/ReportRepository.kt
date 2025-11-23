package com.dito.app.core.repository

import android.util.Log
import com.dito.app.core.data.report.VideoFeedbackItem
import com.dito.app.core.network.ApiService
import com.dito.app.core.storage.AuthTokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val apiService: ApiService,
    private val authTokenManager: AuthTokenManager
) {
    companion object {
        private const val TAG = "ReportRepository"
    }

    /**
     * 피드백 대상 영상 목록 가져오기
     * @return Result<List<VideoFeedbackItem>> 성공시 영상 목록, 실패시 Exception
     */
    suspend fun getVideosForFeedback(): Result<List<VideoFeedbackItem>> = withContext(Dispatchers.IO) {
        try {
            // 1. 액세스 토큰 가져오기
            val accessToken = authTokenManager.getAccessToken()
            if (accessToken == null) {
                Log.e(TAG, "액세스 토큰이 없습니다")
                return@withContext Result.failure(Exception("로그인이 필요합니다"))
            }

            // 2. API 호출
            val response = apiService.getVideosForFeedback("Bearer $accessToken")

            // 3. 응답 처리
            if (response.isSuccessful && response.body()?.error == false) {
                val videos = response.body()?.data
                if (videos != null) {
                    Log.d(TAG, "피드백 영상 ${videos.size}개 로드 성공")
                    Result.success(videos)
                } else {
                    Log.e(TAG, "응답 데이터가 null입니다")
                    Result.failure(Exception("피드백 영상 데이터가 없습니다"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "피드백 영상 로드 실패"
                Log.e(TAG, "API 호출 실패: $errorMessage (code: ${response.code()})")
                Result.failure(Exception(errorMessage))
            }

        } catch (e: Exception) {
            Log.e(TAG, "피드백 영상 로드 중 오류 발생", e)
            Result.failure(e)
        }
    }
}
