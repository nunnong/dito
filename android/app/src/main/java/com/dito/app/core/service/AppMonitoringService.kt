package com.dito.app.core.service

import android.accessibilityservice.AccessibilityService
import com.dito.app.core.data.AppUsageEvent
import com.dito.app.core.data.RealmConfig
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.dito.app.core.network.BehaviorLog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
//앱 전환 감지
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

    //Coroutine으로 실행되는 AI 호출 타이머
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

    //앱 전환 감지 → 이전 앱 세션 종료 + 새 앱 감시 시작
    private fun handleAppSwitch(newApp: String, timestamp: Long) {
        if (newApp == currentApp) return

        Log.v(TAG, "📱 앱 전환: $currentApp → $newApp")

        aiCheckJob?.cancel()

        if (currentApp.isNotEmpty() && currentAppStartTime > 0) {
            val duration = timestamp - currentAppStartTime

            // 3초 미만 무시
            if (duration >= MIN_USAGE_TIME) {
                saveToRealm(
                    packageName = currentApp,
                    startTime = currentAppStartTime,
                    endTime = timestamp,
                    duration = duration,
                    trackType = "TRACK_2"
                )
            }
        }

        currentApp = newApp
        currentAppStartTime = timestamp

        if(Checker.isTargetApp(newApp)){
            scheduleAICheck(newApp, timestamp)
        }
    }

    //10초 후에도 여전히 동일 앱이면 AI 호출 (추후에는 30분으로 조정?)
    private fun scheduleAICheck(packageName: String, startTime: Long) {
        aiCheckJob = CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "[$packageName] 감시 타이머 시작 (10초)")

            delay(Checker.TEST_CHECKER_MS)


            val currentTime = System.currentTimeMillis()
            val duration = currentTime - startTime

            //여전히 같은 앱 사용 중인지 확인
            if (currentApp == packageName) {
                Log.w(TAG, "⚠️ [$packageName] ${duration / 1000}초 사용 중 → AI 호출 시도")

                if (Checker.shouldCallAi(packageName, currentTime)) {
                    //TRACK_1 로그 저장
                    val (eventIds, appName) = saveToRealm(
                        packageName = packageName,
                        startTime = startTime,
                        endTime = currentTime,
                        duration = duration,
                        trackType = "TRACK_1"
                    )

                    aiAgent.requestIntervention(
                        behaviorLog = BehaviorLog(
                            appName = getAppName(packageName),
                            durationSeconds = (duration / 1000).toInt(),
                            usageTimestamp = Checker.formatTimestamp(currentTime)
                        ),
                        eventIds = eventIds
                    )
                }
            } else {
                Log.d(TAG, "10초 내 앱 전환 → AI 호출 취소")
             }

         }
    }

    private fun saveToRealm(
        packageName: String,
        startTime: Long,
        endTime: Long,
        duration: Long,
        trackType: String
    ): Pair<List<String>, String> {
        Log.i(TAG, "💾 Realm 저장 ($trackType): $packageName | ${formatDuration(duration)}")
        val eventIds = mutableListOf<String>()
        var appName = packageName

        try {
            val realm = RealmConfig.getInstance()
            realm.writeBlocking {
                val event = copyToRealm(AppUsageEvent().apply {
                    this.trackType = trackType
                    this.eventType = "APP_USAGE"
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

        // 마지막 세션 TRACK_2로 저장
        if (currentApp.isNotEmpty() && currentAppStartTime > 0) {
            val now = System.currentTimeMillis()
            val duration = now - currentAppStartTime
            if (duration >= MIN_USAGE_TIME) {
                saveToRealm(currentApp, currentAppStartTime, now, duration, "TRACK_2")
            }
        }

        aiCheckJob?.cancel()
        Log.d(TAG, "🛑 AppMonitoringService 종료")
    }
}