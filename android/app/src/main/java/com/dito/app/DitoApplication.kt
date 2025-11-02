package com.dito.app

import android.app.Application
import android.util.Log
import com.dito.app.core.data.RealmConfig
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class DitoApplication : Application() {

    companion object {
        private const val TAG = "DitoApp"
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "DitoApplication 시작")

        // Realm 초기화
        try {
            RealmConfig.init()
            Log.i(TAG, "✅ Realm 초기화 성공")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Realm 초기화 실패", e)
        }

        Log.i(TAG, "✅ 앱 초기화 완료")
    }

    override fun onTerminate() {
        super.onTerminate()

        // Realm 종료
        RealmConfig.close()

        Log.i(TAG, "🛑 DitoApplication 종료")
    }
}