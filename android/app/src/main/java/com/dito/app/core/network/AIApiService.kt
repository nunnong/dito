package com.dito.app.core.network

import com.dito.app.core.data.ai.VideoClassifyRequest
import com.dito.app.core.data.ai.VideoClassifyResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * AI 서버 API 서비스
 * Base URL: http://52.78.96.102:8000/
 */
interface AIApiService {

    @POST("runs/wait")
    suspend fun classifyVideo(
        @Body request: VideoClassifyRequest
    ): VideoClassifyResponse
}
