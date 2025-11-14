package com.dito.app.feature.missionNotification

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dito.app.R
import com.dito.app.core.data.missionNotification.MissionNotificationData
import com.dito.app.core.data.missionNotification.MissionResult
import com.dito.app.core.data.missionNotification.MissionStatus
import com.dito.app.core.ui.component.BottomTab
import com.dito.app.core.ui.component.DitoBottomAppBar
import com.dito.app.core.ui.designsystem.Background
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.OnSurface

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
                            NotificationItem(notification = notification)
                        }
                    }
                }
            }
        }
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
fun NotificationItem(notification: MissionNotificationData) {
    val notificationType = getNotificationType(notification.status, notification.result)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 텍스트 + 뱃지 영역
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 35.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // 제목
                Text(
                    text = notification.missionText,
                    color = OnSurface,
                    style = DitoCustomTextStyles.titleKMedium.copy(fontSize = 16.sp)
                )

                // 제목과 설명 간격
                Spacer(modifier = Modifier.height(15.dp))

                // 설명 (코인 보상)
                Text(
                    text = "${notification.coinReward} 레몬",
                    color = OnSurface,
                    style = DitoTypography.bodyMedium.copy(fontSize = 14.sp)
                )
            }

            Spacer(modifier = Modifier.width(15.dp))

            StatusBadge(type = notificationType)
        }

        // 구분선
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(1.dp)
                .background(OnSurface.copy(alpha = 0.1f))
                .background(Color.Black)

        )
    }
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