package com.dito.app.core.service

import android.util.Log
import android.util.Patterns
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit


object Checker {

    private const val TAG = "Checker"

    // === 타이밍(테스트/운영 스위치 가능) ===
    const val TEST_CHECKER_MS = 20 * 1000L  // 20초 (테스트)
    const val PRODUCTION_CHECKER_MS = 30 * 60 * 1000L // 30분 (운영)
    const val TITLE_CHANGE_SAVE_DELAY_MS: Long = 1_000L // 제목 변경 시 저장 지연
    const val STOP_DEBOUNCE_MS: Long = 300L // STOPPED 연속 이벤트 디바운스
    const val STOP_SAVE_DELAY_MS: Long = 500L // 저장 지연

    // 쿨다운 키
    const val CD_KEY_YT_PLAY = "cd.youtube.play"           // 재생 기반
    const val CD_KEY_YT_EXPLORE = "cd.youtube.explore"     // 탐색 기반
    const val CD_KEY_IG_APP = "cd.instagram.app"           // 앱 기반

    // 패키지명
    const val PKG_YOUTUBE = "com.google.android.youtube"
    const val PKG_INSTAGRAM = "com.instagram.android"

    //쿨다운 시간 설정
    private val COOLDOWN_MS = mapOf(
        CD_KEY_YT_PLAY to TimeUnit.MINUTES.toMillis(2),     // 2분
        CD_KEY_YT_EXPLORE to TimeUnit.MINUTES.toMillis(2),  // 2분
        CD_KEY_IG_APP to TimeUnit.MINUTES.toMillis(2)       // 2분
    )


    private val cooldownMap = ConcurrentHashMap<String, Long>() // key -> lastFiredAt

    private val TARGET_APPS = setOf(
        PKG_YOUTUBE,
        PKG_INSTAGRAM
    )

    //중복 방지 캐시
    private val sentCache = ConcurrentHashMap<String, Long>() // key -> expiryTime
    private const val SENT_CACHE_TTL_MS = 30 * 60 * 1000L // 30분


    private val EDUCATIONAL_KEYWORDS = setOf(
        "강의", "lecture", "tutorial", "강좌", "공부", "study",
        "배우기", "learn", "교육", "education", "수업", "class",
        "코딩", "programming", "개발", "development"
    )

    private val ENTERTAINMENT_KEYWORDS = setOf(
        "브이로그", "vlog", "먹방", "mukbang", "게임", "game",
        "예능", "entertainment", "리액션", "reaction", "쇼츠", "shorts"
    )


    fun isVideoContent(title: String?, channel: String?): Boolean {
        val t = (title ?: "").trim()
        val c = (channel ?: "").trim()

        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "isVideoContent 검증:")
        Log.d(TAG, "  title: '$t' (길이: ${t.length})")
        Log.d(TAG, "  channel: '$c' (길이: ${c.length})")

        if (t.isEmpty()) {
            Log.d(TAG, "  ❌ 제목 비어있음")
            return false
        }
        if (Patterns.WEB_URL.matcher(t).find()) {
            Log.d(TAG, "  ❌ URL 텍스트 제외")
            return false
        }

        // 광고/노이즈 필터링
        val noise = listOf("광고", "AD").any {
            t.contains(it, ignoreCase = true)
        }
        if (noise) {
            Log.d(TAG, "  ❌ 광고/노이즈 필터링")
            return false
        }

