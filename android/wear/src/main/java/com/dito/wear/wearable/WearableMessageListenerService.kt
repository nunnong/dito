package com.dito.wear.wearable

import android.content.Intent
import android.util.Log
import com.dito.wear.MainActivity
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class WearableMessageListenerService : WearableListenerService() {

    companion object {
        private const val TAG = "WearableMsgListener"

        // 메시지 경로
        private const val PATH_START_BREATHING = "/start_breathing"
        private const val PATH_HEALTH_DATA = "/health_data"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🔵 WearableMessageListenerService 생성됨")
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        Log.d(TAG, "📩 메시지 수신: ${messageEvent.path}")

        when (messageEvent.path) {
            PATH_START_BREATHING -> {
                Log.d(TAG, "🌬️ 호흡 운동 시작 요청 받음")
                // MainActivity 실행 (시작 페이지)
                val intent = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivity(intent)
                Log.d(TAG, "✅ MainActivity 실행 완료")
            }

            PATH_HEALTH_DATA -> {
                // 건강 데이터 수신
                val data = String(messageEvent.data)
                Log.d(TAG, "📊 건강 데이터 수신: $data")
                // 필요시 데이터 처리 로직 추가
            }

            else -> {
                Log.d(TAG, "❓ 알 수 없는 메시지 경로: ${messageEvent.path}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🔴 WearableMessageListenerService 종료됨")
    }
}
