package com.dito.app.core.service

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object Checker {

    private const val TAG = "Checker"

    //테스트 모드 & 배포 모드
    const val TEST_CHECKER_MS = 10 * 1000L //10초
    const val PRODUCTION_CHECKER_MS = 30 * 60 * 1000L //30분

    private val TARGET_APPS = setOf(
        "com.google.android.youtube" // 추후 sns, 숏폼 추가
    )

    private val sentCache = ConcurrentHashMap<String, Long>() //최근에 AI 호출한 이벤트를 기록 -> 중복 호출 방지
    private const val BUFFER_TIME = 5000L

    private val EDUCATIONAL_KEYWORDS = setOf(
        "강의", "lecture", "tutorial", "강좌", "공부", "study",
        "배우기", "learn", "교육", "education", "수업", "class",
        "코딩", "programming", "개발", "development"
    )

    private val ENTERTAINMENT_KEYWORDS = setOf(
        "브이로그", "vlog", "먹방", "mukbang", "게임", "game",
        "예능", "entertainment", "리액션", "reaction", "쇼츠", "shorts"
    )

    fun isTargetApp(packageName: String): Boolean{
        return packageName in TARGET_APPS
    }

    //앱 사용 시간 -> AI 호출 체크
    fun shouldCallAi(
        packageName: String,
        sessionStartTime: Long,
        duration: Long
    ): Boolean{
        if(!isTargetApp(packageName)) return false

        // 10초 미만이면 호출 안 함
        if (duration < TEST_CHECKER_MS) return false

        val cacheKey = "$packageName:${sessionStartTime / 1000}"
        if (isCached(cacheKey)){
            Log.d(TAG, "중복 감지 무시: $packageName")
            return false
        }

        markAsSent(cacheKey)
        Log.w(TAG, "AI 호출 조건 충족: $packageName")
        return true
    }


    //MediaSession 콘텐츠 1차 분석
    fun checkMediaSession(
        title: String,
        channel: String,
        watchTime: Long,
        timestamp: Long,
        appPackage: String
    ):CheckPoint? {
        if(watchTime < TEST_CHECKER_MS){
            Log.d(TAG, "시청 시간 너무 짧음 → AI 호출 불필요")
            return null
        }
        if(isEducational(title, channel)){
            Log.d(TAG, "학습 콘텐츠 → TRACK_2 (배치)")
            return null
        }
        if(isEntertainment(title, channel)){
            Log.w(TAG, "오락 콘텐츠 → TRACK_1 (실시간 AI 호출)")
            return CheckPoint(
                appName = "YouTube",
                videoTitle = title,
                channelName = channel,
                durationSeconds = (watchTime / 1000).toInt(),
                usageTimestamp = formatTimestamp(timestamp)
            )
        }

        Log.w(TAG, "콘텐츠 판단 애매함 → TRACK_1 (실시간 AI)")
        return CheckPoint(
            appName = "com.google.android.youtube",
            videoTitle = title,
            channelName = channel,
            durationSeconds = (watchTime / 1000).toInt(),
            usageTimestamp = formatTimestamp(timestamp)
        )
        
    }

    private fun isEntertainment(title: String, channel: String): Boolean {
        val lowerTitle = title.lowercase()
        val lowerChannel = channel.lowercase()

        // 오락 키워드가 1개 이상 포함되면 명확한 오락 콘텐츠
        val entertainmentCount = ENTERTAINMENT_KEYWORDS.count { keyword ->
            lowerTitle.contains(keyword) || lowerChannel.contains(keyword)
        }

        return entertainmentCount >= 1
    }

    private fun isEducational(title: String, channel: String): Boolean {
        val lowerTitle = title.lowercase()
        val lowerChannel = channel.lowercase()

        // 학습 키워드가 2개 이상 포함되면 명확한 학습 콘텐츠
        val educationalCount = EDUCATIONAL_KEYWORDS.count { keyword ->
            lowerTitle.contains(keyword) || lowerChannel.contains(keyword)
        }

        return educationalCount >= 2
    }

    private fun isCached(key: String): Boolean {
        val now = System.currentTimeMillis()
        val expiry = sentCache[key] ?: return false

        return if (now > expiry) {
            sentCache.remove(key)
            false
        } else {
            true
        }
    }

    private fun markAsSent(key: String) {
        val expiryTime = System.currentTimeMillis() + TEST_CHECKER_MS + BUFFER_TIME
        sentCache[key] = expiryTime
    }

    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }


    fun clearExpiredCache(){
        val now = System.currentTimeMillis()
        val beforeSize = sentCache.size
        sentCache.entries.removeIf { (_, expiry) -> now > expiry}
        val afterSize = sentCache.size

        if(beforeSize != afterSize){
            Log.d(TAG, "만료된 캐시 정리: ${beforeSize - afterSize}개 제거, 남은 캐시: ${afterSize}개")
        }
    }

}

data class CheckPoint(
    val appName: String,
    val videoTitle: String? = null,
    val channelName: String? = null,
    val durationSeconds: Int,
    val usageTimestamp: String
)