        // 채널명 존재 + 제목 최소 길이
        if (c.isNotEmpty() && t.length >= 4) {
            Log.d(TAG, "  ✅ 유효 (채널명 있음 + 제목 충분)")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
            return true
        }
        val result = t.length >= 8
        if (result) {
            Log.d(TAG, "  ✅ 유효 (제목 길이 충분)")
        } else {
            Log.d(TAG, "  ❌ 제목 너무 짧음 (최소 8자 필요)")
        }
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
        return result
    }


    fun isTargetApp(packageName: String): Boolean = packageName in TARGET_APPS


    fun canCallYoutubePlay(): Boolean = canFire(CD_KEY_YT_PLAY)
    fun canCallYoutubeExplore(): Boolean = canFire(CD_KEY_YT_EXPLORE)
    fun canCallInstagramApp(): Boolean = canFire(CD_KEY_IG_APP)

    private fun canFire(key: String): Boolean {
        val now = System.currentTimeMillis()
        val last = cooldownMap[key] ?: 0L
        val cooldown = COOLDOWN_MS[key] ?: 0L
        val ok = (now - last) >= cooldown
        if (ok) {
            cooldownMap[key] = now
        }
        return ok
    }

    fun markCooldown(key: String) {
        cooldownMap[key] = System.currentTimeMillis()
    }


    // 앱-타이머 경로 (유튜브 제외, 인스타그램 등)
    fun shouldCallAi(
        packageName: String,
        sessionStartTime: Long,
        duration: Long
    ): Boolean {
        cleanupExpiredCache()

        if (!isTargetApp(packageName)) return false
        if (packageName == PKG_YOUTUBE) return false // 유튜브는 별도 경로
        if (duration < TEST_CHECKER_MS) return false

        // 인스타그램 쿨다운 체크
        if (packageName == PKG_INSTAGRAM) {
            if (!canCallInstagramApp()) {
                Log.d(TAG, "[Instagram] 쿨다운 중 → 호출 생략")
                return false
            }
        }

        // 중복 방지 캐시
        val cacheKey = "$packageName:${sessionStartTime / 1000}"
        if (isRecentlySent(cacheKey)) {
            Log.d(TAG, "중복 감지 무시: $packageName")
            return false
        }
        sentCache[cacheKey] = System.currentTimeMillis() + SENT_CACHE_TTL_MS

        Log.w(TAG, "AI 호출 조건 충족: $packageName")
        return true
    }


    fun shouldCallYoutubeExploreByTimer(): Boolean {
        cleanupExpiredCache()
        return true
    }


    fun checkMediaSession(
        title: String,
        channel: String,
        watchTime: Long,
        timestamp: Long,
        appPackage: String
    ): CheckPoint? {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "checkMediaSession 호출됨:")
        Log.d(TAG, "  title: '$title'")
        Log.d(TAG, "  channel: '$channel'")
        Log.d(TAG, "  watchTime: ${watchTime / 1000}초")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")

        // 시청 시간 체크
        if (watchTime < TEST_CHECKER_MS) {
            Log.d(TAG, "시청 시간 너무 짧음 (${watchTime / 1000}초) → AI 호출 불필요")
            return null
        }

        // 유효 콘텐츠 검증
        if (!isVideoContent(title, channel)) {
            Log.d(TAG, "유효한 영상 콘텐츠 아님 → AI 호출 스킵")
            return null
        }

        // 학습 콘텐츠는 배치만
        if (isEducational(title, channel)) {
            Log.d(TAG, "학습 콘텐츠 → TRACK_2 (배치)")
            return null
        }

        // 오락 콘텐츠 → 실시간 호출
        if (isEntertainment(title, channel)) {
            Log.w(TAG, "오락 콘텐츠 → TRACK_1 (실시간 AI 호출)")
            return createCheckPoint(title, channel, watchTime, timestamp)
        }

        // 애매한 콘텐츠도 실시간 호출
        Log.w(TAG, "콘텐츠 판단 애매함 → TRACK_1 (실시간 AI)")
        return createCheckPoint(title, channel, watchTime, timestamp)
    }

    private fun createCheckPoint(
        title: String,
        channel: String,
        watchTime: Long,
        timestamp: Long
    ): CheckPoint {
        return CheckPoint(
            appName = "YouTube",
            videoTitle = title,
            channelName = channel,
            durationSeconds = (watchTime / 1000).toInt() + 7200, //테스트용 시간 뻥튀기
            usageTimestamp = formatTimestamp(timestamp)
        )
    }


    private fun isEducational(title: String, channel: String): Boolean {
        val lt = title.lowercase()
        val lc = channel.lowercase()
        val cnt = EDUCATIONAL_KEYWORDS.count { lt.contains(it) || lc.contains(it) }
        return cnt >= 2 // 키워드 2개 이상
    }

    private fun isEntertainment(title: String, channel: String): Boolean {
        val lt = title.lowercase()
        val lc = channel.lowercase()
        val cnt = ENTERTAINMENT_KEYWORDS.count { lt.contains(it) || lc.contains(it) }
        return cnt >= 1 // 키워드 1개 이상
    }


    private fun isRecentlySent(key: String): Boolean {
        val exp = sentCache[key] ?: return false
        return exp > System.currentTimeMillis()
    }

    fun cleanupExpiredCache() {
        val now = System.currentTimeMillis()
        val before = sentCache.size
        sentCache.entries.removeIf { (_, exp) -> now > exp }
        val after = sentCache.size
        if (before != after) {
            Log.d(TAG, "만료된 캐시 정리: ${before - after}개 제거, 남은 캐시: ${after}개")
        }
    }


    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}


data class CheckPoint(
    val appName: String,
    val videoTitle: String? = null,
    val channelName: String? = null,
    val durationSeconds: Int,
    val usageTimestamp: String
)
