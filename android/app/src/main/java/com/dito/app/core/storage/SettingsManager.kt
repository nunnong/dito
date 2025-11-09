package com.dito.app.core.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 사용자 설정을 관리하는 클래스
 * 현재 사용 : 미션빈도 저장
 */
@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val PREFS_NAME = "settings_prefs"
        private const val KEY_FREQUENCY = "frequency"
    }

    /**
     * 미션 빈도 저장
     * @param frequency "LOW", "MEDIUM", "HIGH" 중 하나
     */
    fun saveFrequency(frequency: String) {
        prefs.edit { putString(KEY_FREQUENCY, frequency) }
    }

    /**
     * 미션 빈도 조회
     * @return 저장된 frequency, 없으면 "MEDIUM" (기본값)
     */
    fun getFrequency(): String {
        return prefs.getString(KEY_FREQUENCY, "MEDIUM") ?: "MEDIUM"
    }

    /**
     * 모든 설정 초기화 (일주일 단위로 약속)
     */
    fun clearAll() {
        prefs.edit { clear() }
    }
}
