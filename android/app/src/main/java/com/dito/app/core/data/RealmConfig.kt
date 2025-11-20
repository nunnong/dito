package com.dito.app.core.data

import android.util.Log
import com.dito.app.core.data.mission.MissionTrackingLog
import com.dito.app.core.data.phone.AppUsageEvent
import com.dito.app.core.data.phone.MediaSessionEvent
import com.dito.app.core.data.screentime.ScreenTimeLocalLog
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration


object RealmConfig {

    private const val TAG = "RealmConfig"
    private var realm: Realm? = null


    fun init(): Realm {
        if (realm == null || realm?.isClosed() == true) {
            val config = RealmConfiguration.Builder(
                schema = setOf(
                    AppUsageEvent::class,
                    MediaSessionEvent::class,
                    MissionTrackingLog::class,
                    ScreenTimeLocalLog::class
                )
            )
                .name("dito.realm")                     // 파일명
                .schemaVersion(1)                       // 스키마 버전
                // .deleteRealmIfMigrationNeeded()      // 제거: 데이터 보존을 위해
                // 스키마 변경 시 schemaVersion을 올리고, 필요하면 migration 블록 추가
                .build()

            realm = Realm.open(config)
            Log.i(TAG, "✅ Realm 초기화 완료: ${config.name}")
        }
        return realm!!
    }

    /**
     * Realm 인스턴스 가져오기
     * Service나 Repository에서 호출
     */
    fun getInstance(): Realm {
        return realm ?: throw IllegalStateException(
            "❌ Realm이 초기화되지 않았습니다."
        )
    }

    fun close() {
        realm?.close()
        realm = null
        Log.i(TAG, "🛑 Realm 종료")
    }
}