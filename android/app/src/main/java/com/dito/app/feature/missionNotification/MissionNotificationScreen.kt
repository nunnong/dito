package com.dito.app.feature.missionNotification

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
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
import com.dito.app.core.ui.designsystem.Background
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoShapes
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.OnSurface
import com.dito.app.core.ui.designsystem.Primary
import com.dito.app.core.ui.designsystem.Spacing
import com.dito.app.core.ui.designsystem.hardShadow
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
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        MissionNotificationHeader(onBackClick = onBackClick)

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
                                onMissionClick = { viewModel.onMissionClick(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    // 미션 상세 모달
    uiState.selectedMission?.let { mission ->
        MissionDetailModal(
            mission = mission,
            isShowingAnimation = uiState.isClaimingReward,
            onDismiss = { viewModel.dismissModal() },
            onConfirm = { viewModel.onRewardConfirm() }
        )
    }
}

// 상단 헤더: 왼쪽 화살표 + 가운데 정렬 제목
@Composable
private fun MissionNotificationHeader(onBackClick: () -> Unit) {
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
        Spacer(modifier = Modifier.size(28.dp))
    }
}

// 개별 알림 아이템
@Composable
fun NotificationItem(
    notification: MissionNotificationData,
    onMissionClick: (MissionNotificationData) -> Unit = {}
) {
    val notificationType = getNotificationType(notification.status, notification.result)

    // 미션 완료 여부 확인
    val isCompleted = notification.status == MissionStatus.COMPLETED

    // 진행률 계산 (실시간)
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(notification.triggerTime, notification.duration) {
        while (notification.status == MissionStatus.IN_PROGRESS) {
            progress = calculateProgress(notification.triggerTime, notification.duration)
            delay(1000L)  // 1초마다 업데이트

            // 100% 완료되면 루프 종료
            if (progress >= 1f) break
        }
    }

    // 완료된 미션에 결과에 따라 테두리 색상 변경
    val borderModifier = if (isCompleted) {
        val borderColor = when (notification.result) {
            MissionResult.SUCCESS -> Color(0xFF42A5F5)  // 파란색 (성공)
            MissionResult.FAILURE -> Color(0xFFFF5252)  // 빨간색 (실패)
            else -> Color.Transparent
        }
        Modifier.border(2.dp, borderColor, RoundedCornerShape(12.dp))
    } else {
        Modifier
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMissionClick(notification) }
    ) {
        // 카드 영역
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .then(borderModifier)
                .clip(RoundedCornerShape(12.dp))
                .background(Background)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // AI가 준 미션 내용 (크게)
                Text(
                    text = notification.title,
                    color = OnSurface,
                    style = DitoCustomTextStyles.titleKMedium.copy(fontSize = 18.sp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 스탯 변화 표시
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (notification.statChangeSelfCare > 0) {
                        Text(
                            text = "자기관리: +${notification.statChangeSelfCare}",
                            color = OnSurface.copy(alpha = 0.6f),
                            style = DitoTypography.labelSmall.copy(fontSize = 11.sp)
                        )
                    }
                    if (notification.statChangeFocus > 0) {
                        Text(
                            text = "집중: +${notification.statChangeFocus}",
                            color = OnSurface.copy(alpha = 0.6f),
                            style = DitoTypography.labelSmall.copy(fontSize = 11.sp)
                        )
                    }
                    if (notification.statChangeSleep > 0) {
                        Text(
                            text = "수면: +${notification.statChangeSleep}",
                            color = OnSurface.copy(alpha = 0.6f),
                            style = DitoTypography.labelSmall.copy(fontSize = 11.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                        style = DitoTypography.bodyMedium.copy(fontSize = 14.sp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 진행바 (진행 중일 때만)
                if (notification.status == MissionStatus.IN_PROGRESS) {
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFFB39DDB),  // 보라색
                        trackColor = Color(0xFF2A2A2A)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 우측 아이콘 (로딩 or 체크)
            if (isCompleted) {
                val iconColor = when (notification.result) {
                    MissionResult.SUCCESS -> Color(0xFF42A5F5)  // 파란색 (성공)
                    MissionResult.FAILURE -> Color(0xFFFF5252)  // 빨간색 (실패)
                    else -> Color(0xFFFF9800)  // 주황색 (기타)
                }
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color(0xFFB39DDB),  // 보라색
                    strokeWidth = 3.dp
                )
            }
        }

        // 구분선
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(1.dp)
                .background(OnSurface.copy(alpha = 0.1f))
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

    // 1. triggerTime이 있으면 백엔드 데이터 사용
    if (triggerTime != null) {
        return try {
            // 백엔드 Timestamp 형식: "2025-11-13T07:34:50.320+00:00" (ISO 8601)
            val zonedDateTime = java.time.ZonedDateTime.parse(triggerTime, DateTimeFormatter.ISO_DATE_TIME)
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
            // 파싱 실패 시 로그 출력 후 0f 반환
            android.util.Log.e("MissionProgress", "triggerTime 파싱 실패: $triggerTime", e)
            0f
        }
    }

    // 2. triggerTime이 없으면 0f 반환
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

// 미션 상세 모달
@Composable
fun MissionDetailModal(
    mission: MissionNotificationData,
    isShowingAnimation: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(horizontal = Spacing.l, vertical = Spacing.xl),
        contentAlignment = Alignment.Center
    ) {
        DitoModalContainer(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color.White,
            borderColor = Color.Black,
            shadowColor = Color.Black,
            contentPadding = PaddingValues(vertical = Spacing.l)
        ) {

            //뒤로가기 버튼을 왼쪽에 정렬하기 위해
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.m, vertical = Spacing.s)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "뒤로가기",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopStart)
                        .clickable{onDismiss()}
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 제목
                Text(
                    text = mission.missionType,
                    color = Color.Black,
                    style = DitoTypography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = Spacing.m)
                )

                Spacer(modifier = Modifier.height(Spacing.l))

                // AI 피드백 섹션
                mission.feedback?.let { feedback ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.m)
                            .border(1.dp, Color.Black, DitoShapes.small)
                            .background(Background, DitoShapes.small)
                            .padding(Spacing.m)
                    ) {
                        Text(
                            text = "AI 피드백",
                            color = Primary,
                            style = DitoCustomTextStyles.titleDMedium
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = feedback,
                            color = Color.Black,
                            style = DitoTypography.bodyMedium,
                            lineHeight = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.l))
                }

                // 미션 결과
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.m),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "결과",
                        color = Color.Black,
                        style = DitoTypography.labelLarge
                    )

                    val resultText = when (mission.result) {
                        MissionResult.SUCCESS -> "성공"
                        MissionResult.FAILURE -> "실패"
                        MissionResult.IGNORE -> "무시됨"
                        else -> "진행 중"
                    }

                    Text(
                        text = resultText,
                        color = Color.Black,
                        style = DitoCustomTextStyles.titleDMedium
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.m))

                // 레몬 보상
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.m),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "보상",
                        color = Color.Black,
                        style = DitoTypography.labelLarge
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.lemon),
                            contentDescription = "Lemon",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${mission.coinReward}",
                            color = Color.Black,
                            style = DitoCustomTextStyles.titleDMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xl))

                // 확인 버튼 (성공/실패 모두 표시)
                if (mission.status == MissionStatus.COMPLETED) {
                    val isSuccess = mission.result == MissionResult.SUCCESS

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .hardShadow(
                                offsetX = 4.dp,
                                offsetY = 4.dp,
                                cornerRadius = 8.dp,
                                color = Color.Black
                            )
                            .clip(DitoShapes.small)
                            .border(1.dp, Color.Black, DitoShapes.small)
                            .background(if (isSuccess) Primary else Color.White)
                            .clickable(enabled = !isShowingAnimation) { onConfirm() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSuccess) {
                                Image(
                                    painter = painterResource(id = R.drawable.lemon),
                                    contentDescription = "Lemon",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (isSuccess) "레몬 받기" else "확인",
                                color = Color.Black,
                                style = DitoCustomTextStyles.titleDMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.m))
                }



//                Spacer(modifier = Modifier.height(Spacing.m))
            }
        }
    }
}

// 미리보기
@Preview(showBackground = true)
@Composable
fun PreviewMissionNotification() {
    MissionNotificationScaffold()
}