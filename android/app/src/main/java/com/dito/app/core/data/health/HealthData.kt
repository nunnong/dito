package com.dito.app.core.data.health

import java.time.Instant

/**
 * 건강 데이터 통합 모델
 */
data class HealthData(
    val steps: StepsData? = null,
    val heartRate: HeartRateData? = null,
    val sleep: SleepData? = null,
    val distance: DistanceData? = null
)

/**
 * 걸음 수 데이터
 */
data class StepsData(
    val count: Long,
    val startTime: Instant,
    val endTime: Instant
)

/**
 * 심박수 데이터
 */
data class HeartRateData(
    val beatsPerMinute: Long,
    val time: Instant
)

/**
 * 수면 데이터
 */
data class SleepData(
    val startTime: Instant,
    val endTime: Instant,
    val durationMinutes: Long,
    val stages: List<SleepStage> = emptyList()
)

/**
 * 수면 단계
 */
data class SleepStage(
    val stage: SleepStageType,
    val startTime: Instant,
    val endTime: Instant
)

enum class SleepStageType {
    UNKNOWN,        // 사용자가 수면 중인지 지정되지 않았거나 알 수 없음
    AWAKE,          // 수면 주기 내에서 깨어 있음
    SLEEPING,       // 일반적이거나 상세하지 않은 수면
    OUT_OF_BED,     // 수면 세션 중 침대에서 나옴
    AWAKE_IN_BED,   // 침대에서 깨어 있음
    LIGHT,          // 얕은 수면 주기
    DEEP,           // 깊은 수면 주기
    REM             // 렘수면 주기
}

/**
 * 이동 거리 데이터
 */
data class DistanceData(
    val distanceMeters: Double,
    val startTime: Instant,
    val endTime: Instant
)
