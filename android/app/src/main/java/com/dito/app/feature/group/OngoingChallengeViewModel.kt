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
    val totalBetting: Int = 0,
    val startDate: String = "",
    val endDate: String = "",
    val rankings: List<RankingItem> = emptyList(),
    val initialUserOrder: List<Long> = emptyList(),  // 처음 위치 순서 (userId)
    val errorMessage: String? = null,
    val pokedUserIds: Set<Long> = emptySet()  // 찔린 사용자 ID들
)

@HiltViewModel
class OngoingChallengeViewModel @Inject constructor(
    private val groupManager: GroupManager,
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OngoingChallengeUiState())
    val uiState: StateFlow<OngoingChallengeUiState> = _uiState.asStateFlow()

    private var autoRefreshJob: Job? = null
    private val pokeBubbleJobs = mutableMapOf<Long, Job>()

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
                    // totalBetCoin을 GroupManager에 저장
                    groupDetail.totalBetCoin?.let { groupManager.saveTotalBet(it) }

                    _uiState.value = _uiState.value.copy(
                        groupName = groupDetail.groupName ?: groupManager.getGroupName(),
                        goal = groupDetail.goalDescription ?: groupManager.getGoal(),
                        penalty = groupDetail.penaltyDescription ?: groupManager.getPenalty(),
                        period = groupDetail.period ?: groupManager.getPeriod(),
                        bet = groupDetail.betCoin ?: groupManager.getBet(),
                        totalBetting = groupDetail.totalBetCoin ?: groupManager.getTotalBet(),
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
            totalBetting = groupManager.getTotalBet(),
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
                    val currentOrder = _uiState.value.initialUserOrder

                    // 처음 랭킹을 받았을 때만 초기 순서 저장
                    val initialOrder = if (currentOrder.isEmpty()) {
                        response.rankings.take(4).map { it.userId }
                    } else {
                        currentOrder
                    }

                    _uiState.value = _uiState.value.copy(
                        rankings = response.rankings,
                        initialUserOrder = initialOrder
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
                    // 해당 사용자의 이전 타이머가 있으면 취소
                    pokeBubbleJobs[targetUserId]?.cancel()

                    // 찌르기 성공 - 해당 캐릭터 머리 위에 말풍선 표시
                    val currentPokedIds = _uiState.value.pokedUserIds.toMutableSet()
                    currentPokedIds.add(targetUserId)
                    _uiState.value = _uiState.value.copy(pokedUserIds = currentPokedIds)

                    // 해당 사용자별로 독립적인 타이머 시작
                    pokeBubbleJobs[targetUserId] = viewModelScope.launch {
                        delay(1000L)
                        val updatedPokedIds = _uiState.value.pokedUserIds.toMutableSet()
                        updatedPokedIds.remove(targetUserId)
                        _uiState.value = _uiState.value.copy(pokedUserIds = updatedPokedIds)
                        pokeBubbleJobs.remove(targetUserId)
                    }
                },
                onFailure = {
                    // 찌르기 실패 UI 피드백
                }
            )
        }
    }

    fun resetPokeBubble() {
        pokeBubbleJobs.values.forEach { it.cancel() }
        pokeBubbleJobs.clear()
        _uiState.value = _uiState.value.copy(pokedUserIds = emptySet())
    }

    // 테스트용: 랭킹 셔플
    fun shuffleRankingsForTest() {
        val currentRankings = _uiState.value.rankings.toMutableList()
        if (currentRankings.size >= 2) {
            currentRankings.shuffle()
            val updatedRankings = currentRankings.mapIndexed { index, item ->
                item.copy(rank = index + 1)
            }
            _uiState.value = _uiState.value.copy(rankings = updatedRankings)
        }
    }
}
