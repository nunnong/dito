package com.dito.app.core.repository

import android.util.Log
import com.dito.app.core.data.settings.UpdateNicknameRequest
import com.dito.app.core.data.settings.UpdateNicknameResponse
import com.dito.app.core.network.ApiService
import com.dito.app.core.storage.AuthTokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingRepository @Inject constructor(
    private val apiService: ApiService,
    private val authTokenManager: AuthTokenManager
) {
    companion object {
        private const val TAG = "SettingRepository"
    }

    suspend fun updateNickname(nickname: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val token = authTokenManager.getAccessToken()

            if (token.isNullOrEmpty()) {
                Log.e(TAG, "닉네임 변경 실패: 토큰 없음")
                return@withContext Result.failure(Exception("로그인이 필요합니다"))
            }

            val request = UpdateNicknameRequest(nickname = nickname)
            val response = apiService.updateNickname(request, "Bearer $token")

            if (response.isSuccessful && response.body()?.error == false) {
                val message = response.body()?.message ?: "닉네임이 변경되었습니다"
                Log.d(TAG, "닉네임 변경 성공: $nickname")
                Result.success(message)

            } else {
                val errorMessage = response.body()?.message ?: "닉네임 변경에 실패했습니다"
                Log.e(TAG, "닉네임 변경 실패: code=${response.code()}, message=$errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "닉네임 변경 예외", e)
            Result.failure(e)
        }
    }
}
