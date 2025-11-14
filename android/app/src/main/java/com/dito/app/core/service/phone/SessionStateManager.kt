package com.dito.app.core.service.phone

import android.content.Context
import android.media.MediaMetadata
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.dito.app.core.data.RealmConfig
import com.dito.app.core.data.phone.MediaSessionEvent
import com.dito.app.core.network.AppMetadata
import com.dito.app.core.network.BehaviorLog
import com.dito.app.core.service.AIAgent
import com.dito.app.core.service.Checker
import com.dito.app.core.service.mission.MissionTracker
import java.text.SimpleDateFormat
import java.util.concurrent.atomic.AtomicInteger
import java.util.Date
import java.util.Locale

class SessionStateManager(
    private val context: Context,
    private val aiAgent: AIAgent,
    private val missionTracker: MissionTracker
) {



    companion object {
        private const val TAG = "SessionState"
        private const val MIN_WATCH_TIME = 5000L
        private const val SESSION_UPDATE_THRESHOLD = 5000L
        private const val SAVE_DELAY = 500L
        private const val METADATA_WAIT_DELAY = 1000L // 채널명 대기 시간 (1초)

        private const val PKG_YOUTUBE = "com.google.android.youtube"

        @Volatile
        private var instance: SessionStateManager? = null

        fun setInstance(manager: SessionStateManager) {
            instance = manager
        }

        /**
         * 현재 재생 중인 YouTube 세션의 시청 시간 반환 (밀리초)
         * 10초마다 호출되는 랭킹 갱신에서 사용
         */
        fun getCurrentSessionWatchTime(): Long {
            val session = instance?.currentSession ?: return 0L

            // YouTube가 아니면 0 반환
            if (session.appPackage != PKG_YOUTUBE) return 0L

            val currentTime = System.currentTimeMillis()
            val totalTime = currentTime - session.startTime
            var watchTime = totalTime - session.totalPauseTime

            // 현재 일시정지 중이면 일시정지 시간도 차감
            session.lastPauseTime?.let { pauseTime ->
                val currentPauseDuration = currentTime - pauseTime
                watchTime -= currentPauseDuration
            }

            return maxOf(0L, watchTime)
        }
    }

    private var currentSession: ActiveSession? = null
    private var lastSessionTitle: String = ""
    private var lastSessionTime: Long = 0L
    private var lastStoppedAt: Long = 0L
    private var lastStoppedKey: String = ""
    private val stopDebounceMs = 400L
    private val handler = Handler(Looper.getMainLooper())
    private var pendingSaveRunnable: Runnable? = null
    private var pendingSessionSaveRunnable: Runnable? = null // 영상 전환 시 이전 세션 저장 대기
    private var sessionToSave: ActiveSession? = null // 저장 대기 중인 이전 세션
    private var aiCheckRunnable: Runnable? = null // 재생 중 AI 호출 타이머
    private var explorationCheckRunnable: Runnable? = null // 탐색 중 AI 호출 타이머
    private var explorationStartTime: Long = 0L // 탐색 시작 시간




    data class ActiveSession(
        var title: String,
        var channel: String,
        var bestChannel: String,
        var appPackage: String,
        var duration: Long,
        var startTime: Long,
        var lastPauseTime: Long? = null,
        var totalPauseTime: Long = 0L
    )

    fun handlePlaybackStarted(
        metadata: MediaMetadata,
        appPackage: String
    ) {
        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE) ?: ""
        val rawChannel = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: ""
        val channel = rawChannel.ifBlank { "알 수 없음" }
        val duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
        val currentTime = System.currentTimeMillis()

        // PlaybackProbe 기록: 재생 시작
        PlaybackProbe.recordPlayback()

        // 재생 중 AI 호출 타이머 시작 (YouTube만)
        if (appPackage == PKG_YOUTUBE) {
            cancelExplorationCheck() // 탐색 타이머 취소
            scheduleAICheckDuringPlayback()
        }

        if (title.isBlank()) {
            Log.d(TAG, "빈 제목 무시")
            return
        }
        if (title.equals("YouTube", true)) {
            Log.d(TAG, "YouTube 로딩 중 - 대기")
            return
        }

        val isValidChannel = channel !in setOf("알 수 없음", "m.youtube.com", "www.youtube.com", "YouTube", "youtube")

        Log.d(TAG, "재생 시작")
        Log.d(TAG, "   제목: $title")
        Log.d(TAG, "   채널: $channel (유효: $isValidChannel)")

        pendingSaveRunnable?.let { handler.removeCallbacks(it) }
        pendingSaveRunnable = null

        currentSession?.let { session ->
            val isDifferentVideo = session.title != title
            val isLongTimeSinceLastEvent = (currentTime - lastSessionTime) >= SESSION_UPDATE_THRESHOLD

            if (isDifferentVideo) {
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG, "다른 영상 감지: ${session.title} → $title")
                Log.d(TAG, "즉시 저장 (영상 전환)")
                Log.d(TAG, "bestChannel 사용: ${session.bestChannel}")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
                saveSession(session)

                currentSession = ActiveSession(
                    title = title,
                    channel = channel,
                    bestChannel = if (isValidChannel) channel else "",
                    appPackage = appPackage,
                    duration = duration,
                    startTime = System.currentTimeMillis()
                )
                Log.d(TAG, "새 세션 생성 (다른 영상)")
                Log.d(TAG, "  초기 channel: $channel")
                Log.d(TAG, "  초기 bestChannel: ${if (isValidChannel) channel else ""}")

            } else if (isLongTimeSinceLastEvent) {
                // 같은 제목 재시작 분기에서 ifBlank 고착화 제거
                val elapsedTime = System.currentTimeMillis() - session.startTime
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG, "같은 영상 재시작 감지 (${elapsedTime / 1000}초 경과)")
                Log.d(TAG, "즉시 저장 (재시작)")
                Log.d(TAG, "bestChannel 사용: ${session.bestChannel}")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
                saveSession(session)

                currentSession = ActiveSession(
                    title = title,
                    // 표시용 현재 채널: 유효하면 최신값을 우선, 아니면 기존 값 유지
                    channel = if (isValidChannel && channel.isNotBlank()) channel else session.channel,
                    // 신뢰 채널: 기존 값이 있으면 유지, 없으면 이번에 채움
                    bestChannel = when {
                        session.bestChannel.isNotBlank() -> session.bestChannel
                        isValidChannel && channel.isNotBlank() -> channel
                        else -> ""
                    },
                    appPackage = appPackage,
                    duration = duration,
                    startTime = System.currentTimeMillis()
                )
                Log.d(TAG, "새 세션 생성 (재시작)")
                Log.d(TAG, "  channel: ${currentSession?.channel}")
                Log.d(TAG, "  bestChannel: ${currentSession?.bestChannel}")

            } else {
                Log.d(TAG, "기존 세션 유지 (${currentTime - lastSessionTime}ms 경과)")

                // 채널 업데이트(유효하면 무조건 갱신)
                if (isValidChannel) {
                    if (session.bestChannel.isBlank() || session.bestChannel != channel) {
                        Log.d(TAG, "handlePlaybackStarted에서 채널 업데이트: ${session.channel} → $channel")
                        session.channel = channel
                        session.bestChannel = channel
                    } else {
                        Log.d(TAG, "채널 이미 설정됨: $channel")
                    }
                } else {
                    Log.d(TAG, "유효하지 않은 채널이므로 업데이트 안 함: $channel")
                }

                // 일시정지 재개 처리
                session.lastPauseTime?.let { pauseTime ->
                    val pauseDuration = System.currentTimeMillis() - pauseTime
                    session.totalPauseTime += pauseDuration
                    session.lastPauseTime = null
                    Log.d(TAG, "재생 재개 (일시정지: ${pauseDuration / 1000}초)")
                }

                lastSessionTitle = title
                lastSessionTime = currentTime
                return
            }
        } ?: run {
            currentSession = ActiveSession(
                title = title,
                channel = channel,
                bestChannel = if (isValidChannel) channel else "",
                appPackage = appPackage,
                duration = duration,
                startTime = System.currentTimeMillis()
            )
            Log.d(TAG, "새 세션 생성 (첫 재생)")
            Log.d(TAG, "  초기 channel: $channel")
            Log.d(TAG, "  초기 bestChannel: ${if (isValidChannel) channel else ""}")
        }

        lastSessionTitle = title
        lastSessionTime = currentTime
    }

    fun handlePlaybackPaused() {
        // 재생 중 AI 호출 타이머 취소
        cancelAICheck()

        // YouTube 탐색 타이머 시작
        currentSession?.let { session ->
            if (session.appPackage == PKG_YOUTUBE) {
                scheduleExplorationCheck()
            }
            session.lastPauseTime = System.currentTimeMillis()
            Log.d(TAG, "일시정지")
        }
    }

    fun handlePlaybackResumed() {
        // 탐색 타이머 취소 (재생 재개)
        cancelExplorationCheck()

        currentSession?.let { session ->
            session.lastPauseTime?.let { pauseTime ->
                val pauseDuration = System.currentTimeMillis() - pauseTime
                session.totalPauseTime += pauseDuration
                session.lastPauseTime = null
                Log.d(TAG, "재개")
                Log.d(TAG, "  일시정지 시간: ${pauseDuration / 1000}초")
            }
        }
    }

    fun handlePlaybackStopped() {
        // 재생 중 AI 호출 타이머 취소
        cancelAICheck()

        // YouTube 탐색 타이머 시작
        currentSession?.let { session ->
            if (session.appPackage == PKG_YOUTUBE) {
                scheduleExplorationCheck()
            }

            val now = System.currentTimeMillis()
            val stopKey = "${session.appPackage}|${session.title}"
            if (stopKey == lastStoppedKey && (now - lastStoppedAt) < stopDebounceMs) {
                Log.d(TAG, "STOPPED 디바운스 히트(${now - lastStoppedAt}ms) → 중복 STOPPED 무시")
                return
            }
            lastStoppedKey = stopKey
            lastStoppedAt = now


            Log.d(TAG, "재생 종료 → ${SAVE_DELAY}ms 후 저장 예약")
            Log.d(TAG, "   현재 channel: ${session.channel}")
            Log.d(TAG, "   현재 bestChannel: ${session.bestChannel}")

            // 기존 예약이 있으면 취소
            pendingSaveRunnable?.let { handler.removeCallbacks(it) }

            // 예약 저장 runnable
            pendingSaveRunnable = Runnable {
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG, "${SAVE_DELAY}ms 대기 완료 → 세션 저장 시작")
                Log.d(TAG, "   최종 channel: ${session.channel}")
                Log.d(TAG, "   최종 bestChannel: ${session.bestChannel}")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")

                saveSession(session)

                // 세션 종료 정리
                currentSession = null
                lastSessionTitle = ""
                lastSessionTime = 0L
                pendingSaveRunnable = null
            }

            handler.postDelayed(pendingSaveRunnable!!, SAVE_DELAY)
        }
    }

    fun updateMetadata(metadata: MediaMetadata) {
        currentSession?.let { session ->
            val newTitle = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
            val rawChannel = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
            val newChannel = if (rawChannel.isNullOrBlank()) "" else rawChannel

            if (!newTitle.isNullOrBlank() && newTitle != session.title) {
                if (newTitle.equals("YouTube", true)) {
                    Log.d(TAG, "YouTube 로딩 중 제목 무시")
                    return@let
                }

                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG, "updateMetadata에서 제목 변경 감지!")
                Log.d(TAG, "   이전: ${session.title}")
                Log.d(TAG, "   새로운: $newTitle")
                Log.d(TAG, "   새 채널: $newChannel")
                Log.d(TAG, "   이전 세션 현재 상태:")
                Log.d(TAG, "     - channel: ${session.channel}")
                Log.d(TAG, "     - bestChannel: ${session.bestChannel}")
                Log.d(TAG, "   → ${METADATA_WAIT_DELAY}ms 대기 후 이전 세션 저장")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")

                pendingSessionSaveRunnable?.let { handler.removeCallbacks(it) }
                sessionToSave = session.copy()
                pendingSessionSaveRunnable = Runnable {
                    sessionToSave?.let { oldSession ->
                        Log.d(TAG, "⏰ 대기 완료 → 이전 세션 저장")
                        Log.d(TAG, "   제목: ${oldSession.title}")
                        Log.d(TAG, "   최종 channel: ${oldSession.channel}")
                        Log.d(TAG, "   최종 bestChannel: ${oldSession.bestChannel}")
                        saveSession(oldSession)
                    }
                    sessionToSave = null
                    pendingSessionSaveRunnable = null
                }
                handler.postDelayed(pendingSessionSaveRunnable!!, METADATA_WAIT_DELAY)

                val isValidChannel = newChannel.isNotBlank() &&
                        newChannel !in setOf("알 수 없음", "m.youtube.com", "www.youtube.com", "YouTube", "youtube")

                currentSession = ActiveSession(
                    title = newTitle,
                    channel = if (isValidChannel) newChannel else "알 수 없음",
                    bestChannel = if (isValidChannel) newChannel else "",
                    appPackage = session.appPackage,
                    duration = 0L,
                    startTime = System.currentTimeMillis()
                )

                lastSessionTitle = newTitle
                lastSessionTime = System.currentTimeMillis()

                Log.d(TAG, "새 세션 생성 (updateMetadata)")
                Log.d(TAG, "  제목: $newTitle")
                Log.d(TAG, "  초기 channel: ${if (isValidChannel) newChannel else "알 수 없음"}")
                Log.d(TAG, "  초기 bestChannel: ${if (isValidChannel) newChannel else ""}")
                return@let
            }

            if (newChannel.isNotBlank()) {
                val isValidChannel = newChannel !in setOf("알 수 없음", "m.youtube.com", "www.youtube.com", "YouTube", "youtube")
                if (isValidChannel) {
                    if (session.bestChannel.isBlank()) {
                        Log.d(TAG, "updateMetadata에서 채널 업데이트: ${session.channel} → $newChannel")
                        session.channel = newChannel
                        session.bestChannel = newChannel
                    } else if (session.bestChannel != newChannel) {
                        Log.w(TAG, "⚠️ updateMetadata에서 채널 변경 감지: ${session.bestChannel} → $newChannel (같은 제목)")
                        session.channel = newChannel
                        session.bestChannel = newChannel
                    } else {
                        Log.d(TAG, "updateMetadata: 이미 설정된 채널과 동일 ($newChannel)")
                    }
                } else {
                    Log.d(TAG, "updateMetadata: 유효하지 않은 채널 무시 ($newChannel)")
                }
            } else {
                Log.d(TAG, "updateMetadata: 빈 채널명 무시")
            }
        }
    }

    // 재생 중 AI 호출 타이머 시작
    private fun scheduleAICheckDuringPlayback() {
        // 기존 타이머 취소
        cancelAICheck()

        aiCheckRunnable = Runnable {
            currentSession?.let { session ->
                if (session.appPackage != PKG_YOUTUBE) return@let

                val currentTime = System.currentTimeMillis()
                val watchTime = currentTime - session.startTime - session.totalPauseTime

                val payloadWatchTime = 30 * 60 * 1000L

                Log.d(TAG, "⏰ 재생 중 AI 호출 타이머 트리거 (${watchTime / 1000}초 시청)")

                // 쿨다운 체크
                if (!Checker.canCallYoutubePlay()) {
                    Log.d(TAG, "YouTube 재생 쿨다운 중 → AI 호출 스킵")
                    return@let
                }

                val finalChannel = when {
                    session.bestChannel.isNotBlank() -> session.bestChannel
                    session.channel.isNotBlank() && session.channel != "알 수 없음" -> session.channel
                    else -> "알 수 없는 채널"
                }

                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG, "checkMediaSession 호출 직전 값 확인:")
                Log.d(TAG, "  title: '${session.title}'")
                Log.d(TAG, "  finalChannel: '$finalChannel'")
                Log.d(TAG, "  session.bestChannel: '${session.bestChannel}'")
                Log.d(TAG, "  session.channel: '${session.channel}'")
                Log.d(TAG, "  watchTime: $watchTime")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")

                val checkPoint = Checker.checkMediaSession(
                    title = session.title,
                    channel = finalChannel,
                    watchTime = payloadWatchTime,
                    timestamp = currentTime,
                    appPackage = session.appPackage
                )

                if (checkPoint != null) {
                    // Realm에 저장 (TRACK_2, 배치 전송용)
                    val eventIds = mutableListOf<String>()
                    try {
                        val realm = RealmConfig.getInstance()
                        realm.writeBlocking {
                            val event = copyToRealm(MediaSessionEvent().apply {
                                this.trackType = "TRACK_2"
                                this.eventType = "PLAYING_CHECK" // 재생 중 체크
                                this.title = session.title
                                this.channel = finalChannel
                                this.appPackage = session.appPackage
                                this.timestamp = currentTime
                                this.videoDuration = session.duration
                                this.watchTime = watchTime
                                this.pauseTime = session.totalPauseTime
                                this.date = formatDate(currentTime)
                                this.detectionMethod = "playback-timer"
                                this.synced = false
                            })
                            eventIds.add(event._id.toHexString())
                        }
                        Log.d(TAG, "✅ 재생 중 체크 Realm 저장 완료")
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ 재생 중 체크 Realm 저장 실패", e)
                        return@let
                    }

                    Log.d(TAG, "🤖 재생 중 AI 호출 (무의식적 시청 감지)")
                    aiAgent.requestIntervention(
                        behaviorLog = BehaviorLog(
                            appName = checkPoint.appName,
                            durationSeconds = checkPoint.durationSeconds,
                            usageTimestamp = checkPoint.usageTimestamp,
                            recentAppSwitches = null,
                            appMetadata = AppMetadata(
                                title = session.title,
                                channel = finalChannel
                            )
                        ),
                        eventIds = eventIds
                    )
                    // 쿨다운 마킹
                    Checker.markCooldown(Checker.CD_KEY_YT_PLAY)
                } else {
                    Log.d(TAG, "재생 중 체크: AI 호출 조건 불충족")
                }
            }
        }

        handler.postDelayed(aiCheckRunnable!!, Checker.TEST_CHECKER_MS)
        Log.d(TAG, "🎬 재생 중 AI 타이머 시작 (${Checker.TEST_CHECKER_MS / 1000}초 후)")
    }

    // AI 호출 타이머 취소
    private fun cancelAICheck() {
        aiCheckRunnable?.let {
            handler.removeCallbacks(it)
            aiCheckRunnable = null
            Log.d(TAG, "⏹ 재생 중 AI 타이머 취소")
        }
    }

    // 탐색 중 AI 호출 타이머 시작
    private fun scheduleExplorationCheck() {
        // 기존 탐색 타이머 취소
        cancelExplorationCheck()

        // 탐색 시작 시간 기록
        explorationStartTime = System.currentTimeMillis()

        explorationCheckRunnable = Runnable {
            // 20초 이상 비재생 상태인지 확인
            val exploring = PlaybackProbe.isNotPlayingFor(Checker.TEST_CHECKER_MS)

            if (!exploring) {
                Log.d(TAG, "[YouTube 탐색] 재생 재개됨 → 탐색 호출 스킵")
                return@Runnable
            }

            // 쿨다운 체크
            if (!Checker.canCallYoutubeExplore()) {
                Log.d(TAG, "[YouTube 탐색] 쿨다운 중 → 호출 스킵")
                return@Runnable
            }

            Log.d(TAG, "🔍 YouTube 탐색 감지 (앱 내에서 비재생 20초 경과)")

            // 테스트용: 30분 사용시간으로 설정
            val duration = 30 * 60 * 1000L // 30분 (밀리초)

            // Realm 저장
            val eventIds = mutableListOf<String>()
            try {
                val realm = RealmConfig.getInstance()
                realm.writeBlocking {
                    val event = copyToRealm(com.dito.app.core.data.phone.AppUsageEvent().apply {
                        this.trackType = "TRACK_2"
                        this.eventType = "APP_EXPLORATION"
                        this.packageName = PKG_YOUTUBE
                        this.appName = "YouTube"
                        this.timestamp = System.currentTimeMillis()
                        this.duration = duration
                        this.date = formatDate(System.currentTimeMillis())
                        this.synced = false
                        this.aiCalled = true
                    })
                    eventIds.add(event._id.toHexString())
                }
                Log.d(TAG, "✅ 탐색 Realm 저장 완료")
            } catch (e: Exception) {
                Log.e(TAG, "❌ 탐색 Realm 저장 실패", e)
                return@Runnable
            }

            // AI 호출
            Log.d(TAG, "🤖 [YouTube 탐색] AI 실시간 호출")
            aiAgent.requestIntervention(
                behaviorLog = BehaviorLog(
                    appName = "YouTube",
                    durationSeconds = (duration / 1000).toInt(),
                    usageTimestamp = Checker.formatTimestamp(System.currentTimeMillis()),
                    recentAppSwitches = null,
                    appMetadata = null
                ),
                eventIds = eventIds
            )

            // 쿨다운 마킹
            Checker.markCooldown(Checker.CD_KEY_YT_EXPLORE)
        }

        handler.postDelayed(explorationCheckRunnable!!, Checker.TEST_CHECKER_MS)
        Log.d(TAG, "🔍 YouTube 탐색 타이머 시작 (${Checker.TEST_CHECKER_MS / 1000}초 후)")
    }

    // 탐색 타이머 취소
    private fun cancelExplorationCheck() {
        explorationCheckRunnable?.let {
            handler.removeCallbacks(it)
            explorationCheckRunnable = null
            explorationStartTime = 0L
            Log.d(TAG, "⏹ 탐색 타이머 취소")
        }
    }

    private fun saveSession(session: ActiveSession) {
        val endTime = System.currentTimeMillis()
        val totalTime = endTime - session.startTime
        val watchTime = totalTime - session.totalPauseTime

        if (watchTime < MIN_WATCH_TIME) {
            Log.d(TAG, "시청 시간 너무 짧음 (${watchTime}ms) - 저장 안 함")
            return
        }

        val finalChannel = when {
            session.bestChannel.isNotBlank() -> {
                Log.d(TAG, "✅ bestChannel 사용: ${session.bestChannel}")
                session.bestChannel
            }
            session.channel.isNotBlank() && session.channel != "알 수 없음" -> {
                Log.d(TAG, "✅ channel 사용: ${session.channel}")
                session.channel
            }
            else -> {
                Log.w(TAG, "⚠️ 채널 정보 없음 → 기본값 사용")
                "알 수 없는 채널"
            }
        }

        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "세션 저장")
        Log.d(TAG, "   제목: ${session.title}")
        Log.d(TAG, "   채널 상태:")
        Log.d(TAG, "     - session.channel: ${session.channel}")
        Log.d(TAG, "     - session.bestChannel: ${session.bestChannel}")
        Log.d(TAG, "     - 최종 선택: $finalChannel")
        Log.d(TAG, "   앱: ${session.appPackage}")
        Log.d(TAG, "   시작: ${formatTime(session.startTime)}")
        Log.d(TAG, "   종료: ${formatTime(endTime)}")
        Log.d(TAG, "   총 경과: ${totalTime / 1000}초")
        Log.d(TAG, "   시청 시간: ${watchTime / 1000}초")
        Log.d(TAG, "   일시정지: ${session.totalPauseTime / 1000}초")
        Log.d(TAG, "   날짜: ${formatDate(session.startTime)}")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")

        // YouTube 재생 기반 쿨다운 체크
        val canCallAI = if (session.appPackage == PKG_YOUTUBE) {
            Checker.canCallYoutubePlay()
        } else {
            true // 다른 앱은 별도 로직
        }

        // 테스트용: YouTube 재생 시간을 30분으로 강제 설정
        val adjustedWatchTime = if (session.appPackage == PKG_YOUTUBE) {
            30 * 60 * 1000L
        } else {
            watchTime
        }

        val checkPoint = if (canCallAI) {
            Checker.checkMediaSession(
                title = session.title,
                channel = finalChannel,
                watchTime = adjustedWatchTime,
                timestamp = endTime,
                appPackage = session.appPackage
            )
        } else {
            Log.d(TAG, "YouTube 재생 쿨다운 중 (${Checker.CD_KEY_YT_PLAY}) → AI 호출 스킵")
            null
        }

        val trackType = "TRACK_2"
        val eventIds = mutableListOf<String>()

        try {
            val realm = RealmConfig.getInstance()
            realm.writeBlocking {
                val event = copyToRealm(MediaSessionEvent().apply {
                    this.trackType = trackType
                    this.eventType = "VIDEO_END"
                    this.title = session.title
                    this.channel = finalChannel
                    this.appPackage = session.appPackage
                    this.timestamp = endTime
                    this.videoDuration = session.duration
                    this.watchTime = watchTime
                    this.pauseTime = session.totalPauseTime
                    this.date = formatDate(session.startTime)
                    this.detectionMethod = "media-session"
                    this.synced = false
                })
                eventIds.add(event._id.toHexString())
            }
            Log.d(TAG, "✅ Realm 저장 완료 ($trackType)")

            if (missionTracker.isTracking()) {
                missionTracker.onMediaEvent(
                    packageName = session.appPackage,
                    videoTitle = session.title,
                    channelName = finalChannel,
                    watchTimeSeconds = (watchTime / 1000).toInt(),
                    eventType = "VIDEO_END"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Realm 저장 실패", e)
            return
        }

        if (checkPoint != null) {
            Log.d(TAG, "🤖 AI 실시간 호출 (배치 전송과 별개)")
            aiAgent.requestIntervention(
                behaviorLog = BehaviorLog(
                    appName = checkPoint.appName,
                    durationSeconds = checkPoint.durationSeconds,
                    usageTimestamp = checkPoint.usageTimestamp,
                    recentAppSwitches = null,
                    appMetadata = AppMetadata(
                        title = session.title,
                        channel = finalChannel
                    )
                ),
                eventIds = eventIds
            )
        }
    }


    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun cleanup() {
        cancelAICheck()
        cancelExplorationCheck()

        pendingSaveRunnable?.let { handler.removeCallbacks(it) }
        pendingSaveRunnable = null

        pendingSessionSaveRunnable?.let { handler.removeCallbacks(it) }
        pendingSessionSaveRunnable = null

        currentSession?.let { session ->
            Log.d(TAG, "⚠️ 서비스 종료 → 남은 세션 즉시 저장")
            saveSession(session)
        }
    }

    /** 앱 전환 시 현재 세션 강제 저장 */
    fun forceFlushCurrentSession() {
        cancelAICheck()
        cancelExplorationCheck()

        currentSession?.let { session ->
            val currentTime = System.currentTimeMillis()
            val totalTime = currentTime - session.startTime
            val watchTime = totalTime - session.totalPauseTime

            if (watchTime < MIN_WATCH_TIME) {
                Log.d(TAG, "🔄 강제 플러시: 시청 시간 너무 짧음 (${watchTime / 1000}초) - 저장 생략")
                currentSession = null
                lastSessionTitle = ""
                lastSessionTime = 0L
                return
            }

            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "🔄 강제 플러시 → 앱 전환으로 인한 즉시 저장")
            Log.d(TAG, "   제목: ${session.title}")
            Log.d(TAG, "   채널: ${session.channel}")
            Log.d(TAG, "   bestChannel: ${session.bestChannel}")
            Log.d(TAG, "   시청 시간: ${watchTime / 1000}초")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")

            pendingSaveRunnable?.let { handler.removeCallbacks(it) }
            pendingSaveRunnable = null
            pendingSessionSaveRunnable?.let { handler.removeCallbacks(it) }
            pendingSessionSaveRunnable = null

            saveSession(session)
            currentSession = null
            lastSessionTitle = ""
            lastSessionTime = 0L
        } ?: run {
            Log.d(TAG, "🔄 강제 플러시: 저장할 세션 없음")
        }
    }
}
