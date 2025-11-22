package com.dito.app.core.repository

import android.util.Log
import com.dito.app.core.data.group.CreateGroupRequest
import com.dito.app.core.data.group.CreateGroupResponse
import com.dito.app.core.data.group.EnterGroupRequest
import com.dito.app.core.data.group.EnterGroupResponse
import com.dito.app.core.data.group.GetGroupDetailResponse
import com.dito.app.core.data.group.GetParticipantsResponse
import com.dito.app.core.data.group.GetRankingResponse
import com.dito.app.core.data.group.JoinGroupRequest
import com.dito.app.core.data.group.JoinGroupResponse
import com.dito.app.core.data.group.PokeRequest
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

            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                val groupData = apiResponse.data
                    ?: return@withContext Result.failure(
                        Exception("서버 응답에 data 필드가 없습니다. (message=${apiResponse.message})")
                    )
                Log.d(TAG, "챌린지 생성 성공: id=${groupData.id}, inviteCode=${groupData.inviteCode}")
                Log.d(TAG, "파싱된 응답 본문: $groupData")
                Result.success(groupData)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = "챌린지 생성 실패: $errorBody"
                Log.e(
                    TAG,
                    "챌린지 생성 실패: code=${response.code()}, message=${response.message()}, errorBody=$errorBody"
                )
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "챌린지 생성 예외", e)
            Result.failure(e)
        }
    }

    // 초대 코드로 그룹 정보 조회
    suspend fun getGroupInfo(inviteCode: String): Result<JoinGroupResponse> =
        withContext(Dispatchers.IO) {
            try {
                val token = authTokenManager.getAccessToken()
                if (token.isNullOrEmpty()) {
                    Log.e(TAG, "그룹 정보 조회 실패: 토큰 없음")
                    return@withContext Result.failure(Exception("로그인이 필요합니다"))
                }

                val request = JoinGroupRequest(inviteCode = inviteCode)

                Log.d(TAG, "그룹 정보 조회 시도: inviteCode=$inviteCode")

                val response = apiService.getGroupInfo(request, "Bearer $token")

                Log.d(TAG, "응답 코드: ${response.code()}")
                Log.d(TAG, "응답 성공 여부: ${response.isSuccessful}")

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    val groupData = apiResponse.data
                        ?: return@withContext Result.failure(
                            Exception("서버 응답에 data 필드가 없습니다. (message=${apiResponse.message})")
                        )
                    Log.d(
                        TAG,
                        "그룹 정보 조회 성공: groupId=${groupData.groupId}, groupName=${groupData.groupName}"
                    )
                    Log.d(TAG, "파싱된 응답 본문: $groupData")
                    Result.success(groupData)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        // JSON에서 message 필드 추출
                        val jsonObject = org.json.JSONObject(errorBody ?: "")
                        jsonObject.optString("message", "유효하지 않은 초대 코드입니다")
                    } catch (e: Exception) {
                        "유효하지 않은 초대 코드입니다"
                    }
                    Log.e(
                        TAG,
                        "그룹 정보 조회 실패: code=${response.code()}, message=${response.message()}, errorBody=$errorBody"
                    )
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Log.e(TAG, "그룹 정보 조회 예외", e)
                Result.failure(e)
            }
        }

    // 배팅 금액 입력하고 최종 입장
    suspend fun enterGroup(groupId: Long, betCoin: Int): Result<EnterGroupResponse> =
        withContext(Dispatchers.IO) {
            try {
                val token = authTokenManager.getAccessToken()
                if (token.isNullOrEmpty()) {
                    Log.e(TAG, "그룹 입장 실패: 토큰 없음")
                    return@withContext Result.failure(Exception("로그인이 필요합니다"))
                }

                val request = EnterGroupRequest(groupId = groupId, betCoin = betCoin)

                Log.d(TAG, "그룹 입장 시도: groupId=$groupId, betCoin=$betCoin")

                val response = apiService.joinGroup(request, "Bearer $token")

                Log.d(TAG, "응답 코드: ${response.code()}")
                Log.d(TAG, "응답 성공 여부: ${response.isSuccessful}")

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    val enterData = apiResponse.data
                    if (enterData != null) {
                        Log.d(TAG, "그룹 입장 성공: groupId=${enterData.groupId}, status=${enterData.status}")
                        Log.d(TAG, "파싱된 응답 본문: $enterData")
                        Result.success(enterData)
                    } else {
                        // 서버가 data 필드 없이 성공 응답을 보낸 경우 기본값으로 처리
                        Log.d(TAG, "그룹 입장 성공 (data 필드 없음): message=${apiResponse.message}")
                        Result.success(EnterGroupResponse(
                            groupId = groupId,
                            status = "pending",
                            startDate = "",
                            endDate = "",
                            totalBetCoins = betCoin
                        ))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = "그룹 입장 실패: $errorBody"
                    Log.e(
                        TAG,
                        "그룹 입장 실패: code=${response.code()}, message=${response.message()}, errorBody=$errorBody"
                    )
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Log.e(TAG, "그룹 입장 예외", e)
                Result.failure(e)
            }
        }


    // 그룹 챌린지 시작 (방장이 START 버튼 클릭)
    suspend fun startChallenge(groupId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = authTokenManager.getAccessToken()
            if (token.isNullOrEmpty()) {
                Log.e(TAG, "챌린지 시작 실패: 토큰 없음")
                return@withContext Result.failure(Exception("로그인이 필요합니다"))
            }

            Log.d(TAG, "챌린지 시작 시도: groupId=$groupId")

            val response = apiService.startChallenge(groupId, "Bearer $token")

            if (response.isSuccessful) {
                Log.d(TAG, "챌린지 시작 성공: groupId=$groupId")
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = "챌린지 시작 실패: $errorBody"
                Log.e(TAG, "챌린지 시작 실패: code=${response.code()}, errorBody=$errorBody")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "챌린지 시작 예외", e)
            Result.failure(e)
        }
    }

    // 그룹 챌린지 랭킹 조회
    suspend fun getRanking(groupId: Long): Result<GetRankingResponse> =
        withContext(Dispatchers.IO) {
            try {
                val token = authTokenManager.getAccessToken()
                if (token.isNullOrEmpty()) {
                    Log.e(TAG, "랭킹 조회 실패: 토큰 없음")
                    return@withContext Result.failure(Exception("로그인이 필요합니다"))
                }

                Log.d(TAG, "랭킹 조회 시도: groupId=$groupId")

                val response = apiService.getRanking(groupId, "Bearer $token")
                Log.d(TAG, " ${authTokenManager.getAccessToken()}")

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    val ranking = apiResponse.data
                        ?: return@withContext Result.failure(
                            Exception("서버 응답에 data 필드가 없습니다. (message=${apiResponse.message})")
                        )
                    Log.d(
                        TAG,
                        "랭킹 조회 성공: rankings=${ranking.rankings.size}개"
                    )
                    Result.success(ranking)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = "랭킹 조회 실패: $errorBody"
                    Log.e(TAG, "랭킹 조회 실패: code=${response.code()}, errorBody=$errorBody")
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Log.e(TAG, "랭킹 조회 예외", e)
                Result.failure(e)
            }
        }

    /**
     * 소속 그룹 상세 조회
     */
    suspend fun getGroupDetail(): Result<GetGroupDetailResponse> = withContext(Dispatchers.IO) {
        try {
            val token = authTokenManager.getAccessToken()
            if (token.isNullOrEmpty()) {
                Log.e(TAG, "그룹 상세 조회 실패: 토큰 없음")
                return@withContext Result.failure(Exception("로그인이 필요합니다"))
            }

            Log.d(TAG, "그룹 상세 조회 시작")
            val response = apiService.getGroupDetail("Bearer $token")

            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                val groupDetail = apiResponse.data
                    ?: return@withContext Result.failure(
                        Exception("서버 응답에 data 필드가 없습니다. (message=${apiResponse.message})")
                    )

                Log.d(
                    TAG,
                    "그룹 상세 조회 성공: groupId=${groupDetail.groupId}, groupName=${groupDetail.groupName}"
                )
                Result.success(groupDetail)
            } else {
                val errorMessage = "그룹 상세정보 조회 실패: code=${response.code()}"
                Log.e(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "그룹 상세정보 조회 예외", e)
            Result.failure(e)
        }
    }

    // 그룹 챌린지 참여자 목록 조회
    suspend fun getParticipants(groupId: Long): Result<GetParticipantsResponse> =
        withContext(Dispatchers.IO) {
            try {
                val token = authTokenManager.getAccessToken()
                if (token.isNullOrEmpty()) {
                    Log.e(TAG, "참여자 목록 조회 실패: 토큰 없음")
                    return@withContext Result.failure(Exception("로그인이 필요합니다"))
                }

                Log.d(TAG, "참여자 목록 조회 시도: groupId=$groupId")

                val response = apiService.getGroupParticipants(groupId, "Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    val participants = apiResponse.data
                        ?: return@withContext Result.failure(
                            Exception("서버 응답에 data 필드가 없습니다. (message=${apiResponse.message})")
                        )
                    Log.d(
                        TAG,
                        "참여자 목록 조회 성공: count=${participants.count}, participants=${participants.participants.size}개"
                    )
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

    // 콕콕 찌르기
    suspend fun pokeMember(groupId: Long, targetUserId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = authTokenManager.getAccessToken()
            if (token.isNullOrEmpty()) {
                Log.e(TAG, "찌르기 실패: 토큰 없음")
                return@withContext Result.failure(Exception("로그인이 필요합니다"))
            }
            
            val request = PokeRequest(targetUserId = targetUserId)

            val response = apiService.pokeMember(groupId, request, "Bearer $token")

            if (response.isSuccessful) {
                Result.success(Unit)

            } else {
                val msg = response.errorBody()?.string() ?: "찌르기 실패"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
