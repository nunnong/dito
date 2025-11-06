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
import com.google.firebase.messaging.FirebaseMessagingService
import com.dito.app.core.wearable.WearableMessageService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * FCM 메시지 수신 및 토큰 갱신을 처리하는 서비스
 */
@AndroidEntryPoint
class DitoFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmTokenManager: FcmTokenManager

    @Inject
    lateinit var wearableMessageService: WearableMessageService

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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

            val interventionId = data["interventionId"]
            val notificationType = data["type"]
            val title = data["title"] ?: "Dito"
            val body = data["body"] ?: "새로운 intervention이 도착했습니다"

            // 호흡 운동 타입인 경우 워치에 메시지 전송
            if (notificationType == "breathing") {
                serviceScope.launch {
                    val result = wearableMessageService.startBreathingOnWatch()
                    if (result.isSuccess) {
                        Log.d(TAG, "워치에 호흡 운동 시작 메시지 전송 성공")
                    } else {
                        Log.w(TAG, "워치에 메시지 전송 실패 또는 워치 미연결")
                    }
                }
            }

            showNotification(title, body, interventionId, notificationType)
        }

        // Notification payload 처리 (Firebase Console에서 테스트 시)
        message.notification?.let { notification ->
            Log.d(TAG, "Notification payload: title=${notification.title}")
            showNotification(
                title = notification.title ?: "Dito",
                body = notification.body ?: "",
                interventionId = null,
                notificationType = null
            )
        }
    }

    /**
     * 알림 표시
     * @param title 알림 제목
     * @param body 알림 내용
     * @param interventionId Intervention ID (deep link용)
     * @param notificationType 알림 타입 (breathing, intervention 등)
     */
    private fun showNotification(
        title: String,
        body: String,
        interventionId: String?,
        notificationType: String?
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Deep link intent 생성
        val intent = when {
            notificationType == "breathing" -> {
                // 호흡 운동 deep link
                Intent(Intent.ACTION_VIEW, Uri.parse("dito://breathing")).apply {
                    setClass(this@DitoFirebaseMessagingService, MainActivity::class.java)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            }
            interventionId != null -> {
                // Intervention deep link
                Intent(Intent.ACTION_VIEW, Uri.parse("dito://intervention/$interventionId")).apply {
                    setClass(this@DitoFirebaseMessagingService, MainActivity::class.java)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            }
            else -> {
                // 기본 메인 화면
                Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
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
}
