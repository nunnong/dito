package com.dito.app.core.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.ResponseBody

interface AIService {

    @POST("/ai/intervention")
    suspend fun sendToAI(
        @Body request: AIRunRequest
    ): Response<AIRunResponse>

    @POST("/ai/evaluation")
    suspend fun evaluationMission(
        @Body request: AIEvaluationRequest
    ): Response<ResponseBody>
}