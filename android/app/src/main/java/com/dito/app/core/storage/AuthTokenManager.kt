package com.dito.app.core.storage

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * JWT 토큰과 사용자 ID를 중앙에서 관리하는 클래스
 * EventSyncWorker 등 여러 곳에서 사용하던 하드코딩된 토큰을 대체
 */
@Singleton
class AuthTokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val PREFS_NAME = "user_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
    }

    /**
     * JWT Access Token 저장
     */
    fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    /**
     * JWT Access Token 조회
     * @return Bearer prefix 없는 순수 토큰 문자열
     */
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    /**
     * Authorization 헤더용 Bearer 토큰 반환
     * @return "Bearer {token}" 형식 또는 null
     */
    fun getBearerToken(): String? {
        val token = getAccessToken()
        return if (token.isNullOrBlank()) null else "Bearer $token"
    }

    /**
     * Refresh Token 저장
     */
    fun saveRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    /**
     * Refresh Token 조회
     */
    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    /**
     * 사용자 ID 저장
     */
    fun saveUserId(userId: Int) {
        prefs.edit().putInt(KEY_USER_ID, userId).apply()
    }

    /**
     * 사용자 ID 조회
     * @return 저장된 userId, 없으면 -1
     */
    fun getUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }

    /**
     * 로그인 여부 확인
     */
    fun isLoggedIn(): Boolean {
        return !getAccessToken().isNullOrBlank()
    }

    /**
     * 모든 토큰 및 사용자 정보 삭제 (로그아웃)
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }

    /**
     * Access Token만 삭제 (토큰 갱신 시)
     */
    fun clearAccessToken() {
        prefs.edit().remove(KEY_ACCESS_TOKEN).apply()
    }
}
