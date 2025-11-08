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

        // Data payload 처리
        message.data.let { data ->
            Log.d(TAG, "Data payload: $data")

            // mission_id 존재 여부로 미션/일반 알림 구분 (AI 팀 FCM 구조에 맞춤)
            if (data.containsKey("mission_id") && data["mission_id"]?.isNotBlank() == true) {
                // 미션 알림 - 미션 추적 시작
                Log.d(TAG, "미션 알림 감지: mission_id=${data["mission_id"]}")
                handleMissionMessage(data)
            } else {
                // 일반 알림 - 격려 메시지
                Log.d(TAG, "일반 알림 감지 (mission_id 없음)")
                val title = data["title"] ?: message.notification?.title ?: "디토"
                val body = data["message"] ?: message.notification?.body ?: "잘하고 있어요! 건강한 디지털 습관을 유지하세요."
                showNotification(
                    title = title,
                    body = body,
                    interventionId = null
                )
            }
        }

        // Notification payload 처리 (Firebase Console 테스트용)
        if (message.data.isEmpty() && message.notification != null) {
            message.notification?.let { notification ->
                Log.d(TAG, "Notification only payload: title=${notification.title}")
                showNotification(
                    title = notification.title ?: "디토",
                    body = notification.body ?: "",
                    interventionId = null
                )
            }
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
        val instruction = data["message"] ?: "미션을 수행하세요"  // AI 팀: instruction → message
        val duration = data["duration_seconds"]?.toIntOrNull() ?: 300  // AI 팀: duration → duration_seconds
        val coinReward = data["coin_reward"] ?: "10"

        // 타겟 앱 설정 (현재는 하드코딩, 추후 서버에서 받도록 수정 가능)
        val targetApps = when(missionType) {
            "REST" -> listOf("com.google.android.youtube",
                "com.instagram.android",
                "com.zhiliaoapp.musically")  // TikTok 추가
            "MEDITATION" -> emptyList()  // 명상은 특정 앱 차단 불필요
            else -> listOf("com.google.android.youtube", "com.instagram.android")
        }

        Log.i(TAG, "🎯 미션 수신: $missionId")
        Log.d(TAG, "   타입: $missionType")
        Log.d(TAG, "   지시: $instruction")
        Log.d(TAG, "   시간: ${duration}초")
        Log.d(TAG, "   보상: ${coinReward} 코인")
        Log.d(TAG, "   타겟 앱: ${targetApps.joinToString()}")

        // 미션 추적 시작
        missionTracker.startTracking(
            MissionData(
                missionId = missionId,
                missionType = missionType,
                instruction = instruction,
                durationSeconds = duration,
                targetApps = targetApps,
                coinReward = coinReward.toIntOrNull() ?: 10  // MissionData에 추가 필요
            )
        )

        // 알림 표시
        val notificationBody = when(missionType) {
            "REST" -> "$instruction (${duration/60}분간 휴식, 보상: ${coinReward} 코인)"
            "MEDITATION" -> "$instruction (${duration/60}분 명상, 보상: ${coinReward} 코인)"
            else -> "$instruction (보상: ${coinReward} 코인)"
        }

        showNotification(
            title = when(missionType) {
                "REST" -> "💆 휴식이 필요해요!"
                "MEDITATION" -> "🧘 명상 시간입니다"
                else -> "🎯 새로운 미션!"
            },
            body = notificationBody,
            interventionId = missionId
        )
    }
}


