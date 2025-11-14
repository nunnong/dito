package com.dito.app.core.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dito.app.MainActivity
import com.dito.app.R

/**
 * 미션 진행률 알림 헬퍼
 *
 * 표준 NotificationCompat.setProgress() 사용 (모든 API 레벨 지원)
 *
 * 참고: https://developer.android.com/develop/ui/views/notifications/build-notification#Updating
 */
object ProgressNotificationHelper {

    private const val TAG = "ProgressNotification"
    private const val CHANNEL_ID = "dito_intervention"
    const val MISSION_NOTIFICATION_ID = 2000

    /**
     * 초기 진행률 알림 생성
     */
    fun buildInitialNotification(
        context: Context,
        missionId: String,
        missionType: String,
        instruction: String,
        durationSeconds: Int,
        coinReward: Int,
        deepLink: String?
    ): android.app.Notification {
        Log.d(TAG, "초기 진행률 알림 생성: $missionType, ${durationSeconds}초")

        val title = when (missionType) {
            "REST" -> "휴식 미션"
            "MEDITATION" -> "명상 미션"
            else -> "미션 진행 중"
        }

        val contentText = "$instruction\n보상: ${coinReward} 코인"

        return buildProgressNotification(
            context = context,
            missionId = missionId,
            missionType = missionType,
            title = title,
            contentText = contentText,
            progress = 0,
            total = durationSeconds,
            deepLink = deepLink
        )
    }

    /**
     * 진행률 업데이트 알림 생성
     * 표준 setProgress() 사용 (모든 API 레벨 지원)
     */
    fun buildProgressNotification(
        context: Context,
        missionId: String,
        missionType: String,
        title: String,
        contentText: String,
        progress: Int,
        total: Int,
        deepLink: String?
    ): android.app.Notification {
        Log.d(TAG, "진행률 알림 생성/업데이트: $progress/$total")

        val pendingIntent = createPendingIntent(context, deepLink)

        val iconRes = when (missionType) {
            "REST" -> R.drawable.ic_notification
            "MEDITATION" -> R.drawable.ic_notification
            else -> R.drawable.ic_notification
        }

        // 남은 시간 계산
        val remainingSeconds = total - progress
        val remainingMinutes = remainingSeconds / 60
        val remainingSecondsInMinute = remainingSeconds % 60
        val remainingTimeText = "${remainingMinutes}:${remainingSecondsInMinute.toString().padStart(2, '0')}"

        // 표준 NotificationCompat 사용 (모든 API 레벨 지원)
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSubText("남은 시간: $remainingTimeText")
            .setProgress(total, progress, false)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .build()
    }

    /**
     * PendingIntent 생성
     */
    private fun createPendingIntent(context: Context, deepLink: String?): PendingIntent {
        val intent = if (deepLink != null) {
            Intent(Intent.ACTION_VIEW, Uri.parse(deepLink)).apply {
                setClass(context, MainActivity::class.java)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        } else {
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }

        return PendingIntent.getActivity(
            context,
            deepLink?.hashCode() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * 알림 업데이트
     */
    fun updateNotification(
        context: Context,
        notification: android.app.Notification
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(MISSION_NOTIFICATION_ID, notification)
    }

    /**
     * 알림 제거
     */
    fun cancelNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(MISSION_NOTIFICATION_ID)
        Log.d(TAG, "진행률 알림 제거 완료: ID=$MISSION_NOTIFICATION_ID")
    }
}
