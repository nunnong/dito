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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionStateManager(
    private val context: Context,
    private val aiAgent: AIAgent
) {

    companion object {
        private const val TAG = "SessionState"
        private const val MIN_WATCH_TIME = 5000L
        private const val SESSION_UPDATE_THRESHOLD = 5000L
        private const val SAVE_DELAY = 500L
    }

    private var currentSession: ActiveSession? = null
    private var lastSessionTitle: String = ""
    private var lastSessionTime: Long = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var pendingSaveRunnable: Runnable? = null

    data class ActiveSession(
        var title: String,
        var channel: String,
        var bestChannel: String? = null,
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
        val channel = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "알 수 없음"
        val duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
        val currentTime = System.currentTimeMillis()

        if (title == lastSessionTitle && (currentTime - lastSessionTime) < SESSION_UPDATE_THRESHOLD) {
            Log.d(TAG, "중복 세션 무시")
            return
        }

        if (title.isEmpty() || title.isBlank()) {
            Log.d(TAG, "빈 제목 무시")
            return
        }

        if (title == "YouTube" || title == "youtube" ||
            channel == "m.youtube.com" || channel == "www.youtube.com"
        ) {
            Log.d(TAG, "YouTube 로딩 중 - 대기")
            return
        }

        Log.d(TAG, "재생 시작")
        Log.d(TAG, "   제목: $title")
        Log.d(TAG, "   채널: $channel")


        pendingSaveRunnable?.let { handler.removeCallbacks(it) }
        pendingSaveRunnable = null

        currentSession?.let { session ->
            if (session.title != title) {
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG, "즉시 저장 (영상 전환)")
                Log.d(TAG, "bestChannel 사용: ${session.bestChannel ?: session.channel}")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
                saveSession(session)

                // 새 세션 생성 시 초기 채널 설정
                currentSession = ActiveSession(
                    title = title,
                    channel = channel,
                    bestChannel = if (channel != "알 수 없음") channel else null,  // ✅ 추가
                    appPackage = appPackage,
                    duration = duration,
                    startTime = System.currentTimeMillis()
                )
                Log.d(TAG, "새 세션 생성 (재시작) - 초기 채널: $channel")
            } else {
                val elapsedTime = System.currentTimeMillis() - session.startTime
                Log.d(TAG, "같은 영상 재시작 감지 (${elapsedTime / 1000}초 경과)")


                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG, "즉시 저장 (영상 전환)")
                Log.d(TAG, "bestChannel 사용: ${session.bestChannel ?: session.channel}")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
                saveSession(session)


                currentSession = ActiveSession(
                    title = title,
                    channel = session.bestChannel ?: channel,
                    bestChannel = session.bestChannel,
                    appPackage = appPackage,
                    duration = duration,
                    startTime = System.currentTimeMillis()
                )
                Log.d(TAG, "새 세션 생성 (재시작) - 초기 채널: ${currentSession?.channel}")
            }
        } ?: run {

            currentSession = ActiveSession(
                title = title,
                channel = channel,
                bestChannel = if (channel != "알 수 없음") channel else null,  // ✅ 추가
                appPackage = appPackage,
                duration = duration,
                startTime = System.currentTimeMillis()
            )
            Log.d(TAG, "새 세션 생성 (첫 재생) - 초기 채널: $channel")
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
            val newChannel = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)

            // 제목 업데이트 (빈 문자열 무시)
            if (!newTitle.isNullOrBlank() && newTitle != session.title) {
                // YouTube 로딩 중 무시
                if (newTitle == "YouTube" || newTitle == "youtube") {
                    Log.d(TAG, "YouTube 로딩 중 제목 무시")
                    return@let
                }

                Log.d(TAG, "제목 업데이트: ${session.title} → $newTitle")
                session.title = newTitle
                lastSessionTitle = newTitle
            }

            // 채널 업데이트 (빈 문자열 + 무효 값 무시)
            if (!newChannel.isNullOrBlank() &&
                newChannel != "알 수 없음" &&
                newChannel != "m.youtube.com" &&
                newChannel != "www.youtube.com") {

                if (session.channel.isBlank() || session.channel == "알 수 없음") {
                    // 처음으로 실제 채널명 받음
                    Log.d(TAG, "updateMetadata에서 채널 업데이트: ${session.channel} → $newChannel")
                    session.channel = newChannel
                    session.bestChannel = newChannel
                } else if (session.channel != newChannel) {
                    // 채널명이 변경됨 (다른 영상)
                    Log.d(TAG, "updateMetadata에서 채널 변경 감지: ${session.channel} → $newChannel")
                    session.channel = newChannel
                    session.bestChannel = newChannel
                } else {
                    // 같은 채널명 → bestChannel 보강
                    if (session.bestChannel.isNullOrBlank()) {
                        session.bestChannel = newChannel
                        Log.d(TAG, "bestChannel 보강: $newChannel")
                    }
                }
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


        val finalChannel = session.bestChannel ?: session.channel

        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "✅ bestChannel 사용: $finalChannel")
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

        val trackType = if (checkPoint != null) "TRACK_1" else "TRACK_2"
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

        } catch (e: Exception) {
            Log.e(TAG, "❌ Realm 저장 실패", e)
            return
        }

        if (checkPoint != null) {
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
}