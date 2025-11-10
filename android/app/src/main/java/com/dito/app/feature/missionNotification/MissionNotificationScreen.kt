package com.dito.app.feature.missionNotification

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.dito.app.R
import com.dito.app.core.ui.component.BottomTab
import com.dito.app.core.ui.component.DitoBottomAppBar
import com.dito.app.core.ui.designsystem.Background
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.OnSurface

// 알림 아이템 데이터
data class MissionNotification(
    val id: Int,
    val title: String,
    val description: String,
    val type: NotificationType
)

// 상태 뱃지 타입
enum class NotificationType(val label: String, val color: Color) {
    COMPLETE("진행중", Color(0xFFB39DDB)),  // 보라색
    WARNING("완료", Color(0xFFFFF59D)),     // 노란색
    ERROR("실패", Color(0xFFFFCDD2))        // 분홍색
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
    onBackClick: () -> Unit = {}
) {
    val notifications = remember {
        listOf(
            MissionNotification(
                id = 1,
                title = "인스타그램 30분 안 보기",
                description = "30 레몬",
                type = NotificationType.COMPLETE
            ),
            MissionNotification(
                id = 2,
                title = "인스타그램 30분 안 보기",
                description = "30 레몬",
                type = NotificationType.WARNING
            ),
            MissionNotification(
                id = 3,
                title = "인스타그램 30분 안 보기",
                description = "30 레몬",
                type = NotificationType.ERROR
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        MissionNotificationHeader(onBackClick = onBackClick)

        Spacer(modifier = Modifier.height(40.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            notifications.forEach { notification ->
                NotificationItem(notification = notification)
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
fun NotificationItem(notification: MissionNotification) {
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
                    text = notification.title,
                    color = OnSurface,
                    // style = DitoTypography.titleLarge.copy(fontSize = 16.sp)
                    style = DitoCustomTextStyles.titleKMedium.copy(fontSize = 16.sp)
                )

                // 제목과 설명 간격
                Spacer(modifier = Modifier.height(15.dp))

                // 설명 (30 레몬)
                Text(
                    text = notification.description,
                    color = OnSurface,
                    style = DitoTypography.bodyMedium.copy(fontSize = 14.sp)
                )
            }

            Spacer(modifier = Modifier.width(15.dp))

            StatusBadge(type = notification.type)
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
            .widthIn(min = 80.dp)
            .background(type.color, RoundedCornerShape(20.dp))
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