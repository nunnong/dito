package com.dito.app.feature.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dito.app.core.data.health.HealthData
import com.dito.app.core.repository.HealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 건강 데이터 화면 상태
 */
data class HealthUiState(
    val isLoading: Boolean = false,
    val healthData: HealthData? = null,
    val error: String? = null,
    val isHealthConnectAvailable: Boolean = false,
    val hasPermissions: Boolean = false
)

@HiltViewModel
class HealthViewModel @Inject constructor(
    private val healthRepository: HealthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HealthUiState())
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    init {
        checkHealthConnectAvailability()
    }

    /**
     * Health Connect 사용 가능 여부 확인
     */
    private fun checkHealthConnectAvailability() {
        val isAvailable = healthRepository.isHealthConnectAvailable()
        _uiState.value = _uiState.value.copy(isHealthConnectAvailable = isAvailable)
    }

    /**
     * 필요한 권한 목록 가져오기
     */
    fun getRequiredPermissions(): Set<String> {
        return healthRepository.getRequiredPermissions()
    }

    /**
     * 권한 확인
     */
    fun checkPermissions() {
        viewModelScope.launch {
            val hasPermissions = healthRepository.hasAllPermissions()
            _uiState.value = _uiState.value.copy(hasPermissions = hasPermissions)

            if (hasPermissions) {
                loadTodayHealthData()
            }
        }
    }

    /**
     * 오늘 건강 데이터 로드
     */
    fun loadTodayHealthData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            healthRepository.getTodayHealthData()
                .onSuccess { data ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        healthData = data,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
        }
    }

    /**
     * 권한 부여 후 호출
     */
    fun onPermissionsGranted() {
        _uiState.value = _uiState.value.copy(hasPermissions = true)
        loadTodayHealthData()
    }
}
