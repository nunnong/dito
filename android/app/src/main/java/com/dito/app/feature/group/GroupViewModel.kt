package com.dito.app.feature.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dito.app.core.data.group.Participant
import com.dito.app.core.repository.GroupRepository
import com.dito.app.core.storage.GroupManager
import com.dito.app.core.storage.GroupPreferenceManager
import com.dito.app.core.storage.HomeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    val errorMessage: String? = null,
    val costumeUrl: String? = null,
    val skipRefresh: Boolean = false  // 방금 입장한 경우 refresh 건너뛰기
)

@HiltViewModel
class GroupChallengeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val groupManager: GroupManager,
    private val groupRepository: GroupRepository,
    private val homeManager: HomeManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupChallengeUiState())

    val uiState: StateFlow<GroupChallengeUiState> = _uiState.asStateFlow()

    private var participantsPollingJob: Job? = null

    override fun onCleared() {
        super.onCleared()
        stopParticipantsPolling()
    }

    /**
     * 서버에서 최신 그룹 정보 불러오기
     */
    fun refreshGroupInfo() {
        // 방금 입장한 경우 refresh 건너뛰기
        if (_uiState.value.skipRefresh) {
            _uiState.value = _uiState.value.copy(skipRefresh = false)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            groupRepository.getGroupDetail().fold(
                onSuccess = { groupDetail ->
                    if (groupDetail.groupId != null && groupDetail.groupName != null) {
                        val status = when (groupDetail.status) {
                            "pending" -> ChallengeStatus.PENDING
                            "in_progress" -> ChallengeStatus.IN_PROGRESS
                            "completed" -> ChallengeStatus.COMPLETED
                            "cancelled" -> ChallengeStatus.CANCELLED
                            else -> ChallengeStatus.NO_CHALLENGE
                        }

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

                        GroupPreferenceManager.setActiveGroupId(context, groupDetail.groupId)

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

                        if (status == ChallengeStatus.PENDING) {
                            startParticipantsPolling()
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            challengeStatus = ChallengeStatus.NO_CHALLENGE
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.message)
                }
            )
        }
    }

    fun onChallengeEnded() {
        groupManager.endChallenge()
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
        )
    }
    
    fun onCreateDialogOpen() {
        val costumeUrl = homeManager.getCostumeUrl()
        _uiState.value = _uiState.value.copy(showCreateDialog = true, costumeUrl = costumeUrl)
    }

    fun onJoinDialogOpen() {
        _uiState.value = _uiState.value.copy(showJoinDialog = true, errorMessage = null)
    }

    fun onDialogClose() {
        _uiState.value = _uiState.value.copy(
            showCreateDialog = false,
            showJoinDialog = false,
            showChallengeDialog = false,
            showBetInputDialog = false,
            groupName = "",
            errorMessage = null
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

                        GroupPreferenceManager.setActiveGroupId(context, id)

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
                         startParticipantsPolling()
                    } else {
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
            _uiState.value = _uiState.value.copy(errorMessage = "그룹 정보를 찾을 수 없습니다")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            groupRepository.startChallenge(groupId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, showSplash = true)

                    viewModelScope.launch {
                        delay(2000L)
                        groupManager.startChallenge()
                        _uiState.value = _uiState.value.copy(
                            showSplash = false,
                            challengeStatus = ChallengeStatus.IN_PROGRESS
                        )
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

    fun loadParticipants(groupId: Long) {
        if (groupId == 0L) return

        viewModelScope.launch {
            groupRepository.getParticipants(groupId).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        participants = response.participants
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                }
            )
        }
    }

    fun joinGroupWithCode(inviteCode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            groupRepository.getGroupInfo(inviteCode).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showJoinDialog = false,
                        showBetInputDialog = true,
                        joinedGroupId = response.groupId,
                        joinedGroupName = response.groupName,
                        joinedGroupGoal = response.goalDescription,
                        joinedGroupPenalty = response.penaltyDescription,
                        joinedGroupPeriod = response.period,
                        errorMessage = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        // 다이얼로그는 열린 상태 유지
                        errorMessage = error.message ?: "유효하지 않은 초대 코드입니다"
                    )
                }
            )
        }
    }

    fun enterGroupWithBet(betCoin: Int) {
        val groupId = _uiState.value.joinedGroupId
        if (groupId == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "그룹 정보를 찾을 수 없습니다")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            groupRepository.enterGroup(groupId, betCoin).fold(
                onSuccess = { response ->
                    groupManager.saveGroupInfo(
                        groupId = groupId,
                        groupName = _uiState.value.joinedGroupName,
                        goal = _uiState.value.joinedGroupGoal,
                        penalty = _uiState.value.joinedGroupPenalty,
                        period = _uiState.value.joinedGroupPeriod,
                        bet = betCoin,
                        entryCode = "",
                        startDate = response.startDate,
                        endDate = response.endDate,
                        isLeader = false
                    )

                    GroupPreferenceManager.setActiveGroupId(context, groupId)

                    // 대기방으로 바로 이동
                    groupManager.saveChallengeStatus(GroupManager.STATUS_PENDING)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showBetInputDialog = false,
                        showJoinDialog = false,
                        showSplash = false,
                        skipRefresh = true,
                        challengeStatus = ChallengeStatus.PENDING,
                        groupName = _uiState.value.joinedGroupName,
                        goal = _uiState.value.joinedGroupGoal,
                        penalty = _uiState.value.joinedGroupPenalty,
                        period = _uiState.value.joinedGroupPeriod,
                        bet = betCoin,
                        startDate = response.startDate,
                        endDate = response.endDate,
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

    fun startParticipantsPolling() {
        val groupId = groupManager.getGroupId()
        if (groupId == 0L) return

        stopParticipantsPolling()

        participantsPollingJob = viewModelScope.launch {
            while (true) {
                loadParticipants(groupId)
                refreshGroupInfo() // 그룹 상태 체크 (챌린지 시작 여부 확인)
                delay(1000L) // 1초 대기
            }
        }
    }

    fun stopParticipantsPolling() {
        participantsPollingJob?.cancel()
        participantsPollingJob = null
    }
}
