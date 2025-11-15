package com.dito.app.feature.group

import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.asImageBitmap
import coil.compose.AsyncImage
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
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.StrokeText
import com.dito.app.core.ui.designsystem.WiggleClickable
import com.dito.app.core.ui.designsystem.playWiggleSound
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
                .aspectRatio(410f / 635f)
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
                    .width(160.dp)
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
                        .align(Alignment.BottomCenter),
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 0..3) {
                val rankingItem = rankings.getOrNull(i)
                UserInfoCard(
                    nickname = rankingItem?.nickname ?: "",
                    profileImage = rankingItem?.profileImage,
                    screenTime = rankingItem?.totalScreenTimeFormatted ?: "",
                    isEmpty = rankingItem == null,
                    modifier = Modifier.weight(1f)
                )
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

@Composable
fun UserInfoCard(
    nickname: String,
    profileImage: String?,
    screenTime: String,
    isEmpty: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // 얼굴 크롭된 이미지
    val croppedFace = rememberCroppedFace(profileImage)

    // 기본 이미지(dito)도 크롭
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
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 프로필 이미지
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
                        style = DitoTypography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        fillColor = Color.White,
                        strokeColor = Color.Black,
                        strokeWidth = 1.dp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 4.dp)
                    )
                }
            } else {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(98f / 72f)
                )
            }

            // 닉네임 박스
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

    val ropeHeight = 320.dp
    val characterSize = 120.dp
    val baseHeight = 180.dp

    val previousRank = remember { mutableStateOf(rank) }
    val isAnimating = remember { mutableStateOf(false) }
    val animationProgress = remember { Animatable(0f) }
    val heightAnimatable = remember { Animatable(0f) }

    androidx.compose.runtime.LaunchedEffect(rank) {
        if (previousRank.value != rank) {
            isAnimating.value = true
            val previousHeight = (baseHeight - (previousRank.value - 1) * 60.dp).coerceIn(0.dp, ropeHeight - characterSize)
            val targetHeight = (baseHeight - (rank - 1) * 60.dp).coerceIn(0.dp, ropeHeight - characterSize)
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
        heightAnimatable.value.dp
    } else {
        val heightReduction = (rank - 1) * 60.dp
        (baseHeight - heightReduction).coerceIn(0.dp, ropeHeight - characterSize)
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
            Image(
                painter = painterResource(id = R.drawable.rope),
                contentDescription = "Rope",
                modifier = Modifier
                    .width(25.dp)
                    .fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = -characterHeight),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = characterDrawable),
                    contentDescription = "$characterName character",
                    modifier = Modifier.size(characterSize),
                    contentScale = ContentScale.Fit
                )

                if (currentAppPackage != null) {
                    Image(
                        painter = painterResource(id = R.drawable.dito),
                        contentDescription = "Current app",
                        modifier = Modifier
                            .size(28.dp)
                            .offset(y = (-30).dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White)
                            .border(1.dp, Color.Black, RoundedCornerShape(6.dp))
                            .padding(2.dp)
                    )
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

    val faceSize = (w * 0.48f).toInt()
    val faceLeft = ((w - faceSize) / 2f).toInt()
    val faceTop = (h * 0.265f).toInt() // 미세조정 반영

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
                    // 이미지 로딩 실패 시 null 유지
                    croppedFace = null
                }
            }
        } else {
            croppedFace = null
        }
    }

    return croppedFace
}
