package com.dito.app.core.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import java.text.SimpleDateFormat
import java.util.*

class AppMonitoringService : AccessibilityService() {

    companion object {
        private const val TAG = "AppMonitoring"
    }

    // 현재 사용 중인 앱 추적
    private var currentApp = ""
    private var currentAppStartTime = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "AccessibilityService 연결됨")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 앱 전환 이벤트만 처리
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val packageName = event.packageName?.toString() ?: return
        val timestamp = System.currentTimeMillis()

        // 필터링: 무시할 패키지들
        if (shouldIgnorePackage(packageName)) {
            return
        }

        handleAppSwitch(packageName, timestamp)
    }

    private fun shouldIgnorePackage(packageName: String): Boolean {
        return packageName.startsWith("com.android.systemui") ||
                packageName.startsWith("com.samsung.android.biometrics") ||
                packageName.contains("inputmethod") ||
                packageName == "android" ||
                packageName.isEmpty()
    }

    private fun handleAppSwitch(newApp: String, timestamp: Long) {
        // 같은 앱이면 무시
        if (newApp == currentApp) {
            return
        }

        Log.d(TAG, "📱 앱 전환: $currentApp → $newApp")

        // 이전 앱 종료 기록
        if (currentApp.isNotEmpty() && currentAppStartTime > 0) {
            val duration = timestamp - currentAppStartTime

            // 1초 미만은 무시 (오류 방지)
            if (duration >= 1000) {
                saveAppUsage(
                    packageName = currentApp,
                    startTime = currentAppStartTime,
                    endTime = timestamp,
                    duration = duration
                )

                Log.d(TAG, "$currentApp 사용 완료: ${duration / 1000}초")
            }
        }

        // 새 앱 시작 기록
        currentApp = newApp
        currentAppStartTime = timestamp

        Log.d(TAG, "$newApp 사용 시작")
    }

    private fun saveAppUsage(
        packageName: String,
        startTime: Long,
        endTime: Long,
        duration: Long
    ) {

        // 지금은 로그만 출력
        Log.e(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
        Log.e(TAG, "DB 저장 대상:")
        Log.e(TAG, "  - 앱: $packageName")
        Log.e(TAG, "  - 시작: ${formatTime(startTime)}")
        Log.e(TAG, "  - 종료: ${formatTime(endTime)}")
        Log.e(TAG, "  - 사용시간: ${formatDuration(duration)}")
        Log.e(TAG, "  - 날짜: ${formatDate(startTime)}")
        Log.e(TAG, "━━━━━━━━━━━━━━━━━━━━━━")

        // Realm DB 저장 로직 추가 예정

    }

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun formatDuration(duration: Long): String {
        val seconds = duration / 1000
        val minutes = seconds / 60
        val hours = minutes / 60

        return when {
            hours > 0 -> "${hours}시간 ${minutes % 60}분 ${seconds % 60}초"
            minutes > 0 -> "${minutes}분 ${seconds % 60}초"
            else -> "${seconds}초"
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "AccessibilityService 중단됨")
    }

    override fun onDestroy() {
        super.onDestroy()

        // 서비스 종료 시 현재 앱도 기록
        if (currentApp.isNotEmpty() && currentAppStartTime > 0) {
            val now = System.currentTimeMillis()
            val duration = now - currentAppStartTime

            if (duration >= 1000) {
                saveAppUsage(
                    packageName = currentApp,
                    startTime = currentAppStartTime,
                    endTime = now,
                    duration = duration
                )
            }
        }

        Log.d(TAG, "AccessibilityService 종료됨")
    }
}