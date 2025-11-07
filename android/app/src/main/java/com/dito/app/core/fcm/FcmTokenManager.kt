package com.dito.app.core.fcm

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FCM 토큰을 관리하는 클래스
 * 토큰 생성, 저장, 조회, 삭제 기능 제공
 */
@Singleton
class FcmTokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val TAG = "FcmTokenManager"
        private const val PREFS_NAME = "fcm_prefs"
        private const val KEY_FCM_TOKEN = "fcm_token"
    }

    /**
     * Firebase에서 FCM 토큰을 새로 발급받아 저장
     * @return FCM 토큰 문자열 또는 null
     */
    suspend fun refreshToken(): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "FCM 토큰 발급 성공: ${token.take(20)}...")
            saveToken(token)
            token
        } catch (e: Exception) {
            Log.e(TAG, "FCM 토큰 발급 실패", e)
            null
        }
    }

    /**
     * FCM 토큰을 로컬에 저장
     */
    fun saveToken(token: String) {
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
        Log.d(TAG, "FCM 토큰 저장 완료")
    }

    /**
     * 저장된 FCM 토큰 조회
     * @return FCM 토큰 문자열 또는 null
     */
    fun getToken(): String? {
        return prefs.getString(KEY_FCM_TOKEN, null)
    }

    /**
     * 로컬에 저장된 FCM 토큰 삭제
     */
    fun clearToken() {
        prefs.edit().remove(KEY_FCM_TOKEN).apply()
        Log.d(TAG, "FCM 토큰 삭제 완료")
    }

    /**
     * Firebase에서 FCM 토큰을 완전히 삭제하고 로컬에서도 제거
     * 로그아웃 시 호출
     */
    suspend fun deleteToken() {
        try {
            FirebaseMessaging.getInstance().deleteToken().await()
            clearToken()
            Log.d(TAG, "FCM 토큰 완전 삭제 성공")
        } catch (e: Exception) {
            Log.e(TAG, "FCM 토큰 삭제 실패", e)
        }
    }

    /**
     * FCM 토큰 존재 여부 확인
     */
    fun hasToken(): Boolean {
        return !getToken().isNullOrBlank()
    }
}
