package com.dito.app.feature.report

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.dito.app.R
import com.dito.app.core.data.phone.MediaSessionEvent
import com.dito.app.core.data.report.ComparisonItem
import com.dito.app.core.data.report.ComparisonType
import com.dito.app.core.data.report.DiaryUiState
import com.dito.app.core.data.report.FeedbackReasons
import com.dito.app.core.data.report.RadarChartData
import com.dito.app.core.data.report.VideoFeedback
import com.dito.app.core.data.report.VideoFeedbackItem
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.asImageBitmap
import com.dito.app.core.ui.component.BalanceRadarChart
import com.dito.app.core.ui.designsystem.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DailyReportScreen(
    viewModel: DailyReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val diaryUiState by viewModel.diaryUiState.collectAsStateWithLifecycle()
    val showDebugTab by viewModel.showDebugTab.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    // 디토일지가 생성되지 않은 초기 상태에서는 loadDailyReport 호출하지 않음
    // 생성 버튼을 눌렀을 때만 로드

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopReportPolling()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Custom Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(Primary)
                .padding(horizontal = 32.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(28.dp)) // 왼쪽 공간 (뒤로가기 버튼 없음)
            Text(
                text = "디토일지",
                style = DitoTypography.headlineMedium,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            IconButton(onClick = { viewModel.toggleDebugTab() }) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Debug",
                    tint = if (showDebugTab) Color.Red else Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 디토일지 상태에 따라 화면 분기
            when (val state = diaryUiState) {
                is DiaryUiState.LoadingVideos -> {
                    // 영상 로딩 중
                    DiaryLoadingScreen(message = "피드백 영상을 불러오는 중...")
                }

                is DiaryUiState.FeedbackCollection -> {
                    // 피드백 수집 화면
                    FeedbackCollectionScreen(
                        videos = state.videos,
                        feedbacks = state.feedbacks,
                        onFeedbackUpdate = { videoId, isHelpful, reasons ->
                            viewModel.updateFeedback(videoId, isHelpful, reasons)
                        },
                        onGenerateDiary = {
                            viewModel.generateDiary()
                        }
                    )
                }

                is DiaryUiState.GeneratingDiary -> {
                    // 디토일지 생성 중
                    DiaryLoadingScreen(message = "디토일지를 생성하는 중...")
                }

                is DiaryUiState.DiaryGenerated -> {
                    // 디토일지 생성 완료 - 기존 리포트 화면 표시
                    DailyReportContent(
                        userName = state.reportData.userName,
                        costumeUrl = state.reportData.costumeUrl,
                        missionCompletionRate = state.reportData.missionCompletionRate,
                        currentStatus = state.reportData.currentStatus,
                        predictions = state.reportData.predictions,
                        comparisons = state.reportData.comparisons,
                        radarChartData = state.reportData.radarChartData,
                        advice = state.reportData.advice,
                        strategyChanges = state.reportData.strategyChanges
                    )
                }

                is DiaryUiState.Error -> {
                    // 에러 화면
                    DiaryErrorScreen(
                        message = state.message,
                        onRetry = {
                            viewModel.loadVideosForFeedback()
                        },
                        canRetry = state.canRetry
                    )
                }
            }

            // 디버그 모드용 오버레이 (디토일지 생성 후에만 표시)
            if (showDebugTab && diaryUiState is DiaryUiState.DiaryGenerated) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = Primary
                    ) {
                        Tab(
                            selected = pagerState.currentPage == 0,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(0)
                                }
                            },
                            text = { Text("리포트") }
                        )
                        Tab(
                            selected = pagerState.currentPage == 1,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(1)
                                }
                            },
                            text = { Text("디버그") }
                        )
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> {
                                when (val state = uiState) {
                                    is DailyReportUiState.Loading -> {
                                        Box(modifier = Modifier.fillMaxSize())
                                    }
                                    is DailyReportUiState.Success -> {
                                        DailyReportContent(
                                            userName = state.data.userName,
                                            costumeUrl = state.data.costumeUrl,
                                            missionCompletionRate = state.data.missionCompletionRate,
                                            currentStatus = state.data.currentStatus,
                                            predictions = state.data.predictions,
                                            comparisons = state.data.comparisons,
                                            radarChartData = state.data.radarChartData,
                                            advice = state.data.advice
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
                            1 -> {
                                MediaSessionDebugContent(viewModel = viewModel)
                            }
                        }
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
    comparisons: List<ComparisonItem>,
    radarChartData: RadarChartData?,
    advice: String,
    strategyChanges: List<com.dito.app.core.data.report.StrategyChange> = emptyList()
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(28.dp, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(
                start = 32.dp,
                end = 32.dp,
                top = 24.dp,
                bottom = 56.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            // 헤더 섹션
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    if (costumeUrl.isNotEmpty()) {
                        coil.compose.AsyncImage(
                            model = costumeUrl,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .width(53.dp)
                                .height(90.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.lemon_wiggle),
                            contentDescription = "Profile",
                            modifier = Modifier
                                .width(53.dp)
                                .height(90.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Image(
                        painter = painterResource(id = R.drawable.daily_report),
                        contentDescription = "Daily Report",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(172.dp)
                            .height(74.dp)
                    )
                }
            }

            // 미션 수행률 카드
            item {
                MissionCompletionCard(
                    missionCompletionRate = missionCompletionRate
                )
            }

            // 비교 분석 섹션 (종합 밸런스 분석)
            item {
                ComparisonCard(
                    comparisons = comparisons,
                    radarData = radarChartData
                )
            }

            // 현재 상태 섹션
            item {
                CurrentStatusCard(
                    userName = userName,
                    predictions = predictions
                )
            }

            // Dito의 메시지 섹션
            item {
                DitoMessageCard(advice = advice)
            }

            // 시간대별 전략 변경사항 섹션
            if (strategyChanges.isNotEmpty()) {
                item {
                    StrategyChangesCard(changes = strategyChanges)
                }
            }
        }
    }
}

@Composable
fun MissionCompletionCard(missionCompletionRate: Int) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(97.dp)
                .hardShadow(
                    offsetX = 4.dp,
                    offsetY = 4.dp,
                    cornerRadius = 8.dp,
                    color = Color.Black
                )
                .clip(shape = RoundedCornerShape(8.dp))
                .background(color = Color.White)
                .border(
                    border = BorderStroke(1.5.dp, Color.Black),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(all = 16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .background(color = Color.White)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(11.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.mission_star),
                        contentDescription = "Mission Icon",
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "미션 수행률",
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 0.91.em,
                        style = DitoCustomTextStyles.titleDLarge
                    )
                }
                Surface(
                    shape = RoundedCornerShape(48.dp),
                    border = BorderStroke(1.dp, Color.Black),
                    modifier = Modifier.clip(shape = RoundedCornerShape(48.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(23.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(missionCompletionRate / 100f)
                                .height(23.dp)
                                .clip(shape = RoundedCornerShape(48.dp))
                                .background(color = Primary)
                        )
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$missionCompletionRate",
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 1.12.em,
                    style = DitoTypography.displayLarge,
                    fontSize = 42.sp
                )
                Text(
                    text = "%",
                    color = Primary,
                    textAlign = TextAlign.Center,
                    lineHeight = 1.16.em,
                    style = DitoTypography.displayMedium,
                    fontSize = 42.sp
                )
            }
        }

        // Lemon wiggle image - top right
        Image(
            painter = painterResource(id = R.drawable.lemon_wiggle),
            contentDescription = "Lemon Wiggle",
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 12.dp)
        )
    }
}

