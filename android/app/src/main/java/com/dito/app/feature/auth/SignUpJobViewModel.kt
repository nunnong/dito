package com.dito.app.feature.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SignUpJobUiState(
    val selectedJob: String = "",
    val navigateToNext: Boolean = false
)

@HiltViewModel
class SignUpJobViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpJobUiState())
    val uiState: StateFlow<SignUpJobUiState> = _uiState.asStateFlow()

    fun onJobSelect(job: String) {
        _uiState.update { it.copy(selectedJob = job) }
    }

    fun onNextClicked() {
        if (_uiState.value.selectedJob.isNotBlank()) {
            _uiState.update { it.copy(navigateToNext = true) }
        }
    }

    fun onNavigated() {
        _uiState.update { it.copy(navigateToNext = false) }
    }
}
