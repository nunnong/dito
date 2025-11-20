package com.dito.app.core.util

import android.util.Log
import com.dito.app.core.data.ai.VideoClassifyRequest
import com.dito.app.core.data.ai.VideoInput
import com.dito.app.core.network.AIApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 데모용 교육 콘텐츠 감지기
 * 특정 제목이나 채널명을 기반으로 교육 콘텐츠 여부를 판단
 * AI API를 사용하거나 하드코딩된 규칙을 사용할 수 있음
 */
object EducationalContentDetector {

    private const val TAG = "EducationalDetector"

    // AI API 사용 여부 (true: AI API, false: 하드코딩)
    var useAIApi = false

    // AI API 서비스 (Hilt에서 주입받아 설정)
    var aiApiService: AIApiService? = null

    // 데모용 교육 채널 리스트 (조코딩 JoCoding)
    private val EDUCATIONAL_CHANNELS = listOf(
        "조코딩 JoCoding",
        "조코딩",
        "JoCoding"
    )

    // 데모용 교육 키워드 (제목에 포함되면 교육으로 판단)
    private val EDUCATIONAL_KEYWORDS = listOf(
        "컴퓨터 구조와 운영체제",
        "핵심 개념 정복하기",
        "강민철 저자님",
        "프로그래밍",
        "코딩",
        "알고리즘",
        "자료구조",
        "개발",
        "튜토리얼",
        "강의",
        "lecture",
        "tutorial",
        "programming",
        "coding"
    )

    /**
     * 제목과 채널명을 기반으로 교육 콘텐츠 여부 판단 (동기식, 하드코딩)
     */
    fun isEducationalContent(title: String?, channel: String?): Boolean {
        // 채널명 체크
        if (!channel.isNullOrBlank()) {
            if (EDUCATIONAL_CHANNELS.any { channel.contains(it, ignoreCase = true) }) {
                Log.d(TAG, "📚 교육 채널 감지: $channel")
                return true
            }
        }

        // 제목 키워드 체크
        if (!title.isNullOrBlank()) {
            if (EDUCATIONAL_KEYWORDS.any { title.contains(it, ignoreCase = true) }) {
                Log.d(TAG, "📚 교육 키워드 감지: $title")
                return true
            }
        }

        return false
    }

    /**
     * AI API를 사용하여 교육 콘텐츠 여부 판단 (비동기식)
     * @return Pair<Boolean, String?> - (교육 여부, video_type)
     */
    suspend fun classifyWithAI(title: String, channel: String): Pair<Boolean, String?> {
        val service = aiApiService
        if (service == null) {
            Log.w(TAG, "AI API 서비스가 설정되지 않음 → 하드코딩 fallback")
            return Pair(isEducationalContent(title, channel), null)
        }

        return try {
            withContext(Dispatchers.IO) {
                Log.d(TAG, "🤖 AI API 호출: title=$title, channel=$channel")

                val response = service.classifyVideo(
                    VideoClassifyRequest(
                        assistantId = "youtube",
                        input = VideoInput(title = title, channel = channel)
                    )
                )

                val isEducational = response.videoType == "EDUCATIONAL"
                Log.d(TAG, "🤖 AI 응답: video_type=${response.videoType}, keywords=${response.keywords}")

                Pair(isEducational, response.videoType)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ AI API 호출 실패 → 하드코딩 fallback", e)
            Pair(isEducationalContent(title, channel), null)
        }
    }
}
