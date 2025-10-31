package com.dito.app.core.service

import android.app.Notification
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log


class MediaSessionListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "MediaSession"
    }

    private val sessionManager = SessionStateManager()

    // 앱별 MediaController 저장 -> 여러 앱 동시 실행 대비
    private val activeControllers = mutableMapOf<String, MediaController>()

    // 알림 생성 시 호출됨 -> youtube 재생, 상태 변경
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

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

        //MediaController로 재생 상태를 추적할 수 있게 생성
        val controller = MediaController(this, mediaToken)


        activeControllers[packageName]?.unregisterCallback(mediaCallback)


        activeControllers[packageName] = controller
        controller.registerCallback(mediaCallback)

        Log.d(TAG, "MediaController 등록: $packageName")


        logMediaInfo(controller)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)

        val packageName = sbn?.packageName ?: return

        Log.d(TAG, "알림 제거: $packageName")


        activeControllers[packageName]?.let { controller ->
            controller.unregisterCallback(mediaCallback)
            activeControllers.remove(packageName)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "NotificationListener 연결됨")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "NotificationListener 연결 해제됨")


        activeControllers.values.forEach { it.unregisterCallback(mediaCallback) }
        activeControllers.clear()
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

            Log.d(TAG, "재생 상태 변경: ${getStateString(state?.state)}")


            val controller = activeControllers.values.firstOrNull() ?: return
            val metadata = controller.metadata ?: return

            when (state?.state) {
                PlaybackState.STATE_PLAYING -> {
                    // 재생 시작
                    sessionManager.handlePlaybackStarted(
                        metadata = metadata,
                        appPackage = controller.packageName
                    )
                }

                PlaybackState.STATE_PAUSED -> {
                    // 일시정지
                    sessionManager.handlePlaybackPaused()
                }

                PlaybackState.STATE_STOPPED, PlaybackState.STATE_NONE -> {
                    // 재생 종료
                    sessionManager.handlePlaybackStopped()
                }
            }
        }

        // 영상 제목이나 채널 변경 시 업데이트
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            super.onMetadataChanged(metadata)

            if (metadata == null) return

            val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
            val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)

            Log.d(TAG, "메타데이터 변경")
            Log.d(TAG, "  제목: $title")
            Log.d(TAG, "  채널: $artist")

            // SessionManager에 업데이트 전달
            sessionManager.updateMetadata(metadata)
        }
    }
}