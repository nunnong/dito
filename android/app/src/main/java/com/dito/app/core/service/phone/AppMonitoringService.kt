package com.dito.app.core.service.phone

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.dito.app.core.data.RealmConfig
import com.dito.app.core.data.phone.AppUsageEvent
import com.dito.app.core.network.BehaviorLog
import com.dito.app.core.service.AIAgent
import com.dito.app.core.service.Checker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AppMonitoringService : AccessibilityService() {

    companion object {
        private const val TAG = "AppMonitoring"
        private const val MIN_USAGE_TIME = 3000L // 3초 미만 무시
    }

    @Inject
    lateinit var aiAgent: AIAgent

    @Volatile
    private var currentApp = ""
    private var currentAppStartTime = 0L

    // Coroutine으로 실행되는 AI 호출 타이머
    private var aiCheckJob: Job? = null


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
        return packageName.isEmpty() ||
                packageName == "android" ||
                packageName == "com.dito.app" ||
                packageName.startsWith("com.android.systemui") ||
                packageName.contains("inputmethod") ||
                packageName.startsWith("com.google.android.inputmethod") ||
                packageName.startsWith("com.samsung.android.honeyboard") ||
                packageName.startsWith("com.sec.android.inputmethod") ||
                packageName.startsWith("com.android.launcher") ||
                packageName.startsWith("com.google.android.apps.nexuslauncher") ||
                packageName.startsWith("com.sec.android.app.launcher") ||
                packageName.startsWith("com.android.settings") ||
                packageName.startsWith("com.samsung.android.settings") ||
                packageName.startsWith("com.android.camera") ||
                packageName.startsWith("com.sec.android.app.camera") ||
                packageName.startsWith("com.android.incallui") ||
                packageName.startsWith("com.android.dialer") ||
                packageName.startsWith("com.android.vending") ||
                packageName.startsWith("com.google.android.gms")
    }

    // 앱 전환 감지 → 이전 앱 세션 종료 + 새 앱 감시 시작
    private fun handleAppSwitch(newApp: String, timestamp: Long) {
        if (newApp == currentApp) return

        Log.v(TAG, "📱 앱 전환: $currentApp → $newApp")

        aiCheckJob?.cancel()

        if (currentApp.isNotEmpty() && currentAppStartTime > 0) {
            val duration = timestamp - currentAppStartTime

            // 3초 미만 무시
            if (duration >= MIN_USAGE_TIME) {
                // ⭐ 무조건 TRACK_2로 저장 (배치 전송용)
                saveToRealm(
                    packageName = currentApp,
                    startTime = currentAppStartTime,
                    endTime = timestamp,
                    duration = duration
                )
            }
        }

        currentApp = newApp
        currentAppStartTime = timestamp

        if (Checker.isTargetApp(newApp)) {
            scheduleAICheck(newApp, timestamp)
        }
    }

    // 20초 후에도 여전히 동일 앱이면 AI 호출
    private fun scheduleAICheck(packageName: String, startTime: Long) {
        aiCheckJob = CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "[$packageName] 감시 타이머 시작 (20초)")

            delay(Checker.TEST_CHECKER_MS)

            val currentTime = System.currentTimeMillis()
            val duration = currentTime - startTime

            // 여전히 같은 앱 사용 중인지 확인
            if (currentApp == packageName) {
                Log.w(TAG, "⚠️ [$packageName] ${duration / 1000}초 사용 중 → AI 호출 시도")

                if (Checker.shouldCallAi(
                        packageName = packageName,
                        sessionStartTime = startTime,
                        duration = duration
                    )) {


                    val (eventIds, appName) = saveToRealmForAI(
                        packageName = packageName,
                        startTime = startTime,
                        duration = duration
                    )

                    if (eventIds.isNotEmpty()) {
                        Log.d(TAG, "🤖 AI 실시간 호출 (배치 전송과 별개)")
                        aiAgent.requestIntervention(
                            behaviorLog = BehaviorLog(
                                appName = appName,
                                durationSeconds = (duration / 1000).toInt(),
                                usageTimestamp = Checker.formatTimestamp(currentTime)
                            ),
                            eventIds = eventIds
                        )
                    } else {
                        Log.w(TAG, "⚠️ Realm 저장 실패 → AI 호출 불가")
                    }
                }
            } else {
                Log.d(TAG, "10초 내 앱 전환 → AI 호출 취소")
            }
        }
    }

    private fun saveToRealmForAI(
        packageName: String,
        startTime: Long,
        duration: Long
    ): Pair<List<String>, String> {
        Log.i(TAG, "💾 AI용 Realm 저장 (TRACK_2): $packageName | ${formatDuration(duration)}")
        val eventIds = mutableListOf<String>()
        var appName = packageName

        try {
            val realm = RealmConfig.getInstance()
            realm.writeBlocking {
                val event = copyToRealm(AppUsageEvent().apply {
                    this.trackType = "TRACK_2"
                    this.eventType = "APP_CLOSE"
                    this.packageName = packageName
                    this.appName = getAppName(packageName)
                    this.timestamp = System.currentTimeMillis()
                    this.duration = duration
                    this.date = formatDate(System.currentTimeMillis())
                    this.synced = false
                    this.aiCalled = true
                })
                eventIds.add(event._id.toHexString())
                appName = event.appName
            }

            Log.d(TAG, "✅ AI용 Realm 저장 완료")

        } catch (e: Exception) {
            Log.e(TAG, "❌ AI용 Realm 저장 실패", e)
        }

        return Pair(eventIds, appName)
    }


    private fun saveToRealm(
        packageName: String,
        startTime: Long,
        endTime: Long,
        duration: Long
    ): Pair<List<String>, String> {
        Log.i(TAG, "💾 Realm 저장 (TRACK_2): $packageName | ${formatDuration(duration)}")
        val eventIds = mutableListOf<String>()
        var appName = packageName

        try {
            val realm = RealmConfig.getInstance()
            realm.writeBlocking {
                val event = copyToRealm(AppUsageEvent().apply {
                    this.trackType = "TRACK_2"
                    this.eventType = "APP_CLOSE"
                    this.packageName = packageName
                    this.appName = getAppName(packageName)
                    this.timestamp = endTime
                    this.duration = duration
                    this.date = formatDate(endTime)
                    this.synced = false
                    this.aiCalled = false
                })
                eventIds.add(event._id.toHexString())
                appName = event.appName
            }

            Log.d(TAG, "✅ Realm 저장 완료 (배치 전송 대기)")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Realm 저장 실패", e)
        }

        return Pair(eventIds, appName)
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

    private fun formatDuration(duration: Long): String {
        val seconds = duration / 1000
        return when {
            seconds < 60 -> "${seconds}초"
            seconds < 3600 -> "${seconds / 60}분"
            else -> "${seconds / 3600}시간"
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
                saveToRealm(currentApp, currentAppStartTime, now, duration)
            }
        }

        aiCheckJob?.cancel()
        Checker.clearExpiredCache()

        Log.d(TAG, "🛑 AppMonitoringService 종료")
    }
}