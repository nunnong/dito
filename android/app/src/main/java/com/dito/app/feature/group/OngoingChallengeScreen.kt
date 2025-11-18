package com.dito.app.feature.group
import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.key
import androidx.compose.ui.graphics.asImageBitmap
import coil.request.ImageRequest
import coil.request.SuccessResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import coil.ImageLoader
import com.dito.app.R
import com.dito.app.core.ui.designsystem.BounceClickable
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.StrokeText
import com.dito.app.core.ui.designsystem.playWiggleSound
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.rotate
import androidx.core.graphics.rotationMatrix
import com.dito.app.core.ui.designsystem.DitoShapes
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.widget.FrameLayout
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex


@Composable
fun StatisticsCard(
    uiState: OngoingChallengeUiState,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
            .clickable { isExpanded = !isExpanded }
            .padding(16.dp)
    ) {
        // 헤더 (항상 표시)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📊 오늘의 현황",
                style = DitoTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
            Text(
                text = if (isExpanded) "▲ 접기" else "▼ 탭해서 펼치기",
                style = DitoTypography.labelSmall,
                color = Color.Gray
            )
        }

        // 확장된 내용
        if (isExpanded) {
            Spacer(modifier = Modifier.height(12.dp))

            androidx.compose.material3.HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 성공/실패 일수 (현재는 placeholder - 백엔드 데이터 필요)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "💪 목표 달성",
                    style = DitoTypography.bodyMedium,
                    color = Color.Black
                )
                Text(
                    text = "- 일",  // TODO: 백엔드 데이터
                    style = DitoTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "😢 목표 실패",
                    style = DitoTypography.bodyMedium,
                    color = Color.Black
                )
                Text(
                    text = "- 일",  // TODO: 백엔드 데이터
                    style = DitoTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFFF5252)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1등과의 차이
            val myRanking = uiState.rankings.find { it.isMe }
            val firstPlace = uiState.rankings.firstOrNull { it.rank == 1 }
            if (myRanking != null && firstPlace != null && myRanking.rank > 1) {
                val mySeconds = uiState.realTimeScreenTimes[myRanking.userId] ?: 0
                val firstSeconds = uiState.realTimeScreenTimes[firstPlace.userId] ?: 0
                val gapMinutes = (mySeconds - firstSeconds) / 60

                if (gapMinutes > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🏆 1등까지",
                            style = DitoTypography.bodyMedium,
                            color = Color.Black
                        )
                        Text(
                            text = "${gapMinutes}분 차이",
                            style = DitoTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFF9800)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // 오늘 남은 여유
            if (uiState.goalMinutes > 0 && myRanking != null) {
                val mySeconds = uiState.realTimeScreenTimes[myRanking.userId] ?: 0
                val myMinutes = mySeconds / 60
                val remaining = uiState.goalMinutes - myMinutes

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⏰ 오늘 남은 여유",
                        style = DitoTypography.bodyMedium,
                        color = Color.Black
                    )
                    Text(
                        text = if (remaining > 0) "${remaining}분" else "목표 초과!",
                        style = DitoTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (remaining > 0) Color(0xFF4CAF50) else Color(0xFFFF5252)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }



            // 친구 상태 (스크롤 가능)
            Spacer(modifier = Modifier.height(12.dp))

            val youtubeUsers = uiState.rankings.filter {
                !it.isMe && it.currentAppPackage?.contains("com.google.android.youtube", ignoreCase = true) == true
            }

            if (youtubeUsers.isNotEmpty()) {
                Text(
                    text = "👥 지금 YouTube 사용 중:",
                    style = DitoTypography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                youtubeUsers.forEach { user ->
                    val seconds = uiState.realTimeScreenTimes[user.userId] ?: 0
                    val timeStr = formatSecondsToTime(seconds)
                    Text(
                        text = "🔥 ${user.nickname} - ${timeStr}",
                        style = DitoTypography.bodySmall,
                        color = Color(0xFFFF5252)
                    )
                }
            } else {
                Text(
                    text = "🔥 지금 다른 친구들은 YouTube를 사용하지 않고 있어!",
                    style = DitoTypography.bodyMedium,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}


@Composable
fun ProgressHUD(
    currentMinutes: Int,
    goalMinutes: Int,
    modifier: Modifier = Modifier
) {
    if (goalMinutes <= 0) return

    val progress = (currentMinutes.toFloat() / goalMinutes.toFloat()).coerceAtMost(1.5f)

    // 진행률에 따른 색상 (0-70% 녹색, 70-100% 노란색, 100%+ 빨간색)
    val progressColor = when {
        progress < 0.7f -> Color(0xFF4CAF50)  // 녹색
        progress < 1.0f -> Color(0xFFFFEB3B)  // 노란색
        else -> Color(0xFFFF5252)  // 빨간색
    }


    val infiniteTransition = rememberInfiniteTransition(label = "hud_hover")
    val hoverOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hoverOffset"
    )

    Column(
        modifier = modifier.offset(y = hoverOffset.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 진행률 바
        Box(
            modifier = Modifier
                .width(70.dp)
                .height(8.dp)
                .background(Color(0xFF333333), RoundedCornerShape(4.dp))
                .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceAtMost(1.0f))
                    .background(progressColor, RoundedCornerShape(4.dp))
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 텍스트 표시
        StrokeText(
            text = "${currentMinutes}/${goalMinutes}분",
            style = DitoTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
            fillColor = Color.White,
            strokeColor = Color.Black,
            strokeWidth = 1.dp,
            textAlign = TextAlign.Center
        )

        // 목표 초과시 경고
        if (progress >= 1.0f) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .background(Color(0xFFFF5252), RoundedCornerShape(8.dp))
                    .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Goal Over!",
                    style = DitoTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}



@Composable
fun AICoachSpeechBubble(
    message: String,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    if (message.isEmpty()) return

    var showBubble by remember { mutableStateOf(false) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            showBubble = false
            delay(250L)
            showBubble = true
        } else {
            showBubble = false
        }
    }

    // 전체 페이드 인/아웃
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "fadeAnimation"
    )

    // 살짝 위아래 둥둥 떠다니는 효과
    val infiniteTransition = rememberInfiniteTransition(label = "bubble_hover")
    val hoverOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hoverOffset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            // 말풍선 (위에 배치, 가운데 정렬)
            val bubbleAlpha by animateFloatAsState(
                targetValue = if (showBubble) 1f else 0f,
                animationSpec = tween(durationMillis = 200),
                label = "bubbleAlpha"
            )

            Box(
                modifier = Modifier
                    .alpha(bubbleAlpha)
                    .offset(y = hoverOffset.dp)
                    .widthIn(max = 280.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(2.dp, Color.Black, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message,
                    style = DitoTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(7.dp))

            // 농장주인 이미지 (아래, 가운데 고정)
            Image(
                painter = painterResource(id = R.drawable.fruit_owner),
                contentDescription = "AI Coach",
                modifier = Modifier
                    .size(90.dp)
                    .offset(y = hoverOffset.dp)

            )
        }
    }
}


@Composable
fun OngoingChallengeScreen(
    viewModel: OngoingChallengeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    var isInfoPanelVisible by remember { mutableStateOf(false) }
    var isChallengeGuideVisible by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.startAutoRefresh()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopAutoRefresh()
            viewModel.resetPokeBubble()
        }
    }

    val rankings = uiState.rankings

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
        // 상단 레몬나무 배경 이미지
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(410f / 635f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.test),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // 정보 아이콘 (우측 상단)
            BounceClickable(
                onClick = { isChallengeGuideVisible = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) { isPressed ->
                Image(
                    painter = painterResource(id = R.drawable.question),
                    contentDescription = "Info",
                    modifier = Modifier.size(32.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Color.Black)
                )
            }

            // 나무 팻말 (우측 로프 상단)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = 150.dp)
                    .width(210.dp)
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.wooden_sign),
                    contentDescription = "Betting Sign",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = 10.dp, y = (-18).dp).rotate(20f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,

                ) {
                    Image(
                        painter = painterResource(id = R.drawable.lemon),
                        contentDescription = "Lemon",
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.Fit
                    )
//                    Spacer(modifier = Modifier.width(4.dp))
                    StrokeText(
                        text = "x${uiState.totalBetting}",
                        style = DitoTypography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        fillColor = Color(0xFFFFF8DC),
                        strokeColor = Color(0xFF3E2723),
                        strokeWidth = 1.dp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 그룹 정보
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-20).dp, y = (-15).dp)
                    .width(200.dp)
                    .height(150.dp)
                    .clickable { isInfoPanelVisible = !isInfoPanelVisible },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.challenge),
                        contentDescription = "Challenge Sign",
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Fit
                    )

                    StrokeText(
                        text = uiState.groupName,
                        style = DitoTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        fillColor = Color.White,
                        strokeColor = Color.Black,
                        strokeWidth = 2.dp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp).offset(y = 5.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.Bottom
            ) {
                // 처음 위치 순서대로 캐릭터 표시 (순위가 바뀌어도 줄 위치는 고정)
                // 항상 4개의 슬롯 유지
                val displayOrder = uiState.initialUserOrder.take(4)
                repeat(4) { index ->
                    if (index < displayOrder.size) {
                        val userId = displayOrder[index]
                        val rankingItem = rankings.find { it.userId == userId }
                        if (rankingItem != null) {
                            key(rankingItem.userId) {
                                val currentSeconds = uiState.realTimeScreenTimes[rankingItem.userId] ?: 0
                                val currentMinutes = currentSeconds / 60
                                CharacterView(
                                    costumeItemId = rankingItem.costumeItemId,
                                    rank = rankingItem.rank,
                                    maxRank = rankings.size.coerceAtMost(4),
                                    currentAppPackage = rankingItem.currentAppPackage,
                                    isMe = rankingItem.isMe,
                                    showPokeBubble = uiState.pokedUserIds.contains(rankingItem.userId),
                                    currentMinutes = currentMinutes,
                                    goalMinutes = uiState.goalMinutes,
                                    onClick = {
                                        if (!rankingItem.isMe) {
                                            viewModel.pokeMember(rankingItem.userId)
                                        }
                                    }
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(60.dp))
                        }
                    } else {
                        // 빈 슬롯
                        Spacer(modifier = Modifier.width(60.dp))
                    }

                }
            }
            // 레몬 나무 옆에서 샥 등장하는 AI 코치
            AICoachSpeechBubble(
                message = uiState.coachMessage,
                isVisible = uiState.showCoachBubble,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 90.dp)
                    .zIndex(0.5f)
            )
        }

        // Bottom Statistics Card
        StatisticsCard(
            uiState = uiState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 처음 위치 순서대로 정보 카드 표시
            // 항상 4개의 카드 유지
            val displayOrder = uiState.initialUserOrder.take(4)
            repeat(4) { index ->
                if (index < displayOrder.size) {
                    val userId = displayOrder[index]
                    val rankingItem = rankings.find { it.userId == userId }
                    val realTimeSeconds = uiState.realTimeScreenTimes[userId] ?: 0
                    val formattedTime = formatSecondsToTime(realTimeSeconds)
                    UserInfoCard(
                        nickname = rankingItem?.nickname ?: "",
                        profileImage = rankingItem?.profileImage,
                        screenTime = formattedTime,
                        isEmpty = rankingItem == null,
                        isMe = rankingItem?.isMe ?: false,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    UserInfoCard(
                        nickname = "",
                        profileImage = null,
                        screenTime = "",
                        isEmpty = true,
                        isMe = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        }


        // 나무 상자 방 정보 모달
        if (isInfoPanelVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { isInfoPanelVisible = false },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(32.dp)
                        .width(320.dp)
                        .clickable(enabled = false) { }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFF8B6F47),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 4.dp,
                                color = Color(0xFF5D4E37),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 제목
                            StrokeText(
                                text = "Challenge Info",
                                style = DitoTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                fillColor = Color(0xFFFFF8DC),
                                strokeColor = Color(0xFF3E2723),
                                strokeWidth = 2.dp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().offset(y=5.dp)
                            )

                            // 구분선
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(Color(0xFF5D4E37))
                            )

                            // 정보 내용
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                InfoRow(
                                    label = "PERIOD",
                                    value = "${uiState.startDate} ~ ${uiState.endDate}"
                                )
                                InfoRow(
                                    label = "GOAL",
                                    value = uiState.goal
                                )
                                InfoRow(
                                    label = "PENALTY",
                                    value = uiState.penalty
                                )
                                InfoRow(
                                    label = "TOTAL BETTING",
                                    value = uiState.totalBetting
                                )
                            }
                        }
                    }

                    // 닫기 버튼
                    Image(
                        painter = painterResource(id = R.drawable.x),
                        contentDescription = "Close",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 12.dp, y = (-12).dp)
                            .size(32.dp)
                            .clickable { isInfoPanelVisible = false }
                            .background(
                                color = Color(0xFF5D4E37),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(6.dp),
                        colorFilter = ColorFilter.tint(Color(0xFFFFF8DC))
                    )
                }
            }
        }

        // 챌린지 가이드 다이얼로그
        if (isChallengeGuideVisible) {
            ChallengeGuideDialog(
                onDismiss = { isChallengeGuideVisible = false }
            )
        }
    }
}

