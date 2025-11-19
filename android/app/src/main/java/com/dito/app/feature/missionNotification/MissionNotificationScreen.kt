package com.dito.app.feature.missionNotification

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dito.app.R
import com.dito.app.core.data.missionNotification.MissionNotificationData
import com.dito.app.core.data.missionNotification.MissionResult
import com.dito.app.core.data.missionNotification.MissionStatus
import com.dito.app.core.service.mission.MissionTracker
import com.dito.app.core.ui.component.BottomTab
import com.dito.app.core.ui.component.DitoBottomAppBar
import com.dito.app.core.ui.component.DitoModalContainer
import com.dito.app.core.ui.designsystem.BounceClickable
import com.dito.app.core.ui.designsystem.Background
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoShapes
import com.dito.app.core.ui.designsystem.DitoSoftShadow
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.OnSurface
import com.dito.app.core.ui.designsystem.Primary
import com.dito.app.core.ui.designsystem.Secondary
import com.dito.app.core.ui.designsystem.Spacing
import com.dito.app.core.ui.designsystem.Tertiary
import com.dito.app.core.ui.designsystem.hardShadow
import com.dito.app.core.ui.designsystem.softShadow
import com.dito.app.core.ui.designsystem.playPopSound
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.EntryPointAccessors
import javax.inject.Inject

// 상태 뱃지 타입
enum class NotificationType(val label: String, val color: Color) {
    IN_PROGRESS("진행중", Color(0xFFB39DDB)),  // 보라색
    COMPLETED("완료", Color(0xFFFFF59D)),      // 노란색
    FAILED("실패", Color(0xFFFFCDD2))          // 분홍색
}

// MissionNotificationData를 NotificationType으로 변환하는 헬퍼 함수
fun getNotificationType(status: MissionStatus, result: MissionResult?): NotificationType {
    return when (status) {
        MissionStatus.IN_PROGRESS -> NotificationType.IN_PROGRESS
        MissionStatus.COMPLETED -> {
            if (result == MissionResult.SUCCESS) {
                NotificationType.COMPLETED
            } else {
                NotificationType.FAILED
            }
        }
    }
}

// 바텀바까지 포함된 전체 화면
@Composable
fun MissionNotificationScaffold(
    selectedTab: BottomTab = BottomTab.HOME,
    onTabSelected: (BottomTab) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        bottomBar = {
            DitoBottomAppBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        }
    ) { innerPadding ->
        MissionNotificationScreen(
            modifier = Modifier.padding(innerPadding),
            onBackClick = onBackClick
        )
    }
}

