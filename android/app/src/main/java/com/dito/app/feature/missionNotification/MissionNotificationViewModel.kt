package com.dito.app.feature.missionNotification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dito.app.core.data.missionNotification.MissionNotificationData
import com.dito.app.core.repository.MissionNotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MissionNotificationUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val notifications: List<MissionNotificationData> = emptyList(),
    val error: String? = null,
    val canPaginate: Boolean = false,
    val currentPage: Int = 0
)

@HiltViewModel
class MissionNotificationViewModel @Inject constructor(
    private val missionNotificationRepository: MissionNotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MissionNotificationUiState())
    val uiState: StateFlow<MissionNotificationUiState> = _uiState.asStateFlow()

    init {
        loadNotifications(isInitialLoad = true)
    }

    fun loadMoreNotifications() {
        if (_uiState.value.canPaginate && !_uiState.value.isLoadingMore) {
            loadNotifications(isInitialLoad = false)
        }
    }

    fun refresh() {
        _uiState.update { it.copy(notifications = emptyList(), currentPage = 0, canPaginate = false) }
        loadNotifications(isInitialLoad = true)
    }

    private fun loadNotifications(isInitialLoad: Boolean) {
        viewModelScope.launch {
            if (isInitialLoad) {
                _uiState.update { it.copy(isLoading = true) }
            } else {
                _uiState.update { it.copy(isLoadingMore = true) }
            }

            val pageToLoad = if (isInitialLoad) 0 else _uiState.value.currentPage + 1

            missionNotificationRepository.getMissionNotifications(page = pageToLoad)
                .onSuccess { response ->
                    _uiState.update { currentState ->
                        val newNotifications =
                            if (isInitialLoad) response.data
                            else currentState.notifications + response.data

                        currentState.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            notifications = newNotifications,
                            canPaginate = response.pageInfo.hasNext,
                            currentPage = response.pageInfo.page,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = error.message ?: "미션 알림을 불러오는데 실패했습니다."
                        )
                    }
                }
        }
    }
}