@Composable
fun ChallengeGuideDialog(
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .padding(top = 100.dp)
                .width(320.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
                .border(1.dp, Color.Black, RoundedCornerShape(16.dp))
                .clickable(enabled = false) { /* 클릭 이벤트 전파 방지 */ }
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // 제목
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "챌린지 가이드",
                    style = DitoCustomTextStyles.titleDLarge,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }

            androidx.compose.material3.HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = Color.Black
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ChallengeGuideItem(
                    question = "챌린지 순위는 어떻게 정해지나요?",
                    answer = "방장이 선택한 앱을 기준으로 사용 시간에 따라 순위가 정해져요. 사용 시간이 적을수록 순위가 높아집니다."
                )

                ChallengeGuideItem(
                    question = "찌르기 기능이 뭔가요?",
                    answer = "로프에 위치한 다른 캐릭터를 눌러서 찌르기를 할 수 있어요. 친구들에게 재미있는 알림을 보내보세요!"
                )

                ChallengeGuideItem(
                    question = "방 정보는 어떻게 확인하나요?",
                    answer = "나무 상자의 방 제목을 클릭하면 방 정보를 더 자세히 살펴볼 수 있어요. 기간, 목표, 벌칙, 총 배팅 금액 등을 확인할 수 있습니다."
                )
            }
        }
    }
}

