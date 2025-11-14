package com.dito.app.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.dito.app.core.service.mission.ProgressUpdateReceiver

/**
 * AlarmManager를 사용하여 1초마다 정확한 진행률 업데이트 스케줄링
 */
object ProgressAlarmScheduler {

    private const val TAG = "ProgressAlarmScheduler"
    private const val ACTION_UPDATE_PROGRESS = "com.dito.app.ACTION_UPDATE_PROGRESS"
    private const val UPDATE_INTERVAL_MS = 1000L  // 1초

    /**
     * 진행률 업데이트 스케줄링 시작
     * @param context Context
     * @param missionId 미션 ID
     * @param durationSeconds 총 duration (초)
     * @param startTimeMs 시작 시간 (milliseconds)
     * @param delaySeconds 시작 딜레이 (초) - 이 시간만큼 대기 후 progress 시작
     */
    fun scheduleUpdates(
        context: Context,
        missionId: String,
        durationSeconds: Int,
        startTimeMs: Long,
        delaySeconds: Int = 0
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Android 12+ 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e(TAG, "SCHEDULE_EXACT_ALARM 권한이 없습니다. 설정에서 권한을 허용해주세요.")
                // TODO: 사용자에게 권한 요청 UI 표시
                return
            }
        }

        Log.i(TAG, "AlarmManager 스케줄링 시작: $missionId, ${durationSeconds}초 (${delaySeconds}초 딜레이)")

        // 각 초마다 알람 스케줄링 (delaySeconds 후부터 1초, 2초, ..., durationSeconds초)
        for (elapsedSeconds in 1..durationSeconds) {
            val triggerAtMillis = startTimeMs + (delaySeconds * 1000L) + (elapsedSeconds * UPDATE_INTERVAL_MS)

            val intent = Intent(context, ProgressUpdateReceiver::class.java).apply {
                action = ACTION_UPDATE_PROGRESS
                putExtra("mission_id", missionId)
                putExtra("elapsed_seconds", elapsedSeconds)
                putExtra("duration_seconds", durationSeconds)
                putExtra("start_time_ms", startTimeMs)
                putExtra("delay_seconds", delaySeconds)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                getRequestCode(missionId, elapsedSeconds),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Doze 모드에서도 동작하도록 setExactAndAllowWhileIdle 사용
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "AlarmManager 스케줄링 실패: ${e.message}", e)
                break
            }
        }

        Log.d(TAG, "AlarmManager 스케줄링 완료: ${durationSeconds}개 알람 등록")
    }

    /**
     * 모든 예약된 알람 취소
     * @param context Context
     * @param missionId 미션 ID
     * @param durationSeconds 총 duration (초)
     */
    fun cancelAllUpdates(
        context: Context,
        missionId: String,
        durationSeconds: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        Log.i(TAG, "AlarmManager 취소 시작: $missionId")

        for (elapsedSeconds in 1..durationSeconds) {
            val intent = Intent(context, ProgressUpdateReceiver::class.java).apply {
                action = ACTION_UPDATE_PROGRESS
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                getRequestCode(missionId, elapsedSeconds),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )

            // PendingIntent가 존재하면 취소
            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }

        Log.d(TAG, "AlarmManager 취소 완료: ${durationSeconds}개 알람 취소")
    }

    /**
     * 특정 미션의 특정 초에 대한 고유 request code 생성
     * @param missionId 미션 ID
     * @param elapsedSeconds 경과 시간 (초)
     * @return Request code
     */
    private fun getRequestCode(missionId: String, elapsedSeconds: Int): Int {
        // 미션 ID의 hashCode와 elapsedSeconds를 조합하여 고유한 request code 생성
        return (missionId.hashCode() and 0xFFFF) or (elapsedSeconds shl 16)
    }

    /**
     * 남은 알람 개수 확인 (디버깅용)
     * @param context Context
     * @param missionId 미션 ID
     * @param durationSeconds 총 duration (초)
     * @return 남은 알람 개수
     */
    fun getRemainingAlarmCount(
        context: Context,
        missionId: String,
        durationSeconds: Int
    ): Int {
        var count = 0
        for (elapsedSeconds in 1..durationSeconds) {
            val intent = Intent(context, ProgressUpdateReceiver::class.java).apply {
                action = ACTION_UPDATE_PROGRESS
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                getRequestCode(missionId, elapsedSeconds),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )

            if (pendingIntent != null) {
                count++
                pendingIntent.cancel()
            }
        }
        return count
    }
}
