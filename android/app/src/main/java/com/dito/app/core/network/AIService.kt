package com.dito.app.core.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AIService {

    @POST("/runs")
    suspend fun sendToAI(
        @Body request: AIRunRequest
    ): Response<AIRunResponse>
}