@Composable
fun CurrentStatusCard(
    userName: String,
    predictions: List<String>
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .hardShadow(
                offsetX = 4.dp,
                offsetY = 4.dp,
                cornerRadius = 8.dp,
                color = Color.Black
            )
            .clip(shape = RoundedCornerShape(8.dp))
            .background(color = Color.White)
            .border(
                border = BorderStroke(1.5.dp, Color.Black),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Primary)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.now_status),
                    contentDescription = "Status Icon",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "현재 $userName 님은",
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 0.91.em,
                    style = DitoCustomTextStyles.titleDMedium
                )
            }

            // Orange wiggle image - top right
            Image(
                painter = painterResource(id = R.drawable.orange_wiggle),
                contentDescription = "Orange Wiggle",
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.TopEnd)
                    .padding(top = 6.dp, end = 12.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(color = Color.Black)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 27.dp)
        ) {
            Text(
                text = predictions.joinToString("\n"),
                color = Color.Black,
                lineHeight = 1.43.em,
                style = DitoTypography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun ComparisonCard(
    comparisons: List<ComparisonItem>,
    radarData: RadarChartData?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .hardShadow(
                offsetX = 4.dp,
                offsetY = 4.dp,
                cornerRadius = 8.dp,
                color = Color.Black
            )
            .clip(shape = RoundedCornerShape(8.dp))
            .background(color = Color.White)
            .border(
                border = BorderStroke(1.5.dp, Color.Black),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Primary)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.clock),
                    contentDescription = "Comparison Icon",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "종합 밸런스 분석",
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 0.91.em,
                    style = DitoCustomTextStyles.titleDMedium
                )
            }

            // Melon wiggle image - top right
            Image(
                painter = painterResource(id = R.drawable.melon_wiggle),
                contentDescription = "Melon Wiggle",
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.TopEnd)
                    .padding(top = 6.dp, end = 12.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(color = Color.Black)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            // Radar Chart 섹션
            if (radarData != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .align(Alignment.CenterHorizontally)
                ) {
                    BalanceRadarChart(
                        data = radarData,
                        modifier = Modifier.fillMaxWidth()
                        // fillColor는 기본값(Primary 노랑색) 사용
                    )
                }

                // 차트 설명
                Text(
                    text = "파란색은 이전, 빨강색은 현재 상태입니다.",
                    style = DitoTypography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                // 구분선
                HorizontalDivider(
                    thickness = 1.dp,
                    color = Color.LightGray.copy(alpha = 0.5f)
                )
            }

            // 기존 비교 리스트
            comparisons.forEach { comparison ->
                ComparisonItemCard(comparisonItem = comparison)
            }
        }
    }
}

