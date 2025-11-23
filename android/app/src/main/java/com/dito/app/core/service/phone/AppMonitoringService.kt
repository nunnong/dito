package com.dito.app.core.service.phone

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.dito.app.core.data.RealmConfig
import com.dito.app.core.data.phone.AppUsageEvent
import com.dito.app.core.data.screentime.ScreenTimeUpdateRequest
import com.dito.app.core.network.BehaviorLog
import com.dito.app.core.service.AIAgent
import com.dito.app.core.service.Checker
import com.dito.app.core.service.mission.MissionTracker
import com.dito.app.core.service.phone.PlaybackProbe
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
import kotlinx.coroutines.isActive
import java.time.LocalDate
import com.dito.app.core.util.ScreenTimeCollector
import com.dito.app.core.di.ServiceLocator
import com.dito.app.core.storage.GroupPreferenceManager
import com.dito.app.core.data.screentime.UpdateCurrentAppRequest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager


@AndroidEntryPoint
class AppMonitoringService : AccessibilityService() {

    companion object {
        private const val TAG = "AppMonitoring"
        private const val MIN_USAGE_TIME = 3000L // 3초 미만 무시

        private const val PKG_YOUTUBE = "com.google.android.youtube"
        const val PKG_INSTAGRAM = "com.instagram.android"

        @Volatile
        private var instance: AppMonitoringService? = null

        fun getCurrentAppInfo(): Pair<String, Long>? {
            return instance?.let { service ->
                if (service.currentApp.isNotEmpty() && service.currentAppStartTime > 0) {
                    Pair(service.currentApp, service.currentAppStartTime)
                } else {
                    null
                }
            }
        }

        fun notifyYoutubeStopped() {
            instance?.stopYoutubePeriodicSync()
            instance?.updateCurrentAppAfterSessionStop()
        }

        fun notifyYoutubeStarted() {
            instance?.startYoutubePeriodicSync()
        }
    }

