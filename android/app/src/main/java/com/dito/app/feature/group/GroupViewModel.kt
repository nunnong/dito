package com.dito.app.feature.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dito.app.core.data.group.GroupInfo
import com.dito.app.core.data.group.Participant
import com.dito.app.core.data.group.RankingItem
import com.dito.app.core.repository.GroupRepository
import com.dito.app.core.storage.GroupManager
import com.dito.app.core.storage.GroupPreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class ChallengeStatus {
    NO_CHALLENGE,        // 그룹에 참여하지 않음 (프론트엔드 전용)
    PENDING,             // 생성했지만 START 전 (백엔드: pending)
    IN_PROGRESS,         // 진행 중 (백엔드: in_progress)
    COMPLETED,           // 종료됨 (백엔드: completed)
    CANCELLED            // 취소됨 (백엔드: cancelled)
}

data class GroupChallengeUiState(
    val isLoading: Boolean = false,
    val showCreateDialog: Boolean = false,
    val showJoinDialog: Boolean = false,
    val showChallengeDialog: Boolean = false,
    val showBetInputDialog: Boolean = false,
    val showSplash: Boolean = false,
    val groupName: String = "",
    val goal: String = "",
    val penalty: String = "",
    val period: Int = 0,
    val bet: Int = 0,
    val entryCode: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val isLeader: Boolean = false,
    val joinedGroupId: Long? = null,
    val joinedGroupName: String = "",
    val joinedGroupGoal: String = "",
    val joinedGroupPenalty: String = "",
    val joinedGroupPeriod: Int = 0,
    val challengeStatus: ChallengeStatus = ChallengeStatus.NO_CHALLENGE,
    val participants: List<Participant> = emptyList(),
    val rankings: List<RankingItem> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class GroupChallengeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val groupManager: GroupManager,
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupChallengeUiState())

    val uiState: StateFlow<GroupChallengeUiState> = _uiState.asStateFlow()

    private var updateRankingJob: Job? = null

    init {
        // 서버에서 최신 그룹 정보 불러오기
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoRefresh()
    }

    private fun loadChallengeState() {
        val savedStatus = groupManager.getChallengeStatus()
        android.util.Log.d("GroupViewModel", "loadChallengeState - savedStatus: $savedStatus")
        android.util.Log.d("GroupViewModel", "loadChallengeState - groupName: ${groupManager.getGroupName()}")
        android.util.Log.d("GroupViewModel", "loadChallengeState - isLeader: ${groupManager.isLeader()}")

        val status = when (savedStatus) {
            GroupManager.STATUS_PENDING -> ChallengeStatus.PENDING
            GroupManager.STATUS_IN_PROGRESS -> ChallengeStatus.IN_PROGRESS
            GroupManager.STATUS_COMPLETED -> ChallengeStatus.COMPLETED
            GroupManager.STATUS_CANCELLED -> ChallengeStatus.CANCELLED
            else -> ChallengeStatus.NO_CHALLENGE
        }

        android.util.Log.d("GroupViewModel", "loadChallengeState - final status: $status")

        _uiState.value = _uiState.value.copy(
            challengeStatus = status,
            groupName = groupManager.getGroupName(),
            goal = groupManager.getGoal(),
            penalty = groupManager.getPenalty(),
            period = groupManager.getPeriod(),
            bet = groupManager.getBet(),
            entryCode = groupManager.getEntryCode(),
            startDate = groupManager.getStartDate(),
            endDate = groupManager.getEndDate(),
            isLeader = groupManager.isLeader()
        )
    }

    /**
     * 서버에서 최신 그룹 정보 불러오기
     * ViewModel 초기화 시 자동 호출됨
     */
    fun refreshGroupInfo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            groupRepository.getGroupDetail().fold(
                onSuccess = { groupDetail ->
                    // 그룹에 참여 중인 경우
                    if (groupDetail.groupId != null && groupDetail.groupName != null) {
                        android.util.Log.d("GroupViewModel", "그룹 정보 불러오기 성공: ${groupDetail.groupName}")

                        val status = when (groupDetail.status) {
                            "pending" -> ChallengeStatus.PENDING
                            "in_progress" -> ChallengeStatus.IN_PROGRESS
                            "completed" -> ChallengeStatus.COMPLETED
                            "cancelled" -> ChallengeStatus.CANCELLED
                            else -> ChallengeStatus.NO_CHALLENGE
                        }

                        // GroupManager에 저장
                        groupManager.saveGroupInfo(
                            groupId = groupDetail.groupId,
                            groupName = groupDetail.groupName,
                            goal = groupDetail.goalDescription ?: "",
                            penalty = groupDetail.penaltyDescription ?: "",
                            period = groupDetail.period ?: 0,
                            bet = groupDetail.betCoin ?: 0,
                            entryCode = groupDetail.inviteCode ?: "",
                            startDate = groupDetail.startDate ?: "",
                            endDate = groupDetail.endDate ?: "",
                            isLeader = groupDetail.isHost ?: false
                        )
                        groupManager.saveChallengeStatus(
                            when (status) {
                                ChallengeStatus.PENDING -> GroupManager.STATUS_PENDING
                                ChallengeStatus.IN_PROGRESS -> GroupManager.STATUS_IN_PROGRESS
                                ChallengeStatus.COMPLETED -> GroupManager.STATUS_COMPLETED
                                ChallengeStatus.CANCELLED -> GroupManager.STATUS_CANCELLED
                                else -> GroupManager.STATUS_NO_CHALLENGE
                            }
                        )

                        // 스크린타임 동기화를 위해 active_group_id 저장
                        GroupPreferenceManager.setActiveGroupId(context, groupDetail.groupId)

                        // UI 상태 업데이트
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            challengeStatus = status,
                            groupName = groupDetail.groupName,
                            goal = groupDetail.goalDescription ?: "",
                            penalty = groupDetail.penaltyDescription ?: "",
                            period = groupDetail.period ?: 0,
                            bet = groupDetail.betCoin ?: 0,
                            entryCode = groupDetail.inviteCode ?: "",
                            startDate = groupDetail.startDate ?: "",
                            endDate = groupDetail.endDate ?: "",
                            isLeader = groupDetail.isHost ?: false
                        )

                        // 참여자 목록 조회
                        loadParticipants(groupDetail.groupId)

                        // 진행 중이면 자동 갱신 시작
                        if (status == ChallengeStatus.IN_PROGRESS) {
//                            startAutoRefresh()
                            loadRanking()
                        }
                    } else {
                        // 그룹에 참여하지 않음
                        android.util.Log.d("GroupViewModel", "참여 중인 그룹 없음")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            challengeStatus = ChallengeStatus.NO_CHALLENGE
                        )
                    }
                },
                onFailure = { error ->
                    android.util.Log.w("GroupViewModel", "그룹 정보 불러오기 실패: ${error.message}")
                    // 로컬에 저장된 정보로 복원 시도
                    loadChallengeState()
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            )
        }
    }

    fun onCreateDialogOpen() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true)
    }

    fun onJoinDialogOpen() {
        _uiState.value = _uiState.value.copy(showJoinDialog = true)
    }

    fun onDialogClose() {
        _uiState.value = _uiState.value.copy(
            showCreateDialog = false,
            showJoinDialog = false,
            showChallengeDialog = false,
            showBetInputDialog = false,
            groupName = ""
        )
    }

    fun onNavigateToChallenge(groupName: String) {
        _uiState.value = _uiState.value.copy(
            groupName = groupName,
            showCreateDialog = false,
            showChallengeDialog = true
        )
    }

    fun onBackToNameDialog() {
        _uiState.value = _uiState.value.copy(
            showChallengeDialog = false,
            showCreateDialog = true
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun onChallengeCreated(groupName: String, goal: String, penalty: String, period: Int, bet: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            groupRepository.createChallenge(
                groupName = groupName,
                goalDescription = goal,
                penaltyDescription = penalty,
                period = period,
                betCoins = bet
            ).fold(
                onSuccess = { response ->
                    // 필수 필드 확인 (id, groupName, inviteCode, period, betCoins)
                    val id = response.id
                    val groupName = response.groupName
                    val inviteCode = response.inviteCode
                    val period = response.period
                    val betCoins = response.betCoins

                    val goalDescription = response.goalDescription ?: ""
                    val penaltyDescription = response.penaltyDescription ?: ""
                    val startDate = response.startDate ?: ""
                    val endDate = response.endDate ?: ""

                    if (id != null && groupName != null && inviteCode != null &&
                        period != null && betCoins != null) {

                        android.util.Log.d("GroupViewModel", "챌린지 생성 성공 - 저장 시작")
                        android.util.Log.d("GroupViewModel", "groupId: $id, groupName: $groupName, isLeader: true")

                        // GroupManager에 저장
                        groupManager.saveGroupInfo(
                            groupId = id,
                            groupName = groupName,
                            goal = goalDescription,
                            penalty = penaltyDescription,
                            period = period,
                            bet = betCoins,
                            entryCode = inviteCode,
                            startDate = startDate,
                            endDate = endDate,
                            isLeader = true
                        )

                        // 스크린타임 동기화를 위해 active_group_id 저장
                        GroupPreferenceManager.setActiveGroupId(context, id)

                        android.util.Log.d("GroupViewModel", "저장 완료 - status: ${groupManager.getChallengeStatus()}")

                        // UI 상태 업데이트
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            challengeStatus = ChallengeStatus.PENDING,
                            groupName = groupName,
                            goal = goalDescription,
                            penalty = penaltyDescription,
                            period = period,
                            bet = betCoins,
                            entryCode = inviteCode,
                            startDate = startDate,
                            endDate = endDate,
                            isLeader = true,
                            showChallengeDialog = false,
                            showJoinDialog = false,
                            showCreateDialog = false,
                            showBetInputDialog = false
                        )
                    } else {
                        // 필수 필드가 누락된 경우
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "서버 응답에 필수 정보가 누락되었습니다"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "챌린지 생성에 실패했습니다"
                    )
                }
            )
        }
    }

    fun onChallengeStarted() {
        val groupId = groupManager.getGroupId()
        if (groupId == 0L) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "그룹 정보를 찾을 수 없습니다"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            groupRepository.startChallenge(groupId).fold(
                onSuccess = {
                    // API 성공 -> 스플래시 표시
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showSplash = true
                    )

                    // 2초 후 상태 변경
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(2000L)
                        groupManager.startChallenge()
                        _uiState.value = _uiState.value.copy(
                            showSplash = false,
                            challengeStatus = ChallengeStatus.IN_PROGRESS
                        )

                        // 챌린지 시작 시 자동 갱신 시작
//                        startAutoRefresh()
                        // 최초 순위 조회
                        loadRanking()
                    }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "챌린지 시작에 실패했습니다"
                    )
                }
            )
        }
    }

    fun onChallengeEnded() {
        groupManager.endChallenge()
        stopAutoRefresh() // 자동 갱신 중단
        _uiState.value = _uiState.value.copy(
            challengeStatus = ChallengeStatus.NO_CHALLENGE,
            groupName = "",
            goal = "",
            penalty = "",
            period = 0,
            bet = 0,
            entryCode = "",
            startDate = "",
            endDate = "",
            participants = emptyList(),
            rankings = emptyList()
        )
    }

    /**
     * 참여자 목록 조회
     */
    fun loadParticipants(groupId: Long) {
        if (groupId == 0L) {
            return
        }

        viewModelScope.launch {
            groupRepository.getParticipants(groupId).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        participants = response.participants
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    /**
     * 초대 코드로 그룹 정보 조회
     */
    fun joinGroupWithCode(inviteCode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            groupRepository.getGroupInfo(inviteCode).fold(
                onSuccess = { response ->
                    val period = response.period

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showJoinDialog = false,
                        showBetInputDialog = true,
                        joinedGroupId = response.groupId,
                        joinedGroupName = response.groupName,
                        joinedGroupGoal = response.goalDescription,
                        joinedGroupPenalty = response.penaltyDescription,
                        joinedGroupPeriod = period
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "그룹 정보 조회에 실패했습니다"
                    )
                }
            )
        }
    }

    /**
     * 배팅 금액 입력 후 그룹 입장
     */
    fun enterGroupWithBet(betCoin: Int) {
        val groupId = _uiState.value.joinedGroupId
        if (groupId == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "그룹 정보를 찾을 수 없습니다"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            groupRepository.enterGroup(groupId, betCoin).fold(
                onSuccess = { response ->
                    // GroupManager에 저장
                    groupManager.saveGroupInfo(
                        groupId = groupId,
                        groupName = _uiState.value.joinedGroupName,
                        goal = _uiState.value.joinedGroupGoal,
                        penalty = _uiState.value.joinedGroupPenalty,
                        period = _uiState.value.joinedGroupPeriod,
                        bet = betCoin,
                        entryCode = "",
                        startDate = "",
                        endDate = "",
                        isLeader = false
                    )

                    // 스크린타임 동기화를 위해 active_group_id 저장
                    GroupPreferenceManager.setActiveGroupId(context, groupId)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showBetInputDialog = false,
                        showJoinDialog = false,
                        showCreateDialog = false,
                        showChallengeDialog = false,
                        challengeStatus = ChallengeStatus.PENDING,
                        groupName = _uiState.value.joinedGroupName,
                        goal = _uiState.value.joinedGroupGoal,
                        penalty = _uiState.value.joinedGroupPenalty,
                        period = _uiState.value.joinedGroupPeriod,
                        bet = betCoin,
                        isLeader = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "그룹 입장에 실패했습니다"
                    )
                }
            )
        }
    }

    /**
     * 5분마다 순위 자동 갱신 시작
     */
