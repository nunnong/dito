package com.dito.app

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.dito.app.core.background.EventSyncWorker
import com.dito.app.core.data.RealmConfig
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@HiltAndroidApp
class DitoApplication : Application(), Configuration.Provider {

    companion object {
        private const val TAG = "DitoApp"
    }


    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        Log.i(TAG, "DitoApplication 시작")

        // 1. Realm 초기화
        RealmConfig.init()
        Log.i(TAG, "✅ Realm 초기화 성공")

        // 2. WorkManager 시작 (30분마다 배치 전송)
        EventSyncWorker.setupPeriodicSync(this)
        Log.i(TAG, "✅ WorkManager 등록 완료")
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