    private var youtubePeriodicSyncJob: Job? = null
    private var usageHeartbeatJob: Job? = null // Usage Heartbeat Job

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> {
                    Log.d(TAG, "📱 화면 켜짐 -> Heartbeat 시작")
                    startUsageHeartbeat()
                }
                Intent.ACTION_SCREEN_OFF -> {
                    Log.d(TAG, "📱 화면 꺼짐 -> Heartbeat 중지")
                    stopUsageHeartbeat()
                }
            }
        }
    }


    @Inject
    lateinit var aiAgent: AIAgent

    @Inject
    lateinit var missionTracker: MissionTracker

    private lateinit var sessionManager: SessionStateManager

    @Volatile
    private var currentApp = ""
    private var currentAppStartTime = 0L

    // Coroutine으로 실행되는 AI 호출 타이머
    private var aiCheckJob: Job? = null



    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        sessionManager = SessionStateManager(applicationContext, aiAgent, missionTracker)
        Log.d(TAG, "✅ AccessibilityService 연결됨")

        // 화면 상태 리시버 등록
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenReceiver, filter)

        // 초기 화면 상태 체크
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (powerManager.isInteractive) {
            startUsageHeartbeat()
        } else {
            Log.d(TAG, "서비스 시작 시 화면 꺼짐 상태 -> Heartbeat 대기")
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // 앱 전환 시에만 감지
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val packageName = event.packageName?.toString() ?: return

        // Dito 앱일 때는 현재 앱 정보만 업데이트하고 사용 시간은 추적하지 않음
        if (packageName == "com.dito.app") {
            // YouTube MediaSession이 활성이면 YouTube를 현재 앱으로 표시 (PIP 모드)
            val hasYoutubeSession = SessionStateManager.isYoutubeSessionActive()
            if (hasYoutubeSession) {
                sendCurrentAppToServer("com.google.android.youtube", "YouTube")
                Log.d(TAG, "Dito 앱이지만 YouTube 재생 중 (PIP) - 현재 앱 YouTube로 표시")
            } else {
                sendCurrentAppToServer(packageName, "Dito")
            }
            currentApp = packageName
            return
        }

        if (shouldIgnorePackage(packageName)) return

        handleAppSwitch(packageName, System.currentTimeMillis())
    }

    private fun shouldIgnorePackage(packageName: String): Boolean {
        return packageName.isEmpty() ||
                packageName == "android" ||
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

        // YouTube를 떠나는 경우 → MediaSession 세션 강제 저장
        if (currentApp == "com.google.android.youtube" && ::sessionManager.isInitialized) {
            Log.d(TAG, "📺 YouTube → 다른 앱 전환 감지 → 미디어 세션 강제 저장")
            sessionManager.forceFlushCurrentSession()
        }

        if (currentApp.isNotEmpty() && currentAppStartTime > 0) {
            val duration = timestamp - currentAppStartTime

            // 3초 미만 무시
            if (duration >= MIN_USAGE_TIME) {

                val durationSeconds = (duration/1000).toInt()

                if(missionTracker.isTracking()){

                    val adjustedDuration = if (currentApp == "com.google.android.youtube") {
                        durationSeconds
                    } else {
                        durationSeconds
                    }

                    missionTracker.onAppSwitch(
                        packageName = currentApp,
                        appName = getAppName(currentApp),
                        durationSeconds = adjustedDuration
                    )
                }


                // 무조건 TRACK_2로 저장 (배치 전송용)
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

        // 현재 사용 중인 앱 서버에 전송
        // MediaSession 활성화 여부 체크: YouTube 재생 중이면 다른 앱이어도 YouTube로 표시 (PIP 모드)
        val hasYoutubeSession = SessionStateManager.isYoutubeSessionActive()
        if (hasYoutubeSession && newApp != "com.google.android.youtube") {
            // 다른 앱이지만 YouTube 재생 중 (PIP 모드)
            sendCurrentAppToServer("com.google.android.youtube", "YouTube")
            Log.d(TAG, "YouTube PIP 모드 - 현재 앱 YouTube로 표시")
        } else {
            // 일반 앱 전환
            sendCurrentAppToServer(newApp, getAppName(newApp))
        }

        // YouTube 앱 진입 시: 스크린타임 전송은 MediaSession 시작 시에만 (SessionStateManager에서 처리)
        if(newApp == "com.google.android.youtube"){
            Log.d(TAG, "YouTube 앱 진입 - 현재 앱은 YouTube로 표시, 전송은 재생 시작 시")
        }
        // YouTube에서 다른 앱으로 전환 시: MediaSession 비활성이면 전송 중단
        else if(currentApp == "com.google.android.youtube"){
            val hasYoutubeSession = SessionStateManager.isYoutubeSessionActive()
            if (!hasYoutubeSession) {
                stopYoutubePeriodicSync()
                Log.d(TAG, "YouTube 종료 (재생 안 함) - 전송 중단")
            }
        }
    }

    private fun stopYoutubePeriodicSync() {
        youtubePeriodicSyncJob?.cancel()
        youtubePeriodicSyncJob = null
    }

    private fun updateCurrentAppAfterSessionStop() {
        // MediaSession 종료 후 현재 foreground 앱으로 업데이트
        if (currentApp.isNotEmpty()) {
            sendCurrentAppToServer(currentApp, getAppName(currentApp))
            Log.d(TAG, "MediaSession 종료 - 현재 앱을 " + getAppName(currentApp) + "로 업데이트")
        }
    }

    
    // 유튜브 전용 스크린타임 전송
    private fun startYoutubePeriodicSync() {
        // 기존 작업이 있으면 중단
        youtubePeriodicSyncJob?.cancel()

        youtubePeriodicSyncJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    // 즉시 스크린타임 전송
                    sendScreenTimeImmediately()

                    Log.d(TAG, "YouTube 사용 중 - 스크린타임 전송 완료")

                    // 10초 대기
                    delay(10 * 1000L)

                } catch (e: Exception) {
                    Log.e(TAG, "YouTube 주기적 전송 오류", e)
                    delay(10 * 1000L)  // 에러 시에도 10초 후 재시도
                }
            }
        }
    }

    private suspend fun sendScreenTimeImmediately() {
        try {
            val activeGroupId = GroupPreferenceManager.getActiveGroupId(this@AppMonitoringService)

            // SharedPreferences에서 직접 토큰 가져오기
            val prefs = applicationContext.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
            val token = prefs.getString("access_token", null)

            if (activeGroupId == null) {
                Log.d(TAG, "활성 그룹 없음 - 스크린타임 전송 스킵")
                return
            }

            if (token.isNullOrEmpty()) {
                Log.d(TAG, "토큰 없음 - 스크린타임 전송 스킵")
                return
            }

            // 현재 스크린타임 수집
            val today = LocalDate.now().toString()
            val totalMinutes = ScreenTimeCollector(this@AppMonitoringService).getTodayScreenTimeMinutes()
            val youtubeMinutes = ScreenTimeCollector(this@AppMonitoringService).getYouTubeUsageMinutes()

            Log.d(TAG, "스크린타임 수집 - 전체: ${totalMinutes}분, YouTube: ${youtubeMinutes}분")

            // API 요청
            val request = ScreenTimeUpdateRequest(
                groupId = activeGroupId.toLong(),
                date = today,
                totalMinutes = totalMinutes,
                youtubeMinutes = youtubeMinutes
            )

            val response = ServiceLocator.apiService.updateScreenTime(
                token = "Bearer $token",
                request = request
            )

            if (response.isSuccessful) {
                Log.d(TAG, "스크린타임 즉시 전송 성공 - YouTube: ${youtubeMinutes}분")
            } else {
                Log.w(TAG, "스크린타임 전송 실패: ${response.code()} - ${response.message()}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "스크린타임 전송 예외: ${e.message}", e)
        }
    }

    private fun sendCurrentAppToServer(packageName: String, appName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val activeGroupId = GroupPreferenceManager.getActiveGroupId(this@AppMonitoringService)

                // SharedPreferences에서 직접 토큰 가져오기
                val prefs = applicationContext.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
                val token = prefs.getString("access_token", null)

                if (activeGroupId == null) {
                    Log.d(TAG, "활성 그룹 없음 - 현재 앱 전송 스킵")
                    return@launch
                }

                if (token.isNullOrEmpty()) {
                    Log.d(TAG, "토큰 없음 - 현재 앱 전송 스킵")
                    return@launch
                }

                // YouTube 재생 중이면 미디어 세션 정보 가져오기
                val mediaInfo = SessionStateManager.getCurrentMediaInfo()
                val (mediaEventId, mediaEducational, mediaEventTimestamp) = if (mediaInfo != null) {
                    Triple(mediaInfo.first, mediaInfo.second, mediaInfo.third)
                } else {
                    Triple(null, null, null)
                }

                val request = UpdateCurrentAppRequest(
                    groupId = activeGroupId.toLong(),
                    appPackage = packageName,
                    appName = appName,
                    mediaEventId = mediaEventId,
                    mediaEducational = mediaEducational,
                    mediaEventTimestamp = mediaEventTimestamp
                )

                Log.d(TAG, "📤 현재 앱 전송: $appName (미디어: eventId=$mediaEventId, edu=$mediaEducational)")

                val response = ServiceLocator.apiService.updateCurrentApp(
                    token = "Bearer $token",
                    request = request
                )

                if (response.isSuccessful) {
                    Log.d(TAG, "✅ 현재 앱 전송 성공: $appName ($packageName)")
                } else {
                    Log.w(TAG, "⚠️ 현재 앱 전송 실패: ${response.code()}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ 현재 앱 전송 예외: ${e.message}", e)
            }
        }
    }

    // 10초 후에도 여전히 동일 앱이면 AI 호출
    private fun scheduleAICheck(packageName: String, startTime: Long) {
        aiCheckJob = CoroutineScope(Dispatchers.IO).launch {
            val delayMs = Checker.TEST_CHECKER_MS
            Log.d(TAG, "[$packageName] 감시 타이머 시작 (${delayMs/1000}초)")

            delay(delayMs)

            val currentTime = System.currentTimeMillis()

            // 테스트용: YouTube 사용시간을 30분으로 강제 설정
            val duration = if (packageName == PKG_YOUTUBE) {
                30 * 60 * 1000L
            } else {
                currentTime - startTime
            }

            // 여전히 같은 앱 사용 중인지 확인
            if (currentApp != packageName) {
                Log.d(TAG, "감시 윈도 중 앱 전환 → AI 호출 취소")
                return@launch
            }

            // ================
            // YouTube: 탐색(비재생) 경로
            // ================
            if (packageName == PKG_YOUTUBE) {
                // 최근 10초 이상 비재생 상태면 '탐색'으로 간주
                val exploring = PlaybackProbe.isNotPlayingFor(Checker.TEST_CHECKER_MS)

                if (!exploring) {
                    Log.d(TAG, "[YouTube] 현재 재생 중 또는 비재생 시간이 짧음 → 탐색 호출 스킵 (재생 경로가 따로 처리)")
                    return@launch
                }

                // 탐색 경로 쿨다운 체크
                if (!Checker.canCallYoutubeExplore()) {
                    Log.d(TAG, "[YouTube] youtube_explore 쿨다운에 의해 스킵")
                    return@launch
                }

                // (선택) 추가 캐싱/검증
                if (!Checker.shouldCallYoutubeExploreByTimer()) {
                    Log.d(TAG, "[YouTube] 탐색 타이머 조건 불충족(캐시 등) → 스킵")
                    return@launch
                }

                // 저장 & 호출
                val (eventIds, appName) = saveToRealmForAI(
                    packageName = packageName,
                    startTime = startTime,
                    duration = duration
                )

                if (eventIds.isNotEmpty()) {
                    Log.d(TAG, "🤖 [YouTube 탐색] AI 실시간 호출")
                    aiAgent.requestIntervention(
                        behaviorLog = BehaviorLog(
                            appName = appName, // "YouTube"
                            durationSeconds = (duration / 1000).toInt(),
                            usageTimestamp = Checker.formatTimestamp(currentTime),
                            recentAppSwitches = null,
                            appMetadata = null
                        ),
                        eventIds = eventIds
                    )
                    // 명시적으로 쿨다운 마킹
                    Checker.markCooldown(Checker.CD_KEY_YT_EXPLORE)
                } else {
                    Log.w(TAG, "⚠️ Realm 저장 실패 → 탐색 기반 AI 호출 불가")
                }
                return@launch
            }

            // ================
            // 일반 앱: 기존 앱-타이머 경로 유지 (인스타 포함)
            // ================
            if (Checker.shouldCallAi(
                    packageName = packageName,
                    sessionStartTime = startTime,
                    duration = duration
                )
            ) {
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
                            usageTimestamp = Checker.formatTimestamp(currentTime),
                            recentAppSwitches = null,
                            appMetadata = null
                        ),
                        eventIds = eventIds
                    )
                    if (packageName == PKG_INSTAGRAM) {
                        Checker.markCooldown(Checker.CD_KEY_IG_APP)
                    }
                } else {
                    Log.w(TAG, "⚠️ Realm 저장 실패 → AI 호출 불가")
                }
            } else {
                Log.d(TAG, "[$packageName] 앱-타이머 조건 불충족 → 호출하지 않음")
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
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "리시버 해제 실패", e)
        }
        stopYoutubePeriodicSync()
        stopUsageHeartbeat()

        // 마지막 세션 저장
        if (currentApp.isNotEmpty() && currentAppStartTime > 0) {
            val now = System.currentTimeMillis()
            val duration = now - currentAppStartTime
            if (duration >= MIN_USAGE_TIME) {
                saveToRealm(currentApp, currentAppStartTime, now, duration)
            }
        }

        aiCheckJob?.cancel()
        Checker.cleanupExpiredCache()
        instance = null

        Log.d(TAG, "🛑 AppMonitoringService 종료")
    }

    // ============================================================================================
    // Usage Heartbeat Logic
    // ============================================================================================

    private fun startUsageHeartbeat() {
        usageHeartbeatJob?.cancel()
        usageHeartbeatJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    if (currentApp.isNotEmpty()) {
                        sendUsageHeartbeat()
                    }
                    delay(5000L) // 5초 주기
                } catch (e: Exception) {
                    Log.e(TAG, "Usage Heartbeat Error", e)
                    delay(5000L)
                }
            }
        }
        Log.d(TAG, "💓 Usage Heartbeat 시작 (5초 주기)")
    }

    private fun stopUsageHeartbeat() {
        usageHeartbeatJob?.cancel()
        usageHeartbeatJob = null
        Log.d(TAG, "💔 Usage Heartbeat 중지")
    }

    private suspend fun sendUsageHeartbeat() {
        try {
            val activeGroupId = GroupPreferenceManager.getActiveGroupId(this@AppMonitoringService)
            val prefs = applicationContext.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
            val token = prefs.getString("access_token", null)

            if (activeGroupId == null || token.isNullOrEmpty()) {
                return
            }

            val appName = getAppName(currentApp)
            val request = com.dito.app.core.data.report.HeartbeatRequest(
                timestamp = System.currentTimeMillis(),
                mediaSession = null, // 일반 앱 사용 시에는 미디어 세션 없음
                currentApp = com.dito.app.core.data.report.HeartbeatRequest.CurrentAppInfo(
                    packageName = currentApp,
                    appName = appName
                )
            )

            val response = ServiceLocator.apiService.updateHeartbeat(
                token = "Bearer $token",
                request = request
            )

            if (response.isSuccessful) {
                Log.v(TAG, "💓 Usage Heartbeat 전송: $appName ($currentApp)")
            } else {
                Log.w(TAG, "⚠️ Usage Heartbeat 실패: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Usage Heartbeat 예외", e)
        }
    }
}