@Composable
fun DitoMessageCard(advice: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .hardShadow(
                offsetX = 4.dp,
                offsetY = 4.dp,
                cornerRadius = 8.dp,
                color = Color.Black
            )
            .clip(shape = RoundedCornerShape(8.dp))
            .background(color = Color.White)
            .border(
                border = BorderStroke(1.5.dp, Color.Black),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Primary)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.letter),
                    contentDescription = "Message Icon",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Dito의 메시지",
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 0.91.em,
                    style = DitoCustomTextStyles.titleDMedium
                )
            }

            // Grape wiggle image - top right
            Image(
                painter = painterResource(id = R.drawable.grape_wiggle),
                contentDescription = "Grape Wiggle",
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.TopEnd)
                    .padding(top = 6.dp, end = 12.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(color = Color.Black)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 27.dp)
        ) {
            Text(
                text = advice,
                color = Color.Black,
                lineHeight = 1.43.em,
                style = DitoTypography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
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
        ComparisonType.POSITIVE -> Color(0xFFEBF5FF)
        ComparisonType.NEGATIVE -> Color(0xFFFFEBEB)
        ComparisonType.NEUTRAL -> Color.Gray.copy(alpha = 0.1f)
    }

    val borderColor = when (comparisonItem.type) {
        ComparisonType.POSITIVE -> Color(0xFF0080FF)
        ComparisonType.NEGATIVE -> Color(0xFFEC3E3E)
        ComparisonType.NEUTRAL -> Color.Gray
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
        else -> R.drawable.lemon
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = MaterialTheme.shapes.medium)
            .background(color = backgroundColor)
            .border(
                border = BorderStroke(1.dp, borderColor),
                shape = MaterialTheme.shapes.medium
            )
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .width(31.dp)
                .height(34.dp)
                .align(Alignment.Top)
        )
        Text(
            text = comparisonItem.description,
            color = textColor,
            lineHeight = 1.33.em,
            style = DitoTypography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

// 시간 포맷 유틸리티 함수
private fun formatWatchTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes}분 ${seconds}초"
}

