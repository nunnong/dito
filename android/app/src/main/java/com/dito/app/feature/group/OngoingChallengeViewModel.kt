package com.dito.app.feature.group

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dito.app.core.background.ScreenTimeSyncWorker
import com.dito.app.core.data.group.RankingItem
import com.dito.app.core.repository.GroupRepository
import com.dito.app.core.storage.GroupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OngoingChallengeUiState(
    val isLoading: Boolean = false,
    val groupName: String = "",
    val goal: String = "",
    val penalty: String = "",
    val period: Int = 0,
    val bet: Int = 0,
    val totalBetting: Int = 0,
    val startDate: String = "",
    val endDate: String = "",
    val rankings: List<RankingItem> = emptyList(),
    val initialUserOrder: List<Long> = emptyList(),  // 처음 위치 순서 (userId)
    val errorMessage: String? = null,
    val pokedUserIds: Set<Long> = emptySet(),  // 찔린 사용자 ID들
    val realTimeScreenTimes: Map<Long, Int> = emptyMap(),  // userId -> 초 단위 스크린타임
    val coachMessage: String = "",  // AI 코치 말풍선 메시지 (1줄)
    val showCoachBubble: Boolean = false,  // 말풍선 표시 여부
    val goalMinutes: Int = 0  // 오늘 목표 시간 (분)
)

