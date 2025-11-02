package com.dito.app.core.service

import android.accessibilityservice.AccessibilityService
import com.dito.app.core.data.AppUsageEvent
import com.dito.app.core.data.RealmConfig
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import java.text.SimpleDateFormat
import java.util.*

//앱 전환 감지
class AppMonitoringService : AccessibilityService() {

    companion object {
        private const val TAG = "AppMonitoring"
        private const val MIN_USAGE_TIME = 3000L // 3초 미만 무시
    }

    private var currentApp = ""
    private var currentAppStartTime = 0L
    private val sessionManager = SessionStateManager()

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "✅ AccessibilityService 연결됨")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return


        // 앱 전환 시에만 감지
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val packageName = event.packageName?.toString() ?: return

        if (shouldIgnorePackage(packageName)) return

        handleAppSwitch(packageName, System.currentTimeMillis())
    }

    private fun shouldIgnorePackage(packageName: String): Boolean {
        return packageName.startsWith("com.android.systemui") ||
                packageName.startsWith("com.samsung.android.biometrics") ||
                packageName.contains("inputmethod") ||
                packageName == "android" ||
                packageName == "com.dito.app" || // 자기 앱 제외
                packageName.isEmpty()
    }

    private fun handleAppSwitch(newApp: String, timestamp: Long) {
        if (newApp == currentApp) return

        Log.v(TAG, "📱 앱 전환: $currentApp → $newApp")

        if (currentApp.isNotEmpty() && currentAppStartTime > 0) {
            val duration = timestamp - currentAppStartTime

            // 3초 미만 무시
            if (duration >= MIN_USAGE_TIME) {
                saveAppUsage(
                    packageName = currentApp,
                    startTime = currentAppStartTime,
                    endTime = timestamp,
                    duration = duration
                )
            }
        }

        currentApp = newApp
        currentAppStartTime = timestamp
    }

    private fun saveAppUsage(packageName: String, startTime: Long, endTime: Long, duration: Long) {

        Log.i(TAG, "💾 저장: $packageName | ${formatDuration(duration)} | ${formatTime(startTime)}")

        try {
            val realm = RealmConfig.getInstance()

            realm.writeBlocking {
                // OPEN 이벤트
                copyToRealm(AppUsageEvent().apply {
                    this.eventType = "APP_OPEN"
                    this.packageName = packageName
                    this.appName = getAppName(packageName)
                    this.timestamp = startTime
                    this.duration = 0L
                    this.date = formatDate(startTime)
                    this.synced = false
                })

                // CLOSE 이벤트
                copyToRealm(AppUsageEvent().apply {
                    this.eventType = "APP_CLOSE"
                    this.packageName = packageName
                    this.appName = getAppName(packageName)
                    this.timestamp = endTime
                    this.duration = duration
                    this.date = formatDate(endTime)
                    this.synced = false
                })
            }

            Log.d(TAG, "✅ Realm 저장 완료")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Realm 저장 실패", e)
        }
    }

    private fun getAppName(packageName: String): String {
        return try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun formatDuration(duration: Long): String {
        val seconds = duration / 1000
        val minutes = seconds / 60
        val hours = minutes / 60

        return when {
            hours > 0 -> "${hours}h ${minutes % 60}m"
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "⚠️ AccessibilityService 중단됨")
    }

    override fun onDestroy() {
        super.onDestroy()

        // 마지막 세션 저장
        if (currentApp.isNotEmpty() && currentAppStartTime > 0) {
            val now = System.currentTimeMillis()
            val duration = now - currentAppStartTime
            if (duration >= MIN_USAGE_TIME) {
                saveAppUsage(currentApp, currentAppStartTime, now, duration)
            }
        }

        Log.d(TAG, "🛑 AccessibilityService 종료됨")
    }
}