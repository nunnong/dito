package com.dito.app.core.data.report

import kotlinx.serialization.Serializable

/**
 * 시청 기록 아이템 (디토일지 피드백용)
 * 추후 API로 대체될 예정
 */
@Serializable
data class VideoFeedbackItem(
    val id: String,
    val title: String,
    val channel: String,
    val thumbnailBase64: String? = null, // Base64 인코딩된 bitmap 이미지 (API용)
    val thumbnailResName: String? = null, // Drawable 리소스 이름 (로컬 mock용)
    val watchTimeMinutes: Int // 시청 시간 (분)
)

/**
 * JSON 파일에서 읽어온 모킹 데이터 래퍼
 */
@Serializable
data class MockVideosData(
    val videos: List<VideoFeedbackItem>
)

/**
 * 사용자 피드백 데이터
 */
data class VideoFeedback(
    val videoId: String,
    val isHelpful: Boolean? = null, // null: 선택 안함, true: 도움됨, false: 도움안됨
    val selectedReasons: Set<String> = emptySet() // 다중 선택된 이유들
)

/**
 * 피드백 이유 옵션 정의
 */
object FeedbackReasons {
    /**
     * 도움됨 (디지털 웰빙에 긍정적) 옵션
     */
    val HELPFUL_OPTIONS = listOf(
        "새로운 지식/기술을 배웠어요",
        "필요한 정보를 찾기 위해 봤어요",
        "의도한 시간만큼만 시청했어요",
        "동기부여/영감을 얻었어요"
    )

    /**
     * 도움안됨 (디지털 웰빙에 부정적) 옵션
     */
    val UNHELPFUL_OPTIONS = listOf(
        "무의식적으로 클릭했어요",
        "다음 영상을 계속 보게 됐어요",
        "예상보다 오래 시청했어요",
        "시청 후 죄책감이 들었어요"
    )
}
