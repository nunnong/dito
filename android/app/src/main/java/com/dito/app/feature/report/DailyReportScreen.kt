package com.dito.app.feature.report

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.dito.app.R
import com.dito.app.core.data.report.ComparisonItem
import com.dito.app.core.data.report.ComparisonType
import com.dito.app.core.ui.designsystem.*

@Composable
fun DailyReportScreen(
    viewModel: DailyReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadDailyReport()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is DailyReportUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is DailyReportUiState.Success -> {
                DailyReportContent(
                    userName = state.data.userName,
                    costumeUrl = state.data.costumeUrl,
                    missionCompletionRate = state.data.missionCompletionRate,
                    currentStatus = state.data.currentStatus,
                    predictions = state.data.predictions,
                    comparisons = state.data.comparisons
                )
            }
            is DailyReportUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Spacing.l),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.message,
                        style = DitoTypography.bodyLarge,
                        color = OnSurface
                    )
                    Spacer(modifier = Modifier.height(Spacing.m))
                    Button(onClick = { viewModel.loadDailyReport() }) {
                        Text("다시 시도")
                    }
                }
            }
        }
    }
}

@Composable
fun DailyReportContent(
    userName: String,
    costumeUrl: String,
    missionCompletionRate: Int,
    currentStatus: com.dito.app.core.data.report.StatusDescription,
    predictions: List<String>,
    comparisons: List<ComparisonItem>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(0.dp)
    ) {
        // 헤더 섹션
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.l)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = Spacing.m)
                ) {
                    if (costumeUrl.isNotEmpty()) {
                        coil.compose.AsyncImage(
                            model = costumeUrl,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
//                                .background(Primary),
                                    ,
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.width(Spacing.m))
                    Text(
                        text = "Daily Report",
                        style = DitoTypography.headlineLarge,
                        color = OnSurface,
                        fontSize = 38.sp
                    )
                }

                // 미션 수행률 카드
                MissionCompletionCard(
                    missionCompletionRate = missionCompletionRate
                )
            }
        }

        // 현재 상태 섹션 + 비교 분석 섹션 (하나의 카드로)
        item {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.l)
                    .hardShadow(
                        offsetX = 4.dp,
                        offsetY = 4.dp,
                        cornerRadius = 32.dp,
                        color = Color.Black
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.l)
                ) {
                    // 현재 상태 섹션
                    Text(
                        text = "현재 $userName 님은",
                        style = DitoTypography.titleLarge,
                        color = OnSurface,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.height(Spacing.s))

                    predictions.forEach { prediction ->
                        Text(
                            text = prediction,
                            style = DitoTypography.bodyLarge,
                            color = OnSurface,
                            lineHeight = 24.sp,
                            modifier = Modifier.padding(bottom = Spacing.s)
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.l))

                    // 비교 분석 섹션
                    Text(
                        text = "이전과 비교했을 때...",
                        style = DitoTypography.titleLarge,
                        color = OnSurface,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(bottom = Spacing.s)
                    )

                    // 비교 항목들
                    comparisons.forEach { comparison ->
                        ComparisonItemCard(
                            comparisonItem = comparison,
                            modifier = Modifier.padding(vertical = Spacing.xs)
                        )
                    }
                }
            }
        }

        // 하단 여백
        item {
            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

@Composable
fun MissionCompletionCard(missionCompletionRate: Int) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.m)
            .hardShadow(
                offsetX = 4.dp,
                offsetY = 4.dp,
                cornerRadius = 32.dp,
                color = Color.Black
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.m)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = Spacing.s)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.check),
                        contentDescription = "Mission Icon",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = "미션 수행률",
                        style = DitoTypography.titleMedium,
                        color = OnSurface,
                        fontSize = 22.sp
                    )
                }

                // 진행률 바
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            width = 0.6.dp,
                            color = OnTertiaryContainer,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .background(Color(0xFFF0F0F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(missionCompletionRate / 100f)
                            .background(
                                Primary,
                                shape = RoundedCornerShape(10.dp)
                            )
                    )
                }
            }

            // 퍼센트 표시
            Text(
                text = "$missionCompletionRate%",
                style = DitoTypography.displayLarge,
                color = OnSurface,
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = Spacing.xs)
            )
        }
    }
}

@Composable
fun ComparisonItemCard(
    comparisonItem: ComparisonItem,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (comparisonItem.type) {
        ComparisonType.POSITIVE -> Color(0xFF0080FF).copy(alpha = 0.05f)
        ComparisonType.NEGATIVE -> Color.Red.copy(alpha = 0.05f)
        ComparisonType.NEUTRAL -> Color.Gray.copy(alpha = 0.05f)
    }

    val textColor = when (comparisonItem.type) {
        ComparisonType.POSITIVE -> Color(0xFF0080FF)
        ComparisonType.NEGATIVE -> Color(0xFFEC3E3E)
        ComparisonType.NEUTRAL -> OnSurface
    }

    val iconRes = when (comparisonItem.iconRes) {
        "phone" -> R.drawable.report_phone
        "self_control" -> R.drawable.self_control
        "sleep" -> R.drawable.sleep
        else -> R.drawable.report_phone // 기본값
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.m)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = Spacing.s)
            )

            Text(
                text = comparisonItem.description,
                style = DitoTypography.bodyLarge,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
