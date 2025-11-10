package com.dito.app.feature.group

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class GroupNameUiState(
    val isLoading: Boolean = false,
    val showCreateInfoDialog: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class GroupNameVIewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(GroupChallengeUiState())

    val uiState: StateFlow<GroupChallengeUiState> = _uiState.asStateFlow()


    fun onCreateInfoOpen() {
        _uiState.value = _uiState.value.copy()
    }

    fun onCreateInfoClose() {
        _uiState.value = _uiState.value.copy(
        )
    }


}