@Composable
private fun ChallengeGuideItem(
    question: String,
    answer: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = question,
            style = DitoTypography.labelLarge,
            color = Color.Black
        )
        Text(
            text = answer,
            style = DitoTypography.bodySmall,
            color = Color.Black
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = DitoTypography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFFFFF8DC)
        )
        Text(
            text = value,
            style = DitoTypography.bodyMedium,
            color = Color.White
        )
    }
}

@Composable
fun InfoRow(label: String, value: Int) {
    InfoRow(label, value.toString())
}

@Composable
fun UserInfoCard(
    nickname: String,
    profileImage: String?,
    screenTime: String,
    isEmpty: Boolean,
    isMe: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val croppedFace = rememberCroppedFace(profileImage)
    val croppedDefaultFace = remember {
        try {
            val ditoBitmap = android.graphics.BitmapFactory.decodeResource(
                context.resources,
                R.drawable.dito
            )
            cropFace(ditoBitmap).asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    Box(
        modifier = modifier
            .background(
                if (isMe) Color(0xFFFFEB3B).copy(alpha = 0.2f) else Color.White,
                RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isMe) 3.dp else 2.dp,
                color = if (isMe) Color(0xFFFFEB3B) else Color.Black,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!isEmpty) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(98f / 72f)
                ) {
                    val faceToShow = croppedFace ?: croppedDefaultFace
                    if (faceToShow != null) {
                        Image(
                            bitmap = faceToShow,
                            contentDescription = "Profile Face",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }


                    // 사용시간 텍스트 (이미지 위에 overlay)
                    StrokeText(
                        text = screenTime,
                        style = DitoTypography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        fillColor = Color(0xFFFFD700),  // 골드색
                        strokeColor = Color.Black,
                        strokeWidth = 2.dp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 1.dp)
                    )

                }
            } else {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(98f / 72f)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(6.dp))
                    .border(1.dp, Color.Black, RoundedCornerShape(6.dp))
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isEmpty) "" else nickname,
                    style = DitoTypography.labelSmall,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

