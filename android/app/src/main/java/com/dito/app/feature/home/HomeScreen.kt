package com.dito.app.feature.home

import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.dito.app.R
import com.dito.app.core.data.home.HomeData
import com.dito.app.core.ui.designsystem.BounceClickable
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoHardShadow
import com.dito.app.core.ui.designsystem.DitoSoftShadow
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.Primary
import com.dito.app.core.ui.designsystem.WiggleClickable
import com.dito.app.core.ui.designsystem.hardShadow
import com.dito.app.core.ui.designsystem.playPopSound
import com.dito.app.core.ui.designsystem.playWiggleSound
import com.dito.app.core.ui.designsystem.softShadow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onLogout: () -> Unit,
    onCartClick: () -> Unit,
    onClosetClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadHomeData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            uiState.errorMessage != null -> {
                Text(
                    text = uiState.errorMessage ?: "알 수 없는 오류가 발생했습니다.",
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            uiState.homeData != null -> {
                val homeData = uiState.homeData!!
                HomeContent(
                    homeData = homeData,
                    onCartClick = onCartClick,
                    onClosetClick = onClosetClick,
                    onSettingsClick = onSettingsClick
                )
            }
        }
    }
}

@Composable
fun HomeContent(
    homeData: HomeData,
    onCartClick: () -> Unit,
    onClosetClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    var showDitoFaceDialog by remember { mutableStateOf(false) }

    android.util.Log.d("HomeScreen", "HomeData: $homeData")

    val coinInteractionSource = remember { MutableInteractionSource() }
    val isCoinPressed by coinInteractionSource.collectIsPressedAsState()
    val coinScale by animateFloatAsState(if (isCoinPressed) 0.8f else 1f, label = "coin_scale")
    val lemonRotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var wiggleImageOverride by remember { mutableStateOf(false) }
    var showSpeechBubble by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    var animationKey by remember { mutableStateOf(0) }

    // 갈매기 상태
    var isSeagullFlying by remember { mutableStateOf(false) }
    var seagullTrigger by remember { mutableStateOf(0) }

    // 바다 배경인지 확인 (busan 또는 ocean)
    val isOceanBackground = remember(homeData.backgroundUrl) {
        val isOcean = homeData.backgroundUrl?.let { url ->
            url.contains("busan.png", ignoreCase = true) || url.contains("ocean.png", ignoreCase = true)
        } ?: false
        android.util.Log.d("HomeScreen", "Background URL: ${homeData.backgroundUrl}, isOceanBackground: $isOcean")
        isOcean
    }

    // 야구장 배경인지 확인
    val isBaseballBackground = remember(homeData.backgroundUrl) {
        val isBaseball = homeData.backgroundUrl?.contains("baseball.png", ignoreCase = true) ?: false
        android.util.Log.d("HomeScreen", "Background URL: ${homeData.backgroundUrl}, isBaseballBackground: $isBaseball")
        isBaseball
    }

    // 바다 배경일 때 파도 소리 재생
    DisposableEffect(isOceanBackground) {
        var mediaPlayer: MediaPlayer? = null
        if (isOceanBackground) {
            try {
                mediaPlayer = MediaPlayer.create(context, R.raw.busan)
                mediaPlayer?.let { mp ->
                    mp.isLooping = true
                    mp.setVolume(1.0f, 1.0f)
                    mp.start()
                    android.util.Log.d("HomeScreen", "파도 소리 재생 시작")
                } ?: android.util.Log.e("HomeScreen", "MediaPlayer 생성 실패")
            } catch (e: Exception) {
                android.util.Log.e("HomeScreen", "파도 소리 재생 오류: ${e.message}")
            }
        }
        onDispose {
            mediaPlayer?.apply {
                try {
                    if (isPlaying) stop()
                    release()
                    android.util.Log.d("HomeScreen", "파도 소리 정지")
                } catch (e: Exception) {
                    android.util.Log.e("HomeScreen", "MediaPlayer 정지 오류: ${e.message}")
                }
            }
        }
    }

    // 야구장 배경일 때 야구공 소리 (한 번)
    DisposableEffect(isBaseballBackground) {
        var mediaPlayer: MediaPlayer? = null
        if (isBaseballBackground) {
            try {
                mediaPlayer = MediaPlayer.create(context, R.raw.baseball)
                mediaPlayer?.setVolume(0.5f, 0.5f)
                mediaPlayer?.setOnCompletionListener { mp -> mp.release() }
                mediaPlayer?.start()
            } catch (e: Exception) {
                android.util.Log.e("HomeContent", "Error playing baseball sound", e)
            }
        }
        onDispose {
            mediaPlayer?.release()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                animationKey++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (showDitoFaceDialog) {
        DitoFaceDialog(onDismiss = { showDitoFaceDialog = false })
    }

    // Frame 156: 메인 노란색 카드 (359x589)
    Column(
        modifier = Modifier
            .width(359.dp)
            .height(589.dp)
            .hardShadow(
                DitoHardShadow.Modal.copy(
                    cornerRadius = 0.dp, offsetX = 6.dp, offsetY = 6.dp
                )
            )
            .background(Primary, RectangleShape)
            .border(2.dp, Color.Black, RectangleShape)
            .padding(top = 25.dp, bottom = 25.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Frame 159: 흰색 내부 카드 (327x477)
        Column(
            modifier = Modifier
                .width(327.dp)
                .height(477.dp)
                .background(Color.White)
                .border(2.dp, Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상단 블랙 바 (Frame 165)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Color.Black)
                    .padding(start = 12.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 왼쪽 아이콘 2개 (Frame 164)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .width(80.dp)
                        .height(24.dp)
                ) {
                    BounceClickable(
                        onClick = {
                            playPopSound(context)
                            onCartClick()
                        },
                        modifier = Modifier.size(24.dp)
                    ) { isPressed ->
                        Image(
                            painter = painterResource(id = R.drawable.cart),
                            contentDescription = "Cart",
                            modifier = Modifier.fillMaxSize(),
                            colorFilter = if (isPressed) ColorFilter.tint(Primary) else ColorFilter.tint(
                                Color.White
                            ),
                            contentScale = ContentScale.Fit
                        )
                    }
                    BounceClickable(
                        onClick = {
                            playPopSound(context)
                            onClosetClick()
                        },
                        modifier = Modifier.size(20.dp)
                    ) { isPressed ->
                        Image(
                            painter = painterResource(id = R.drawable.closet),
                            contentDescription = "Closet",
                            modifier = Modifier.fillMaxSize(),
                            colorFilter = if (isPressed) ColorFilter.tint(Primary) else null,
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // 오른쪽 아이콘 1개
                BounceClickable(
                    onClick = {
                        playPopSound(context)
                        onSettingsClick()
                    },
                    modifier = Modifier.size(24.dp)
                ) { isPressed ->
                    Image(
                        painter = painterResource(id = R.drawable.settings),
                        contentDescription = "settings",
                        modifier = Modifier.fillMaxSize(),
                        colorFilter = if (isPressed) ColorFilter.tint(Primary) else ColorFilter.tint(
                            Color.White
                        ),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Frame 162 - 내부 컨텐츠 (배경 영역)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clipToBounds()
                    .pointerInput(isOceanBackground) {
                        // 배경(상단) 탭 → 갈매기
                        detectTapGestures(onTap = { offset ->
                            // 상단 55%만 배경 영역으로 간주 (캐릭터/코인 제외)
                            if (offset.y < size.height * 0.55f && isOceanBackground && !isSeagullFlying) {
                                isSeagullFlying = true
                                seagullTrigger++

                                // 갈매기 소리 재생
                                try {
                                    val mp = MediaPlayer.create(context, R.raw.seagulls)
                                    mp?.apply {
                                        setVolume(1.0f, 1.0f)
                                        setOnCompletionListener { player -> player.release() }
                                        start()
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e(
                                        "HomeScreen",
                                        "갈매기 소리 재생 오류: ${e.message}"
                                    )
                                }
                            }
                        })
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                // 배경 이미지
                if (!homeData.backgroundUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = homeData.backgroundUrl,
                        contentDescription = "Background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onError = { error ->
                            android.util.Log.e(
                                "HomeScreen",
                                "배경 이미지 로딩 실패: ${homeData.backgroundUrl}",
                                error.result.throwable
                            )
                        },
                        onSuccess = {
                            android.util.Log.d(
                                "HomeScreen",
                                "배경 이미지 로딩 성공: ${homeData.backgroundUrl}"
                            )
                        }
                    )
                }

                // 파도 효과
                if (isOceanBackground) {
                    OceanEffect(modifier = Modifier.fillMaxSize())
                }

                // 야구공 효과 (배경 위, 캐릭터 뒤)
                if (isBaseballBackground) {
                    BaseballEffect(modifier = Modifier.fillMaxSize())
                }

                // 갈매기 떼 (배경 위, 캐릭터 뒤)
                if (isSeagullFlying) {
                    SeagullFlock(
                        modifier = Modifier.fillMaxSize(),
                        trigger = seagullTrigger,
                        onFinished = { isSeagullFlying = false }
                    )
                }

                // 캐릭터/말풍선
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val speechBubbleAlpha by animateFloatAsState(
                        targetValue = if (showSpeechBubble) 1f else 0f,
                        animationSpec = tween(durationMillis = 200),
                        label = "speech_bubble_alpha"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (showSpeechBubble || speechBubbleAlpha > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        alpha = speechBubbleAlpha
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.speech_bubble),
                                    contentDescription = "Speech Bubble",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                                Text(
                                    text = "오늘은 유튜브를 많이 보셨네요!",
                                    style = DitoCustomTextStyles.titleDSmall,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(
                                        start = 24.dp,
                                        end = 24.dp,
                                        bottom = 16.dp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    WiggleClickable(
                        modifier = Modifier.size(110.dp),
                        onClick = {
                            if (!wiggleImageOverride) {
                                scope.launch {
                                    playWiggleSound(context)
                                    showSpeechBubble = true
                                    wiggleImageOverride = true
                                    delay(400)
                                    wiggleImageOverride = false
                                    delay(1000)
                                    showSpeechBubble = false
                                }
                            }
                        }
                    ) {
                        if (wiggleImageOverride) {
                            Image(
                                painter = painterResource(id = getWiggleDrawable(homeData.costumeUrl)),
                                contentDescription = "Character",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            AsyncImage(
                                model = if (homeData.costumeUrl.isNotEmpty()) {
                                    homeData.costumeUrl
                                } else {
                                    R.drawable.dito
                                },
                                contentDescription = "Character",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                                onError = { error ->
                                    android.util.Log.e(
                                        "HomeScreen",
                                        "이미지 로딩 실패",
                                        error.result.throwable
                                    )
                                },
                                onSuccess = {
                                    android.util.Log.d(
                                        "HomeScreen",
                                        "이미지 로딩 성공"
                                    )
                                }
                            )
                        }
                    }
                }

                // 코인 표시
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 8.dp, bottom = 12.dp)
                        .wrapContentSize(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = coinScale
                                scaleY = coinScale
                            }
                            .clickable(
                                interactionSource = coinInteractionSource,
                                indication = null,
                                onClick = {
                                    scope.launch {
                                        for (i in 0..1) {
                                            lemonRotation.animateTo(
                                                targetValue = -15f,
                                                animationSpec = tween(75)
                                            )
                                            lemonRotation.animateTo(
                                                targetValue = 15f,
                                                animationSpec = tween(75)
                                            )
                                        }
                                        lemonRotation.animateTo(
                                            targetValue = 0f,
                                            animationSpec = tween(75)
                                        )
                                    }
                                }
                            )
                            .softShadow(DitoSoftShadow.Low.copy(cornerRadius = 48.dp))
                            .widthIn(min = 97.dp)
                            .height(36.dp)
                            .background(Color.White, RoundedCornerShape(48.dp))
                            .border(1.dp, Color.Black, RoundedCornerShape(48.dp))
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = homeData.coinBalance.toString(),
                            style = DitoCustomTextStyles.titleDLarge,
                            color = Color.Black
                        )
                        Image(
                            painter = painterResource(id = R.drawable.lemon),
                            contentDescription = "Coin",
                            modifier = Modifier
                                .size(28.dp)
                                .graphicsLayer {
                                    rotationZ = lemonRotation.value
                                },
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            // 프로그레스 바 섹션
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(191.dp)
                    .background(Color.White),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Black)
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProgressBarItem(
                            label = "자기관리",
                            progress = homeData.selfCareStatus / 100.0f,
                            animationKey = animationKey
                        )
                        ProgressBarItem(
                            label = "집중",
                            progress = homeData.focusStatus / 100.0f,
                            animationKey = animationKey
                        )
                        ProgressBarItem(
                            label = "수면",
                            progress = homeData.sleepStatus / 100.0f,
                            animationKey = animationKey
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 하단 닉네임
        Row(
            modifier = Modifier
                .width(310.dp)
                .height(52.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .width(252.dp)
                    .height(52.dp)
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = homeData.nickname,
                    style = DitoTypography.headlineMedium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Color.Black)
                )
            }

            BounceClickable(
                onClick = {
                    playPopSound(context)
                    showDitoFaceDialog = true
                },
                modifier = Modifier.size(60.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.face_dialog_btn),
                    contentDescription = "Face Dialog Button",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

// ===== 파도 효과 =====
@Composable
fun OceanEffect(
    modifier: Modifier = Modifier,
    bottomStopRatio: Float = 0.88f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ocean_effect")

    val waveProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_progress"
    )

    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val sparkles = remember {
        List(30) { Pair(Random.nextFloat(), Random.nextFloat()) }
    }

    Box(
        modifier = modifier.drawWithContent {
            drawContent()

            val oceanStartY = size.height * 0.52f
            val oceanEndY = size.height * bottomStopRatio
            val oceanHeight = (oceanEndY - oceanStartY).coerceAtLeast(0f)

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x0000CFFF),
                        Color(0x3300CFFF),
                        Color(0x1100CFFF)
                    ),
                    startY = oceanStartY,
                    endY = oceanEndY
                )
            )

            val twoPi = (Math.PI * 2f).toFloat()

            fun drawCurvedWaveBand(
                progress: Float,
                phase: Float,
                intensity: Float
            ) {
                if (oceanHeight <= 0f) return

                val thicknessFactor = 1f - (progress * 0.7f)
                val alphaFactor = 1f - (progress * 0.6f)

                val baseCenterY = oceanStartY + oceanHeight * progress
                val bandHeight = oceanHeight * 0.12f * thicknessFactor

                val bandTopBase = (baseCenterY - bandHeight / 2f)
                val bandBottomBase = (baseCenterY + bandHeight / 2f)

                val amplitude = oceanHeight * 0.05f * thicknessFactor

                val waveLength = size.width / 4f
                val step = size.width / 40f

                val path = Path()

                path.moveTo(0f, bandBottomBase)
                var x = 0f
                while (x <= size.width) {
                    val t = (x / waveLength) + (waveProgress * 1.5f) + phase
                    val offset = (kotlin.math.sin(t * twoPi) * amplitude).toFloat()
                    val y = (bandBottomBase + offset).coerceIn(oceanStartY, oceanEndY)
                    path.lineTo(x, y)
                    x += step
                }

                x = size.width
                while (x >= 0f) {
                    val t = (x / waveLength) + (waveProgress * 1.5f) + phase + 0.7f
                    val offset = (kotlin.math.sin(t * twoPi) * amplitude).toFloat()
                    val y = (bandTopBase + offset).coerceIn(oceanStartY, oceanEndY)
                    path.lineTo(x, y)
                    x -= step
                }

                path.close()

                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0f),
                            Color.White.copy(alpha = 0.8f * intensity * alphaFactor),
                            Color.White.copy(alpha = 0.95f * intensity * alphaFactor),
                            Color.White.copy(alpha = 0f)
                        ),
                        startY = bandTopBase.coerceIn(oceanStartY, oceanEndY),
                        endY = bandBottomBase.coerceIn(oceanStartY, oceanEndY)
                    )
                )
            }

            drawCurvedWaveBand(progress = waveProgress, phase = 0f, intensity = 1f)
            drawCurvedWaveBand(progress = (waveProgress + 0.35f) % 1f, phase = 0.4f, intensity = 0.8f)
            drawCurvedWaveBand(progress = (waveProgress + 0.7f) % 1f, phase = 0.9f, intensity = 0.6f)

            sparkles.forEach { (nx, ny) ->
                val baseY = oceanStartY + ny * oceanHeight
                val localPhase = (shimmer + nx + ny) % 1f
                val offsetY = (kotlin.math.sin(localPhase * twoPi) * 6f).toFloat()

                val y = (baseY + offsetY).coerceIn(oceanStartY, oceanEndY)
                val alpha = (kotlin.math.sin(localPhase * Math.PI).coerceAtLeast(0.0) * 0.9).toFloat()
                val radius = (3f + kotlin.math.sin(localPhase * Math.PI) * 2f).toFloat()

                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(
                        x = nx * this.size.width,
                        y = y
                    )
                )
            }
        }
    )
}

// ===== 야구공 효과 (기존) =====
@Composable
fun BaseballEffect(modifier: Modifier = Modifier) {
    val ballPainter = painterResource(id = R.drawable.baseball_ball)

    val progress = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(2600, easing = LinearEasing)
            )
        }

        launch {
            val flightDuration = 2600f
            val rotationDuration = 500f
            val totalRotations = flightDuration / rotationDuration
            rotation.animateTo(
                targetValue = 360f * totalRotations,
                animationSpec = tween(flightDuration.toInt(), easing = LinearEasing)
            )
        }
    }

    Canvas(modifier = modifier) {
        if ((progress.value == 0f || progress.value == 1f) && !progress.isRunning) {
            return@Canvas
        }

        val w = size.width
        val h = size.height

        val startX = -0.15f * w
        val endX = 1.15f * w

        val baseY = h * 0.42f
        val arcHeight = h * 0.18f

        fun parabola(t: Float): Float =
            (-4f * (t - 0.5f) * (t - 0.5f) + 1f).coerceAtLeast(0f)

        val x = startX + (endX - startX) * progress.value
        val y = baseY - arcHeight * parabola(progress.value)

        val startSize = w * 0.10f
        val endSize = w * 0.24f
        val currentSize = startSize + (endSize - startSize) * progress.value

        val center = Offset(x, y)
        val topLeft = Offset(
            x = center.x - currentSize / 2,
            y = center.y - currentSize / 2
        )

        rotate(degrees = rotation.value, pivot = center) {
            translate(left = topLeft.x, top = topLeft.y) {
                with(ballPainter) {
                    draw(
                        size = Size(currentSize, currentSize)
                    )
                }
            }
        }
    }
}

