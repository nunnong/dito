package com.dito.app.feature.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dito.app.core.data.report.ComparisonItem
import com.dito.app.core.data.report.ComparisonType
import com.dito.app.core.data.report.DailyReportData
import com.dito.app.core.data.report.RadarChartData
import com.dito.app.core.data.report.StatusDescription
import com.dito.app.core.network.ApiService
import com.dito.app.core.repository.HomeRepository
import com.dito.app.core.storage.AuthTokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DailyReportUiState {
    data object Loading : DailyReportUiState()
    data class Success(val data: DailyReportData) : DailyReportUiState()
    data class Error(val message: String) : DailyReportUiState()
}

@HiltViewModel
class DailyReportViewModel @Inject constructor(
    private val apiService: ApiService,
    private val authTokenManager: AuthTokenManager,
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DailyReportUiState>(DailyReportUiState.Loading)
    val uiState: StateFlow<DailyReportUiState> = _uiState.asStateFlow()

    private val _isPolling = MutableStateFlow(false)
    val isPolling: StateFlow<Boolean> = _isPolling.asStateFlow()

    private var reportPollingJob: Job? = null

    fun loadDailyReport() {
        viewModelScope.launch {
            val isPolling = reportPollingJob?.isActive == true

            try {
                // HomeRepository에서 사용자 정보 가져오기
                val homeResult = homeRepository.getHomeData()
                val homeData = homeResult.getOrNull()
                val userName = homeData?.nickname ?: "디토"
                val costumeUrl = homeData?.costumeUrl ?: ""

                // 토큰 가져오기
                val token = authTokenManager.getBearerToken()
                if (token == null) {
                    _uiState.value = DailyReportUiState.Error("로그인이 필요합니다")
                    return@launch
                }

                // API 호출
                val response = apiService.getDailyReport(token)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.data != null) {
                        val reportData = apiResponse.data

                        // status를 먼저 확인
                        if (reportData.status == "COMPLETED") {
                            // COMPLETED 상태이고 데이터가 모두 있을 때만 UI 업데이트
                            if (reportData.reportOverview != null &&
                                reportData.insights != null &&
                                reportData.advice != null &&
                                reportData.missionSuccessRate != null) {

                                val predictions = reportData.reportOverview.split("\n").filter { it.isNotBlank() }

                                // insights 순서대로 아이콘 매핑: [0]=수면, [1]=조절력, [2]=집중
                                val comparisons = reportData.insights.mapIndexed { index, insight ->
                                    val iconRes = when (index) {
                                        0 -> "sleep"         // 🌙 수면
                                        1 -> "self_control"  // ⚖️ 조절력
                                        2 -> "report_phone"  // 🎯 집중
                                        else -> "self_control"
                                    }

                                    ComparisonItem(
                                        type = insight.type,
                                        iconRes = iconRes,
                                        description = insight.description
                                    )
                                }

                                // Radar Chart 데이터 추출 (insights 순서: 수면, 조절력, 집중)
                                val radarData = if (reportData.insights.size >= 3) {
                                    RadarChartData(
                                        sleepScore = reportData.insights[0].score.after,
                                        selfControlScore = reportData.insights[1].score.after,
                                        focusScore = reportData.insights[2].score.after,
                                        sleepBefore = reportData.insights[0].score.before,
                                        selfControlBefore = reportData.insights[1].score.before,
                                        focusBefore = reportData.insights[2].score.before
                                    )
                                } else null

                                val uiData = DailyReportData(
                                    status = reportData.status,
                                    userName = userName,
                                    costumeUrl = costumeUrl,
                                    missionCompletionRate = reportData.missionSuccessRate,
                                    currentStatus = StatusDescription(
                                        title = "현재 $userName 님은",
                                        description = reportData.reportOverview
                                    ),
                                    predictions = predictions,
                                    comparisons = comparisons,
                                    radarChartData = radarData,
                                    advice = reportData.advice
                                )
                                _uiState.value = DailyReportUiState.Success(uiData)
                                stopReportPolling()
                            }
                        }
                        // IN_PROGRESS 상태이거나 데이터가 불완전할 때는 아무것도 하지 않음 (기존 UI 유지)
                    } else {
                        _uiState.value = DailyReportUiState.Error("데이터를 불러올 수 없습니다")
                    }
                } else {
                    // 폴링 중이 아닐 때만 Error 상태로 전환
                    if (!isPolling) {
                        _uiState.value = DailyReportUiState.Error(
                            "서버 오류가 발생했습니다"
                        )
                    }
                }
            } catch (e: Exception) {
                // 폴링 중이 아닐 때만 Error 상태로 전환
                if (!isPolling) {
                    _uiState.value = DailyReportUiState.Error(
                        e.message ?: "알 수 없는 오류가 발생했습니다"
                    )
                }
            }
        }
    }

    fun startReportPolling() {
        // 기존 폴링이 있다면 중지
        stopReportPolling()

        _isPolling.value = true
        reportPollingJob = viewModelScope.launch {
            while (true) {
                loadDailyReport()
                delay(1000L) // 1초 대기
            }
        }
    }

    fun stopReportPolling() {
        reportPollingJob?.cancel()
        reportPollingJob = null
        _isPolling.value = false
    }

    override fun onCleared() {
        super.onCleared()
        stopReportPolling()
    }
}
