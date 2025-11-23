package com.dito.app.core.data.report

/**
 * 디토일지 화면 상태
 */
sealed class DiaryUiState {
    /**
     * 피드백 영상 로딩 중
     */
    data object LoadingVideos : DiaryUiState()

    /**
     * 피드백 수집 중
     */
    data class FeedbackCollection(
        val videos: List<VideoFeedbackItem>,
        val feedbacks: Map<String, VideoFeedback> = emptyMap()
    ) : DiaryUiState()

    /**
     * 디토일지 생성 중 (로딩)
     */
    data object GeneratingDiary : DiaryUiState()

    /**
     * 디토일지 생성 완료
     */
    data class DiaryGenerated(
        val reportData: DailyReportData
    ) : DiaryUiState()

    /**
     * 에러
     */
    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : DiaryUiState()
}
