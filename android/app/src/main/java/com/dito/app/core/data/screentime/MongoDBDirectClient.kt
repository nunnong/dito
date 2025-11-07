package com.dito.app.core.data.screentime

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * MongoDB Atlas Data API 직접 호출 클라이언트
 *
 * MongoDB Atlas에서 제공하는 Data API를 사용하여
 * HTTP로 직접 데이터를 삽입/조회
 *
 * 설정 방법:
 * 1. MongoDB Atlas → Data API 활성화
 * 2. API Key 생성
 * 3. 아래 상수값 설정
 */
object MongoDBDirectClient {

    private const val TAG = "MongoDBDirectClient"

    // TODO: 실제 MongoDB Atlas 정보로 변경 필요
    private const val MONGO_API_URL = "https://data.mongodb-api.com/app/<APP_ID>/endpoint/data/v1"
    private const val MONGO_API_KEY = "YOUR_API_KEY_HERE"
    private const val DATABASE_NAME = "S13P31A708"
    private const val COLLECTION_SUMMARY = "screen_time_daily_summary"
    private const val COLLECTION_SNAPSHOT = "screen_time_snapshots"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Summary 데이터 upsert (있으면 업데이트, 없으면 삽입)
     */
    suspend fun upsertSummary(data: ScreenTimeDailySummaryMongo): Boolean = withContext(Dispatchers.IO) {
        try {
            val payload = buildUpsertPayload(
                collection = COLLECTION_SUMMARY,
                filter = mapOf(
                    "group_id" to data.group_id,
                    "user_id" to data.user_id,
                    "date" to data.date
                ),
                update = mapOf(
                    "\$set" to mapOf(
                        "total_minutes" to data.total_minutes,
                        "last_updated_at" to data.last_updated_at
                    )
                ),
                upsert = true
            )

            executeRequest("$MONGO_API_URL/action/updateOne", payload)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Summary upsert 실패", e)
            false
        }
    }

    /**
     * Snapshot 데이터 삽입
     */
    suspend fun insertSnapshot(data: ScreenTimeSnapshotMongo): Boolean = withContext(Dispatchers.IO) {
        try {
            val payload = buildInsertPayload(
                collection = COLLECTION_SNAPSHOT,
                document = mapOf(
                    "group_id" to data.group_id,
                    "user_id" to data.user_id,
                    "date" to data.date,
                    "recorded_at" to data.recorded_at,
                    "screen_time_minutes" to data.screen_time_minutes,
                    "expire_at" to data.expire_at
                )
            )

            executeRequest("$MONGO_API_URL/action/insertOne", payload)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Snapshot insert 실패", e)
            false
        }
    }

    /**
     * HTTP 요청 실행
     */
    private fun executeRequest(url: String, payload: String): Boolean {
        val mediaType = "application/json".toMediaType()
        val requestBody = payload.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("api-key", MONGO_API_KEY)
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                Log.d(TAG, "✅ MongoDB 직접 저장 성공")
                return true
            } else {
                Log.e(TAG, "❌ MongoDB 직접 저장 실패: ${response.code} ${response.message}")
                return false
            }
        }
    }

    /**
     * Upsert 페이로드 생성
     */
    private fun buildUpsertPayload(
        collection: String,
        filter: Map<String, Any>,
        update: Map<String, Any>,
        upsert: Boolean
    ): String {
        return json.encodeToString(
            mapOf(
                "dataSource" to "Cluster0",
                "database" to DATABASE_NAME,
                "collection" to collection,
                "filter" to filter,
                "update" to update,
                "upsert" to upsert
            )
        )
    }

    /**
     * Insert 페이로드 생성
     */
    private fun buildInsertPayload(
        collection: String,
        document: Map<String, Any>
    ): String {
        return json.encodeToString(
            mapOf(
                "dataSource" to "Cluster0",
                "database" to DATABASE_NAME,
                "collection" to collection,
                "document" to document
            )
        )
    }
}