package com.dito.wear.fcm

import android.content.Intent
import android.util.Log
import com.dito.wear.BreathingActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class WearFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "From: ${remoteMessage.from}")
        Log.d(TAG, "Message data: ${remoteMessage.data}")

        // 푸시 알림에서 호흡 운동 타입인지 확인
        if (remoteMessage.data["type"] == "breathing") {
            // 호흡 운동 액티비티 자동 실행
            val intent = Intent(this, BreathingActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
            Log.d(TAG, "호흡 운동 액티비티 자동 실행")
        }

        // 알림 표시 로직은 필요시 추가
        remoteMessage.notification?.let {
            Log.d(TAG, "Notification Title: ${it.title}")
            Log.d(TAG, "Notification Body: ${it.body}")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Wear FCM Token: $token")
        // 필요시 서버에 토큰 전송 로직 추가
    }

    companion object {
        private const val TAG = "WearFCMService"
    }
}
