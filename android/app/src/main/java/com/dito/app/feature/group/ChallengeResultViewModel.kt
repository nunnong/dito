package com.dito.app.feature.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dito.app.core.data.group.GetRankingResponse
import com.dito.app.core.data.group.GroupInfo
import com.dito.app.core.data.group.RankingItem
import com.dito.app.core.repository.GroupRepository
import com.dito.app.core.storage.GroupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChallengeResultUiState(
    val isLoading: Boolean = false,
    val groupInfo: GroupInfo? = null,
    val rankings: List<RankingItem> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class ChallengeResultViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val groupManager: GroupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengeResultUiState())
    val uiState: StateFlow<ChallengeResultUiState> = _uiState.asStateFlow()

    init {
        loadRanking()
    }

    fun loadRanking() {
        val groupId = groupManager.getGroupId()
        if (groupId == 0L) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "그룹 정보를 찾을 수 없습니다"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            groupRepository.getRanking(groupId).fold(
                onSuccess = { ranking ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        groupInfo = ranking.groupInfo,
                        rankings = ranking.rankings,
                        errorMessage = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "랭킹 조회에 실패했습니다"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
