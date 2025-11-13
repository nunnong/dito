package com.dito.app.core.util

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
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
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE)
                    as UsageStatsManager

            val endTime = System.currentTimeMillis()
            val startTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )

            if (stats == null || stats.isEmpty()) {
                Log.w("ScreenTimeCollector", "⚠️ UsageStats 데이터가 비어있습니다 (권한 확인 필요)")
                return 0
            }

            val youtubePackage = "com.google.android.youtube"
            val youtubeStats = stats.firstOrNull {
                it.packageName == youtubePackage
            }

            val totalMillis = youtubeStats?.totalTimeInForeground ?: 0L
            val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(totalMillis).toInt()

            Log.d("ScreenTimeCollector", "YouTube 사용시간: ${totalMinutes}분 (${totalMillis}ms)")

            return totalMinutes
        } catch (e: Exception) {
            Log.e("ScreenTimeCollector", "❌ YouTube 사용시간 조회 실패", e)
            return 0
        }
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