// ===== 갈매기 떼 효과 =====
@Composable
fun SeagullFlock(
    modifier: Modifier = Modifier,
    trigger: Int,
    onFinished: () -> Unit
) {
    val seagullPainter = painterResource(id = R.drawable.seagull)
    val progress = remember { Animatable(0f) }

    // 각 갈매기마다 약간씩 다른 위치/딜레이
    val birdsMeta = remember {
        val count = 6
        List(count) {
            BirdMeta(
                delay = it * 0.10f,                    // 순차 딜레이
                xJitter = Random.nextFloat() * 0.08f, // 살짝 좌우 퍼짐
                yJitter = Random.nextFloat() * 0.08f, // 살짝 상하 퍼짐
                scale = 0.8f + Random.nextFloat() * 0.4f
            )
        }
    }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2200, easing = FastOutSlowInEasing)
        )
        onFinished()
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 오른쪽 중앙 → 왼쪽 상단
        val startX = w * 0.9f
        val startY = h * 0.4f
        val endX = w * 0.1f
        val endY = h * 0.1f

        birdsMeta.forEach { bird ->
            val tRaw = progress.value - bird.delay
            if (tRaw <= 0f || tRaw >= 1.2f) return@forEach
            val t = tRaw.coerceIn(0f, 1f)

            val x = startX + (endX - startX) * t - bird.xJitter * w
            val y = startY + (endY - startY) * t - bird.yJitter * h

            // 멀어질수록 작아지도록
            val baseSize = w * 0.12f
            val sizeFactor = bird.scale * (1f - t * 0.3f)
            val birdWidth = baseSize * sizeFactor
            val intrinsic = seagullPainter.intrinsicSize
            val ratio =
                if (intrinsic.width > 0 && intrinsic.height > 0) intrinsic.height / intrinsic.width else 1f
            val birdHeight = birdWidth * ratio

            // 날갯짓 느낌: 위아래 살짝 흔들기
            val flapPhase = (t * 4f * Math.PI).toFloat()
            val flapOffset = kotlin.math.sin(flapPhase) * (birdHeight * 0.08f)

            val topLeft = Offset(
                x = x - birdWidth / 2f,
                y = y - birdHeight / 2f + flapOffset.toFloat()
            )

            val alpha = 1f - t * 0.3f

            translate(left = topLeft.x, top = topLeft.y) {
                with(seagullPainter) {
                    draw(
                        size = androidx.compose.ui.geometry.Size(birdWidth, birdHeight),
                        alpha = alpha
                    )
                }
            }
        }
    }
}