private fun formatTimestamp(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(millis))
}

@Composable
fun MediaSessionDebugContent(
    viewModel: DailyReportViewModel
) {
    val mediaSessionEvents by viewModel.mediaSessionEvents.collectAsStateWithLifecycle()
    val debugFilter by viewModel.debugFilter.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 필터 버튼들
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = debugFilter == DebugFilter.ALL,
                onClick = { viewModel.setDebugFilter(DebugFilter.ALL) },
                label = { Text("전체") }
            )
            FilterChip(
                selected = debugFilter == DebugFilter.TODAY,
                onClick = { viewModel.setDebugFilter(DebugFilter.TODAY) },
                label = { Text("오늘") }
            )
            FilterChip(
                selected = debugFilter == DebugFilter.UNSYNCED,
                onClick = { viewModel.setDebugFilter(DebugFilter.UNSYNCED) },
                label = { Text("미동기화") }
            )
            FilterChip(
                selected = debugFilter == DebugFilter.YOUTUBE,
                onClick = { viewModel.setDebugFilter(DebugFilter.YOUTUBE) },
                label = { Text("YouTube") }
            )
        }

        // 통계 헤더
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("총 이벤트:", style = DitoTypography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("${mediaSessionEvents.size}개", style = DitoTypography.bodyMedium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("동기화 완료:", style = DitoTypography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("${mediaSessionEvents.count { it.synced }}개", style = DitoTypography.bodyMedium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("미동기화:", style = DitoTypography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("${mediaSessionEvents.count { !it.synced }}개", style = DitoTypography.bodyMedium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("교육적:", style = DitoTypography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("${mediaSessionEvents.count { it.isEducational }}개", style = DitoTypography.bodyMedium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("썸네일 있음:", style = DitoTypography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("${mediaSessionEvents.count { it.thumbnailUri.isNotBlank() }}개", style = DitoTypography.bodyMedium)
                }
            }
        }

        // 새로고침 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { viewModel.refreshDebugData() }) {
                Icon(Icons.Default.Refresh, "새로고침")
            }
        }

        // MediaSessionEvent 리스트
        if (mediaSessionEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "데이터가 없습니다",
                    style = DitoTypography.bodyLarge,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mediaSessionEvents) { event ->
                    MediaSessionEventCard(event = event)
                }
            }
        }
    }
}

