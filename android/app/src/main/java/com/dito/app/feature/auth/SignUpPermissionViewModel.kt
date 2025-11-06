package com.dito.app.feature.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import com.dito.app.core.util.PermissionHelper
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

    /**
     * 현재 권한 상태를 확인하여 UI 상태 업데이트
     */
    fun checkPermissions(context: Context) {
        _uiState.update {
            it.copy(
                accessibilityPermission = PermissionHelper.isAccessibilityPermissionGranted(context),
                usageStatsPermission = PermissionHelper.isUsageStatsPermissionGranted(context),
                notificationPermission = PermissionHelper.isNotificationPermissionGranted(context)
            )
        }
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
