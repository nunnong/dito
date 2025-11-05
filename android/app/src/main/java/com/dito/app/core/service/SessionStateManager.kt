package com.dito.app.core.service

import android.content.Context
import android.media.MediaMetadata
import com.dito.app.core.data.MediaSessionEvent
import com.dito.app.core.data.RealmConfig
import android.util.Log
import com.dito.app.core.network.BehaviorLog
import java.text.SimpleDateFormat
import java.util.*


class SessionStateManager (
    private val context: Context,
    private val aiAgent: AIAgent
){

    companion object {
        private const val TAG = "SessionState"
        private const val MIN_WATCH_TIME = 5000L // 5초 미만은 무시
        private const val SESSION_UPDATE_THRESHOLD = 5000L //5초 이내 같은 영상은 무시
    }

    private var currentSession: ActiveSession? = null
    private var lastSessionTitle: String = ""
    private var lastSessionTime: Long = 0L


    //활성 세션 데이터를 Realm에 저장하기 전 메모리에서 추적
    data class ActiveSession(
        var title: String,
        var channel: String,
        var appPackage: String,
        var duration: Long,
        var startTime: Long,
        var lastPauseTime: Long? = null,
        var totalPauseTime: Long = 0L,
        var bestChannel: String = ""
    )


    fun handlePlaybackStarted(
        metadata: MediaMetadata,
        appPackage: String
    ) {
        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE) ?: ""
        val channel = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: ""
        val duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
        val currentTime = System.currentTimeMillis()

        if (title == lastSessionTitle && (currentTime - lastSessionTime) < SESSION_UPDATE_THRESHOLD){
            Log.d(TAG, "중복 세션 무시")
            return
        }

        if (title.isEmpty() || title.isBlank()) {
            Log.d(TAG, "빈 제목 무시")
            return
        }
        if (title == "YouTube" || title == "youtube" ||
            channel == "m.youtube.com" || channel == "www.youtube.com") {
            Log.d(TAG, "YouTube 로딩 중 - 대기")
            return
        }

        Log.d(TAG, "재생 시작")
        Log.d(TAG, "   제목: $title")
        Log.d(TAG, "   채널: $channel")


        currentSession?.let { session ->

            if (session.title != title) {
                // 다른 영상 → 기존 세션 저장
                saveSession(session)

                // 새 세션 생성
                val initialChannel = if (isValidChannel(channel)) channel else "알 수 없음"
                currentSession = ActiveSession(
                    title = title,
                    channel = initialChannel,
                    appPackage = appPackage,
                    duration = duration,
                    startTime = System.currentTimeMillis(),
                    bestChannel = if (isValidChannel(channel)) channel else ""
                )
                Log.d(TAG, "새 세션 생성 (다른 영상)")
            } else {
                // 같은 영상 - 채널 업데이트 시도
                if (isValidChannel(channel) && channel != session.channel) {
                    session.channel = channel
                    session.bestChannel = channel
                    Log.d(TAG, "채널 업데이트됨: $channel")
                }

                session.lastPauseTime?.let { pauseTime ->
                    val pauseDuration = System.currentTimeMillis() - pauseTime
                    session.totalPauseTime += pauseDuration
                    session.lastPauseTime = null
                    Log.d(TAG, "재생 재개 (일시정지: ${pauseDuration / 1000}초)")
                } ?: run {
                    Log.d(TAG, "기존 세션 계속 (일시정지 없음)")
                }
            }
        } ?: run {
            // 세션이 없으면 새로 생성
            val initialChannel = if (isValidChannel(channel)) channel else "알 수 없음"
            currentSession = ActiveSession(
                title = title,
                channel = initialChannel,
                appPackage = appPackage,
                duration = duration,
                startTime = System.currentTimeMillis(),
                bestChannel = if (isValidChannel(channel)) channel else ""
            )
            Log.d(TAG, "새 세션 생성 (첫 재생)")
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
            Log.d(TAG, "재생 종료")
            saveSession(session)
            currentSession = null
            lastSessionTitle = ""
            lastSessionTime = 0L
        }
    }


    fun updateMetadata(metadata: MediaMetadata) {
        currentSession?.let { session ->
            val newTitle = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
            val newChannel = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)

            // 제목 업데이트 - 빈 값이 아니고, 유효한 제목일 때만 업데이트
            if (newTitle != null && newTitle.isNotBlank() && newTitle != session.title) {
                // YouTube 로딩 화면 무시
                if (newTitle != "YouTube" && newTitle != "youtube") {
                    Log.d(TAG, "제목 업데이트: ${session.title} → $newTitle")
                    session.title = newTitle
                    lastSessionTitle = newTitle
                }
            }

            // 채널 업데이트 - 유효한 채널명이면 무조건 업데이트
            if (newChannel != null && isValidChannel(newChannel)) {
                if (session.bestChannel.isBlank() || newChannel != session.bestChannel) {
                    Log.d(TAG, "채널 업데이트: ${session.channel} → $newChannel")
                    session.channel = newChannel
                    session.bestChannel = newChannel
                }
            }
        }
    }


    private fun isValidChannel(channel: String?): Boolean {
        if (channel.isNullOrBlank()) return false

        val invalidValues = setOf(
            "알 수 없음",
            "m.youtube.com",
            "www.youtube.com",
            "YouTube",
            "youtube",
            ""
        )

        return channel !in invalidValues && channel.length >= 2
    }

    private fun saveSession(session: ActiveSession) {
        val endTime = System.currentTimeMillis()
        val totalTime = endTime - session.startTime
        val watchTime = totalTime - session.totalPauseTime

        // 5초 미만 무시
        if (watchTime < MIN_WATCH_TIME) {
            Log.d(TAG, "시청 시간 너무 짧음 (${watchTime}ms) - 저장 안 함")
            return
        }

        // 최종 채널명 결정 - bestChannel 우선 사용
        val finalChannel = when {
            session.bestChannel.isNotBlank() -> session.bestChannel
            session.channel.isNotBlank() && session.channel != "알 수 없음" -> session.channel
            else -> "알 수 없는 채널"
        }

        // 최종 제목 검증
        val finalTitle = session.title.ifBlank {
            Log.w(TAG, "⚠️ 제목이 비어있음 - 저장 중단")
            return
        }

        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "세션 저장")
        Log.d(TAG, "   제목: $finalTitle")
        Log.d(TAG, "   채널: $finalChannel (bestChannel: ${session.bestChannel})")
        Log.d(TAG, "   앱: ${session.appPackage}")
        Log.d(TAG, "   시작: ${formatTime(session.startTime)}")
        Log.d(TAG, "   종료: ${formatTime(endTime)}")
        Log.d(TAG, "   총 경과: ${totalTime / 1000}초")
        Log.d(TAG, "   시청 시간: ${watchTime / 1000}초")
        Log.d(TAG, "   일시정지: ${session.totalPauseTime / 1000}초")
        Log.d(TAG, "   날짜: ${formatDate(session.startTime)}")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")

        val checkPoint = Checker.checkMediaSession(
            title = finalTitle,
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
                    this.title = finalTitle
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
        } catch (e: Exception) {
            Log.e(TAG, "Realm 저장 실패", e)
            return
        }

        // AI 에이전트 호출
        if (checkPoint != null) {
            aiAgent.requestIntervention(
                behaviorLog = BehaviorLog(
                    appName = checkPoint.appName,
                    durationSeconds = checkPoint.durationSeconds,
                    usageTimestamp = checkPoint.usageTimestamp
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