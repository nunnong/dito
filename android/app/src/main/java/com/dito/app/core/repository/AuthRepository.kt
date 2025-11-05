package com.dito.app.core.repository

import android.util.Log
import com.dito.app.core.data.auth.SignInRequest
import com.dito.app.core.data.auth.SignUpRequest
import com.dito.app.core.fcm.FcmTokenManager
import com.dito.app.core.network.ApiService
import com.dito.app.core.storage.AuthTokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 인증 관련 비즈니스 로직을 처리하는 Repository
 */
@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val authTokenManager: AuthTokenManager,
    private val fcmTokenManager: FcmTokenManager
) {
    companion object {
        private const val TAG = "AuthRepository"
    }

    /**
     * 로그인
     * @param username 사용자 아이디
     * @param password 비밀번호
     * @return 성공 여부
     */
    suspend fun signIn(username: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // FCM 토큰 발급 (없으면 새로 발급)
            val fcmToken = fcmTokenManager.getToken() ?: fcmTokenManager.refreshToken()

            val request = SignInRequest(
                username = username,
                password = password,
                fcmToken = fcmToken
            )

            Log.d(TAG, "로그인 시도: username=$username, fcmToken=${fcmToken?.take(20)}...")

            val response = apiService.signIn(request)

            if (response.isSuccessful && response.body()?.error == false) {
                val authData = response.body()?.data
                if (authData != null) {
                    // 토큰 저장
                    authTokenManager.saveAccessToken(authData.accessToken)
                    authTokenManager.saveRefreshToken(authData.refreshToken)
                    authTokenManager.savePersonalId(username)

                    Log.d(TAG, "✅ 로그인 성공: username=$username")
                    Result.success(Unit)
                } else {
                    Log.e(TAG, "❌ 로그인 실패: 응답 데이터 없음")
                    Result.failure(Exception("응답 데이터가 없습니다"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "로그인 실패"
                Log.e(TAG, "❌ 로그인 실패: code=${response.code()}, message=$errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 로그인 예외", e)
            Result.failure(e)
        }
    }

    /**
     * 회원가입
     * @param username 사용자 아이디
     * @param password 비밀번호
     * @param nickname 닉네임 (선택)
     * @return 성공 여부
     */
    suspend fun signUp(username: String, password: String, nickname: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // FCM 토큰 발급 (없으면 새로 발급)
            val fcmToken = fcmTokenManager.getToken() ?: fcmTokenManager.refreshToken()

            val request = SignUpRequest(
                username = username,
                password = password,
                nickname = nickname,
                fcmToken = fcmToken
            )

            Log.d(TAG, "회원가입 시도: username=$username, fcmToken=${fcmToken?.take(20)}...")

            val response = apiService.signUp(request)

            if (response.isSuccessful && response.body()?.error == false) {
                val authData = response.body()?.data
                if (authData != null) {
                    // 토큰 저장
                    authTokenManager.saveAccessToken(authData.accessToken)
                    authTokenManager.saveRefreshToken(authData.refreshToken)
                    authTokenManager.savePersonalId(username)

                    Log.d(TAG, "✅ 회원가입 성공: username=$username")
                    Result.success(Unit)
                } else {
                    Log.e(TAG, "❌ 회원가입 실패: 응답 데이터 없음")
                    Result.failure(Exception("응답 데이터가 없습니다"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "회원가입 실패"
                Log.e(TAG, "❌ 회원가입 실패: code=${response.code()}, message=$errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 회원가입 예외", e)
            Result.failure(e)
        }
    }

    /**
     * 로그아웃
     * 로컬 토큰 및 FCM 토큰 삭제
     */
    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // FCM 토큰 삭제
            fcmTokenManager.deleteToken()

            // 로컬 인증 정보 삭제
            authTokenManager.clearAll()

            Log.d(TAG, "✅ 로그아웃 완료")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 로그아웃 예외", e)
            Result.failure(e)
        }
    }

    /**
     * 로그인 상태 확인
     */
    fun isLoggedIn(): Boolean {
        return authTokenManager.isLoggedIn()
    }
}
