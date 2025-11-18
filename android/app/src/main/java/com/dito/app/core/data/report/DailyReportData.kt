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
    val reportOverview: String,
    val insights: List<InsightItem>,
    val advice: String,
    @SerialName("missionSuccessRate")
    val missionSuccessRate: Int,
    @SerialName("createdAt")
    val createdAt: String
)

@Serializable
data class InsightItem(
    val type: ComparisonType,
    val description: String
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
    val advice: String
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
