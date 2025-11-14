package com.dito.app.core.service.mission

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * AlarmManager로부터 1초마다 호출받아 진행률 업데이트 처리
 */
class ProgressUpdateReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ProgressUpdateReceiver"
        const val ACTION_UPDATE_PROGRESS = "com.dito.app.ACTION_UPDATE_PROGRESS"
        const val ACTION_SERVICE_UPDATE = "com.dito.app.ACTION_SERVICE_UPDATE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_UPDATE_PROGRESS) {
            return
        }

        val missionId = intent.getStringExtra("mission_id") ?: return
        val elapsedSeconds = intent.getIntExtra("elapsed_seconds", 0)
        val durationSeconds = intent.getIntExtra("duration_seconds", 0)
        val startTimeMs = intent.getLongExtra("start_time_ms", 0L)
        val delaySeconds = intent.getIntExtra("delay_seconds", 0)

        Log.d(TAG, "진행률 업데이트 수신: $missionId ($elapsedSeconds/${durationSeconds}초)")

        // 현재 시간과 시작 시간(+딜레이)을 비교하여 실제 경과 시간 계산
        val currentTimeMs = System.currentTimeMillis()
        val actualElapsedMs = currentTimeMs - (startTimeMs + delaySeconds * 1000L)
        val actualElapsedSeconds = (actualElapsedMs / 1000).toInt().coerceAtLeast(0)

        // MissionProgressService에 업데이트 브로드캐스트 전송
        val updateIntent = Intent(ACTION_SERVICE_UPDATE).apply {
            setPackage(context.packageName)
            putExtra("mission_id", missionId)
            putExtra("elapsed_seconds", actualElapsedSeconds)
            putExtra("duration_seconds", durationSeconds)
        }

        context.sendBroadcast(updateIntent)

        // 주의: duration 완료 시 서비스 중지는 MissionProgressService 내부에서 처리됨
        // Receiver에서는 업데이트만 전송
    }
}
