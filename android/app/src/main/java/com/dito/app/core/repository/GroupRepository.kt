package com.dito.app.core.repository

import android.util.Log
import com.dito.app.core.data.group.CreateGroupRequest
import com.dito.app.core.data.group.CreateGroupResponse
import com.dito.app.core.data.group.GetParticipantsResponse
import com.dito.app.core.network.ApiService
import com.dito.app.core.storage.AuthTokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(

    private val apiService: ApiService,
    private val authTokenManager: AuthTokenManager,
) {
    companion object {
        private const val TAG = "GroupRepository"
    }
    suspend fun createChallenge(
        groupName: String,
        goalDescription: String,
        penaltyDescription: String,
        period: Int,
        betCoins: Int
    ): Result<CreateGroupResponse> = withContext(Dispatchers.IO) {
        try {
            val token = authTokenManager.getAccessToken()
            if (token.isNullOrEmpty()) {
                Log.e(TAG, "챌린지 생성 실패: 토큰 없음")
                return@withContext Result.failure(Exception("로그인이 필요합니다"))
            }

            val request = CreateGroupRequest(
                groupName = groupName,
                goalDescription = goalDescription,
                penaltyDescription = penaltyDescription,
                period = period,
                betCoins = betCoins
            )

            Log.d(TAG, "챌린지 생성 시도: request=$request")

            val response = apiService.createChallenge(request, "Bearer $token")

            Log.d(TAG, "응답 코드: ${response.code()}")
            Log.d(TAG, "응답 성공 여부: ${response.isSuccessful}")

            // 응답 본문 로깅 추가
            if (response.isSuccessful) {
                Log.d(TAG, "응답 본문: ${response.body()}")
            } else {
                Log.e(TAG, "에러 응답: ${response.errorBody()?.string()}")
            }

            if (response.isSuccessful && response.body() != null) {
                val groupData = response.body()!!
                Log.d(TAG, "챌린지 생성 성공: id=${groupData.id}, inviteCode=${groupData.inviteCode}")
                Result.success(groupData)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = "챌린지 생성 실패: $errorBody"
                Log.e(TAG, "챌린지 생성 실패: code=${response.code()}, body=${response.message()}")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "챌린지 생성 예외", e)
            Result.failure(e)
        }
    }



    // 그룹 챌린지 랭킹 조회


    // 그룹 챌린지 참여자 목록 조회
    suspend fun getParticipants(groupId: Long): Result<GetParticipantsResponse> = withContext(Dispatchers.IO) {
        try {
            val token = authTokenManager.getAccessToken()
            if (token.isNullOrEmpty()) {
                Log.e(TAG, "참여자 목록 조회 실패: 토큰 없음")
                return@withContext Result.failure(Exception("로그인이 필요합니다"))
            }

            Log.d(TAG, "참여자 목록 조회 시도: groupId=$groupId")

            val response = apiService.getGroupParticipants(groupId, "Bearer $token")

            if (response.isSuccessful && response.body() != null) {
                val participants = response.body()!!
                Log.d(TAG, "참여자 목록 조회 성공: count=${participants.count}")
                Result.success(participants)
            } else {
                val errorMessage = "참여자 목록 조회 실패"
                Log.e(TAG, "참여자 목록 조회 실패: code=${response.code()}")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "참여자 목록 조회 예외", e)
            Result.failure(e)
        }
    }

}