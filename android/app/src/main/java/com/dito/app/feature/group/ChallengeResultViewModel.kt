package com.dito.app.feature.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dito.app.core.data.group.GetRankingResponse
import com.dito.app.core.repository.GroupRepository
import com.dito.app.core.storage.GroupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChallengeResultViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val groupManager: GroupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengeResultUiState())
    val uiState = _uiState.asStateFlow()

    fun fetchChallengeResult() {
        viewModelScope.launch {
            val groupId = groupManager.getGroupId()
            if (groupId != 0L) {
                groupRepository.getRanking(groupId).onSuccess {
                    _uiState.value = _uiState.value.copy(result = it)
                }
            }
            _uiState.value = _uiState.value.copy(
                groupName = groupManager.getGroupName(),
                startDate = groupManager.getStartDate(),
                endDate = groupManager.getEndDate(),
                totalBetCoins = groupManager.getBet(),
                penaltyDescription = groupManager.getPenalty()
            )
        }
    }
}

data class ChallengeResultUiState(
    val groupName: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val totalBetCoins: Int = 0,
    val penaltyDescription: String = "",
    val result: GetRankingResponse? = null
)
