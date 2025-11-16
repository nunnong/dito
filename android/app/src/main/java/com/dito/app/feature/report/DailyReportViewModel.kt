package com.dito.app.feature.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dito.app.core.data.report.ComparisonItem
import com.dito.app.core.data.report.ComparisonType
import com.dito.app.core.data.report.DailyReportData
import com.dito.app.core.data.report.StatusDescription
import com.dito.app.core.network.ApiService
import com.dito.app.core.repository.HomeRepository
import com.dito.app.core.storage.AuthTokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun loadDailyReport() {
        viewModelScope.launch {
            _uiState.value = DailyReportUiState.Loading

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

                        // reportOverview를 줄바꿈으로 분리
                        val predictions = reportData.reportOverview.split("\n").filter { it.isNotBlank() }

                        // insights를 ComparisonItem으로 변환
                        val comparisons = reportData.insights.map { insight ->
                            // type에 따라 적절한 아이콘 선택
                            val iconRes = when (insight.type) {
                                ComparisonType.POSITIVE -> if (insight.description.contains("야간")) "sleep" else "self_control"
                                ComparisonType.NEGATIVE -> "phone"
                                ComparisonType.NEUTRAL -> "self_control"
                            }

                            ComparisonItem(
                                type = insight.type,
                                iconRes = iconRes,
                                description = insight.description
                            )
                        }

                        val uiData = DailyReportData(
                            userName = userName,
                            costumeUrl = costumeUrl,
                            missionCompletionRate = reportData.missionSuccessRate,
                            currentStatus = StatusDescription(
                                title = "현재 $userName 님은",
                                description = reportData.reportOverview
                            ),
                            predictions = predictions,
                            comparisons = comparisons,
                            advice = reportData.advice
                        )

                        _uiState.value = DailyReportUiState.Success(uiData)
                    } else {
                        _uiState.value = DailyReportUiState.Error("데이터를 불러올 수 없습니다")
                    }
                } else {
                    _uiState.value = DailyReportUiState.Error(
                        "서버 오류가 발생했습니다"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = DailyReportUiState.Error(
                    e.message ?: "알 수 없는 오류가 발생했습니다"
                )
            }
        }
    }
}
