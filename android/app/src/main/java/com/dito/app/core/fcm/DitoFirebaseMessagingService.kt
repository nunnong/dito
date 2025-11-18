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

            val type = data["type"]  // intervention or evaluation
            val title = data["title"] ?: message.notification?.title ?: "디토"
            val body = data["message"] ?: message.notification?.body ?: ""
            val deepLink = data["deep_link"]

            // mission_id 존재 여부로 미션/일반 알림 구분
            if (data.containsKey("mission_id") && data["mission_id"]?.isNotBlank() == true) {
                val missionId = data["mission_id"]!!

                when (type) {
                    "intervention" -> {
                        // 미션 시작 알림 - 미션 추적 시작 (progress 포함)
                        Log.d(TAG, "🎯 Intervention 알림 감지: mission_id=$missionId")
                        val missionDeepLink = deepLink ?: "dito://mission/$missionId"
                        Log.d(TAG, "   딥링크: $missionDeepLink")
                        handleMissionMessage(data, missionDeepLink)
                    }
                    "evaluation" -> {
                        // 평가 결과 알림 - 모달 자동 열기용 딥링크
                        Log.d(TAG, "📊 Evaluation 알림 감지: mission_id=$missionId")
                        val evaluationDeepLink = "dito://mission/$missionId?openDetail=true"
                        Log.d(TAG, "   딥링크: $evaluationDeepLink")
                        showEvaluationNotification(title, body, evaluationDeepLink)
                    }
                    else -> {
                        // type 없으면 intervention으로 처리 (하위 호환성)
                        Log.d(TAG, "⚠️ type 없는 미션 알림: mission_id=$missionId")
                        val missionDeepLink = deepLink ?: "dito://mission/$missionId"
                        handleMissionMessage(data, missionDeepLink)
                    }
                }
            } else {
                // 일반 알림 - 격려 메시지
                Log.d(TAG, "일반 알림 감지 (mission_id 없음)")
                val title = data["title"] ?: message.notification?.title ?: "디토"
                val body = data["message"] ?: message.notification?.body ?: "잘하고 있어요! 건강한 디지털 습관을 유지하세요."
                showNotification(
                    title = title,
                    body = body,
                    deepLink = null
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
                    deepLink = null
                )
            }
        }
    }

    /**
     * 알림 표시
     * @param title 알림 제목
     * @param body 알림 내용
     * @param deepLink 딥링크 URI (예: dito://mission/7)
     */
    private fun showNotification(title: String, body: String, deepLink: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 딥링크 방식으로 Intent 생성
        val intent = if (deepLink != null) {
            Intent(Intent.ACTION_VIEW, Uri.parse(deepLink)).apply {
                setClass(this@DitoFirebaseMessagingService, MainActivity::class.java)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        } else {
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }

        Log.d(TAG, "Intent 생성 완료")
        Log.d(TAG, "   Deep Link: $deepLink")
        Log.d(TAG, "   Data URI: ${intent.data}")

        val pendingIntent = PendingIntent.getActivity(
            this,
            deepLink?.hashCode() ?: 0,
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

        val notificationId = deepLink?.hashCode() ?: NOTIFICATION_ID_BASE
        notificationManager.notify(notificationId, notification)

        Log.d(TAG, "✅ 알림 표시 완료: id=$notificationId, title=$title")
    }

    /**
     * 평가 결과 알림 표시 (progress 없음)
     * Evaluation FCM 전용 - 미션 추적을 시작하지 않고 단순 알림만 표시
     *
     * @param title 알림 제목
     * @param message 알림 내용 (AI 피드백)
     * @param deepLink 딥링크 URI (예: dito://mission/7)
     */
    private fun showEvaluationNotification(title: String, message: String, deepLink: String?) {
        Log.d(TAG, "📊 평가 결과 알림 표시 중...")
        Log.d(TAG, "   제목: $title")
        Log.d(TAG, "   메시지: $message")
        Log.d(TAG, "   딥링크: $deepLink")

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 딥링크 Intent 생성
        val intent = if (deepLink != null) {
            Intent(Intent.ACTION_VIEW, Uri.parse(deepLink)).apply {
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
            deepLink?.hashCode() ?: System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 알림 생성 (progress 없음)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))  // 긴 텍스트 지원
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val notificationId = deepLink?.hashCode() ?: NOTIFICATION_ID_BASE + 1000
        notificationManager.notify(notificationId, notification)

        Log.d(TAG, "✅ 평가 결과 알림 표시 완료: id=$notificationId")
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

    private fun handleMissionMessage(data: Map<String, String>, deepLink: String) {
        val missionId = data["mission_id"] ?: return
        val missionType = data["mission_type"] ?: "REST"
        val instruction = data["message"] ?: "미션을 수행하세요"  // AI 팀: instruction → message
        val duration = data["duration_seconds"]?.toIntOrNull() ?: 300  // AI 팀: duration → duration_seconds
        val coinReward = data["coin_reward"] ?: "10"

        // 타겟 앱 설정 (현재는 하드코딩, 추후 서버에서 받도록 수정 가능)
        val targetApps = when(missionType) {
            "REST" -> listOf("com.google.android.youtube",
                "com.instagram.android",
                "com.zhiliaoapp.musically")
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
                coinReward = coinReward.toIntOrNull() ?: 10,
                deepLink = deepLink
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
            deepLink = deepLink
        )
    }
}