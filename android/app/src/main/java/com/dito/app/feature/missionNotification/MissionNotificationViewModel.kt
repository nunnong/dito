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
    val currentPage: Int = 0,
    val selectedMission: MissionNotificationData? = null,  // 상세 모달에 표시할 미션
    val isClaimingReward: Boolean = false  // 레몬 획득 중 로딩 상태
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

    fun onMissionClick(mission: MissionNotificationData) {
        _uiState.update { it.copy(selectedMission = mission) }
    }

    fun openMissionById(missionId: String) {
        val mission = _uiState.value.notifications.find { it.id.toString() == missionId }
        if (mission != null) {
            _uiState.update { it.copy(selectedMission = mission) }
        }
    }

    fun dismissModal() {
        _uiState.update { it.copy(selectedMission = null) }
    }

    /**
     * 미션 ID로 모달 열기 (딥링크용)
     * Evaluation FCM 딥링크를 통해 특정 미션 상세 모달을 자동으로 엽니다.
     *
     * @param missionId 열고자 하는 미션의 ID
     */
    fun openMissionById(missionId: Long?) {
        if (missionId == null) return

        val mission = _uiState.value.notifications.find { it.id == missionId }
        if (mission != null) {
            _uiState.update { it.copy(selectedMission = mission) }
            android.util.Log.d("MissionNotificationVM", "🎯 딥링크로 미션 모달 자동 오픈: ID=$missionId")
        } else {
            android.util.Log.w("MissionNotificationVM", "⚠️ 미션을 찾을 수 없음: ID=$missionId")
        }
    }

    fun onRewardConfirm() {
        // 백엔드에서 이미 자동으로 코인 지급 완료
        // 여기서는 애니메이션 트리거만 하고 모달 닫기
        _uiState.update {
            it.copy(
                isClaimingReward = true  // 애니메이션 트리거
            )
        }

        // 애니메이션 후 모달 닫기
        viewModelScope.launch {
            kotlinx.coroutines.delay(800L)  // 애니메이션 시간
            _uiState.update {
                it.copy(
                    selectedMission = null,
                    isClaimingReward = false
                )
            }
        }
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
