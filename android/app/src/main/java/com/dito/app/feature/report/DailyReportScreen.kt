package com.dito.app.feature.report

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.em
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
                    comparisons = state.data.comparisons,
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
}

@Composable
fun DailyReportContent(
    userName: String,
    costumeUrl: String,
    missionCompletionRate: Int,
    currentStatus: com.dito.app.core.data.report.StatusDescription,
    predictions: List<String>,
    comparisons: List<ComparisonItem>,
    advice: String
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
                top = 48.dp,
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

            // 현재 상태 섹션
            item {
                CurrentStatusCard(
                    userName = userName,
                    predictions = predictions
                )
            }

            // 비교 분석 섹션
            item {
                ComparisonCard(
                    comparisons = comparisons
                )
            }

            // Dito의 메시지 섹션
            item {
                DitoMessageCard(advice = advice)
            }
        }
    }
}

@Composable
fun MissionCompletionCard(missionCompletionRate: Int) {
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
                fontSize = 54.sp
            )
            Text(
                text = "%",
                color = Primary,
                textAlign = TextAlign.Center,
                lineHeight = 1.16.em,
                style = DitoTypography.displayMedium,
                fontSize = 54.sp
            )
        }
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
    comparisons: List<ComparisonItem>
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
                text = "이전과 비교해서",
                color = Color.Black,
                textAlign = TextAlign.Center,
                lineHeight = 0.91.em,
                style = DitoCustomTextStyles.titleDMedium
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
        )
        Text(
            text = comparisonItem.description,
            color = textColor,
            lineHeight = 1.33.em,
            style = DitoTypography.bodyMedium,
            modifier = Modifier
                .weight(1f)
                .height(41.dp)
                .wrapContentHeight(align = Alignment.CenterVertically)
        )
    }
}