// 상단 헤더 + 알림 리스트 영역
@Composable
fun MissionNotificationScreen(
    modifier: Modifier = Modifier,
    viewModel: MissionNotificationViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    initialMissionId: String? = null,
    initialOpenDetail: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // 설명 다이얼로그 상태
    var showInfoDialog by remember { mutableStateOf(false) }

    // 화면이 보이는 동안 주기적으로 새로고침 (진행 중인 미션이 있을 때)
    LaunchedEffect(Unit) {
        while (true) {
            delay(10000L) // 10초마다 새로고침

            // 진행 중인 미션이 있는지 확인
            val hasInProgressMission = uiState.notifications.any {
                it.status == MissionStatus.IN_PROGRESS
            }

            if (hasInProgressMission) {
                Log.d("MissionNotificationScreen", "🔄 자동 새로고침 - 진행 중인 미션 있음")
                viewModel.refresh()
            }
        }
    }

    // FCM 평가 알림 딥링크로 모달 자동 열기
    LaunchedEffect(initialMissionId, initialOpenDetail) {
        if (initialMissionId != null) {
            if (initialOpenDetail) {
                // openDetail=true 일 때: 평가 알림 → 즉시 새로고침 후 모달 열기
                Log.d("MissionNotificationScreen", "🎯 FCM 평가 알림 딥링크 처리")
                Log.d("MissionNotificationScreen", "   missionId: $initialMissionId")
                Log.d("MissionNotificationScreen", "   openDetail: $initialOpenDetail")

                // 평가 완료 데이터 가져오기 위해 즉시 새로고침
                Log.d("MissionNotificationScreen", "🔄 평가 완료 데이터 로딩을 위한 즉시 새로고침")
                viewModel.refresh()

                // ViewModel 메서드로 모달 열기 (내부에서 재시도 로직 포함)
                viewModel.openMissionById(initialMissionId.toLongOrNull())
            } else {
                // 개입 알림 (휴식하라)
                Log.d("MissionNotificationScreen", "🎯 FCM 개입 알림 딥링크 처리")
                Log.d("MissionNotificationScreen", "   missionId: $initialMissionId")
                Log.d("MissionNotificationScreen", "   프로그래스바 애니메이션이 자동으로 시작됩니다")
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        MissionNotificationHeader(
            onBackClick = onBackClick,
            onInfoClick = { showInfoDialog = true }
        )

        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                uiState.isLoading && uiState.notifications.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = OnSurface
                    )
                }

                uiState.error != null && uiState.notifications.isEmpty() -> {
                    Text(
                        text = uiState.error ?: "오류가 발생했습니다.",
                        modifier = Modifier.align(Alignment.Center),
                        color = OnSurface,
                        style = DitoTypography.bodyMedium
                    )
                }

                uiState.notifications.isEmpty() -> {
                    Text(
                        text = "미션 알림이 없습니다.",
                        modifier = Modifier.align(Alignment.Center),
                        color = OnSurface,
                        style = DitoTypography.bodyMedium
                    )
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = uiState.notifications,
                            key = { it.id }
                        ) { notification ->
                            NotificationItem(
                                notification = notification,
                                onMissionClick = { viewModel.onMissionClick(it) },
                                triggeredByDeepLink = initialMissionId != null && notification.id.toString() == initialMissionId
                            )
                        }
                    }
                }
            }
        }
    }

    // 미션 상세 모달
    uiState.selectedMission?.let { mission ->
        MissionDetailDialog(
            mission = mission,
            isShowingAnimation = uiState.isClaimingReward,
            onDismiss = { viewModel.dismissModal() },
            onConfirm = { viewModel.onRewardConfirm() }
        )
    }

    // 미션 알림 페이지 설명 다이얼로그
    if (showInfoDialog) {
        MissionInfoDialog(
            onDismiss = { showInfoDialog = false }
        )
    }
}

// 상단 헤더: 왼쪽 화살표 + 가운데 정렬 제목
@Composable
private fun MissionNotificationHeader(
    onBackClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Color.Black)
            .padding(horizontal = 32.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.angle_left),
            contentDescription = "Back",
            modifier = Modifier
                .size(28.dp)
                .clickable { onBackClick() },
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color.White)
        )
        Text(
            text = "알림",
            style = DitoTypography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        BounceClickable(
            onClick = {
                scope.launch {
                    playPopSound(context)
                    delay(150L)
                    onInfoClick()
                }
            }
        ) {
            Image(
                painter = painterResource(id = R.drawable.question),
                contentDescription = "Info",
                modifier = Modifier.size(28.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color.White)
            )
        }
    }
}