@Composable
fun MediaSessionEventCard(event: MediaSessionEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (event.synced) Color.White else Color(0xFFFFF9C4)
        ),
        border = BorderStroke(1.dp, Color.Black)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 썸네일 이미지 (있는 경우에만 표시)
            if (event.thumbnailUri.isNotBlank()) {
                AsyncImage(
                    model = event.thumbnailUri,
                    contentDescription = "Video Thumbnail",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(4.dp))
            } else {
                // 썸네일이 없는 경우 디버그 정보 표시
                Text(
                    text = "썸네일 없음 (기존 데이터 또는 추출 실패)",
                    style = DitoTypography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // 이벤트 타입 & 동기화 상태
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = when (event.eventType) {
                        "VIDEO_START" -> Color(0xFF4CAF50)
                        "VIDEO_PAUSE" -> Color(0xFFFFC107)
                        "VIDEO_END" -> Color(0xFFF44336)
                        else -> Color.Gray
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = event.eventType,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = DitoTypography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (event.synced) {
                        Text("✓ 동기화", style = DitoTypography.labelSmall, color = Color(0xFF4CAF50))
                    } else {
                        Text("✗ 미동기화", style = DitoTypography.labelSmall, color = Color(0xFFF44336))
                    }
                    if (event.isEducational) {
                        Text("📚 교육", style = DitoTypography.labelSmall, color = Color(0xFF2196F3))
                    }
                    if (event.aiCalled) {
                        Text("🤖 AI", style = DitoTypography.labelSmall, color = Color(0xFF9C27B0))
                    }
                }
            }

            HorizontalDivider(color = Color.LightGray)

            // 제목 & 채널
            if (event.title.isNotBlank()) {
                Text(
                    text = event.title,
                    style = DitoTypography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
            }
            if (event.channel.isNotBlank()) {
                Text(
                    text = "채널: ${event.channel}",
                    style = DitoTypography.bodySmall,
                    color = Color.Gray
                )
            }

            // 앱 패키지
            Text(
                text = "앱: ${event.appPackage}",
                style = DitoTypography.bodySmall,
                color = Color.Gray
            )

            // 시청 시간 정보
            if (event.watchTime > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "시청 시간:",
                        style = DitoTypography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatWatchTime(event.watchTime),
                        style = DitoTypography.bodySmall
                    )
                }
            }
            if (event.pauseTime > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "일시정지 시간:",
                        style = DitoTypography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatWatchTime(event.pauseTime),
                        style = DitoTypography.bodySmall
                    )
                }
            }

            // 타임스탬프
            Text(
                text = "시간: ${formatTimestamp(event.timestamp)}",
                style = DitoTypography.labelSmall,
                color = Color.Gray
            )

            // ObjectId
            Text(
                text = "ID: ${event._id.toHexString()}",
                style = DitoTypography.labelSmall,
                color = Color.Gray,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )

            // Thumbnail URI (디버그용)
            if (event.thumbnailUri.isNotBlank()) {
                Text(
                    text = "썸네일 URI: ${event.thumbnailUri}",
                    style = DitoTypography.labelSmall,
                    color = Color.Blue,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    maxLines = 2
                )
            }
        }
    }
}

// ========== 디토일지 피드백 수집 화면 ==========

/**
 * 피드백 수집 메인 화면
 */