fun getCharacterNameFromItemId(itemId: Int?): String {
    return when (itemId) {
        1 -> "lemon"
        2 -> "grape"
        4 -> "melon"
        6 -> "tomato"
        else -> "lemon"
    }
}

/**
 * 초 단위를 mm:ss 또는 h:mm:ss 형식으로 변환
 */
fun formatSecondsToTime(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

fun getAppIconFromPackage(packageName: String?): Int {
    return when {
        packageName == null -> R.drawable.dito
        packageName.contains("com.google.android.youtube", ignoreCase = true) -> R.drawable.youtube
        packageName.contains("com.twitter.android", ignoreCase = true) -> R.drawable.ic_twitter
        packageName.contains("com.android.chrome", ignoreCase = true) -> R.drawable.ic_chrome
        packageName.contains("com.instagram.android", ignoreCase = true) -> R.drawable.instagram
        packageName.contains("dito", ignoreCase = true) -> R.drawable.dito
        else -> R.drawable.dito
    }
}

@Composable
fun CharacterView(
    costumeItemId: Int?,
    rank: Int,
    maxRank: Int,
    currentAppPackage: String?,
    isMe: Boolean,
    showPokeBubble: Boolean = false,
    currentMinutes: Int = 0,
    goalMinutes: Int = 0,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isWiggling by remember { mutableStateOf(false) }
    var wiggleFrame by remember { mutableStateOf(0) }
    var showChain by remember { mutableStateOf(false) }
    var rememberedItemId by remember { mutableStateOf(costumeItemId) }
    if (costumeItemId != null) {
        rememberedItemId = costumeItemId
    }

    val characterName = getCharacterNameFromItemId(rememberedItemId)

    // 위아래로 씰룩거리는 애니메이션 (각 캐릭터마다 다른 타이밍)
    val infiniteTransition = rememberInfiniteTransition(label = "bounce_$rank")
    // 각 캐릭터마다 다른 duration으로 다른 속도로 움직임
    val animationDuration = 1500 + (rank * 200) // 순위마다 200ms씩 차이
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounceOffset"
    )

    val ropeHeight = 320.dp
    val characterSize = 120.dp
    val minHeight = 0.dp // 캐릭터 발이 로프 하단
    val maxHeight = ropeHeight - characterSize // 캐릭터 머리가 로프 상단
    val baseHeight = maxHeight

    val previousRank = remember { mutableStateOf(rank) }
    val isAnimating = remember { mutableStateOf(false) }
    val animationProgress = remember { Animatable(0f) }
    val heightAnimatable = remember { Animatable(0f) }

    androidx.compose.runtime.LaunchedEffect(rank) {
        if (previousRank.value != rank) {
            isAnimating.value = true
            val previousHeight = (baseHeight - (previousRank.value - 1) * 60.dp).coerceIn(minHeight, maxHeight)
            val targetHeight = (baseHeight - (rank - 1) * 60.dp).coerceIn(minHeight, maxHeight)
            val rankDiff = kotlin.math.abs(rank - previousRank.value)
            val animationTarget = rankDiff * 4f
            val animationDuration = (rankDiff * 1000).coerceAtMost(3000)

            heightAnimatable.snapTo(previousHeight.value)
            animationProgress.snapTo(0f)

            launch {
                heightAnimatable.animateTo(
                    targetValue = targetHeight.value,
                    animationSpec = tween(durationMillis = animationDuration, easing = LinearEasing)
                )
            }
            animationProgress.animateTo(
                targetValue = animationTarget,
                animationSpec = tween(durationMillis = animationDuration, easing = LinearEasing)
            )
            isAnimating.value = false
            previousRank.value = rank
        }
    }

    val characterHeight = if (isAnimating.value) {
        heightAnimatable.value.dp.coerceIn(minHeight, maxHeight)
    } else {
        val heightReduction = (rank - 1) * 60.dp
        (baseHeight - heightReduction).coerceIn(minHeight, maxHeight)
    }

    androidx.compose.runtime.LaunchedEffect(isWiggling) {
        if (isWiggling) {
            for (i in 0..3) { // 4 frames total (1 second)
                wiggleFrame = i % 2
                delay(250)
            }
            isWiggling = false
            wiggleFrame = 0
        }
    }

    val animPhase = if (isAnimating.value) (animationProgress.value % 1f) else 0f
    val showRight = if (isWiggling) {
        wiggleFrame == 1
    } else if (isAnimating.value) {
        animPhase >= 0.5f
    } else {
        false
    }

    val characterDrawable = when (characterName) {
        "lemon" -> if (showRight) R.drawable.lemon_right else R.drawable.lemon_left
        "grape" -> if (showRight) R.drawable.grape_right else R.drawable.grape_left
        "melon" -> if (showRight) R.drawable.melon_right else R.drawable.melon_left
        "tomato" -> if (showRight) R.drawable.tomato_right else R.drawable.tomato_left
        else -> if (showRight) R.drawable.lemon_right else R.drawable.lemon_left
    }

    val chainDrawable = when (characterName) {
        "lemon" -> R.drawable.chain_lemon
        "grape" -> R.drawable.chain_grape
        "melon" -> R.drawable.chain_melon
        "tomato" -> R.drawable.chain_tomato
        else -> R.drawable.chain_lemon
    }

    LaunchedEffect(showChain) {
        if (showChain) {
            delay(1000)
            showChain = false
        }
    }

    Column(
        modifier = Modifier
            .width(50.dp)
            .clickable(onClick = {
                if (!isWiggling) {
                    playWiggleSound(context)
                    isWiggling = true
                }
                if (isMe) {
                    showChain = true
                } else {
                    onClick()
                }
            }),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(ropeHeight),
            contentAlignment = Alignment.TopCenter
        ) {
            Image(
                painter = painterResource(id = R.drawable.rope),
                contentDescription = "Rope",
                modifier = Modifier
                    .width(40.dp)
                    .fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = -characterHeight + bounceOffset.dp)
            ) {
                Image(
                    painter = painterResource(id = if (showChain) chainDrawable else characterDrawable),
                    contentDescription = "$characterName character",
                    modifier = Modifier.size(characterSize),
                    contentScale = ContentScale.Crop
                )

                // 현재 사용 중인 앱 아이콘
                Image(
                    painter = painterResource(id = getAppIconFromPackage(currentAppPackage)),
                    contentDescription = if (currentAppPackage != null) "Current app: $currentAppPackage" else "No app running",
                    modifier = Modifier
                        .size(54.dp)
                        .align(Alignment.BottomCenter)
                        .offset(y = 24.dp)
                        .clip(DitoShapes.medium)
                        .padding(4.dp)
                )

                // Progress HUD (내 캐릭터에만 표시)
                if (isMe && goalMinutes > 0) {
                    ProgressHUD(
                        currentMinutes = currentMinutes,
                        goalMinutes = goalMinutes,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-25).dp)
                    )
                }

                if (showPokeBubble || showChain) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = if (isMe && goalMinutes > 0) (-75).dp else (-60).dp)
                            .size(if (showChain) 90.dp else 70.dp, 120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.speech_bubble),
                            contentDescription = "Poke Bubble",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            text = if (showChain) "It's me" else "아얏!",
                            style = DitoTypography.labelMedium,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(bottom = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 프로필 이미지에서 얼굴 부분만 크롭하는 함수
 */
fun cropFace(original: Bitmap): Bitmap {
    val w = original.width
    val h = original.height

    val faceSize = (w * 0.95f).toInt()

    val faceLeft = ((w - faceSize) / 2f).toInt()
    val faceTop = (h * 0.20f).toInt()

    return Bitmap.createBitmap(original, faceLeft, faceTop, faceSize, faceSize)
}


/**
 * URL에서 이미지를 로드하고 얼굴 부분을 크롭해서 반환하는 Composable 함수
 */
@Composable
fun rememberCroppedFace(imageUrl: String?): ImageBitmap? {
    val context = LocalContext.current
    var croppedFace by remember(imageUrl) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(imageUrl) {
        if (imageUrl != null) {
            withContext(Dispatchers.IO) {
                try {
                    val imageLoader = ImageLoader(context)
                    val request = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .allowHardware(false) // Bitmap 변환을 위해 필요
                        .build()

                    val result = imageLoader.execute(request)
                    if (result is SuccessResult) {
                        val bitmap = result.drawable.toBitmap()
                        val cropped = cropFace(bitmap)
                        croppedFace = cropped.asImageBitmap()
                    }
                } catch (e: Exception) {
                    croppedFace = null
                }
            }
        } else {
            croppedFace = null
        }
    }

    return croppedFace
}
