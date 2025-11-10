package com.dito.app.feature.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dito.app.core.repository.AuthRepository
import com.dito.app.core.util.PermissionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignUpPermissionUiState(
    val accessibilityPermission: Boolean = false,
    val usageStatsPermission: Boolean = false,
    val notificationPermission: Boolean = false,
    val notificationListenerPermission: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val navigateToNext: Boolean = false
)

@HiltViewModel
class SignUpPermissionViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

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
                notificationPermission = PermissionHelper.isNotificationPermissionGranted(context),
                notificationListenerPermission = PermissionHelper.isNotificationListenerPermissionGranted(context)
            )
        }
    }

    fun onLetsStartClicked(
        username: String,
        password: String,
        nickname: String,
        birthYear: Int,
        birthMonth: Int,
        birthDay: Int,
        gender: String,
        job: String
    ) {
        if (!allPermissionsGranted()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 생년월일을 yyyy-MM-dd 형식으로 변환
            val birth = String.format("%04d-%02d-%02d", birthYear, birthMonth, birthDay)

            val result = authRepository.signUp(
                username = username,
                password = password,
                nickname = nickname,
                birth = birth,
                gender = gender,
                job = job
            )

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, navigateToNext = true) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "회원가입에 실패했습니다"
                        )
                    }
                }
            )
        }
    }

    fun onNavigated() {
        _uiState.update { it.copy(navigateToNext = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun allPermissionsGranted(): Boolean {
        val currentState = _uiState.value
        return currentState.accessibilityPermission &&
                currentState.usageStatsPermission &&
                currentState.notificationPermission &&
                currentState.notificationListenerPermission
    }
}
