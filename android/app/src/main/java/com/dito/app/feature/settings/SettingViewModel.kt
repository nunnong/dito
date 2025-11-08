package com.dito.app.feature.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dito.app.core.repository.SettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingUiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val settingRepository: SettingRepository
) : ViewModel() {

    companion object {
        private const val TAG = "SettingViewModel"
    }

    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState: StateFlow<SettingUiState> = _uiState.asStateFlow()

    /**
     * 닉네임 변경
     */
    fun updateNickname(nickname: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)

            settingRepository.updateNickname(nickname).fold(
                onSuccess = { message ->
                    Log.d(TAG, "닉네임 변경 성공: $message")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = message
                    )
                    onSuccess()
                },
                onFailure = { error ->
                    Log.e(TAG, "닉네임 변경 실패: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "닉네임 변경에 실패했습니다"
                    )
                }
            )
        }
    }

    /**
     * 미션 빈도 변경
     */
    fun updateFrequency(frequency: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)

            settingRepository.updateFrequency(frequency).fold(
                onSuccess = { message ->
                    Log.d(TAG, "미션 빈도 변경 성공: $message")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = message
                    )
                    onSuccess()
                },
                onFailure = { error ->
                    Log.e(TAG, "미션 빈도 변경 실패: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "미션 빈도 변경에 실패했습니다"
                    )
                }
            )
        }
    }

    /**
     * 저장된 미션 빈도 조회
     */
    fun getFrequency(): String {
        return settingRepository.getFrequency()
    }
}
