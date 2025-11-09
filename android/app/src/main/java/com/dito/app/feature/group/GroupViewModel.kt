package com.dito.app.feature.group

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


data class GroupChallengeUiState(
    val isLoading: Boolean = false,
    val showCreateDialog: Boolean = false,
    val showJoinDialog: Boolean = false,
    val showChallengeDialog: Boolean = false,
    val groupName: String = "",
    val errorMessage: String? = null
)

@HiltViewModel

class GroupChallengeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(GroupChallengeUiState())

    val uiState: StateFlow<GroupChallengeUiState> = _uiState.asStateFlow()

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
}



