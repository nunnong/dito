package com.dito.app.core.data.report

import kotlinx.serialization.Serializable

/**
 * 일일 리포트 응답 데이터
 */
@Serializable
data class DailyReportResponse(
    val error: Boolean,
    val message: String,
    val data: DailyReportData?
)

@Serializable
data class DailyReportData(
    val userName: String,
    val costumeUrl: String, // 캐릭터 이미지 URL
    val missionCompletionRate: Int, // 퍼센트 (0-100)
    val currentStatus: StatusDescription,
    val predictions: List<String>,
    val comparisons: List<ComparisonItem>
)

@Serializable
data class StatusDescription(
    val title: String,
    val description: String
)

@Serializable
data class ComparisonItem(
    val type: ComparisonType, // POSITIVE, NEGATIVE, NEUTRAL
    val iconRes: String, // 리소스 이름 (백엔드에서 아이콘 타입 지정)
    val description: String
)

enum class ComparisonType {
    POSITIVE,  // 파란색 배경
    NEGATIVE,  // 빨간색 배경
    NEUTRAL    // 중립 (기본)
}
