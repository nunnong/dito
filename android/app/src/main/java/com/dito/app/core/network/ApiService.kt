package com.dito.app.core.network

import com.dito.app.core.data.phone.AppUsageBatchRequest
import com.dito.app.core.data.phone.BatchUploadResponse
import com.dito.app.core.data.phone.MediaSessionBatchRequest
import com.dito.app.core.data.auth.AuthResponse
import com.dito.app.core.data.auth.CheckUsernameResponse
import com.dito.app.core.data.auth.SignInRequest
import com.dito.app.core.data.auth.SignUpRequest
import com.dito.app.core.data.group.CreateGroupRequest
import com.dito.app.core.data.group.CreateGroupResponse
import com.dito.app.core.data.group.JoinGroupRequest
import com.dito.app.core.data.group.JoinGroupResponse
import com.dito.app.core.data.settings.UpdateFrequencyRequest
import com.dito.app.core.data.settings.UpdateFrequencyResponse
import com.dito.app.core.data.settings.UpdateNicknameRequest
import com.dito.app.core.data.settings.UpdateNicknameResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

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

    @GET("/auth/check/personal-id")
    suspend fun checkUsername(
        @Query("personalId") username: String
    ): Response<CheckUsernameResponse>

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

    // Group

    @POST("/challenges/groups")
    suspend fun createChallenge(
        @Body request: CreateGroupRequest,
        @Header("Authorization") token: String
    ): Response<CreateGroupResponse>

    @PUT("/challenges/groups/{group_id}/start")
    suspend fun startChallenge(
        @Path("group_id") groupId: Long,
        @Header("Authorization") token: String
    ): Response<Unit>

    @POST("/challenges/groups/join")
    suspend fun joinGroup(
        @Body request: JoinGroupRequest,
        @Header("Authorization") token: String
    ): Response<JoinGroupResponse>

    @GET("/challenges/groups/{group_id}")
    suspend fun getGroupDetail(
        @Path("group_id") groupId: Long,
        @Header("Authorization") token: String
    ): Response<CreateGroupResponse>

    @GET("/challenges/groups/{group_id}/participants")
    suspend fun getGroupParticipants(
        @Path("group_id") groupId: Long,
        @Header("Authorization") token: String
    ): Response<List<String>>


    // Setting

    @POST("/logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<Unit>

    @POST("/sign-out")
    suspend fun signOut(
        @Header("Authorization") token: String
    ): Response<Unit>

    @PATCH("/user") // 닉네임 변경
    suspend fun updateNickname(
        @Body request: UpdateNicknameRequest,
        @Header("Authorization") token: String
    ): Response<UpdateNicknameResponse>

    // 미션 빈도 변경
    @PATCH("/user/frequency")
    suspend fun updateFrequency(
        @Body request: UpdateFrequencyRequest,
        @Header("Authorization") token: String
    ): Response<UpdateFrequencyResponse>



}