@HiltViewModel
class OngoingChallengeViewModel @Inject constructor(
    private val groupManager: GroupManager,
    private val groupRepository: GroupRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(OngoingChallengeUiState())
    val uiState: StateFlow<OngoingChallengeUiState> = _uiState.asStateFlow()

    private var autoRefreshJob: Job? = null
    private var realTimeTickerJob: Job? = null
    private val pokeBubbleJobs = mutableMapOf<Long, Job>()
    private var coachMessageJob: Job? = null

    init {
        refreshGroupDetails()
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoRefresh()
        stopRealTimeTicker()
        stopCoachMessageRotation()
    }

    private fun refreshGroupDetails() {
        viewModelScope.launch {
            groupRepository.getGroupDetail().fold(
                onSuccess = { groupDetail ->
                    // totalBetCoin을 GroupManager에 저장
                    groupDetail.totalBetCoin?.let { groupManager.saveTotalBet(it) }

                    val goalDesc = groupDetail.goalDescription ?: groupManager.getGoal()
                    val goalMins = parseGoalToMinutes(goalDesc)

                    _uiState.value = _uiState.value.copy(
                        groupName = groupDetail.groupName ?: groupManager.getGroupName(),
                        goal = goalDesc,
                        penalty = groupDetail.penaltyDescription ?: groupManager.getPenalty(),
                        period = groupDetail.period ?: groupManager.getPeriod(),
                        bet = groupDetail.betCoin ?: groupManager.getBet(),
                        totalBetting = groupDetail.totalBetCoin ?: groupManager.getTotalBet(),
                        startDate = groupDetail.startDate ?: groupManager.getStartDate(),
                        endDate = groupDetail.endDate ?: groupManager.getEndDate(),
                        goalMinutes = goalMins
                    )
                },
                onFailure = {
                    loadGroupDetailsFromManager()
                }
            )
            loadRanking()
        }
    }

    private fun loadGroupDetailsFromManager() {
        val goalDesc = groupManager.getGoal()
        val goalMins = parseGoalToMinutes(goalDesc)

        _uiState.value = _uiState.value.copy(
            groupName = groupManager.getGroupName(),
            goal = goalDesc,
            penalty = groupManager.getPenalty(),
            period = groupManager.getPeriod(),
            bet = groupManager.getBet(),
            totalBetting = groupManager.getTotalBet(),
            startDate = groupManager.getStartDate(),
            endDate = groupManager.getEndDate(),
            goalMinutes = goalMins
        )
    }

    fun startAutoRefresh() {
        stopAutoRefresh()
        autoRefreshJob = viewModelScope.launch {
            while (true) {
                // 자신의 YouTube 시간을 서버에 즉시 업로드
                ScreenTimeSyncWorker.triggerImmediateSync(context)
                // 약간의 딜레이 후 랭킹 조회 (서버가 업데이트할 시간)
                delay(500L)
                loadRanking()
                delay(9_500L) // 총 10초 주기
            }
        }
        startRealTimeTicker()
        startCoachMessageRotation()    }

    fun stopAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
        stopRealTimeTicker()
    }

    private fun startRealTimeTicker() {
        // 실시간 증가 로직 제거: 서버 데이터만 사용
        stopRealTimeTicker()
    }

    private fun stopRealTimeTicker() {
        realTimeTickerJob?.cancel()
        realTimeTickerJob = null
    }

    private fun incrementScreenTimes() {
        // 실시간 증가 로직 제거: 서버에서 받은 데이터만 사용
        // 10초마다 서버에서 최신 랭킹을 받아와서 표시
    }

    fun loadRanking() {
        val groupId = groupManager.getGroupId()
        if (groupId == 0L) return

        viewModelScope.launch {
            groupRepository.getRanking(groupId).fold(
                onSuccess = { response ->
                    val currentOrder = _uiState.value.initialUserOrder

                    // 처음 랭킹을 받았을 때만 초기 순서 저장
                    val initialOrder = if (currentOrder.isEmpty()) {
                        response.rankings.take(4).map { it.userId }
                    } else {
                        currentOrder
                    }

                    // 서버에서 받은 스크린타임을 초 단위
                    val serverTimes = mutableMapOf<Long, Int>()
                    response.rankings.forEach { ranking ->
                        val serverSeconds = ranking.totalSeconds
                        serverTimes[ranking.userId] = serverSeconds
                    }

                    _uiState.value = _uiState.value.copy(
                        rankings = response.rankings,
                        initialUserOrder = initialOrder,
                        realTimeScreenTimes = serverTimes // 서버 시간 그대로 사용
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    fun pokeMember(targetUserId: Long) {
        val groupId = groupManager.getGroupId()
        if (groupId == 0L) return

        viewModelScope.launch {
            groupRepository.pokeMember(groupId, targetUserId).fold(
                onSuccess = {
                    // 해당 사용자의 이전 타이머가 있으면 취소
                    pokeBubbleJobs[targetUserId]?.cancel()

                    // 찌르기 성공 - 해당 캐릭터 머리 위에 말풍선 표시
                    val currentPokedIds = _uiState.value.pokedUserIds.toMutableSet()
                    currentPokedIds.add(targetUserId)
                    _uiState.value = _uiState.value.copy(pokedUserIds = currentPokedIds)

                    // 해당 사용자별로 독립적인 타이머 시작
                    pokeBubbleJobs[targetUserId] = viewModelScope.launch {
                        delay(1000L)
                        val updatedPokedIds = _uiState.value.pokedUserIds.toMutableSet()
                        updatedPokedIds.remove(targetUserId)
                        _uiState.value = _uiState.value.copy(pokedUserIds = updatedPokedIds)
                        pokeBubbleJobs.remove(targetUserId)
                    }
                },
                onFailure = {
                    // 찌르기 실패 UI 피드백
                }
            )
        }
    }

    fun resetPokeBubble() {
        pokeBubbleJobs.values.forEach { it.cancel() }
        pokeBubbleJobs.clear()
        _uiState.value = _uiState.value.copy(pokedUserIds = emptySet())
    }

    // 테스트용: 랭킹 셔플
    fun shuffleRankingsForTest() {
        val currentRankings = _uiState.value.rankings.toMutableList()
        if (currentRankings.size >= 2) {
            currentRankings.shuffle()
            val updatedRankings = currentRankings.mapIndexed { index, item ->
                item.copy(rank = index + 1)
            }
            _uiState.value = _uiState.value.copy(rankings = updatedRankings)
        }
    }

    fun startCoachMessageRotation() {
        stopCoachMessageRotation()
        showCoachBubble()  // 즉시 첫 메시지 표시
        coachMessageJob = viewModelScope.launch {
            while (true) {
                delay(5000L)  // 5초마다 (3초 표시 + 4초 대기)
                showCoachBubble()
            }
        }
    }

    private fun stopCoachMessageRotation() {
        coachMessageJob?.cancel()
        coachMessageJob = null
    }

    private fun showCoachBubble() {
        viewModelScope.launch {
            // 메시지 생성
            val message = generateCoachMessage()

            // 말풍선 표시
            _uiState.value = _uiState.value.copy(
                coachMessage = message,
                showCoachBubble = true
            )

            // 3초 후 자동으로 숨김
            delay(3000L)
            _uiState.value = _uiState.value.copy(
                showCoachBubble = false
            )
        }
    }

    private fun generateCoachMessage(): String {
        val state = _uiState.value
        val rankings = state.rankings
        val my = rankings.find { it.isMe }
            ?: return "오늘도 나랑 가볍게 시작해볼까? 🍋"

        val times = state.realTimeScreenTimes
        val mySeconds = times[my.userId] ?: 0
        val myMinutes = mySeconds / 60

        val first = rankings.firstOrNull { it.rank == 1 }
        val second = rankings.firstOrNull { it.rank == 2 }
        val goalMinutes = state.goalMinutes

        val messages = mutableListOf<String>()

        // 1) 친구 실시간 사용 상황 코멘트
        val youtubeUsers = rankings.filter {
            !it.isMe && it.currentAppPackage?.contains("com.google.android.youtube", ignoreCase = true) == true
        }

        if (youtubeUsers.isNotEmpty()) {
            val user = youtubeUsers.random()
            val sec = times[user.userId] ?: 0
            val min = sec / 60
            messages.add(
                "지금 ${user.nickname}님이, YouTube를 달리고 있어.\n" +
                        "한 번 찔러서 숨 고르게 해볼까? 👀 (${min}분 사용)"
            )
        }

        // 2) 1등 경쟁 상황 코멘트 (그룹 전체 관점)
        if (first != null && second != null) {
            val firstSec = times[first.userId] ?: 0
            val secondSec = times[second.userId] ?: 0
            val gapMin = kotlin.math.abs(firstSec - secondSec) / 60

            if (gapMin <= 5) {
                messages.add(
                    "지금 ${first.nickname}님이 1위!\n" +
                            "${second.nickname}님이 바로 뒤를 쫓는 중 🔥\n"
                )
            } else {
                messages.add(
                    "${first.nickname}님이 여유 있게 1위 유지 중이야\n" +
                            "${second.nickname}님이 따라가려면 노력이 필요해!"
                )
            }
        }

        // 3) 나 기준으로 “1등을 하려면 오늘 ○○시간 이하” 안내
        if (first != null && first.userId != my.userId) {
            val firstSec = times[first.userId] ?: 0
            val mySec = mySeconds
            val needDiff = (firstSec - mySec).coerceAtLeast(0)
            val needExtraMargin = 10 * 60 // 10분 정도의 여유 마진
            val targetSecForWin = mySec + needDiff + needExtraMargin
            val targetMinForWin = targetSecForWin / 60

            // 목표 시간이 있는 경우에는 그 안에서만 안내
            if (goalMinutes > 0) {
                val safeLimit = minOf(goalMinutes, targetMinForWin)
                messages.add(
                    "오늘 전체 사용을 ${safeLimit}분 안으로 막으면,\n" +
                            "${first.nickname}님 추격도 충분히 가능해 🏃‍♂️💨"
                )
            } else {
                messages.add(
                    "지금 페이스라면 영상 시간을 조금만 더 줄이면\n" +
                            "${first.nickname}님 추격도 가능해"
                )
            }
        }

        // 4) 나의 진행률 & 목표 대비 차이
        if (goalMinutes > 0) {
            val remaining = goalMinutes - myMinutes
            val usedPercent = (myMinutes * 100 / goalMinutes).coerceIn(0, 300)

            val goalMessage = when {
                remaining > 30 -> "오늘 목표의 ${usedPercent}%만 썼어. 아직 여유있어!\n지금 페이스 유지해봐 😎"
                remaining in 11..30 -> "목표까지 ${remaining}분 남았어.\n 지금부터는 의식적으로 사용해볼까? 👌"
                remaining in 1..10 -> "⚠️ 목표까지 ${remaining}분밖에 안 남았어.\n지금 끄면 내일이 훨씬 편해질거야"
                remaining == 0 -> "딱 목표만큼 사용했어.\n이제는 진짜 쉴 시간! 화면 대신 몸을 좀 풀어볼까? 🙆‍♂️"
                else -> {
                    val over = -remaining
                    "오늘 목표를 ${over}분 초과했어.\n남은 시간은 최대한 오프라인에 투자해보자"
                }
            }
            messages.add(goalMessage)
        }

        // 5) 아무 조건도 안 걸렸을 때 기본 코멘트
        if (messages.isEmpty()) {
            messages.add(
                "오늘도 레몬 나무 레이스 진행 중!\n" +
                        "잠깐 쉬어가고 싶을 때는 화면 대신 주변을 한 번 둘러보는 건 어때? 🍋"
            )
        }

        return messages.random()
    }


    private fun formatTimeShort(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        return if (hours > 0) {
            "${hours}시간 ${minutes}분"
        } else {
            "${minutes}분"
        }
    }

    private fun parseGoalToMinutes(goal: String): Int {
        // "하루 유튜브 1시간 이하" 같은 패턴에서 숫자 추출
        val hourRegex = """(\d+)\s*시간""".toRegex()
        val minuteRegex = """(\d+)\s*분""".toRegex()

        var totalMinutes = 0

        hourRegex.find(goal)?.let { match ->
            val hours = match.groupValues[1].toIntOrNull() ?: 0
            totalMinutes += hours * 60
        }

        minuteRegex.find(goal)?.let { match ->
            val minutes = match.groupValues[1].toIntOrNull() ?: 0
            totalMinutes += minutes
        }

        return totalMinutes
    }
}