//    private fun startAutoRefresh() {
//        stopAutoRefresh() // 기존 작업이 있으면 중단
//
//        updateRankingJob = viewModelScope.launch {
//            while (true) {
//                loadRanking() // 1분마다 순위 조회
//                delay(1 * 60 * 1000L) // 1분 대기
//            }
//        }
//    }

    /**
     * 자동 갱신 중단
     */
    private fun stopAutoRefresh() {
        updateRankingJob?.cancel()
        updateRankingJob = null
    }

    /**
     * 순위 조회
     */
    fun loadRanking() {
        viewModelScope.launch {
            try {
                // GroupManager에서 groupId 가져오기
                val groupId = groupManager.getGroupId()

                if (groupId == 0L) {
                    Log.w("GroupViewModel", "⚠️ groupId가 없어 랭킹 조회 스킵")
                    return@launch
                }

                Log.d("GroupViewModel", "📊 랭킹 조회 시작 - groupId: $groupId")

                val response = groupRepository.getRanking(groupId)

                response.fold(
                    onSuccess = { rankingRes ->
                        _uiState.value = _uiState.value.copy(
                            rankings = rankingRes.rankings
                        )
                        Log.d("GroupViewModel", "✅ 랭킹 조회 성공 - ${rankingRes.rankings.size}명")

                        rankingRes.rankings.forEach { rank ->
                            Log.d("GroupViewModel", "  ${rank.rank}위: ${rank.nickname} - ${rank.totalScreenTimeFormatted}")
                        }
                    },
                    onFailure = { error ->
                        Log.e("GroupViewModel", "❌ 랭킹 조회 실패: ${error.message}", error)
                    }
                )
            } catch (e: Exception) {
                Log.e("GroupViewModel", "❌ 랭킹 조회 예외", e)
            }
        }
    }
}