@Composable
fun FeedbackCollectionScreen(
    videos: List<VideoFeedbackItem>,
    feedbacks: Map<String, VideoFeedback>,
    onFeedbackUpdate: (String, Boolean?, Set<String>) -> Unit,
    onGenerateDiary: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = Spacing.l, vertical = Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.l)
        ) {
            // 헤더: 주요 영상 목록 설명
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Text(
                        text = "피드백 필요 영상 목록",
                        style = DitoCustomTextStyles.titleDLarge,
                        color = Color.Black
                    )
                    Text(
                        text = "에이전트의 알고리즘을 향상시키기위해서 오늘 봤던 영상에서 피드백을 주세요!",
                        style = DitoTypography.bodyMedium,
                        color = OnSurfaceVariant,
                        lineHeight = 1.43.em
                    )
                }
            }

            // 영상 피드백 카드들
            items(videos.size) { index ->
                val video = videos[index]
                val feedback = feedbacks[video.id]

                VideoFeedbackCard(
                    video = video,
                    feedback = feedback,
                    onFeedbackUpdate = { isHelpful, reasons ->
                        onFeedbackUpdate(video.id, isHelpful, reasons)
                    }
                )
            }
        }

        // 디토일지 생성 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(Spacing.l)
        ) {
            GenerateDiaryButton(
                enabled = feedbacks.values.any { it.isHelpful != null },
                onClick = onGenerateDiary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 개별 영상 피드백 카드
 */
@Composable
fun VideoFeedbackCard(
    video: VideoFeedbackItem,
    feedback: VideoFeedback?,
    onFeedbackUpdate: (Boolean?, Set<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var localIsHelpful by remember(feedback) { mutableStateOf(feedback?.isHelpful) }
    var localSelectedReasons by remember(feedback) {
        mutableStateOf(feedback?.selectedReasons ?: emptySet())
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .hardShadow(
                offsetX = 4.dp,
                offsetY = 4.dp,
                cornerRadius = 8.dp,
                color = Color.Black
            )
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(BorderStroke(1.5.dp, Color.Black), RoundedCornerShape(8.dp))
            .padding(Spacing.m)
    ) {
        // 상단: 썸네일 + 제목 + 도움됨/도움안됨 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            // 썸네일
            val imageBytes = Base64.decode(video.thumbnailBase64, Base64.DEFAULT)
            val bitmap = remember(video.thumbnailBase64) {
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            }

            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Video Thumbnail",
                    modifier = Modifier
                        .size(width = 120.dp, height = 90.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(width = 120.dp, height = 90.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("썸네일", style = DitoTypography.bodySmall, color = Color.Gray)
                }
            }

            // 제목 + 버튼
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = video.title,
                    style = DitoTypography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 2
                )
                Text(
                    text = video.channel,
                    style = DitoTypography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "${video.watchTimeMinutes}분 시청",
                    style = DitoTypography.labelSmall,
                    color = OnSurfaceVariant
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                // 도움됨/도움안됨 버튼
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    FeedbackButton(
                        text = "도움됨",
                        isSelected = localIsHelpful == true,
                        onClick = {
                            val newValue = if (localIsHelpful == true) null else true
                            localIsHelpful = newValue
                            if (newValue == null) {
                                localSelectedReasons = emptySet()
                            }
                            onFeedbackUpdate(newValue, localSelectedReasons)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FeedbackButton(
                        text = "도움안됨",
                        isSelected = localIsHelpful == false,
                        onClick = {
                            val newValue = if (localIsHelpful == false) null else false
                            localIsHelpful = newValue
                            if (newValue == null) {
                                localSelectedReasons = emptySet()
                            }
                            onFeedbackUpdate(newValue, localSelectedReasons)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 하단: 상세 이유 옵션 (선택 시에만 표시)
        if (localIsHelpful != null) {
            Spacer(modifier = Modifier.height(Spacing.m))
            HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(Spacing.m))

            FeedbackReasonOptions(
                isHelpful = localIsHelpful!!,
                selectedReasons = localSelectedReasons,
                onReasonsChange = { newReasons ->
                    localSelectedReasons = newReasons
                    onFeedbackUpdate(localIsHelpful, newReasons)
                }
            )
        }
    }
}

/**
 * 도움됨/도움안됨 버튼
 */
@Composable
fun FeedbackButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(DitoShapes.small)
            .background(if (isSelected) Primary else Color.White)
            .border(1.dp, Color.Black, DitoShapes.small)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.xs, vertical = Spacing.xs),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.Black,
            style = DitoTypography.labelMedium,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 피드백 이유 선택 옵션 (다중 선택 체크박스)
 */
@Composable
fun FeedbackReasonOptions(
    isHelpful: Boolean,
    selectedReasons: Set<String>,
    onReasonsChange: (Set<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = if (isHelpful) {
        FeedbackReasons.HELPFUL_OPTIONS
    } else {
        FeedbackReasons.UNHELPFUL_OPTIONS
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Text(
            text = if (isHelpful) "어떤 점이 도움됐나요?" else "어떤 점이 문제였나요?",
            style = DitoTypography.labelMedium,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )

        // 4개 행으로 나열
        options.forEach { option ->
            ReasonCheckbox(
                text = option,
                isChecked = selectedReasons.contains(option),
                onCheckedChange = { isChecked ->
                    val newReasons = if (isChecked) {
                        selectedReasons + option
                    } else {
                        selectedReasons - option
                    }
                    onReasonsChange(newReasons)
                }
            )
        }
    }
}

/**
 * 이유 선택 체크박스
 */
@Composable
fun ReasonCheckbox(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(DitoShapes.small)
            .background(if (isChecked) Primary.copy(alpha = 0.3f) else Background)
            .border(
                1.dp,
                if (isChecked) Color.Black else Color.Gray,
                DitoShapes.small
            )
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = Spacing.xs, vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(if (isChecked) Primary else Color.White)
                .border(1.dp, Color.Black, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isChecked) {
                Text(
                    text = "✓",
                    style = DitoTypography.labelSmall,
                    color = Color.Black,
                    fontSize = 10.sp
                )
            }
        }
        Text(
            text = text,
            style = DitoTypography.labelSmall,
            color = Color.Black,
            maxLines = 2,
            fontSize = 11.sp,
            lineHeight = 1.2.em
        )
    }
}

/**
 * 디토일지 생성 버튼
 */
@Composable
fun GenerateDiaryButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .hardShadow(
                offsetX = 4.dp,
                offsetY = 4.dp,
                cornerRadius = 8.dp,
                color = if (enabled) Color.Black else Color.Gray
            )
            .clip(DitoShapes.small)
            .border(1.dp, Color.Black, DitoShapes.small)
            .background(if (enabled) Primary else Background)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "디토일지 생성하기",
            color = Color.Black,
            style = DitoCustomTextStyles.titleDMedium
        )
    }
}

/**
 * 디토일지 생성 중 로딩 화면
 */
@Composable
fun DiaryLoadingScreen(
    modifier: Modifier = Modifier,
    message: String = "디토일지를 생성하고 있습니다..."
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.l)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = Primary,
                strokeWidth = 6.dp
            )
            Text(
                text = message,
                style = DitoTypography.bodyLarge,
                color = Color.Black
            )
            Text(
                text = "잠시만 기다려주세요",
                style = DitoTypography.bodyMedium,
                color = OnSurfaceVariant
            )
        }
    }
}

