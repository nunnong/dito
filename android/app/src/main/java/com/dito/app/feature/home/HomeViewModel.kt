package com.dito.app.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dito.app.core.data.home.HomeData
import com.dito.app.core.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val homeRepository: HomeRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "HomeViewModel"
    }

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = homeRepository.getHomeData()

            result.onSuccess { homeData ->
                Log.d(TAG, "홈 데이터 로드 성공: $homeData")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        homeData = homeData,
                        weeklyGoalInput = homeData.weeklyGoal ?: "",
                        errorMessage = null
                    )
                }
            }.onFailure { exception ->
                Log.e(TAG, "홈 데이터 로드 실패: ${exception.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        homeData = null,
                        errorMessage = exception.message ?: "데이터를 불러올 수 없습니다"
                    )
                }
            }
        }
    }

    // 주간 목표 편집 시작 (weeklyGoal이 null일때만 가능)
    fun startEditingWeeklyGoal() {
        _uiState.update { currentState ->
            currentState.copy(
                isEditingWeeklyGoal = true,
                weeklyGoalInput = ""
            )
        }
        Log.d(TAG, "주간 목표 편집 모드 시작")
    }

    // 주간 목표 입력 변경 (TextField에 타이핑 시)
    fun onWeeklyGoalInputChange(input: String) {
        _uiState.update { it.copy(weeklyGoalInput = input) }
    }

    // 주간 목표 저장 (체크 아이콘 클릭 시)
    fun saveWeeklyGoal() {
        val goalText = _uiState.value.weeklyGoalInput.trim()

        if (goalText.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "주간 목표를 입력해주세요") }
            Log.w(TAG, "주간 목표가 비어있음")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            Log.d(TAG, "주간 목표 저장 시작: $goalText")

            homeRepository.updateWeeklyGoal(goalText)
                .onSuccess {
                    Log.d(TAG, "주간 목표 저장 성공")
                    // 성공 시 homeData 업데이트 및 편집 모드 종료
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            isEditingWeeklyGoal = false,
                            homeData = currentState.homeData?.copy(weeklyGoal = goalText),
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "주간 목표 저장 실패: ${error.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "주간 목표 저장에 실패했습니다"
                        )
                    }
                }
        }
    }
}