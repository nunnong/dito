package com.dito.app.core.service.mission

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.dito.app.core.notification.ProgressAlarmScheduler
import com.dito.app.core.notification.ProgressNotificationHelper

/**
 * 미션 진행률을 Foreground 알림으로 표시하는 서비스
 * - ProgressStyle 알림 관리
 * - AlarmManager 스케줄링
 * - 진행률 업데이트 수신 및 처리
 */
class MissionProgressService : Service() {

    companion object {
        private const val TAG = "MissionProgressService"
        const val ACTION_STOP_SERVICE = "com.dito.app.ACTION_STOP_SERVICE"

        // 서비스 상태 추적을 위한 변수
        private var isServiceRunning = false

        fun isRunning(): Boolean = isServiceRunning
    }

    private var missionId: String? = null
    private var missionType: String? = null
    private var instruction: String? = null
    private var durationSeconds: Int = 0
    private var coinReward: Int = 0
    private var deepLink: String? = null
    private var startTimeMs: Long = 0L

    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ProgressUpdateReceiver.ACTION_SERVICE_UPDATE) {
                val receivedMissionId = intent.getStringExtra("mission_id")
                val elapsedSeconds = intent.getIntExtra("elapsed_seconds", 0)
                val totalSeconds = intent.getIntExtra("duration_seconds", 0)

                // 현재 미션과 일치하는지 확인
                if (receivedMissionId == missionId) {
                    updateProgress(elapsedSeconds, totalSeconds)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "서비스 생성")

        // 업데이트 브로드캐스트 리시버 등록
        val filter = IntentFilter(ProgressUpdateReceiver.ACTION_SERVICE_UPDATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(updateReceiver, filter)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: action=${intent?.action}")

        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                Log.i(TAG, "서비스 중지 요청")
                stopService()
                return START_NOT_STICKY
            }
            else -> {
                // 미션 시작
                startMissionProgress(intent)
                return START_STICKY
            }
        }
    }

    private fun startMissionProgress(intent: Intent?) {
        if (intent == null) {
            Log.e(TAG, "Intent가 null입니다")
            stopSelf()
            return
        }

        // 미션 정보 추출
        missionId = intent.getStringExtra("mission_id")
        missionType = intent.getStringExtra("mission_type") ?: "REST"
        instruction = intent.getStringExtra("instruction") ?: "미션을 수행하세요"
        durationSeconds = intent.getIntExtra("duration_seconds", 300)
        coinReward = intent.getIntExtra("coin_reward", 10)
        deepLink = intent.getStringExtra("deep_link")
        startTimeMs = intent.getLongExtra("start_time_ms", System.currentTimeMillis())
        val delaySeconds = intent.getIntExtra("delay_seconds", 0)

        if (missionId == null) {
            Log.e(TAG, "mission_id가 없습니다")
            stopSelf()
            return
        }

        Log.i(TAG, "미션 진행 시작: $missionId ($missionType, ${durationSeconds}초)")

        // Foreground 알림 표시
        val notification = ProgressNotificationHelper.buildInitialNotification(
            context = this,
            missionId = missionId!!,
            missionType = missionType!!,
            instruction = instruction!!,
            durationSeconds = durationSeconds,
            coinReward = coinReward,
            deepLink = deepLink
        )

        startForeground(ProgressNotificationHelper.MISSION_NOTIFICATION_ID, notification)
        isServiceRunning = true

        Log.d(TAG, "Foreground 알림 표시 완료")

        // AlarmManager 스케줄링 시작 (delaySeconds 후부터)
        ProgressAlarmScheduler.scheduleUpdates(
            context = this,
            missionId = missionId!!,
            durationSeconds = durationSeconds,
            startTimeMs = startTimeMs,
            delaySeconds = delaySeconds
        )

        Log.d(TAG, "AlarmManager 스케줄링 완료")
    }

    /**
     * 진행률 업데이트
     */
    private fun updateProgress(elapsedSeconds: Int, totalSeconds: Int) {
        if (missionId == null) {
            Log.w(TAG, "mission_id가 null이므로 업데이트 무시")
            return
        }

        Log.d(TAG, "진행률 업데이트: $elapsedSeconds/${totalSeconds}초")

        val title = when (missionType) {
            "REST" -> "💆 휴식 진행 중"
            "MEDITATION" -> "🧘 명상 진행 중"
            else -> "🎯 미션 진행 중"
        }

        val contentText = "$instruction\n보상: ${coinReward} 코인"

        // 알림 업데이트
        val notification = ProgressNotificationHelper.buildProgressNotification(
            context = this,
            missionId = missionId!!,
            missionType = missionType!!,
            title = title,
            contentText = contentText,
            progress = elapsedSeconds,
            total = totalSeconds,
            deepLink = deepLink
        )

        ProgressNotificationHelper.updateNotification(this, notification)

        // duration 완료 시 서비스 중지
        if (elapsedSeconds >= totalSeconds) {
            Log.i(TAG, "미션 완료: $missionId ($elapsedSeconds/${totalSeconds}초)")
            stopService()
        }
    }

    /**
     * 서비스 중지 및 정리
     * 주의: 알림은 제거하지 않고 유지함 (MissionEvaluationWorker에서 제거)
     */
    private fun stopService() {
        Log.i(TAG, "서비스 중지 시작: $missionId (알림 유지)")

        // AlarmManager 취소
        if (missionId != null && durationSeconds > 0) {
            ProgressAlarmScheduler.cancelAllUpdates(
                context = this,
                missionId = missionId!!,
                durationSeconds = durationSeconds
            )
        }

        // Foreground 알림 유지 (false = 알림 그대로 둠)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(false)  // 알림 유지
        } else {
            @Suppress("DEPRECATION")
            stopForeground(false)  // 알림 유지
        }

        isServiceRunning = false

        // 서비스 종료
        stopSelf()

        Log.d(TAG, "서비스 중지 완료 (알림은 유지됨)")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "서비스 파괴")

        // 브로드캐스트 리시버 해제
        try {
            unregisterReceiver(updateReceiver)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "리시버가 이미 해제되었습니다: ${e.message}")
        }

        // AlarmManager 취소 (혹시 모를 경우 대비)
        if (missionId != null && durationSeconds > 0) {
            ProgressAlarmScheduler.cancelAllUpdates(
                context = this,
                missionId = missionId!!,
                durationSeconds = durationSeconds
            )
        }

        isServiceRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Bound Service가 아니므로 null 반환
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.w(TAG, "앱이 최근 작업에서 제거되었습니다. 서비스 계속 실행.")
        // Foreground Service는 앱이 종료되어도 계속 실행됨
    }
}
