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

    /**
     * 미션 ID로 모달 열기 (딥링크용)
     * Evaluation FCM 딥링크를 통해 특정 미션 상세 모달을 자동으로 엽니다.
     *
     * @param missionId 열고자 하는 미션의 ID (Long)
     */
    fun openMissionById(missionId: Long?) {
        if (missionId == null) return

        viewModelScope.launch {
            // 최대 3초 동안 미션이 목록에 나타날 때까지 재시도
            val maxRetries = 10
            var retryCount = 0

            while (retryCount < maxRetries) {
                val mission = _uiState.value.notifications.find { it.id == missionId }

                if (mission != null) {
                    _uiState.update { it.copy(selectedMission = mission) }
                    android.util.Log.d("MissionNotificationVM", "🎯 딥링크로 미션 모달 자동 오픈: ID=$missionId (시도 ${retryCount + 1}회)")
                    return@launch
                }

                if (retryCount == 0) {
                    android.util.Log.d("MissionNotificationVM", "⏳ 미션 로딩 대기 중: ID=$missionId")
                }

                kotlinx.coroutines.delay(300)
                retryCount++
            }

            // 최대 재시도 후에도 찾지 못한 경우
            android.util.Log.w("MissionNotificationVM", "⚠️ 미션을 찾을 수 없음: ID=$missionId (${maxRetries}회 재시도 후)")
            android.util.Log.d("MissionNotificationVM", "   현재 미션 목록: ${_uiState.value.notifications.map { it.id }}")

            // 한 번 더 새로고침 시도
            android.util.Log.d("MissionNotificationVM", "🔄 미션 목록 재로딩 시도")
            refresh()

            // 재로딩 후 한 번 더 찾기
            kotlinx.coroutines.delay(1000)
            val mission = _uiState.value.notifications.find { it.id == missionId }
            if (mission != null) {
                _uiState.update { it.copy(selectedMission = mission) }
                android.util.Log.d("MissionNotificationVM", "✅ 재로딩 후 미션 발견 및 모달 오픈: ID=$missionId")
            }
        }
    }

    fun dismissModal() {
        _uiState.update { it.copy(selectedMission = null) }
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
