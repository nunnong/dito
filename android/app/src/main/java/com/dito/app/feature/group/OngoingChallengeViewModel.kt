package com.dito.app.feature.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dito.app.core.data.group.RankingItem
import com.dito.app.core.repository.GroupRepository
import com.dito.app.core.storage.GroupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OngoingChallengeUiState(
    val isLoading: Boolean = false,
    val groupName: String = "",
    val goal: String = "",
    val penalty: String = "",
    val period: Int = 0,
    val bet: Int = 0,
    val startDate: String = "",
    val endDate: String = "",
    val rankings: List<RankingItem> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class OngoingChallengeViewModel @Inject constructor(
    private val groupManager: GroupManager,
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OngoingChallengeUiState())
    val uiState: StateFlow<OngoingChallengeUiState> = _uiState.asStateFlow()

    private var autoRefreshJob: Job? = null

    init {
        refreshGroupDetails()
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoRefresh()
    }

    private fun refreshGroupDetails() {
        viewModelScope.launch {
            groupRepository.getGroupDetail().fold(
                onSuccess = { groupDetail ->
                    _uiState.value = _uiState.value.copy(
                        groupName = groupDetail.groupName ?: groupManager.getGroupName(),
                        goal = groupDetail.goalDescription ?: groupManager.getGoal(),
                        penalty = groupDetail.penaltyDescription ?: groupManager.getPenalty(),
                        period = groupDetail.period ?: groupManager.getPeriod(),
                        bet = groupDetail.betCoin ?: groupManager.getBet(),
                        startDate = groupDetail.startDate ?: groupManager.getStartDate(),
                        endDate = groupDetail.endDate ?: groupManager.getEndDate()
                    )
                },
                onFailure = {
                    loadGroupDetailsFromManager()
                }
            )
            loadRanking() 
        }
    }

    private fun loadGroupDetailsFromManager() {
        _uiState.value = _uiState.value.copy(
            groupName = groupManager.getGroupName(),
            goal = groupManager.getGoal(),
            penalty = groupManager.getPenalty(),
            period = groupManager.getPeriod(),
            bet = groupManager.getBet(),
            startDate = groupManager.getStartDate(),
            endDate = groupManager.getEndDate()
        )
    }

    fun startAutoRefresh() {
        stopAutoRefresh()
        autoRefreshJob = viewModelScope.launch {
            while (true) {
                loadRanking()
                delay(10_000L) // 10초마다 갱신
            }
        }
    }

    fun stopAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
    }

    fun loadRanking() {
        val groupId = groupManager.getGroupId()
        if (groupId == 0L) return

        viewModelScope.launch {
            groupRepository.getRanking(groupId).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        rankings = response.rankings
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

    fun pokeMember(targetUserId: Long) {
        val groupId = groupManager.getGroupId()
        if (groupId == 0L) return

        viewModelScope.launch {
            groupRepository.pokeMember(groupId, targetUserId).fold(
                onSuccess = {
                    // 찌르기 성공 UI 피드백 (예: Toast 메시지)
                },
                onFailure = {
                    // 찌르기 실패 UI 피드백
                }
            )
        }
    }
}
