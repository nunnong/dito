package com.dito.app.core.service

import android.app.Notification
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MediaSessionListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "MediaSession"
    }

    @Inject
    lateinit var aiAgent: AIAgent
    private lateinit var sessionManager: SessionStateManager

    // 앱별 MediaController 저장 -> 여러 앱 동시 실행 대비
    private val activeControllers = mutableMapOf<String, MediaController>()

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionStateManager(applicationContext, aiAgent)
        Log.d(TAG, "SessionStateManager 초기화 완료")
    }

    // 알림 생성 시 호출됨 -> youtube 재생, 상태 변경
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        try {
            val notification = sbn?.notification ?: return
            val packageName = sbn.packageName

            Log.d(TAG, "알림 수신: $packageName")

            if (!isMediaApp(packageName)) {
                return
            }

            // MediaSession 토큰 추출
            val mediaToken = notification.extras.getParcelable<MediaSession.Token>(
                Notification.EXTRA_MEDIA_SESSION
            )

            if (mediaToken == null) {
                Log.d(TAG, "MediaSession 토큰 없음")
                return
            }

            //MediaController로 재생 상태를 추적할 수 있게 생성(api)
            val controller = MediaController(this, mediaToken)

            activeControllers[packageName]?.unregisterCallback(mediaCallback)

            activeControllers[packageName] = controller
            controller.registerCallback(mediaCallback)

            Log.d(TAG, "MediaController 등록: $packageName")

            logMediaInfo(controller)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 알림 처리 실패", e)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)

        try {
            val packageName = sbn?.packageName ?: return

            Log.d(TAG, "알림 제거: $packageName")

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


    private fun isMediaApp(packageName: String): Boolean {
        return packageName in listOf(
            "com.google.android.youtube",           // YouTube
            "com.google.android.youtube.music",     // YouTube Music
            "com.samsung.android.app.music",        // 삼성 뮤직
            "com.android.chrome"                    // Chrome (YouTube 웹)
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
            PlaybackState.STATE_PLAYING -> "PLAYING"
            PlaybackState.STATE_PAUSED -> "PAUSED"
            PlaybackState.STATE_STOPPED -> "STOPPED"
            PlaybackState.STATE_NONE -> "NONE"
            else -> "UNKNOWN($state)"
        }
    }

    // 재생 상태 변경 감지
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