package com.dito.app.core.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dito.app.MainActivity
import com.dito.app.R
import com.dito.app.core.service.mission.MissionData
import com.dito.app.core.service.mission.MissionTracker
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * FCM 메시지 수신 및 토큰 갱신을 처리하는 서비스
 */
@AndroidEntryPoint
class DitoFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var missionTracker: MissionTracker

    @Inject
    lateinit var fcmTokenManager: FcmTokenManager

    companion object {
        private const val TAG = "DitoFCM"
        private const val CHANNEL_ID = "dito_intervention"
        private const val CHANNEL_NAME = "Intervention Notifications"
        private const val NOTIFICATION_ID_BASE = 1000
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    /**
     * FCM 토큰이 새로 발급되거나 갱신될 때 호출
     * 앱 최초 설치, 앱 재설치, 앱 데이터 삭제 시 발생
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "새 FCM 토큰 발급: ${token.take(20)}...")

        // 로컬에 토큰 저장
        fcmTokenManager.saveToken(token)

        // TODO: 서버에 토큰 전송은 로그인 시점에 수행
        // 여기서는 저장만 하고, 로그인/회원가입 시 서버로 전송
    }

    /**
     * FCM 메시지 수신 시 호출
     * 앱이 foreground/background 모두에서 처리
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "FCM 메시지 수신: from=${message.from}")

        // Data payload 처리 (서버에서 data 필드로 전송)
        message.data.let { data ->
            Log.d(TAG, "Data payload: $data")

            message.data.let { data ->
                Log.d(TAG, "Data payload: $data")

                val interventionId = data["interventionId"]
                val title = data["title"] ?: "Dito"
                val body = data["body"] ?: "새로운 intervention이 도착했습니다"

                showNotification(title, body, interventionId)
            }



        }

        // Notification payload 처리 (Firebase Console에서 테스트 시)
        message.notification?.let { notification ->
            Log.d(TAG, "Notification payload: title=${notification.title}")
            showNotification(
                title = notification.title ?: "Dito",
                body = notification.body ?: "",
                interventionId = null
            )
        }
    }

    /**
     * 알림 표시
     * @param title 알림 제목
     * @param body 알림 내용
     * @param interventionId Intervention ID (deep link용)
     */
    private fun showNotification(title: String, body: String, interventionId: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Deep link intent 생성
        val intent = if (interventionId != null) {
            Intent(Intent.ACTION_VIEW, Uri.parse("dito://intervention/$interventionId")).apply {
                setClass(this@DitoFirebaseMessagingService, MainActivity::class.java)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        } else {
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            interventionId?.hashCode() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 알림 생성
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val notificationId = interventionId?.hashCode() ?: NOTIFICATION_ID_BASE
        notificationManager.notify(notificationId, notification)

        Log.d(TAG, "알림 표시 완료: id=$notificationId, title=$title")
    }

    /**
     * Android O(8.0) 이상에서 필요한 알림 채널 생성
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Intervention 알림을 수신합니다"
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Log.d(TAG, "알림 채널 생성 완료: $CHANNEL_ID")
        }
    }

    private fun handleMissionMessage(data: Map<String, String>) {
        val missionId = data["mission_id"] ?: return
        val missionType = data["mission_type"] ?: "REST"
        val instruction = data["instruction"] ?: "미션을 수행하세요"
        val duration = 30 //data["duration"]?.toIntOrNull() ?: 300
        val targetAppsStr = data["target_apps"] ?: ""
        val targetApps = if (targetAppsStr.isNotEmpty()) {
            targetAppsStr.split(",").map { it.trim() }
        } else {
            listOf("com.google.android.youtube", "com.instagram.android")
        }

        Log.i(TAG, "🎯 미션 수신: $missionId")
        Log.d(TAG, "   타입: $missionType")
        Log.d(TAG, "   지시: $instruction")
        Log.d(TAG, "   시간: ${duration}초")
        Log.d(TAG, "   타겟 앱: ${targetApps.joinToString()}")

        // 미션 추적 시작
        missionTracker.startTracking(
            MissionData(
                missionId = missionId,
                missionType = missionType,
                instruction = instruction,
                durationSeconds = duration,
                targetApps = targetApps
            )
        )

        // 알림 표시
        showNotification(
            title = "🎯 새로운 미션!",
            body = "$instruction (보상: ${data["coin_reward"] ?: "100"} 코인)",
            interventionId = missionId
        )
    }
}


