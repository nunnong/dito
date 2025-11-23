package com.dito.app.feature.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dito.app.core.data.RealmConfig
import com.dito.app.core.data.phone.MediaSessionEvent
import com.dito.app.core.data.report.ComparisonItem
import com.dito.app.core.data.report.ComparisonType
import com.dito.app.core.data.report.DailyReportData
import com.dito.app.core.data.report.DiaryUiState
import com.dito.app.core.data.report.RadarChartData
import com.dito.app.core.data.report.StatusDescription
import com.dito.app.core.data.report.VideoFeedback
import com.dito.app.core.data.report.VideoFeedbackItem
import android.util.Base64
import com.dito.app.core.network.ApiService
import com.dito.app.core.repository.HomeRepository
import com.dito.app.core.storage.AuthTokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

sealed class DailyReportUiState {
    data object Loading : DailyReportUiState()
    data class Success(val data: DailyReportData) : DailyReportUiState()
    data class Error(val message: String) : DailyReportUiState()
}

enum class DebugFilter {
    ALL,        // 전체
    TODAY,      // 오늘
    UNSYNCED,   // 미동기화
    YOUTUBE     // YouTube만
}

@HiltViewModel
class DailyReportViewModel @Inject constructor(
    private val apiService: ApiService,
    private val authTokenManager: AuthTokenManager,
    private val homeRepository: HomeRepository,
    private val reportRepository: com.dito.app.core.repository.ReportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DailyReportUiState>(DailyReportUiState.Loading)
    val uiState: StateFlow<DailyReportUiState> = _uiState.asStateFlow()

    private val _isPolling = MutableStateFlow(false)
    val isPolling: StateFlow<Boolean> = _isPolling.asStateFlow()

    private var reportPollingJob: Job? = null

    // Debug 상태
    private val _showDebugTab = MutableStateFlow(false)
    val showDebugTab: StateFlow<Boolean> = _showDebugTab.asStateFlow()

    private val _mediaSessionEvents = MutableStateFlow<List<MediaSessionEvent>>(emptyList())
    val mediaSessionEvents: StateFlow<List<MediaSessionEvent>> = _mediaSessionEvents.asStateFlow()

    private val _debugFilter = MutableStateFlow(DebugFilter.ALL)
    val debugFilter: StateFlow<DebugFilter> = _debugFilter.asStateFlow()

    // 디토일지 상태
    private val _diaryUiState = MutableStateFlow<DiaryUiState>(DiaryUiState.LoadingVideos)
    val diaryUiState: StateFlow<DiaryUiState> = _diaryUiState.asStateFlow()

    init {
        // 앱 시작 시 피드백 영상 로드
        loadVideosForFeedback()
    }

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
                                    advice = reportData.advice,
                                    strategyChanges = reportData.strategy ?: emptyList()
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

    // Debug 기능
    fun toggleDebugTab() {
        _showDebugTab.value = !_showDebugTab.value
        if (_showDebugTab.value) {
            loadMediaSessionEvents()
        }
    }

    fun setDebugFilter(filter: DebugFilter) {
        _debugFilter.value = filter
        loadMediaSessionEvents()
    }

    fun loadMediaSessionEvents() {
        viewModelScope.launch {
            try {
                val realm = RealmConfig.getInstance()
                val events = when (_debugFilter.value) {
                    DebugFilter.ALL -> {
                        realm.query<MediaSessionEvent>().find()
                    }
                    DebugFilter.TODAY -> {
                        val today = getToday()
                        realm.query<MediaSessionEvent>("date == $0", today).find()
                    }
                    DebugFilter.UNSYNCED -> {
                        realm.query<MediaSessionEvent>("synced == false").find()
                    }
                    DebugFilter.YOUTUBE -> {
                        realm.query<MediaSessionEvent>(
                            "appPackage == $0",
                            "com.google.android.youtube"
                        ).find()
                    }
                }
                _mediaSessionEvents.value = events.sortedByDescending { it.timestamp }
            } catch (e: Exception) {
                _mediaSessionEvents.value = emptyList()
            }
        }
    }

    fun refreshDebugData() {
        loadMediaSessionEvents()
    }

    private fun getToday(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // ========== 디토일지 관련 함수들 ==========


    /**
     * 피드백 업데이트
     */
    fun updateFeedback(videoId: String, isHelpful: Boolean?, selectedReasons: Set<String> = emptySet()) {
        val currentState = _diaryUiState.value
        if (currentState is DiaryUiState.FeedbackCollection) {
            val updatedFeedbacks = currentState.feedbacks.toMutableMap()
            updatedFeedbacks[videoId] = VideoFeedback(
                videoId = videoId,
                isHelpful = isHelpful,
                selectedReasons = selectedReasons
            )
            _diaryUiState.value = currentState.copy(feedbacks = updatedFeedbacks)
        }
    }

    /**
     * 디토일지 생성
     */
    fun generateDiary() {
        viewModelScope.launch {
            // 로딩 상태로 전환
            _diaryUiState.value = DiaryUiState.GeneratingDiary

            try {
                // 스피너 표시를 위한 최소 대기 시간
                delay(2000L)

                // 기존 리포트 로드 로직 실행
                loadDailyReport()

                // 리포트 로드 완료 대기
                delay(500L)

                // 현재 uiState를 기반으로 DiaryGenerated 상태로 전환
                val currentUiState = _uiState.value
                if (currentUiState is DailyReportUiState.Success) {
                    _diaryUiState.value = DiaryUiState.DiaryGenerated(
                        reportData = currentUiState.data
                    )
                } else {
                    // 실패 시 에러 상태
                    _diaryUiState.value = DiaryUiState.Error("디토일지 생성에 실패했습니다")
                }
            } catch (e: Exception) {
                _diaryUiState.value = DiaryUiState.Error(
                    e.message ?: "알 수 없는 오류가 발생했습니다"
                )
            }
        }
    }

    /**
     * 디토일지 상태 초기화 (피드백 수집 화면으로 복귀)
     */
    /**
     * 피드백 영상 목록 로드 (API 호출)
     */
    fun loadVideosForFeedback() {
        viewModelScope.launch {
            _diaryUiState.value = DiaryUiState.LoadingVideos

            try {
                val result = reportRepository.getVideosForFeedback()

                result.onSuccess { videos ->
                    if (videos.isNotEmpty()) {
                        _diaryUiState.value = DiaryUiState.FeedbackCollection(
                            videos = videos,
                            feedbacks = emptyMap()
                        )
                    } else {
                        // 영상이 없는 경우
                        _diaryUiState.value = DiaryUiState.Error(
                            message = "피드백 대상 영상이 없습니다",
                            canRetry = true
                        )
                    }
                }.onFailure { exception ->
                    // API 실패시 에러 표시
                    android.util.Log.e("DailyReportViewModel", "영상 로드 실패: ${exception.message}")
                    _diaryUiState.value = DiaryUiState.Error(
                        message = exception.message ?: "영상을 불러오는데 실패했습니다",
                        canRetry = true
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("DailyReportViewModel", "영상 로드 중 예외 발생", e)
                _diaryUiState.value = DiaryUiState.Error(
                    message = "영상을 불러오는데 실패했습니다",
                    canRetry = true
                )
            }
        }
    }

    /**
     * 디토일지 상태 초기화
     */
    fun resetDiaryState() {
        loadVideosForFeedback()
    }
}
