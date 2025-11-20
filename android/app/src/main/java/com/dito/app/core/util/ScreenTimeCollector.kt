package com.dito.app.core.util

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import com.dito.app.core.data.RealmConfig
import com.dito.app.core.data.phone.MediaSessionEvent
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * 스크린타임 수집 유틸리티
 * UsageStatsManager를 사용하여 오늘 하루의 스크린타임을 분 단위로 계산
 */
class ScreenTimeCollector(private val context: Context) {

    companion object {
        private const val TAG = "ScreenTimeCollector"
    }

    private val usageStatsManager: UsageStatsManager by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }

    /**
     * 오늘 하루의 총 스크린타임 조회 (분 단위)
     * @return 총 스크린타임 (분)
     */
    fun getTodayScreenTimeMinutes(): Int {
        val (startTime, endTime) = getTodayRange()
        return getScreenTimeMinutes(startTime, endTime)
    }

    fun getYouTubeUsageMinutes(): Int {
        try {
            // Realm에서 오늘 하루의 YouTube 세션 데이터를 조회
            val today = getTodayDateString()

            val realm = try {
                RealmConfig.getInstance()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Realm 초기화 실패", e)
                return getYouTubeUsageFromUsageStats() // 폴백: UsageStatsManager 사용
            }

            val sessions = realm.query<MediaSessionEvent>(
                "date == $0 AND appPackage == $1",
                today,
                "com.google.android.youtube"
            ).find()

            // 교육 콘텐츠가 아닌 세션만 합산
            val savedWatchTimeMillis = sessions
                .filter { !it.isEducational }
                .sumOf { it.watchTime }

            val educationalCount = sessions.count { it.isEducational }
            val nonEducationalCount = sessions.size - educationalCount

            // 현재 재생 중인 세션의 시청 시간 (아직 저장되지 않은 실시간 시간)
            val currentSessionTime = try {
                val sessionTime = com.dito.app.core.service.phone.SessionStateManager.getCurrentSessionWatchTime()
                // 현재 세션이 교육 콘텐츠인지 확인
                val isCurrentEducational = com.dito.app.core.service.phone.SessionStateManager.isCurrentSessionEducational()
                if (isCurrentEducational) {
                    Log.d(TAG, "📚 현재 재생 중인 세션이 교육 콘텐츠 → 시간 제외")
                    0L
                } else {
                    sessionTime
                }
            } catch (e: Exception) {
                Log.w(TAG, "현재 세션 조회 실패", e)
                0L
            }

            val totalWatchTimeMillis = savedWatchTimeMillis + currentSessionTime
            val realmMinutes = TimeUnit.MILLISECONDS.toMinutes(totalWatchTimeMillis).toInt()

            Log.d(TAG, "YouTube 사용시간 (Realm): ${realmMinutes}분 (${totalWatchTimeMillis}ms, 교육: ${educationalCount}개 제외, 비교육: ${nonEducationalCount}개)")

            // Realm 데이터가 없거나 0이면 UsageStatsManager로 폴백
            if (realmMinutes == 0) {
                val usageStatsMinutes = getYouTubeUsageFromUsageStats()
                if (usageStatsMinutes > 0) {
                    Log.d(TAG, "📊 Realm 데이터 없음 → UsageStatsManager 폴백: ${usageStatsMinutes}분")
                    return usageStatsMinutes
                }
            }

            return realmMinutes
        } catch (e: Exception) {
            Log.e(TAG, "❌ YouTube 사용시간 조회 실패", e)
            return getYouTubeUsageFromUsageStats() // 폴백: UsageStatsManager 사용
        }
    }

    /**
     * UsageStatsManager를 사용하여 YouTube 사용 시간 조회 (폴백용)
     * Realm 데이터가 없을 때 시스템 레벨의 사용 통계를 사용
     */
    private fun getYouTubeUsageFromUsageStats(): Int {
        try {
            val (startTime, endTime) = getTodayRange()
            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )

            if (usageStats == null || usageStats.isEmpty()) {
                Log.w(TAG, "UsageStats가 비어있습니다")
                return 0
            }

            // YouTube 패키지의 사용 시간 찾기
            val youtubeStats = usageStats.find {
                it.packageName == "com.google.android.youtube"
            }

            val youtubeMillis = youtubeStats?.totalTimeInForeground ?: 0L
            val youtubeMinutes = (youtubeMillis / (1000 * 60)).toInt()

            Log.d(TAG, "YouTube 사용시간 (UsageStats 폴백): ${youtubeMinutes}분")
            return youtubeMinutes

        } catch (e: Exception) {
            Log.e(TAG, "❌ UsageStats YouTube 조회 실패", e)
            return 0
        }
    }

    private fun getTodayDateString(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    /**
     * 특정 날짜의 총 스크린타임 조회 (분 단위)
     * @param date 조회할 날짜
     * @return 총 스크린타임 (분)
     */
    fun getScreenTimeMinutesForDate(date: LocalDate): Int {
        val (startTime, endTime) = getDateRange(date)
        return getScreenTimeMinutes(startTime, endTime)
    }

    /**
     * 특정 시간 범위의 스크린타임 계산
     */
    private fun getScreenTimeMinutes(startTime: Long, endTime: Long): Int {
        try {
            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )

            if (usageStats == null || usageStats.isEmpty()) {
                Log.w(TAG, "사용 통계가 비어있습니다. PACKAGE_USAGE_STATS 권한을 확인하세요.")
                return 0
            }

            // 모든 앱의 포그라운드 시간 합계 계산
            val totalMillis = usageStats
                .filter { it.totalTimeInForeground > 0 }
                .sumOf { it.totalTimeInForeground }

            // 밀리초를 분으로 변환
            val totalMinutes = (totalMillis / (1000 * 60)).toInt()

            Log.d(TAG, "스크린타임: ${totalMinutes}분 (앱 ${usageStats.size}개)")
            return totalMinutes

        } catch (e: Exception) {
            Log.e(TAG, "스크린타임 계산 중 오류 발생", e)
            return 0
        }
    }

    /**
     * 오늘 00:00:00 ~ 현재 시각의 범위 반환
     */
    private fun getTodayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        return Pair(startTime, endTime)
    }

    /**
     * 특정 날짜 00:00:00 ~ 23:59:59의 범위 반환
     */
    private fun getDateRange(date: LocalDate): Pair<Long, Long> {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

        return Pair(startOfDay, endOfDay)
    }

    /**
     * PACKAGE_USAGE_STATS 권한 확인
     */
    fun hasUsageStatsPermission(): Boolean {
        val now = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            now - 1000 * 60,  // 1분 전
            now
        )

        // 권한이 있으면 stats가 비어있지 않음
        return stats != null && stats.isNotEmpty()
    }

    /**
     * 앱별 사용 시간 상세 정보 조회 (디버깅용)
     */
    fun getAppUsageDetails(): List<AppUsage> {
        val (startTime, endTime) = getTodayRange()

        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: return emptyList()

        return usageStats
            .filter { it.totalTimeInForeground > 0 }
            .map { stats ->
                AppUsage(
                    packageName = stats.packageName,
                    appName = getAppName(stats.packageName),
                    usageTimeMinutes = (stats.totalTimeInForeground / (1000 * 60)).toInt()
                )
            }
            .sortedByDescending { it.usageTimeMinutes }
    }

    /**
     * 패키지명으로 앱 이름 조회
     */
    private fun getAppName(packageName: String): String {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    /**
     * 앱 사용 정보
     */
    data class AppUsage(
        val packageName: String,
        val appName: String,
        val usageTimeMinutes: Int
    )
}