// 개별 알림 아이템
@Composable
fun NotificationItem(
    notification: MissionNotificationData,
    onMissionClick: (MissionNotificationData) -> Unit = {},
    triggeredByDeepLink: Boolean = false  // 푸시알림으로 진입했는지 여부
) {
    val scope = rememberCoroutineScope()
    val notificationType = getNotificationType(notification.status, notification.result)

    // 미션 완료 여부 확인
    val isCompleted = notification.status == MissionStatus.COMPLETED

    // 진행률 계산 - 고정 10초 프로그래스바
    var progress by remember { mutableFloatStateOf(0f) }
    var isWaitingForEvaluation by remember { mutableStateOf(false) }

    // 프로그래스바 맥박 효과를 위한 애니메이션 상태
    var pulseScale by remember { mutableFloatStateOf(1f) }

    // 백엔드 status 변화에 따라 "평가 대기" 상태 동기화
    LaunchedEffect(notification.status) {
        if (notification.status == MissionStatus.COMPLETED) {
            // 서버에서 완료 내려오면 "평가를 기다려주세요..." 카드 숨기고, 진행도는 100%로 고정
            isWaitingForEvaluation = false
            progress = 1f
        }
    }



    // 맥박 효과 애니메이션 (진행 중일 때만)
    LaunchedEffect(notification.status) {
        if (notification.status == MissionStatus.IN_PROGRESS) {
            while (true) {
                // 1.0 → 1.05 → 1.0 반복 (맥박 효과)
                pulseScale = 1.05f
                delay(500)
                pulseScale = 1.0f
                delay(500)
            }
        }
    }

    // 화면 진입 시점부터 남은 시간 기준 프로그레스바 (더 부드러운 UX)
    LaunchedEffect(notification.id, notification.triggerTime, notification.status, notification.duration) {
        if (notification.status != MissionStatus.IN_PROGRESS) return@LaunchedEffect

        // notification.duration(초) 사용, 없으면 15초 기본값
        val totalDurationMillis = (notification.duration ?: 15) * 1000L

        // 미션 시작 시각 파싱 (백엔드에서 내려주는 triggerTime 사용)
        val missionStartMillis = try {
            notification.triggerTime?.let { timeString ->
                ZonedDateTime.parse(timeString, DateTimeFormatter.ISO_DATE_TIME)
                    .toInstant()
                    .toEpochMilli()
            } ?: System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e("NotificationItem", "triggerTime 파싱 실패: ${notification.triggerTime}", e)
            System.currentTimeMillis()
        }

        // 실제 경과 시간 계산
        val nowMillis = System.currentTimeMillis()
        val actualElapsed = nowMillis - missionStartMillis
        val remainingMillis = (totalDurationMillis - actualElapsed).coerceAtLeast(0)

        // 화면 진입 시점의 초기 진행률
        val initialProgress = (actualElapsed.toFloat() / totalDurationMillis.toFloat()).coerceIn(0f, 1f)
        progress = initialProgress

        Log.d(
            "NotificationItem",
            "🎬 프로그레스바 시작: mission=${notification.id}, 실제 경과=${actualElapsed}ms, 남은 시간=${remainingMillis}ms, 초기 진행률=${(initialProgress * 100).toInt()}%"
        )

        // 이미 완료 시간을 넘긴 경우 즉시 평가 대기 상태로
        if (remainingMillis == 0L) {
            progress = 1f
            isWaitingForEvaluation = true
            Log.d("NotificationItem", "✅ 이미 완료 시간 경과 → 평가 대기 상태 진입 (mission=${notification.id})")
            return@LaunchedEffect
        }

        // 화면 진입 시점부터 시작
        val uiStartMillis = System.currentTimeMillis()
        val remainingProgress = 1f - initialProgress  // 남은 진행률 (0 ~ 1)

        try {
            while (notification.status == MissionStatus.IN_PROGRESS) {
                val uiElapsed = System.currentTimeMillis() - uiStartMillis

                // 남은 시간이 모두 경과한 경우
                if (uiElapsed >= remainingMillis) {
                    progress = 1f
                    isWaitingForEvaluation = true
                    Log.d("NotificationItem", "✅ ${notification.duration ?: 15}초 경과 → 평가 대기 상태 진입 (mission=${notification.id})")
                    break
                }

                // 진행률 = 초기 진행률 + (UI 경과 시간 / 남은 시간 × 남은 진행률)
                val additionalProgress = (uiElapsed.toFloat() / remainingMillis.toFloat()) * remainingProgress
                progress = (initialProgress + additionalProgress).coerceIn(0f, 1f)

                delay(50L)
            }
        } catch (e: Exception) {
            Log.e("NotificationItem", "프로그래스 계산 실패", e)
        }
    }

    // 완료된 미션에 결과에 따라 테두리 색상 변경
    val borderColor = if (isCompleted) {
        when (notification.result) {
            MissionResult.SUCCESS -> Color(0xFF42A5F5)  // 파란색 (성공)
            MissionResult.FAILURE -> Color(0xFFFF5252)  // 빨간색 (실패)
            else -> Color.Black
        }
    } else {
        Color.Black
    }

    BounceClickable(
        onClick = {
            scope.launch {
                delay(250L)
                onMissionClick(notification)
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) { isPressed ->
        // 평가 대기 중일 때는 완전히 다른 UI 표시 (진행 중 상태에서만)
        if (isWaitingForEvaluation && notification.status == MissionStatus.IN_PROGRESS) {
            // 레몬 회전 애니메이션 상태
            var lemonRotation by remember { mutableFloatStateOf(0f) }
            var lemonScale by remember { mutableFloatStateOf(1f) }

            // 레몬 회전 + 크기 변화 애니메이션
            LaunchedEffect(Unit) {
                while (true) {
                    // 회전 (연속적으로)
                    for (i in 0..360) {
                        lemonRotation = i.toFloat()

                        // 회전과 동시에 크기 변화 (0~180도: 확대, 180~360도: 축소)
                        lemonScale = if (i < 180) {
                            1.0f + (i / 180f) * 0.1f
                        } else {
                            1.1f - ((i - 180) / 180f) * 0.1f
                        }

                        delay(5L)  // 2초에 360도 회전
                    }
                }
            }

            // "평가를 기다려주세요..." 전용 카드
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .softShadow(DitoSoftShadow.Low.copy(cornerRadius = 12.dp))
                    .border(1.dp, Primary, RoundedCornerShape(12.dp))  // 보라색 테두리
                    .clip(RoundedCornerShape(12.dp))
                    .background(Background)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // 레몬 로딩 애니메이션 (회전 + 크기 변화)
                    Image(
                        painter = painterResource(id = R.drawable.lemon),
                        contentDescription = "Loading Lemon",
                        modifier = Modifier
                            .size(48.dp)
                            .graphicsLayer {
                                rotationZ = lemonRotation  // Z축 회전
                                scaleX = lemonScale
                                scaleY = lemonScale
                            }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "평가를 기다려주세요...",
                        color = OnSurface,
                        style = DitoCustomTextStyles.titleDLarge,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "AI가 미션 수행 결과를 분석하고 있어요",
                        color = OnSurface.copy(alpha = 0.7f),
                        style = DitoTypography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // 기존 카드 UI (진행 중 or 완료)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .softShadow(DitoSoftShadow.Low.copy(cornerRadius = 12.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Background)
                    .padding(16.dp)
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp, horizontal = 6.dp)
                ) {
                    // AI가 준 미션 내용 (크게)
                    Text(
                        text = notification.title,
                        color = OnSurface,
                        style = DitoCustomTextStyles.titleKSmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 스탯 변화 표시 (pill 버튼 형태)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (notification.statChangeSelfCare > 0) {
                            StatPill(
                                label = "자기관리 +${notification.statChangeSelfCare}",
                                backgroundColor = Primary
                            )
                        }
                        if (notification.statChangeFocus > 0) {
                            StatPill(
                                label = "집중 +${notification.statChangeFocus}",
                                backgroundColor = Secondary
                            )
                        }
                        if (notification.statChangeSleep > 0) {
                            StatPill(
                                label = "수면 +${notification.statChangeSleep}",
                                backgroundColor = Tertiary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 레몬 이미지 + 개수
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.lemon),
                            contentDescription = "Lemon",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${notification.coinReward}",
                            color = OnSurface,
                            style = DitoCustomTextStyles.titleDMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 진행바 (진행 중일 때만) - 동적 색상 변화
                    if (notification.status == MissionStatus.IN_PROGRESS) {
                        // 진행도에 따라 색상 변화 (0% 보라색 → 50% 파란색 → 100% 초록색)
                        val progressColor = when {
                            progress < 0.3f -> Primary  // 보라색
                            progress < 0.6f -> Color(0xFF42A5F5)  // 파란색
                            else -> Color(0xFF66BB6A)  // 초록색
                        }

                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 16.dp)
                                .height(8.dp)  // 높이 증가
                                .clip(RoundedCornerShape(4.dp))
                                .graphicsLayer {
                                    scaleY = pulseScale  // 맥박 효과
                                },
                            color = progressColor,
                            trackColor = Color(0xFF2A2A2A)
                        )
                    }
                }

                // 구분선
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color.Black.copy(alpha = 0.2f))
                )

                Spacer(modifier = Modifier.width(16.dp))

                // 우측 아이콘 (로딩 or 체크)
                if (isCompleted) {
                    when (notification.result) {
                        MissionResult.FAILURE -> {
                            Image(
                                painter = painterResource(id = R.drawable.fail),
                                contentDescription = "Failed",
                                modifier = Modifier.size(32.dp),
                                colorFilter = ColorFilter.tint(Color(0xFFFF5252))
                            )
                        }

                        else -> {
                            Image(
                                painter = painterResource(id = R.drawable.complete),
                                contentDescription = "Success",
                                modifier = Modifier.size(32.dp),
                                colorFilter = ColorFilter.tint(Color(0xFF42A5F5))
                            )
                        }
                    }
                } else {
                    // 진행중일 때 CircularProgressIndicator 표시
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Primary,
                        strokeWidth = 3.dp
                    )
                }
            }  // Row 닫기
        }  // if-else 닫기
    }  // BounceClickable 닫기
}

