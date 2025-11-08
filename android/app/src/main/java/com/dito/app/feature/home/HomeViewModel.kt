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
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel(){
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "HomeViewModel"
    }

    init {
        loadHomeData()
    }

    fun loadHomeData(){
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = homeRepository.getHomeData()

            result.onSuccess { homeData ->
                Log.d(TAG, "홈 데이터 로드 성공: $homeData")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        homeData = homeData,
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
}

