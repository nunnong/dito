package com.dito.app.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dito.app.core.data.home.HomeData
import com.dito.app.core.repository.HomeRepository
import com.dito.app.core.storage.HomeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Home UI 상태
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val homeData: HomeData? = null,
    val errorMessage: String? = null,
    val isEditingWeeklyGoal: Boolean = false,
    val weeklyGoalInput: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val homeManager: HomeManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "HomeViewModel"
    }

    init {
        // HomeManager의 데이터를 구독하여 UI 상태 업데이트
        homeManager.homeData.onEach { homeData ->
            _uiState.update {
                it.copy(
                    homeData = homeData,
                    weeklyGoalInput = homeData?.weeklyGoal ?: ""
                )
            }
        }.launchIn(viewModelScope)

        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = homeRepository.getHomeData()

            result.onSuccess { homeData ->
                Log.d(TAG, "서버 홈 데이터 로드 성공: $homeData")
                homeManager.saveHomeData(homeData) // Manager를 통해 저장
                _uiState.update { it.copy(isLoading = false, errorMessage = null) }
            }.onFailure { exception ->
                Log.e(TAG, "서버 홈 데이터 로드 실패: ${exception.message}")
                if (homeManager.homeData.value == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "데이터를 불러올 수 없습니다"
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    // 주간 목표 편집 시작
    fun startEditingWeeklyGoal() {
        _uiState.update { it.copy(isEditingWeeklyGoal = true, weeklyGoalInput = "") }
        Log.d(TAG, "주간 목표 편집 모드 시작")
    }

    // 주간 목표 입력 변경
    fun onWeeklyGoalInputChange(input: String) {
        _uiState.update { it.copy(weeklyGoalInput = input) }
    }

    // 주간 목표 저장
    fun saveWeeklyGoal() {
        val goalText = _uiState.value.weeklyGoalInput.trim()
        if (goalText.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "주간 목표를 입력해주세요") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            homeRepository.updateWeeklyGoal(goalText).onSuccess {
                Log.d(TAG, "주간 목표 저장 성공")
                val updatedHomeData = homeManager.homeData.value?.copy(weeklyGoal = goalText)
                if (updatedHomeData != null) {
                    homeManager.saveHomeData(updatedHomeData) // Manager를 통해 저장
                }
                _uiState.update { it.copy(isLoading = false, isEditingWeeklyGoal = false) }
            }.onFailure { error ->
                Log.e(TAG, "주간 목표 저장 실패: ${error.message}")
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }
}
