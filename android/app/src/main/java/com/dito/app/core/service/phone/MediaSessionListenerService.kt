package com.dito.app.core.service.phone

import android.app.Notification
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.dito.app.core.service.AIAgent
import com.dito.app.core.service.mission.MissionTracker
import com.dito.app.core.service.phone.PlaybackProbe
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MediaSessionListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "MediaSession"
    }

    @Inject
    lateinit var aiAgent: AIAgent

    @Inject
    lateinit var missionTracker: MissionTracker

    @Inject
    lateinit var apiService: com.dito.app.core.network.ApiService

    @Inject
    lateinit var authTokenManager: com.dito.app.core.storage.AuthTokenManager

    private lateinit var sessionManager: SessionStateManager
    private val activeControllers = mutableMapOf<String, MediaController>()

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionStateManager(applicationContext, aiAgent, missionTracker, apiService, authTokenManager)
        SessionStateManager.setInstance(sessionManager)
        Log.d(TAG, "SessionStateManager 초기화 완료")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        try {
            val notification = sbn?.notification ?: return
            val packageName = sbn.packageName

            Log.d(TAG, "📢 알림 수신: $packageName")
            if (!isMediaApp(packageName)) {
                Log.d(TAG, "   ⏭️ 미디어 앱 아님, 무시")
                return
            }
            Log.d(TAG, "   ✅ 미디어 앱 감지!")

            val mediaToken: MediaSession.Token? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notification.extras.getParcelable(
                        Notification.EXTRA_MEDIA_SESSION,
                        MediaSession.Token::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    notification.extras.getParcelable(Notification.EXTRA_MEDIA_SESSION)
                }

            if (mediaToken == null) {
                Log.d(TAG, "MediaSession 토큰 없음")
                return
            }

            val controller = MediaController(this, mediaToken)

            activeControllers[packageName]?.unregisterCallback(mediaCallback)
            activeControllers[packageName] = controller
            controller.registerCallback(mediaCallback)

            Log.d(TAG, "MediaController 등록: $packageName")
            logMediaInfo(controller)

            // 등록 직후에도 이미 메타데이터/상태가 유효할 수 있음 → 즉시 반영
            val state = controller.playbackState?.state
            val md = controller.metadata
            if (md != null) {
                val title = md.getString(MediaMetadata.METADATA_KEY_TITLE)
                val hasValidTitle = !title.isNullOrBlank() && !title.equals("YouTube", true)
                when {
                    state == PlaybackState.STATE_PLAYING && hasValidTitle -> {
                        // 재생 상태 이벤트가 아직 안 와도 세션 시작을 보장
                        sessionManager.handlePlaybackStarted(md, controller.packageName)
                    }
                    hasValidTitle -> {
                        // 최소한 채널/타이틀은 세션에 반영
                        sessionManager.updateMetadata(md)
                    }
                    else -> {
                        // 타이틀이 아직 비었으면 후속 콜백에서 처리
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 알림 처리 실패", e)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)

        try {
            val packageName = sbn?.packageName ?: return
            Log.d(TAG, "알림 제거: $packageName")

            if (isMediaApp(packageName)) {
                sessionManager.handlePlaybackStopped()
                Log.d(TAG, "⚠️ 알림 제거 → 세션 저장 트리거")
            }

            activeControllers[packageName]?.let { controller ->
                controller.unregisterCallback(mediaCallback)
                activeControllers.remove(packageName)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 알림 제거 처리 실패", e)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "NotificationListener 연결됨")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "NotificationListener 연결 해제됨")
        try {
            activeControllers.values.forEach { it.unregisterCallback(mediaCallback) }
            activeControllers.clear()
        } catch (e: Exception) {
            Log.e(TAG, "❌ 연결 해제 처리 실패", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            sessionManager.cleanup()
            activeControllers.values.forEach { it.unregisterCallback(mediaCallback) }
            activeControllers.clear()
        } catch (e: Exception) {
            Log.e(TAG, "❌ onDestroy 처리 실패", e)
        }
        Log.i(TAG, "🛑 MediaSessionListenerService 종료")
    }

    private fun isMediaApp(packageName: String): Boolean {
        return packageName in listOf(
            "com.google.android.youtube",
            "com.google.android.youtube.music",
            "com.samsung.android.app.music",
            "com.android.chrome"
        )
    }

    private fun logMediaInfo(controller: MediaController) {
        try {
            val metadata = controller.metadata
            val playbackState = controller.playbackState

            if (metadata == null) {
                Log.d(TAG, "메타데이터 없음")
                return
            }

            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "앱: ${controller.packageName}")
            Log.d(TAG, "제목: ${metadata.getString(MediaMetadata.METADATA_KEY_TITLE)}")
            Log.d(TAG, "채널: ${metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)}")
            Log.d(TAG, "길이: ${metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)}ms")
            Log.d(TAG, "상태: ${getStateString(playbackState?.state)}")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━")
        } catch (e: Exception) {
            Log.e(TAG, "❌ 미디어 정보 로깅 실패", e)
        }
    }

    private fun getStateString(state: Int?): String {
        return when (state) {
            PlaybackState.STATE_NONE -> "NONE"
            PlaybackState.STATE_STOPPED -> "STOPPED"
            PlaybackState.STATE_PAUSED -> "PAUSED"
            PlaybackState.STATE_PLAYING -> "PLAYING"
            PlaybackState.STATE_FAST_FORWARDING -> "FAST_FORWARDING"
            PlaybackState.STATE_REWINDING -> "REWINDING"
            PlaybackState.STATE_BUFFERING -> "BUFFERING"
            PlaybackState.STATE_ERROR -> "ERROR"
            PlaybackState.STATE_CONNECTING -> "CONNECTING"
            PlaybackState.STATE_SKIPPING_TO_NEXT -> "SKIPPING_TO_NEXT"
            PlaybackState.STATE_SKIPPING_TO_PREVIOUS -> "SKIPPING_TO_PREVIOUS"
            PlaybackState.STATE_SKIPPING_TO_QUEUE_ITEM -> "SKIPPING_TO_QUEUE_ITEM"
            else -> "UNKNOWN($state)"
        }
    }

    private val mediaCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            super.onPlaybackStateChanged(state)
            try {
                Log.d(TAG, "재생 상태 변경: ${getStateString(state?.state)}")

                val controller = activeControllers.values.firstOrNull()
                if (controller == null) {
                    Log.d(TAG, "활성 컨트롤러 없음")
                    return
                }

                val metadata = controller.metadata
                if (metadata == null) {
                    Log.d(TAG, "메타데이터 없음")
                    return
                }

                // PlaybackProbe 기록
                if (state?.state == PlaybackState.STATE_PLAYING) {
                    PlaybackProbe.recordPlayback()
                }

                when (state?.state) {
                    PlaybackState.STATE_PLAYING -> {
                        sessionManager.handlePlaybackStarted(
                            metadata = metadata,
                            appPackage = controller.packageName
                        )
                    }
                    PlaybackState.STATE_PAUSED -> {
                        sessionManager.handlePlaybackPaused()
                    }
                    PlaybackState.STATE_STOPPED, PlaybackState.STATE_NONE -> {
                        sessionManager.handlePlaybackStopped()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ 재생 상태 변경 처리 실패", e)
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            super.onMetadataChanged(metadata)
            try {
                if (metadata == null) return

                val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
                val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)

                Log.d(TAG, "메타데이터 변경")
                Log.d(TAG, "  제목: $title")
                Log.d(TAG, "  채널: $artist")

                sessionManager.updateMetadata(metadata)
            } catch (e: Exception) {
                Log.e(TAG, "❌ 메타데이터 변경 처리 실패", e)
            }
        }
    }
}
