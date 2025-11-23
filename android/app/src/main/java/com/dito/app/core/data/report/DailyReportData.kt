package com.dito.app.core.data.report

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 일일 리포트 API 응답
 */
@Serializable
data class DailyReportResponse(
    val id: Int,
    val status: String,
    @SerialName("reportOverview")
    val reportOverview: String? = null,
    val insights: List<InsightItem>? = null,
    val advice: String? = null,
    @SerialName("missionSuccessRate")
    val missionSuccessRate: Int? = null,
    @SerialName("createdAt")
    val createdAt: String? = null,
    val strategy: List<StrategyChange>? = null
)

@Serializable
data class InsightItem(
    val type: ComparisonType,
    val description: String,
    val score: ScoreComparison
)

@Serializable
data class ScoreComparison(
    val before: Int,
    val after: Int
)

/**
 * UI에서 사용할 데이터 모델
 */
data class DailyReportData(
    val status: String,
    val userName: String,
    val costumeUrl: String,
    val missionCompletionRate: Int,
    val currentStatus: StatusDescription,
    val predictions: List<String>,
    val comparisons: List<ComparisonItem>,
    val radarChartData: RadarChartData? = null,
    val advice: String,
    val strategyChanges: List<StrategyChange> = emptyList()
)

/**
 * 레이더 차트 데이터 (수면, 집중, 조절력 점수)
 */
data class RadarChartData(
    val sleepScore: Int,           // 수면 현재 점수 (0-100)
    val focusScore: Int,           // 집중 현재 점수 (0-100)
    val selfControlScore: Int,     // 조절력 현재 점수 (0-100)
    val sleepBefore: Int = 0,      // 수면 이전 점수 (0-100)
    val focusBefore: Int = 0,      // 집중 이전 점수 (0-100)
    val selfControlBefore: Int = 0 // 조절력 이전 점수 (0-100)
)

data class StatusDescription(
    val title: String,
    val description: String
)

data class ComparisonItem(
    val type: ComparisonType,
    val iconRes: String,
    val description: String
)

enum class ComparisonType {
    POSITIVE,
    NEGATIVE,
    NEUTRAL
}
