package com.dito.app.core.service

import android.media.MediaMetadata
import com.dito.app.core.data.MediaSessionEvent
import com.dito.app.core.data.RealmConfig
import android.util.Log
import io.realm.kotlin.internal.platform.currentTime
import java.text.SimpleDateFormat
import java.util.*


class SessionStateManager {

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
                saveSession(session)
            }
        }


        currentSession = ActiveSession(
            title = title,
            channel = channel,
            appPackage = appPackage,
            duration = duration,
            startTime = System.currentTimeMillis()
        )

        lastSessionTitle = title
        lastSessionTime = currentTime

        Log.d(TAG, "새 세션 생성")
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


            if (newTitle != null && newTitle != session.title) {
                Log.d(TAG, "제목 업데이트: ${session.title} → $newTitle")
                session.title = newTitle
                lastSessionTitle = newTitle
            }

            //0.5초 후 메타데이터 업데이트로 실제 채널명 받기
            if (newChannel != null && newChannel != "알 수 없음" && session.channel == "알 수 없음") {
                Log.d(TAG, "채널 업데이트: $newChannel")
                session.channel = newChannel
            }
        }
    }

    private fun saveSession(session: ActiveSession) {
        val endTime = System.currentTimeMillis()
        val totalTime = endTime - session.startTime
        val watchTime = totalTime - session.totalPauseTime //실제 시청 시간

        // 5초 미만 무시
        if (watchTime < MIN_WATCH_TIME) {
            Log.d(TAG, "시청 시간 너무 짧음 (${watchTime}ms) - 저장 안 함")
            return
        }

        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "세션 저장")
        Log.d(TAG, "   제목: ${session.title}")
        Log.d(TAG, "   채널: ${session.channel}")
        Log.d(TAG, "   앱: ${session.appPackage}")
        Log.d(TAG, "   시작: ${formatTime(session.startTime)}")
        Log.d(TAG, "   종료: ${formatTime(endTime)}")
        Log.d(TAG, "   시청 시간: ${watchTime / 1000}초")
        Log.d(TAG, "   일시정지: ${session.totalPauseTime / 1000}초")
        Log.d(TAG, "   날짜: ${formatDate(session.startTime)}")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")

        try {
            val realm = RealmConfig.getInstance()

            realm.writeBlocking {
                copyToRealm(MediaSessionEvent().apply {
                    this.eventType = "VIDEO_END"
                    this.title = session.title
                    this.channel = session.channel
                    this.appPackage = session.appPackage
                    this.timestamp = endTime
                    this.videoDuration = session.duration
                    this.watchTime = watchTime
                    this.pauseTime = session.totalPauseTime
                    this.date = formatDate(session.startTime)
                    this.detectionMethod = "media-session"
                    this.synced = false
                })
            }

            Log.d(TAG, "✅ Realm 저장 완료")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Realm 저장 실패", e)
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