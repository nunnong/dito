package com.dito.app.core.repository

import android.util.Log
import com.dito.app.core.data.auth.SignInRequest
import com.dito.app.core.data.auth.SignUpRequest
import com.dito.app.core.data.common.ApiErrorResponse
import com.dito.app.core.fcm.FcmTokenManager
import com.dito.app.core.network.ApiService
import com.dito.app.core.storage.AuthTokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 인증 관련 비즈니스 로직을 처리하는 Repository
 */
@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val authTokenManager: AuthTokenManager,
    private val fcmTokenManager: FcmTokenManager,
    private val json: Json
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
     * @param nickname 닉네임
     * @param birth 생년월일 (yyyy-MM-dd 형식)
     * @param gender 성별 (MALE/FEMALE)
     * @param job 직업 (STUDENT 등)
     * @return 성공 여부
     */
    suspend fun signUp(
        username: String,
        password: String,
        nickname: String,
        birth: String,
        gender: String,
        job: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // FCM 토큰 발급 (없으면 새로 발급)
            val fcmToken = fcmTokenManager.getToken() ?: fcmTokenManager.refreshToken()

            val request = SignUpRequest(
                username = username,
                password = password,
                nickname = nickname,
                birth = birth,
                gender = gender,
                job = job,
                frequency = null,
                fcmToken = fcmToken
            )

            Log.d(TAG, "회원가입 시도: username=$username, nickname=$nickname, birth=$birth, gender=$gender, job=$job, fcmToken=${fcmToken?.take(20)}...")

            val response = apiService.signUp(request)

            if (response.isSuccessful && response.body()?.error == false) {
                Log.d(TAG, "✅ 회원가입 성공: username=$username")

                // 회원가입 성공 후 자동 로그인
                Log.d(TAG, "자동 로그인 시도...")
                val loginResult = signIn(username, password)

                return@withContext loginResult.fold(
                    onSuccess = {
                        Log.d(TAG, "✅ 자동 로그인 성공")
                        Result.success(Unit)
                    },
                    onFailure = { error ->
                        Log.e(TAG, "❌ 자동 로그인 실패: ${error.message}")
                        Result.failure(error)
                    }
                )
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
     * 서버에 로그아웃 요청 + 로컬 토큰 및 FCM 토큰 삭제
     */
    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = authTokenManager.getAccessToken()

            // 서버에 로그아웃 요청
            if (!token.isNullOrEmpty()) {
                try {
                    val response = apiService.logout("Bearer $token")
                    if (!response.isSuccessful) {
                        Log.w(TAG, "서버 로그아웃 요청 실패: code=${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "서버 로그아웃 요청 예외 (계속 진행)", e)
                }
            }

            // FCM 토큰 삭제
            fcmTokenManager.deleteToken()

            // 로컬 인증 정보 삭제
            authTokenManager.clearAll()

            Log.d(TAG, "로그아웃 완료")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "로그아웃 예외", e)
            Result.failure(e)
        }
    }

    /**
     * 회원탈퇴
     * 서버에 회원탈퇴 요청 + 로컬 토큰 및 FCM 토큰 삭제
     */
    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = authTokenManager.getAccessToken()

            if (token.isNullOrEmpty()) {
                Log.e(TAG, "회원탈퇴 실패: 토큰 없음")
                return@withContext Result.failure(Exception("로그인이 필요합니다"))
            }

            // 서버에 회원탈퇴 요청
            val response = apiService.signOut("Bearer $token")

            if (response.isSuccessful) {
                // FCM 토큰 삭제
                fcmTokenManager.deleteToken()

                // 로컬 인증 정보 삭제
                authTokenManager.clearAll()

                Log.d(TAG, "회원탈퇴 완료")
                Result.success(Unit)
            } else {
                val errorMessage = "회원탈퇴에 실패했습니다"
                Log.e(TAG, "회원탈퇴 실패: code=${response.code()}")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "회원탈퇴 예외", e)
            Result.failure(e)
        }
    }

    /**
     * 로그인 상태 확인
     */
    fun isLoggedIn(): Boolean {
        return authTokenManager.isLoggedIn()
    }

    /**
     * 아이디 중복 확인
     * @param username 확인할 아이디
     * @return 사용 가능하면 true, 아니면 false
     */
    suspend fun checkUsernameAvailability(username: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.checkUsername(username)
            val responseBody = response.body()

            if (response.isSuccessful && responseBody != null) {
                if (responseBody.error == false) {
                    // User confirmed: `data: true` means available.
                    val isAvailable = responseBody.data ?: false // Default to not available if data is null
                    Result.success(isAvailable)
                } else {
                    // Handle cases where the server returns 200 OK but with an error flag in the body.
                    val errorMessage = responseBody.message ?: "이미 사용 중인 아이디입니다."
                    Result.failure(Exception(errorMessage))
                }
            } else {
                // Case 3: Non-2xx responses (e.g., 400, 500)
                val errorBody = response.errorBody()?.string()
                var errorMessage = "아이디 확인 실패" // Default error message
                if (errorBody != null) {
                    try {
                        val errorResponse = json.decodeFromString<ApiErrorResponse>(errorBody)
                        errorMessage = errorResponse.message ?: errorMessage
                    } catch (e: Exception) {
                        // Ignore if parsing fails, use default message
                    }
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
