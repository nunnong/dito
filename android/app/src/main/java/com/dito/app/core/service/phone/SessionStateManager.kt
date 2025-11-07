package com.dito.app.core.service.phone

import android.content.Context
import android.media.MediaMetadata
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.dito.app.core.data.RealmConfig
import com.dito.app.core.data.phone.MediaSessionEvent
import com.dito.app.core.network.BehaviorLog
import com.dito.app.core.service.AIAgent
import com.dito.app.core.service.Checker
import com.dito.app.core.service.mission.MissionTracker
import java.text.SimpleDateFormat
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
    }

    private var currentSession: ActiveSession? = null
    private var lastSessionTitle: String = ""
    private var lastSessionTime: Long = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var pendingSaveRunnable: Runnable? = null
    private var pendingSessionSaveRunnable: Runnable? = null // 영상 전환 시 이전 세션 저장 대기
    private var sessionToSave: ActiveSession? = null // 저장 대기 중인 이전 세션

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


        if (title.isBlank()) {
            Log.d(TAG, "빈 제목 무시")
            return
        }


        if (title == "YouTube" || title == "youtube") {
            Log.d(TAG, "YouTube 로딩 중 - 대기")
            return
        }


        val isValidChannel = channel != "알 수 없음" &&
                channel != "m.youtube.com" &&
                channel != "www.youtube.com" &&
                channel != "YouTube" &&
                channel != "youtube"

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
                // 같은 제목이지만 5초 이상 지남 → 재시작으로 간주
                val elapsedTime = System.currentTimeMillis() - session.startTime
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG, "같은 영상 재시작 감지 (${elapsedTime / 1000}초 경과)")
                Log.d(TAG, "즉시 저장 (재시작)")
                Log.d(TAG, "bestChannel 사용: ${session.bestChannel}")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
                saveSession(session)

                // 새 세션 생성 (기존 bestChannel 유지)
                currentSession = ActiveSession(
                    title = title,
                    channel = session.bestChannel.ifBlank { channel },
                    bestChannel = session.bestChannel.ifBlank { (if (isValidChannel) channel else "") },
                    appPackage = appPackage,
                    duration = duration,
                    startTime = System.currentTimeMillis()
                )
                Log.d(TAG, "새 세션 생성 (재시작)")
                Log.d(TAG, "  channel: ${currentSession?.channel}")
                Log.d(TAG, "  bestChannel: ${currentSession?.bestChannel}")

            } else {

                Log.d(TAG, "기존 세션 유지 (${currentTime - lastSessionTime}ms 경과)")

                // 채널 업데이트
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
        currentSession?.let { session ->
            session.lastPauseTime = System.currentTimeMillis()
            Log.d(TAG, "일시정지")
        }
    }

    fun handlePlaybackResumed() {
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
        currentSession?.let { session ->
            Log.d(TAG, "재생 종료 → ${SAVE_DELAY}ms 후 저장 예약")
            Log.d(TAG, "   현재 channel: ${session.channel}")
            Log.d(TAG, "   현재 bestChannel: ${session.bestChannel}")

            // 기존 저장 작업 취소
            pendingSaveRunnable?.let { handler.removeCallbacks(it) }

            // 새로운 저장 작업 예약
            pendingSaveRunnable = Runnable {
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG, "${SAVE_DELAY}ms 대기 완료 → 세션 저장 시작")
                Log.d(TAG, "   최종 channel: ${session.channel}")
                Log.d(TAG, "   최종 bestChannel: ${session.bestChannel}")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")

                saveSession(session)
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
                if (newTitle == "YouTube" || newTitle == "youtube") {
                    Log.d(TAG, "YouTube 로딩 중 제목 무시")
                    return@let
                }

                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG, "⚠️ updateMetadata에서 제목 변경 감지!")
                Log.d(TAG, "   이전: ${session.title}")
                Log.d(TAG, "   새로운: $newTitle")
                Log.d(TAG, "   새 채널: $newChannel")
                Log.d(TAG, "   이전 세션 현재 상태:")
                Log.d(TAG, "     - channel: ${session.channel}")
                Log.d(TAG, "     - bestChannel: ${session.bestChannel}")
                Log.d(TAG, "   → ${METADATA_WAIT_DELAY}ms 대기 후 이전 세션 저장")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")

                // 이전 대기 작업 취소
                pendingSessionSaveRunnable?.let { handler.removeCallbacks(it) }

                // 이전 세션 저장 (채널명 대기) - 복사본 생성
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
                        newChannel != "알 수 없음" &&
                        newChannel != "m.youtube.com" &&
                        newChannel != "www.youtube.com" &&
                        newChannel != "YouTube" &&
                        newChannel != "youtube"

                currentSession = ActiveSession(
                    title = newTitle,
                    channel = if (isValidChannel) newChannel else "알 수 없음",
                    bestChannel = if (isValidChannel) newChannel else "",
                    appPackage = session.appPackage,
                    duration = 0L,  // 새 영상이므로 duration은 나중에 업데이트됨
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
                val isValidChannel = newChannel != "알 수 없음" &&
                        newChannel != "m.youtube.com" &&
                        newChannel != "www.youtube.com" &&
                        newChannel != "YouTube" &&
                        newChannel != "youtube"

                if (isValidChannel) {
                    // 현재 세션 채널 업데이트
                    if (session.bestChannel.isBlank()) {
                        // 처음으로 유효한 채널명 받음
                        Log.d(TAG, "updateMetadata에서 채널 업데이트: ${session.channel} → $newChannel")
                        session.channel = newChannel
                        session.bestChannel = newChannel
                    } else if (session.bestChannel != newChannel) {
                        // 채널명이 변경됨
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

        val checkPoint = Checker.checkMediaSession(
            title = session.title,
            channel = finalChannel,
            watchTime = watchTime,
            timestamp = endTime,
            appPackage = session.appPackage
        )

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

            if(missionTracker.isTracking()){
                missionTracker.onMediaEvent(
                    packageName = session.appPackage,
                    videoTitle = session.title,
                    channelName = finalChannel,
                    watchTimeSeconds = (watchTime/1000).toInt(),
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
                    videoTitle = session.title,
                    channelName = finalChannel
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
        pendingSaveRunnable?.let { handler.removeCallbacks(it) }
        pendingSaveRunnable = null

        pendingSessionSaveRunnable?.let { handler.removeCallbacks(it) }
        pendingSessionSaveRunnable = null

        currentSession?.let { session ->
            Log.d(TAG, "⚠️ 서비스 종료 → 남은 세션 즉시 저장")
            saveSession(session)
        }
    }

    /**
     * 앱 전환 시 현재 세션을 강제로 저장
     * (일시정지 후 앱 전환 등의 경우를 처리)
     */
    fun forceFlushCurrentSession() {
        currentSession?.let { session ->
            val currentTime = System.currentTimeMillis()
            val totalTime = currentTime - session.startTime
            val watchTime = totalTime - session.totalPauseTime

            // 최소 시청 시간 체크 (5초 미만 무시)
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

            // 대기 중인 작업 모두 취소
            pendingSaveRunnable?.let { handler.removeCallbacks(it) }
            pendingSaveRunnable = null

            pendingSessionSaveRunnable?.let { handler.removeCallbacks(it) }
            pendingSessionSaveRunnable = null

            // 즉시 저장
            saveSession(session)
            currentSession = null
            lastSessionTitle = ""
            lastSessionTime = 0L
        } ?: run {
            Log.d(TAG, "🔄 강제 플러시: 저장할 세션 없음")
        }
    }
}