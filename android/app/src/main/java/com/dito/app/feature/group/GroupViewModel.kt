package com.dito.app.feature.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dito.app.core.data.group.Participant
import com.dito.app.core.repository.GroupRepository
import com.dito.app.core.storage.GroupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ChallengeStatus {
    NO_CHALLENGE,        // 생성 전
    WAITING_TO_START,    // 생성했지만 START 전
    IN_PROGRESS          // START 이후
}

data class GroupChallengeUiState(
    val isLoading: Boolean = false,
    val showCreateDialog: Boolean = false,
    val showJoinDialog: Boolean = false,
    val showChallengeDialog: Boolean = false,
    val showSplash: Boolean = false,
    val groupName: String = "",
    val goal: String = "",
    val penalty: String = "",
    val period: Int = 0,
    val bet: Int = 0,
    val entryCode: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val challengeStatus: ChallengeStatus = ChallengeStatus.NO_CHALLENGE,
    val participants: List<Participant> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class GroupChallengeViewModel @Inject constructor(
    private val groupManager: GroupManager,
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupChallengeUiState())

    val uiState: StateFlow<GroupChallengeUiState> = _uiState.asStateFlow()

    init {
        loadChallengeState()
    }

    private fun loadChallengeState() {
        val status = when (groupManager.getChallengeStatus()) {
            GroupManager.STATUS_WAITING_TO_START -> ChallengeStatus.WAITING_TO_START
            GroupManager.STATUS_IN_PROGRESS -> ChallengeStatus.IN_PROGRESS
            else -> ChallengeStatus.NO_CHALLENGE
        }

        _uiState.value = _uiState.value.copy(
            challengeStatus = status,
            groupName = groupManager.getGroupName(),
            goal = groupManager.getGoal(),
            penalty = groupManager.getPenalty(),
            period = groupManager.getPeriod(),
            bet = groupManager.getBet(),
            entryCode = groupManager.getEntryCode(),
            startDate = groupManager.getStartDate(),
            endDate = groupManager.getEndDate()
        )
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

                    // Nullable 필드 (기본값 사용)
                    val goalDescription = response.goalDescription ?: ""
                    val penaltyDescription = response.penaltyDescription ?: ""
                    val startDate = response.startDate ?: ""
                    val endDate = response.endDate ?: ""

                    if (id != null && groupName != null && inviteCode != null &&
                        period != null && betCoins != null) {

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
                            endDate = endDate
                        )

                        // UI 상태 업데이트
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            challengeStatus = ChallengeStatus.WAITING_TO_START,
                            groupName = groupName,
                            goal = goalDescription,
                            penalty = penaltyDescription,
                            period = period,
                            bet = betCoins,
                            entryCode = inviteCode,
                            startDate = startDate,
                            endDate = endDate,
                            showChallengeDialog = false
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
            participants = emptyList()
        )
    }

    /**
     * 참여자 목록 조회
     */
    fun loadParticipants() {
        val groupId = groupManager.getGroupId()
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
}