/**
 * 디토일지 에러 화면
 */
@Composable
fun DiaryErrorScreen(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    canRetry: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(Spacing.l),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = DitoTypography.bodyLarge,
            color = OnSurface,
            textAlign = TextAlign.Center
        )
        if (canRetry) {
            Spacer(modifier = Modifier.height(Spacing.m))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = Color.Black
                )
            ) {
                Text("다시 시도")
            }
        }
    }
}

/**
 * 시간대별 전략 변경사항 카드
 */
@Composable
fun StrategyChangesCard(
    changes: List<com.dito.app.core.data.report.StrategyChange>,
    modifier: Modifier = Modifier
) {
    if (changes.isEmpty()) return

    Column(
        modifier = modifier
            .hardShadow(offsetX = 4.dp, offsetY = 4.dp, cornerRadius = 8.dp, color = Color.Black)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(BorderStroke(1.5.dp, Color.Black), RoundedCornerShape(8.dp))
    ) {
        // 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Primary)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🤖 시간대별 전략, 변경사항",
                style = DitoTypography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Image(
                painter = painterResource(R.drawable.lemon_wiggle),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.5.dp).background(Color.Black))

        // 변경사항 리스트
        Column(modifier = Modifier.padding(16.dp)) {
            changes.forEachIndexed { index, change ->
                StrategyChangeItem(change)

                if (index < changes.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        thickness = 1.dp,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}

/**
 * 개별 전략 변경사항 아이템
 */
@Composable
private fun StrategyChangeItem(change: com.dito.app.core.data.report.StrategyChange) {
    val timeSlot = com.dito.app.core.data.report.TimeSlot.from(change.timeSlot)
    val prevMode = com.dito.app.core.data.report.StrategyMode.from(change.previous)
    val currMode = com.dito.app.core.data.report.StrategyMode.from(change.current)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 시간대
        Text(
            text = "${timeSlot.toEmoji()} ${timeSlot.toDisplayName()}",
            style = DitoTypography.titleMedium
        )

        // 모드 변경
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModeBadge(mode = prevMode)
            Text("→", style = DitoTypography.headlineMedium)
            ModeBadge(mode = currMode)
        }

        // 변경 이유
        Text(
            text = change.reason,
            style = DitoTypography.bodyMedium,
            color = Color.Gray
        )
    }
}

/**
 * 모드 배지
 */
@Composable
private fun ModeBadge(mode: com.dito.app.core.data.report.StrategyMode) {
    Row(
        modifier = Modifier
            .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(mode.toIconRes()),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = mode.toDisplayName(),
            style = DitoTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}
