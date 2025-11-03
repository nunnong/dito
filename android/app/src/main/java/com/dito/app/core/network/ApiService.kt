package com.dito.app.core.network

import com.dito.app.core.data.AppUsageBatchRequest
import com.dito.app.core.data.BatchUploadResponse
import com.dito.app.core.data.MediaSessionBatchRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

//Retrofit API 서비스
interface ApiService {

    @POST("/api/events/app-usage")
    suspend fun uploadAppUsageEvents(
        @Body request: AppUsageBatchRequest
    ): Response<BatchUploadResponse>


    @POST("/api/events/media-session")
    suspend fun uploadMediaSessionEvents(
        @Body request: MediaSessionBatchRequest
    ): Response<BatchUploadResponse>

}