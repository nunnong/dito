package com.dito.app.feature.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SignUpPermissionUiState(
    val accessibilityPermission: Boolean = false,
    val usageStatsPermission: Boolean = false,
    val notificationPermission: Boolean = false,
    val navigateToNext: Boolean = false
)

@HiltViewModel
class SignUpPermissionViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpPermissionUiState())
    val uiState: StateFlow<SignUpPermissionUiState> = _uiState.asStateFlow()

    fun onAccessibilityPermissionChange(granted: Boolean) {
        _uiState.update { it.copy(accessibilityPermission = granted) }
    }

    fun onUsageStatsPermissionChange(granted: Boolean) {
        _uiState.update { it.copy(usageStatsPermission = granted) }
    }

    fun onNotificationPermissionChange(granted: Boolean) {
        _uiState.update { it.copy(notificationPermission = granted) }
    }

    fun onLetsStartClicked() {
        if (allPermissionsGranted()) {
            _uiState.update { it.copy(navigateToNext = true) }
        }
    }

    fun onNavigated() {
        _uiState.update { it.copy(navigateToNext = false) }
    }

    private fun allPermissionsGranted(): Boolean {
        val currentState = _uiState.value
        return currentState.accessibilityPermission &&
                currentState.usageStatsPermission &&
                currentState.notificationPermission
    }
}
