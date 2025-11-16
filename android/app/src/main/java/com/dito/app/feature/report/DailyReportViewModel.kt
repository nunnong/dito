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

            // HomeRepository에서 사용자 정보 가져오기
            val homeResult = homeRepository.getHomeData()
            val homeData = homeResult.getOrNull()
            val userName = homeData?.nickname ?: "디토"
            val costumeUrl = homeData?.costumeUrl ?: ""

            // TODO: API 연결 시 아래 주석 해제하고 더미 데이터 삭제
            // ApiService에 getDailyReport 메서드 추가 필요:
            // @GET("/user/report")
            // suspend fun getDailyReport(@Header("Authorization") token: String): Response<DailyReportResponse>

            /*
            try {
                val token = authTokenManager.getBearerToken()
                if (token == null) {
                    _uiState.value = DailyReportUiState.Error("로그인이 필요합니다")
                    return@launch
                }

                val response = apiService.getDailyReport(token)

                if (response.isSuccessful && response.body()?.error == false) {
                    val data = response.body()?.data
                    if (data != null) {
                        _uiState.value = DailyReportUiState.Success(data)
                    } else {
                        _uiState.value = DailyReportUiState.Error("데이터를 불러올 수 없습니다")
                    }
                } else {
                    _uiState.value = DailyReportUiState.Error(
                        response.body()?.message ?: "서버 오류가 발생했습니다"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = DailyReportUiState.Error(
                    e.message ?: "알 수 없는 오류가 발생했습니다"
                )
            }
            */

            // 임시 더미 데이터 (API 연결 전까지 사용)
            try {
                delay(500) // 로딩 시뮬레이션

                val dummyData = DailyReportData(
                    userName = userName, // HomeRepository에서 가져온 실제 사용자 닉네임
                    costumeUrl = costumeUrl, // HomeRepository에서 가져온 실제 캐릭터 이미지
                    missionCompletionRate = 73,
                    currentStatus = StatusDescription(
                        title = "현재 디토 님은",
                        description = "건강한 디지털 습관을 유지하고 있습니다"
                    ),
                    predictions = listOf(
                        "새벽 2시 이후에 잠드는 날이 많고, 침대에 누워 쇼츠를 40~60분 정도 연속 시청하는 패턴이 관찰됐어요. 수면 전 스크린 타임이 길어, 다음날 피로 누적 위험이 높은 상태입니다.",
                        "야간 시간대에 모바일 사용이 집중되어 있어, 수면 패턴이 불규칙할 가능성이 높습니다."
                    ),
                    comparisons = listOf(
                        ComparisonItem(
                            type = ComparisonType.POSITIVE,
                            iconRes = "sleep",
                            description = "야간에 사용하는 시간이 전일 대비 -24% 감소했어요"
                        ),
                        ComparisonItem(
                            type = ComparisonType.NEGATIVE,
                            iconRes = "phone",
                            description = "핸드폰 사용 시간이 2시간 증가했어요"
                        ),
                        ComparisonItem(
                            type = ComparisonType.POSITIVE,
                            iconRes = "self_control",
                            description = "미션을 잘 실천하고 있어요"
                        )
                    )
                )

                _uiState.value = DailyReportUiState.Success(dummyData)
            } catch (e: Exception) {
                _uiState.value = DailyReportUiState.Error(
                    e.message ?: "알 수 없는 오류가 발생했습니다"
                )
            }
        }
    }
}
