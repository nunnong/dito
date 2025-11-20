package com.dito.app.core.storage

import android.content.Context
import com.dito.app.core.data.home.HomeData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("home_storage", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HOME_DATA = "home_data"
    }

    fun saveHomeData(homeData: HomeData) {
        val jsonString = Json.encodeToString(homeData)
        prefs.edit().putString(KEY_HOME_DATA, jsonString).apply()
    }

    fun getHomeData(): HomeData? {
        val jsonString = prefs.getString(KEY_HOME_DATA, null)
        return jsonString?.let {
            try {
                Json.decodeFromString<HomeData>(it)
            } catch (e: Exception) {
                // JSON 파싱 실패 시 null 반환 또는 로그 기록
                null
            }
        }
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
