package com.dito.app.core.data

import android.util.Log
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

/**
 * Realm Database 싱글톤 관리자
 *
 * 초기화: DitoApplication.onCreate()에서 호출
 * 사용: RealmConfig.getInstance()로 어디서든 접근
 */
object RealmConfig {

    private const val TAG = "RealmConfig"
    private var realm: Realm? = null

    /**
     * Realm 초기화
     * Application.onCreate()에서 최초 1회 호출
     */
    fun init(): Realm {
        if (realm == null || realm?.isClosed() == true) {
            val config = RealmConfiguration.Builder(
                schema = setOf(
                    AppUsageEvent::class,
                    MediaSessionEvent::class
                )
            )
                .name("dito.realm")                     // 파일명
                .schemaVersion(1)                       // 스키마 버전
                .deleteRealmIfMigrationNeeded()         // 개발 중: 스키마 변경 시 DB 삭제
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
            "❌ Realm이 초기화되지 않았습니다. Application.onCreate()에서 init() 호출 필요"
        )
    }

    /**
     * Realm 닫기 (앱 종료 시)
     */
    fun close() {
        realm?.close()
        realm = null
        Log.i(TAG, "🛑 Realm 종료")
    }
}