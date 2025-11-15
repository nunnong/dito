package com.dito.app.feature.group

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.hilt.navigation.compose.hiltViewModel
import com.dito.app.R
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.StrokeText
import com.dito.app.core.ui.designsystem.WiggleClickable
import com.dito.app.core.ui.designsystem.playWiggleSound
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun OngoingChallengeScreen(
    viewModel: OngoingChallengeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    var isInfoPanelVisible by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.startAutoRefresh()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopAutoRefresh()
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
                .height(621.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.test),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // 그룹 정보
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 0.dp, top = 0.dp)
                    .width(280.dp)
                    .height(120.dp)
                    .clickable { isInfoPanelVisible = !isInfoPanelVisible },
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
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }

            if (rankings.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .offset(y = (-80).dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    rankings.take(4).forEach { rankingItem ->
                        CharacterView(
                            costumeItemId = rankingItem.costumeItemId,
                            rank = rankingItem.rank,
                            maxRank = rankings.size.coerceAtMost(4),
                            currentAppPackage = rankingItem.currentAppPackage,
                            isMe = rankingItem.isMe,
                            onClick = {
                                if (!rankingItem.isMe) {
                                    viewModel.pokeMember(rankingItem.userId)
                                }
                             }
                        )
                    }
                }
            }
        }
        }

        // 나무 상자 모달
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
                    // 나무 상자 배경
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
                                modifier = Modifier.fillMaxWidth()
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
                                    value = uiState.bet
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

fun getCharacterNameFromItemId(itemId: Int?): String {
    return when (itemId) {
        1 -> "lemon"
        2 -> "grape"
        4 -> "melon"
        6 -> "tomato"
        else -> "lemon" // 기본값
    }
}

@Composable
fun CharacterView(
    costumeItemId: Int?,
    rank: Int,
    maxRank: Int,
    currentAppPackage: String?,
    isMe: Boolean,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isWiggling by remember { mutableStateOf(false) }
    var wiggleFrame by remember { mutableStateOf(0) }
    var rememberedItemId by remember { mutableStateOf(costumeItemId) }
    if (costumeItemId != null) {
        rememberedItemId = costumeItemId
    }

    val characterName = getCharacterNameFromItemId(rememberedItemId)

    val ropeHeight = 240.dp
    val characterSize = 100.dp
    val baseHeight = 180.dp
    val heightReduction = (rank - 1) * 60.dp
    val characterHeight = (baseHeight - heightReduction).coerceIn(0.dp, ropeHeight - characterSize)

    val previousRank = remember { mutableStateOf(rank) }
    val isAnimating = remember { mutableStateOf(false) }
    val animationProgress = remember { Animatable(0f) }

    // Rank change animation
    androidx.compose.runtime.LaunchedEffect(rank) {
        if (previousRank.value != rank) {
            isAnimating.value = true
            animationProgress.snapTo(0f)
            animationProgress.animateTo(
                targetValue = 6f,
                animationSpec = tween(durationMillis = 1500, easing = LinearEasing)
            )
            isAnimating.value = false
            previousRank.value = rank
        }
    }

    // Wiggle animation (4 fps = 250ms per frame)
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

    Column(
        modifier = Modifier.width(60.dp)
            .clickable(onClick = {
                if (isMe) {
                    if (!isWiggling) {
                        playWiggleSound(context)
                        isWiggling = true
                    }
                } else {
                    onClick()
                }
            }),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(ropeHeight),
            contentAlignment = Alignment.TopCenter
        ) {
            // Rope (fixed height for all ranks)
            Image(
                painter = painterResource(id = R.drawable.rope),
                contentDescription = "Rope",
                modifier = Modifier
                    .width(25.dp)
                    .fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            // Character positioned on rope based on rank
            Image(
                painter = painterResource(id = characterDrawable),
                contentDescription = "$characterName character",
                modifier = Modifier
                    .size(characterSize)
                    .align(Alignment.BottomCenter)
                    .offset(y = -characterHeight),
                contentScale = ContentScale.Fit
            )
        }

        if (currentAppPackage != null) {
            Image(
                painter = painterResource(id = R.drawable.dito), // Placeholder
                contentDescription = "Current app",
                modifier = Modifier
                    .size(28.dp)
                    .offset(y = (-10).dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White)
                    .border(1.dp, Color.Black, RoundedCornerShape(6.dp))
                    .padding(2.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