// 스탯 pill 컴포넌트
@Composable
private fun StatPill(
    label: String,
    backgroundColor: Color
) {
    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(48.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = DitoTypography.labelSmall,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 미션 진행률 계산
 * @param triggerTime 미션 시작 시각 (ISO 8601) - 없으면 MissionTracker 사용
 * @param duration 미션 소요 시간 (초)
 * @return 진행률 (0.0 ~ 1.0)
 */
private fun calculateProgress(triggerTime: String?, duration: Int?): Float {
    // duration이 없으면 계산 불가
    if (duration == null || duration <= 0) return 0f

    // 1. triggerTime이 있으면 계산
    if (triggerTime != null) {
        return try {
            val zonedDateTime =
                ZonedDateTime.parse(triggerTime, DateTimeFormatter.ISO_DATE_TIME)
            val startMillis = zonedDateTime.toInstant().toEpochMilli()
            val endMillis = startMillis + (duration * 1000L)
            val nowMillis = System.currentTimeMillis()

            when {
                nowMillis < startMillis -> 0f
                nowMillis > endMillis -> 1f
                else -> {
                    val elapsed = nowMillis - startMillis
                    val total = endMillis - startMillis
                    (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MissionProgress", "triggerTime 파싱 실패: $triggerTime", e)
            0f
        }
    }

    // 2. triggerTime이 없으면 0f
    return 0f
}

// 상태 뱃지
@Composable
fun StatusBadge(type: NotificationType) {
    Box(
        modifier = Modifier
            .widthIn(min = 70.dp)
            .background(type.color, RoundedCornerShape(100.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = type.label,
            color = Color.Black,
            style = DitoTypography.labelSmall.copy(fontSize = 12.sp)
        )
    }
}

// 미리보기
@Preview(showBackground = true)
@Composable
fun PreviewMissionNotification() {
    MissionNotificationScaffold()
}
