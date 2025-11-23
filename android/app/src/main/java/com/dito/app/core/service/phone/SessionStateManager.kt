package com.dito.app.core.service.phone

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.net.Uri
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
import com.dito.app.core.util.EducationalContentDetector
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.concurrent.atomic.AtomicInteger
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId

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

        /**
         * 현재 재생 중인 세션이 교육 콘텐츠인지 확인
         * 제목과 채널명을 기반으로 판단
         */
        fun isCurrentSessionEducational(): Boolean {
            val session = instance?.currentSession ?: return false

            // YouTube가 아니면 false
            if (session.appPackage != PKG_YOUTUBE) return false

            val finalChannel = when {
                session.bestChannel.isNotBlank() -> session.bestChannel
                session.channel.isNotBlank() && session.channel != "알 수 없음" -> session.channel
                else -> ""
            }

            return EducationalContentDetector.isEducationalContent(session.title, finalChannel)
        }

        /**
         * YouTube 세션이 활성화되어 있는지 확인
         */
        fun isYoutubeSessionActive(): Boolean {
            val session = instance?.currentSession ?: return false
            return session.appPackage == PKG_YOUTUBE
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

    // Heartbeat
    private var heartbeatJob: kotlinx.coroutines.Job? = null
    private val HEARTBEAT_INTERVAL = 5000L





    data class ActiveSession(
        var title: String,
        var channel: String,
        var bestChannel: String,
        var appPackage: String,
        var duration: Long,
        var startTime: Long,
        var thumbnailUri: String = "",
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

        // 썸네일 URI 추출 (URL 또는 Bitmap)
        val thumbnailUri = extractThumbnailUri(metadata)

        // PlaybackProbe 기록: 재생 시작
        PlaybackProbe.recordPlayback()

        // 재생 중 AI 호출 타이머 시작 (YouTube만)
        if (appPackage == PKG_YOUTUBE) {
            cancelExplorationCheck() // 탐색 타이머 취소
            scheduleAICheckDuringPlayback()
            AppMonitoringService.notifyYoutubeStarted()
            startHeartbeat() // Heartbeat 시작
            Log.d(TAG, "YouTube 재생 시작 - 스크린타임 전송 시작")
        }

        // ========== 광고 및 더미 데이터 필터링 ==========

        // 1. 제목 필터링: 빈 값이거나 "YouTube"인 경우 (로딩 중 더미 데이터)
        if (title.isBlank()) {
            Log.d(TAG, "⚠️ 빈 제목 무시")
            return
        }
        if (title.equals("YouTube", true)) {
            Log.d(TAG, "⚠️ YouTube 로딩 중 - 대기")
            return
        }

        // 2. Duration 필터링: 5초 미만은 광고 가능성
        if (duration > 0 && duration < 5000L) {
            Log.d(TAG, "⚠️ 짧은 영상 무시 (광고 가능성): ${duration}ms")
            return
        }

        // 3. 썸네일 필터링: 썸네일이 없으면 유효하지 않은 데이터
        if (thumbnailUri.isBlank()) {
            Log.d(TAG, "⚠️ 썸네일 없는 메타데이터 무시 (광고 또는 로딩 중)")
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
                    startTime = System.currentTimeMillis(),
                    thumbnailUri = thumbnailUri
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
                    startTime = System.currentTimeMillis(),
                    thumbnailUri = thumbnailUri
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
                startTime = System.currentTimeMillis(),
                thumbnailUri = thumbnailUri
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
                // 일시정지 상태 전송 (한 번만)
                sendHeartbeat(session, "PAUSED")
                stopHeartbeat() // 반복 전송 중단
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
            if (session.appPackage == PKG_YOUTUBE) {
                startHeartbeat() // Heartbeat 재개
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
                AppMonitoringService.notifyYoutubeStopped()
                // 정지 상태 전송 (한 번만)
                sendHeartbeat(session, "STOPPED")
                stopHeartbeat()
                Log.d(TAG, "YouTube 재생 멈춤 - 스크린타임 전송 중단")
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

            // 썸네일 URI 추출 (URL 또는 Bitmap)
            val thumbnailUri = extractThumbnailUri(metadata)

            // 썸네일 중복 업데이트 방지: 이미 있으면 업데이트 안 함 (첫 번째 썸네일 유지)
            if (thumbnailUri.isNotBlank() && session.thumbnailUri.isBlank()) {
                Log.d(TAG, "✅ 썸네일 URI 설정: $thumbnailUri")
                session.thumbnailUri = thumbnailUri
            } else if (thumbnailUri.isNotBlank() && session.thumbnailUri.isNotBlank()) {
                Log.d(TAG, "⏭️ 썸네일 이미 존재, 업데이트 안 함 (기존: ${session.thumbnailUri})")
            }

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
                    startTime = System.currentTimeMillis(),
                    thumbnailUri = thumbnailUri
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

                val payloadWatchTime = watchTime

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
                                this.trackType = MediaSessionEvent.TRACK_TYPE_DEFAULT
                                this.eventType = MediaSessionEvent.EVENT_TYPE_PLAYING_CHECK // 재생 중 체크
                                this.title = session.title
                                this.channel = finalChannel
                                this.appPackage = session.appPackage
                                this.timestamp = currentTime
                                this.videoDuration = session.duration
                                this.watchTime = watchTime
                                this.pauseTime = session.totalPauseTime
                                this.date = formatDate(currentTime)
                                this.detectionMethod = MediaSessionEvent.METHOD_PLAYBACK_TIMER
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

            // 실제 경과 시간 사용
            val duration = System.currentTimeMillis() - explorationStartTime

            // Realm 저장
            val eventIds = mutableListOf<String>()
            try {
                val realm = RealmConfig.getInstance()
                realm.writeBlocking {
                    val event = copyToRealm(com.dito.app.core.data.phone.AppUsageEvent().apply {
                        this.trackType = MediaSessionEvent.TRACK_TYPE_DEFAULT
                        this.eventType = MediaSessionEvent.EVENT_TYPE_APP_EXPLORATION
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

        // 실제 시청 시간 사용
        val adjustedWatchTime = watchTime

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

        val trackType = MediaSessionEvent.TRACK_TYPE_DEFAULT
        val eventIds = mutableListOf<String>()

        // 교육 콘텐츠 여부 판단
        val isEducational = EducationalContentDetector.isEducationalContent(session.title, finalChannel)
        if (isEducational) {
            Log.d(TAG, "📚 교육 콘텐츠로 판단됨 → 챌린지 시간에서 제외")
        }

        try {
            val realm = RealmConfig.getInstance()
            realm.writeBlocking {
                val event = copyToRealm(MediaSessionEvent().apply {
                    this.trackType = trackType
                    this.eventType = MediaSessionEvent.EVENT_TYPE_VIDEO_END
                    this.title = session.title
                    this.channel = finalChannel
                    this.appPackage = session.appPackage
                    this.timestamp = endTime
                    this.videoDuration = session.duration
                    this.watchTime = watchTime
                    this.pauseTime = session.totalPauseTime
                    this.date = formatDate(session.startTime)
                    this.detectionMethod = MediaSessionEvent.METHOD_MEDIA_SESSION
                    this.synced = false
                    this.isEducational = isEducational  // 교육 콘텐츠 여부
                    this.thumbnailUri = session.thumbnailUri  // 썸네일 URI
                })
                eventIds.add(event._id.toHexString())
            }
            Log.d(TAG, "✅ Realm 저장 완료 ($trackType, 교육: $isEducational)")

            // AI API로 재분류 (비동기, 백그라운드)
            if (EducationalContentDetector.useAIApi && session.appPackage == PKG_YOUTUBE) {
                classifyWithAIAsync(eventIds.firstOrNull(), session.title, finalChannel)
            }

            if (missionTracker.isTracking()) {
                missionTracker.onMediaEvent(
                    packageName = session.appPackage,
                    videoTitle = session.title,
                    channelName = finalChannel,
                    watchTimeSeconds = (watchTime / 1000).toInt(),
                    eventType = MediaSessionEvent.EVENT_TYPE_VIDEO_END
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

    /**
     * 썸네일 URI 또는 Bitmap을 추출하여 최종 URI 문자열 반환
     * 1. URL 문자열이 있으면 그대로 반환
     * 2. Bitmap만 있으면 Base64로 인코딩하여 반환
     */
    private fun extractThumbnailUri(metadata: MediaMetadata): String {
        // 1. 우선 URL 문자열 확인 (우선순위: ART_URI > ALBUM_ART_URI > DISPLAY_ICON_URI)
        val artUri = metadata.getString(MediaMetadata.METADATA_KEY_ART_URI)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI)

        if (!artUri.isNullOrEmpty()) {
            Log.d(TAG, "썸네일 URL 발견: $artUri")
            return artUri
        }

        // 2. URL이 없다면 Bitmap 확인 및 Base64 인코딩
        val bitmap = metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)
            ?: metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)

        if (bitmap != null) {
            val base64Data = convertBitmapToBase64(bitmap)
            if (base64Data.isNotBlank()) {
                Log.d(TAG, "✅ 썸네일 Bitmap을 Base64로 변환 완료 (${base64Data.length} bytes)")
                return base64Data
            }
        }

        Log.d(TAG, "⚠️ 썸네일 없음")
        return ""
    }

    /**
     * Bitmap을 Base64 문자열로 변환
     * data URI scheme 형식으로 반환: data:image/jpeg;base64,{base64_string}
     */
    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        return try {
            val outputStream = java.io.ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val byteArray = outputStream.toByteArray()

            val base64String = android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)
            "data:image/jpeg;base64,$base64String"
        } catch (e: Exception) {
            Log.e(TAG, "❌ Bitmap Base64 변환 실패", e)
            ""
        }
    }

    // ============================================================================================
    // Heartbeat Logic
    // ============================================================================================

    private fun startHeartbeat() {
        stopHeartbeat() // 기존 Job 취소

        heartbeatJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                currentSession?.let { session ->
                    if (session.appPackage == PKG_YOUTUBE) {
                        sendHeartbeat(session, "PLAYING")
                    }
                }
                delay(HEARTBEAT_INTERVAL)
            }
        }
        Log.d(TAG, "💓 Heartbeat 시작 (5초 주기)")
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
        Log.d(TAG, "💔 Heartbeat 중지")
    }

    private fun sendHeartbeat(session: ActiveSession, status: String) {
        val currentTime = System.currentTimeMillis()
        val totalTime = currentTime - session.startTime
        var watchTime = totalTime - session.totalPauseTime

        // 일시정지 중이면 현재 일시정지 시간도 차감
        if (status == "PAUSED" || status == "STOPPED") {
             session.lastPauseTime?.let { pauseTime ->
                val currentPauseDuration = currentTime - pauseTime
                watchTime -= currentPauseDuration
            }
        }
        
        val finalWatchTime = maxOf(0L, watchTime)

        val finalChannel = when {
            session.bestChannel.isNotBlank() -> session.bestChannel
            session.channel.isNotBlank() && session.channel != "알 수 없음" -> session.channel
            else -> "알 수 없는 채널"
        }

        val request = com.dito.app.core.data.report.HeartbeatRequest(
            timestamp = currentTime,
            mediaSession = com.dito.app.core.data.report.HeartbeatRequest.MediaSessionInfo(
                videoId = "", // Video ID는 현재 추출 불가
                title = session.title,
                channel = finalChannel,
                appPackage = session.appPackage,
                thumbnailUri = session.thumbnailUri,
                status = status,
                watchTime = finalWatchTime,
                videoDuration = session.duration,
                pauseTime = session.totalPauseTime
            ),
            currentApp = null // 미디어 세션 중에는 앱 정보 불필요
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // SharedPreferences에서 토큰 가져오기 (Context 필요)
                val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                val token = prefs.getString("access_token", null)

                if (!token.isNullOrEmpty()) {
                    val response = com.dito.app.core.di.ServiceLocator.apiService.updateHeartbeat(
                        token = "Bearer $token",
                        request = request
                    )
                    if (response.isSuccessful) {
                        Log.v(TAG, "💓 Heartbeat 전송 성공: $status (${finalWatchTime/1000}s)")
                    } else {
                        Log.w(TAG, "⚠️ Heartbeat 전송 실패: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Heartbeat 전송 에러", e)
            }
        }
    }




    /**
     * Bitmap을 캐시 디렉토리에 저장하고 file:// URI 반환
     */
    private fun saveBitmapToCache(bitmap: Bitmap): String {
        return try {
            val cachePath = File(context.cacheDir, "youtube_thumbs")
            if (!cachePath.exists()) {
                cachePath.mkdirs()
            }

            // 타임스탬프를 사용하여 고유한 파일명 생성
            val timestamp = System.currentTimeMillis()
            val file = File(cachePath, "thumb_$timestamp.jpg")

            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            }

            val uri = Uri.fromFile(file).toString()
            Log.d(TAG, "✅ 썸네일 Bitmap 저장 완료: $uri")
            uri
        } catch (e: Exception) {
            Log.e(TAG, "❌ Bitmap 저장 실패", e)
            ""
        }
    }

    /**
     * AI API를 사용하여 비동기로 교육 콘텐츠 분류 후 Realm 업데이트
     */
    private fun classifyWithAIAsync(eventId: String?, title: String, channel: String) {
        if (eventId == null) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val (isEducational, videoType) = EducationalContentDetector.classifyWithAI(title, channel)
                Log.d(TAG, "🤖 AI 분류 결과: $title → $videoType (교육: $isEducational)")

                // Realm 업데이트
                val realm = RealmConfig.getInstance()
                realm.write {
                    val event = query(MediaSessionEvent::class, "_id == $0", ObjectId(eventId))
                        .first()
                        .find()
                    event?.let {
                        it.isEducational = isEducational
                        Log.d(TAG, "✅ Realm 교육 여부 업데이트 완료: $isEducational")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ AI 분류 실패", e)
            }
        }
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
