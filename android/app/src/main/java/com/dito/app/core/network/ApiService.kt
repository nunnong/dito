package com.dito.app.core.network

import com.dito.app.core.data.phone.AppUsageBatchRequest
import com.dito.app.core.data.phone.BatchUploadResponse
import com.dito.app.core.data.phone.MediaSessionBatchRequest
import com.dito.app.core.data.auth.AuthResponse
import com.dito.app.core.data.auth.SignInRequest
import com.dito.app.core.data.auth.SignUpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

//Retrofit API 서비스
interface ApiService {

    // ========== Auth ==========
    @POST("/auth/sign-in")
    suspend fun signIn(
        @Body request: SignInRequest
    ): Response<AuthResponse>

    @POST("/auth/sign-up")
    suspend fun signUp(
        @Body request: SignUpRequest
    ): Response<AuthResponse>

    // ========== Events ==========
    @POST("/event/app-usage")
    suspend fun uploadAppUsageEvents(
        @Body request: AppUsageBatchRequest,
        @Header("Authorization") token: String
    ): Response<BatchUploadResponse>

    @POST("/event/media-session")
    suspend fun uploadMediaSessionEvents(
        @Body request: MediaSessionBatchRequest,
        @Header("Authorization") token: String
    ): Response<BatchUploadResponse>

}