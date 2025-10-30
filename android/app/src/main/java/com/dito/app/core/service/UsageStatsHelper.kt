package com.dito.app.core.service

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

/**
 * UsageStats
 *
 * 기능:
 * - 앱 사용 시간 조회
 * - 권한 확인/요청
 * - 통계 출력
 */
object UsageStatsHelper {

    private const val TAG = "UsageStats"

    // 권한 확인
    fun hasUsagePermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openUsagePermissionSettings(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        context.startActivity(intent)
    }

    //일일 사용량 로그 출력
    fun logDailyUsage(context: Context) {
        if (!hasUsagePermission(context)) {
            Log.w(TAG, "권한 없음")
            return
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 60 * 60 * 24 // 24시간

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        val format = SimpleDateFormat("HH:mm", Locale.getDefault())

        Log.d(TAG, "========== 24시간 앱 사용량 ==========")
        stats?.forEach {
            if (it.totalTimeInForeground > 0) {
                Log.d(
                    TAG,
                    "${it.packageName} - ${it.totalTimeInForeground / 1000 / 60}분 (마지막: ${format.format(Date(it.lastTimeUsed))})"
                )
            }
        }
        Log.d(TAG, "======================================")
    }

    //오늘 사용량 -> Map으로 반환
    fun getTodayUsageMap(context: Context): Map<String, Long> {
        if (!hasUsagePermission(context)) {
            return emptyMap()
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        // 오늘 00:00:00부터 현재까지
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val now = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startOfDay,
            now
        )

        val usageMap = mutableMapOf<String, Long>()
        stats?.forEach {
            if (it.totalTimeInForeground > 0) {
                usageMap[it.packageName] = it.totalTimeInForeground
            }
        }

        return usageMap
    }

    //특정 앱 오늘 사용 시간(초)
    fun getAppUsageToday(context: Context, packageName: String): Long {
        val usageMap = getTodayUsageMap(context)
        return usageMap[packageName] ?: 0L
    }

    //오늘 스크린타임 총 시간(초)
    fun getTotalScreenTimeToday(context: Context): Long {
        val usageMap = getTodayUsageMap(context)
        return usageMap.values.sum()
    }

    //상위 사용 앱
    fun getTopApps(context: Context, limit: Int = 10): List<Pair<String, Long>> {
        val usageMap = getTodayUsageMap(context)
        return usageMap.entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key to it.value }
    }


    fun logTopApps(context: Context, limit: Int = 5) {
        val topApps = getTopApps(context, limit)

        Log.d(TAG, "========== Top $limit 앱 ==========")
        topApps.forEachIndexed { index, (packageName, time) ->
            val minutes = time / 1000 / 60
            Log.d(TAG, "${index + 1}. $packageName: ${minutes}분")
        }
        Log.d(TAG, "===================================")
    }

    //총 스크린타임
    fun logTotalScreenTime(context: Context) {
        val totalTime = getTotalScreenTimeToday(context)
        val minutes = totalTime / 1000 / 60
        val hours = minutes / 60
        val remainingMinutes = minutes % 60

        Log.d(TAG, "총 스크린 타임: ${hours}시간 ${remainingMinutes}분")
    }
}

