package com.dito.app

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.dito.app.core.background.EventSyncWorker
import com.dito.app.core.background.ScreenTimeSyncWorker
import com.dito.app.core.data.RealmConfig
import com.dito.app.core.di.ServiceLocator
import com.dito.app.core.network.ApiService
import com.dito.app.core.network.AIApiService
import com.dito.app.core.util.EducationalContentDetector
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@HiltAndroidApp
class DitoApplication : Application(), Configuration.Provider {

    companion object {
        private const val TAG = "DitoApp"
    }


    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var aiApiService: AIApiService

    override fun onCreate() {
        super.onCreate()

        Log.i(TAG, "DitoApplication 시작")

        // 0. ServiceLocator 초기화 (AccessibilityService에서 ApiService 사용 가능하도록)
        ServiceLocator.apiService = apiService
        Log.i(TAG, "✅ ServiceLocator 초기화 완료")

        // AI API 서비스 설정
        EducationalContentDetector.aiApiService = aiApiService
        // AI API 사용 여부 설정 (true: AI API 사용, false: 하드코딩 사용)
        EducationalContentDetector.useAIApi = false  // 기본값: 하드코딩 사용
        Log.i(TAG, "✅ AI API 서비스 설정 완료 (useAIApi: ${EducationalContentDetector.useAIApi})")

        // 1. Realm 초기화
        RealmConfig.init()
        Log.i(TAG, "✅ Realm 초기화 성공")

        // 2. WorkManager 시작 (30분마다 배치 전송)
        EventSyncWorker.setupPeriodicSync(this)
        Log.i(TAG, "✅ WorkManager 등록 완료")

        // 3. 스크린타임 동기화 WorkManager 시작 (15분마다)
        ScreenTimeSyncWorker.setupPeriodicSync(this)
        Log.i(TAG, "✅ ScreenTimeSyncWorker 등록 완료")

        Log.i(TAG, "✅ Application 초기화 완료")
    }


    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onTerminate() {
        super.onTerminate()
        RealmConfig.close()
        Log.i(TAG, "🛑 Realm 종료")
    }
}