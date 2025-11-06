package com.dito.app.core.repository

import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.dito.app.core.data.health.DistanceData
import com.dito.app.core.data.health.HeartRateData
import com.dito.app.core.data.health.HealthData
import com.dito.app.core.data.health.SleepData
import com.dito.app.core.data.health.SleepStage
import com.dito.app.core.data.health.SleepStageType
import com.dito.app.core.data.health.StepsData
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

interface HealthRepository {
    /**
     * Health Connect SDK 사용 가능 여부 확인
     */
    fun isHealthConnectAvailable(): Boolean

    /**
     * 필요한 권한 목록 반환
     */
    fun getRequiredPermissions(): Set<String>

    /**
     * 권한 확인
     */
    suspend fun hasAllPermissions(): Boolean

    /**
     * 오늘 건강 데이터 가져오기
     */
    suspend fun getTodayHealthData(): Result<HealthData>

    /**
     * 특정 기간 건강 데이터 가져오기
     */
    suspend fun getHealthData(startTime: Instant, endTime: Instant): Result<HealthData>
}

@Singleton
class HealthRepositoryImpl @Inject constructor(
    private val healthConnectClient: HealthConnectClient?
) : HealthRepository {

    companion object {
        private const val TAG = "HealthRepository"
    }

    override fun isHealthConnectAvailable(): Boolean {
        return healthConnectClient != null
    }

    override fun getRequiredPermissions(): Set<String> {
        return setOf(
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(DistanceRecord::class)
        )
    }

    override suspend fun hasAllPermissions(): Boolean {
        if (healthConnectClient == null) return false

        return try {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            getRequiredPermissions().all { it in granted }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getTodayHealthData(): Result<HealthData> {
        // 한국 시간대 설정
        val koreaZoneId = ZoneId.of("Asia/Seoul")

        // 현재 시간 (한국 시간 기준)
        val now = ZonedDateTime.now(koreaZoneId)

        // 오늘 자정 00:00:00 (한국 시간 기준)
        val startOfDay = now.toLocalDate().atStartOfDay(koreaZoneId)

        // Instant로 변환
        val startInstant = startOfDay.toInstant()
        val endInstant = now.toInstant()

        Log.d(TAG, "한국 시간 조회 기간: ${startOfDay.toLocalDateTime()} ~ ${now.toLocalDateTime()}")

        return getHealthData(startInstant, endInstant)
    }

    override suspend fun getHealthData(startTime: Instant, endTime: Instant): Result<HealthData> {
        if (healthConnectClient == null) {
            return Result.failure(Exception("Health Connect is not available"))
        }

        return try {
            Log.d(TAG, "건강 데이터 조회 시작: $startTime ~ $endTime")
            val timeRangeFilter = TimeRangeFilter.between(startTime, endTime)

            // 걸음 수 조회
            val stepsData = readStepsData(timeRangeFilter)
            Log.d(TAG, "걸음 수 데이터: ${stepsData?.count ?: "없음"}")

            // 심박수 조회 (최근값)
            val heartRateData = readHeartRateData(timeRangeFilter)
            Log.d(TAG, "심박수 데이터: ${heartRateData?.beatsPerMinute ?: "없음"} BPM")

            // 수면 데이터 조회 (어제부터 오늘까지 - 전날 밤 수면 포함)
            val sleepData = readSleepData(endTime)
            Log.d(TAG, "수면 데이터: ${sleepData?.durationMinutes ?: "없음"}분")

            // 이동 거리 조회
            val distanceData = readDistanceData(timeRangeFilter)
            Log.d(TAG, "이동 거리 데이터: ${distanceData?.distanceMeters ?: "없음"}m")

            Result.success(
                HealthData(
                    steps = stepsData,
                    heartRate = heartRateData,
                    sleep = sleepData,
                    distance = distanceData
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "건강 데이터 조회 실패", e)
            Result.failure(e)
        }
    }

    private suspend fun readStepsData(timeRangeFilter: TimeRangeFilter): StepsData? {
        return try {
            val response = healthConnectClient!!.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = timeRangeFilter
                )
            )

            Log.d(TAG, "걸음 수 레코드 개수: ${response.records.size}")

            if (response.records.isEmpty()) return null

            // 모든 걸음 수 합산
            val totalSteps = response.records.sumOf { it.count }
            val startTime = response.records.minOf { it.startTime }
            val endTime = response.records.maxOf { it.endTime }

            StepsData(
                count = totalSteps,
                startTime = startTime,
                endTime = endTime
            )
        } catch (e: Exception) {
            Log.e(TAG, "걸음 수 조회 실패", e)
            null
        }
    }

    private suspend fun readHeartRateData(timeRangeFilter: TimeRangeFilter): HeartRateData? {
        return try {
            val response = healthConnectClient!!.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = timeRangeFilter
                )
            )

            Log.d(TAG, "심박수 레코드 개수: ${response.records.size}")

            if (response.records.isEmpty()) return null

            // 가장 최근 심박수 데이터 사용
            val latestRecord = response.records.maxByOrNull { it.endTime }
            latestRecord?.samples?.lastOrNull()?.let { sample ->
                HeartRateData(
                    beatsPerMinute = sample.beatsPerMinute,
                    time = latestRecord.endTime
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "심박수 조회 실패", e)
            null
        }
    }

    private suspend fun readSleepData(currentTime: Instant): SleepData? {
        return try {
            // 어제 정오(12:00)부터 현재까지 조회 (전날 밤 수면 세션 포함)
            val yesterdayNoon = currentTime.minus(36, ChronoUnit.HOURS)
            val sleepTimeRangeFilter = TimeRangeFilter.between(yesterdayNoon, currentTime)

            val response = healthConnectClient!!.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = sleepTimeRangeFilter
                )
            )

            Log.d(TAG, "수면 레코드 개수: ${response.records.size}")

            if (response.records.isEmpty()) {
                Log.d(TAG, "조회 기간: $yesterdayNoon ~ $currentTime")
                return null
            }

            // 가장 최근에 종료된 수면 세션 사용 (endTime 기준)
            val latestSleep = response.records.maxByOrNull { it.endTime }
            latestSleep?.let { sleepRecord ->
                Log.d(TAG, "수면 세션 발견: ${sleepRecord.startTime} ~ ${sleepRecord.endTime}")
                Log.d(TAG, "수면 단계 개수: ${sleepRecord.stages.size}")

                val durationMinutes = ChronoUnit.MINUTES.between(sleepRecord.startTime, sleepRecord.endTime)

                // 수면 단계(stages) 데이터 추출
                val stages = sleepRecord.stages.map { stage ->
                    SleepStage(
                        stage = mapSleepStageType(stage.stage),
                        startTime = stage.startTime,
                        endTime = stage.endTime
                    )
                }

                SleepData(
                    startTime = sleepRecord.startTime,
                    endTime = sleepRecord.endTime,
                    durationMinutes = durationMinutes,
                    stages = stages
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "수면 데이터 조회 실패", e)
            null
        }
    }

    private suspend fun readDistanceData(timeRangeFilter: TimeRangeFilter): DistanceData? {
        return try {
            val response = healthConnectClient!!.readRecords(
                ReadRecordsRequest(
                    recordType = DistanceRecord::class,
                    timeRangeFilter = timeRangeFilter
                )
            )

            Log.d(TAG, "이동 거리 레코드 개수: ${response.records.size}")

            if (response.records.isEmpty()) return null

            // 모든 거리 합산
            val totalDistance = response.records.sumOf { it.distance.inMeters }
            val startTime = response.records.minOf { it.startTime }
            val endTime = response.records.maxOf { it.endTime }

            DistanceData(
                distanceMeters = totalDistance,
                startTime = startTime,
                endTime = endTime
            )
        } catch (e: Exception) {
            Log.e(TAG, "이동 거리 조회 실패", e)
            null
        }
    }

    private fun mapSleepStageType(stage: Int): SleepStageType {
        return when (stage) {
            SleepSessionRecord.STAGE_TYPE_UNKNOWN -> SleepStageType.UNKNOWN
            SleepSessionRecord.STAGE_TYPE_AWAKE -> SleepStageType.AWAKE
            SleepSessionRecord.STAGE_TYPE_SLEEPING -> SleepStageType.SLEEPING
            SleepSessionRecord.STAGE_TYPE_OUT_OF_BED -> SleepStageType.OUT_OF_BED
            SleepSessionRecord.STAGE_TYPE_LIGHT -> SleepStageType.LIGHT
            SleepSessionRecord.STAGE_TYPE_DEEP -> SleepStageType.DEEP
            SleepSessionRecord.STAGE_TYPE_REM -> SleepStageType.REM
            SleepSessionRecord.STAGE_TYPE_AWAKE_IN_BED -> SleepStageType.AWAKE_IN_BED
            else -> SleepStageType.UNKNOWN
        }
    }
}