private data class BirdMeta(
    val delay: Float,
    val xJitter: Float,
    val yJitter: Float,
    val scale: Float
)

// ===== 기타 기존 함수들 =====
@Composable
private fun getWiggleDrawable(costumeUrl: String): Int {
    val context = LocalContext.current
    val resources = context.resources
    val packageName = context.packageName

    val fileName = costumeUrl.substringAfterLast('/')
    val characterName = fileName.substringBefore('_')

    if (characterName.isNotEmpty()) {
        val wiggleDrawableName = "${characterName}_wiggle"
        val resourceId = resources.getIdentifier(wiggleDrawableName, "drawable", packageName)
        if (resourceId != 0) {
            return resourceId
        }
    }

    return R.drawable.lemon_wiggle
}

@Composable
private fun ProgressBarItem(label: String, progress: Float, animationKey: Any?) {
    var showValue by remember { mutableStateOf(false) }
    val animatedProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(animationKey) {
        animatedProgress.stop()
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "progress_bar_scale")

    Box {
        Row(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .fillMaxWidth()
                .height(47.67.dp)
                .background(Color.Black)
                .padding(horizontal = 16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            try {
                                awaitRelease()
                            } finally {
                                isPressed = false
                            }
                        },
                        onTap = {
                            scope.launch {
                                animatedProgress.stop()
                                animatedProgress.snapTo(0f)
                                animatedProgress.animateTo(
                                    targetValue = progress,
                                    animationSpec = tween(
                                        durationMillis = 800,
                                        easing = FastOutSlowInEasing
                                    )
                                )
                                showValue = true
                                delay(500)
                                showValue = false
                            }
                        }
                    )
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .width(80.dp)
                    .height(52.dp)
                    .padding(top = 14.dp, bottom = 14.dp, end = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = DitoCustomTextStyles.titleKMedium,
                    color = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .width(171.dp)
                    .height(24.dp)
                    .border(1.dp, Color.White, RectangleShape),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(11) {
                        Spacer(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(Color.White.copy(alpha = 0.5f))
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .height(12.dp)
                        .fillMaxWidth(animatedProgress.value)
                        .background(Primary, RectangleShape)
                )
            }
        }

        AnimatedVisibility(
            visible = showValue,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = 6.dp, x = (-10).dp),
            enter = fadeIn(animationSpec = tween(100)) + scaleIn(
                animationSpec = tween(100), initialScale = 0.8f
            ),
            exit = fadeOut(animationSpec = tween(100)) + scaleOut(
                animationSpec = tween(100), targetScale = 0.8f
            )
        ) {
            Box(
                modifier = Modifier
                    .background(Primary, RoundedCornerShape(4.dp))
                    .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "+${(progress * 100).toInt()}",
                    style = DitoCustomTextStyles.titleDSmall,
                    color = Color.Black
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val fakeHomeData = HomeData(
        nickname = "낙동강오리알",
        costumeUrl = "",
        backgroundUrl = null,
        coinBalance = 100,
        weeklyGoal = "주간 목표 예시",
        selfCareStatus = 70,
        focusStatus = 50,
        sleepStatus = 90
    )
    HomeContent(
        homeData = fakeHomeData,
        onCartClick = {},
        onClosetClick = {},
        onSettingsClick = {}
    )
}
