package com.dito.app.core.network

import com.dito.app.core.data.ApiResponse
import com.dito.app.core.data.phone.AppUsageBatchRequest
import com.dito.app.core.data.phone.BatchUploadResponse
import com.dito.app.core.data.phone.MediaSessionBatchRequest
import com.dito.app.core.data.auth.AuthResponse
import com.dito.app.core.data.auth.CheckUsernameResponse
import com.dito.app.core.data.auth.SignInRequest
import com.dito.app.core.data.auth.SignUpRequest
import com.dito.app.core.data.group.CreateGroupRequest
import com.dito.app.core.data.group.CreateGroupResponse
import com.dito.app.core.data.group.EnterGroupRequest
import com.dito.app.core.data.group.EnterGroupResponse
import com.dito.app.core.data.group.GetParticipantsResponse
import com.dito.app.core.data.group.GetRankingResponse
import com.dito.app.core.data.group.JoinGroupRequest
import com.dito.app.core.data.group.JoinGroupResponse
import com.dito.app.core.data.home.HomeResponse
import com.dito.app.core.data.home.UpdateWeeklyGoalRequest
import com.dito.app.core.data.home.UpdateWeeklyGoalResponse
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

    // ========== Home ==========
    @GET("/user/main")
    suspend fun getHomeData(
        @Header("Authorization") token: String
    ): Response<HomeResponse>

    @POST("/weekly-goal")
    suspend fun updateWeeklyGoal(
        @Header("Authorization") token: String,
        @Body request: UpdateWeeklyGoalRequest
    ): Response<UpdateWeeklyGoalResponse>

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

    // 그룹 챌린지 생성
    @POST("/challenges/groups")
    suspend fun createChallenge(
        @Body request: CreateGroupRequest,
        @Header("Authorization") token: String
    ): Response<ApiResponse<CreateGroupResponse>>

    // 그룹 챌린지 시작(방장이 스타트)
    @PUT("/challenges/groups/{group_id}/start")
    suspend fun startChallenge(
        @Path("group_id") groupId: Long,
        @Header("Authorization") token: String
    ): Response<Unit>

    
    // 초대 코드로 입장
    @POST("/challenges/groups/join")
    suspend fun getGroupInfo(
        @Body request: JoinGroupRequest,
        @Header("Authorization") token: String
    ): Response<JoinGroupResponse>

    // 방 정보 확인 후 최종 입장
    @PUT("challenges/groups/{group_id}/start")
    suspend fun joinGroup(
        @Body request: EnterGroupRequest,
        @Header("Authorization") token: String
    ): Response<EnterGroupResponse>

    // 그룹 챌린지 랭킹 조회
    @GET("/challenges/groups/{groups_id}/ranking")
    suspend fun getRanking(
        @Path("group_id") groupId: Long,
        @Header("Authorization") token:String
    ): Response<GetRankingResponse>
    
    // 그룹 챌린지 참여자 목록 조회
    @GET("/challenges/groups/{group_id}/participants")
    suspend fun getGroupParticipants(
        @Path("group_id") groupId: Long,
        @Header("Authorization") token: String
    ): Response<ApiResponse<GetParticipantsResponse>>


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