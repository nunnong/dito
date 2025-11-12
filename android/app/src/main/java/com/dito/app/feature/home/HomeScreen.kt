package com.dito.app.feature.home

import android.media.MediaPlayer
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dito.app.R
import com.dito.app.core.data.home.HomeData
import com.dito.app.core.ui.designsystem.*
import coil.compose.AsyncImage
import androidx.compose.foundation.text.BasicTextField

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dito.app.core.ui.designsystem.BounceClickable

fun playPopSound(context: Context) {
    val mediaPlayer = MediaPlayer.create(context, R.raw.pop)
    mediaPlayer?.start()
    mediaPlayer?.setOnCompletionListener { mp ->
        mp.release()
    }
}

fun playWiggleSound(context: Context) {
    val mediaPlayer = MediaPlayer.create(context, R.raw.wiggle)
    mediaPlayer?.setVolume(0.2f, 0.2f) // Reduce volume to 50%
    mediaPlayer?.start()
    mediaPlayer?.setOnCompletionListener { mp ->
        mp.release()
    }
}



@Composable
fun WiggleClickable(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationZ = rotation.value
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    scope.launch {
                        for (i in 0..1) {
                            rotation.animateTo(targetValue = -15f, animationSpec = tween(75))
                            rotation.animateTo(targetValue = 15f, animationSpec = tween(75))
                        }
                        rotation.animateTo(targetValue = 0f, animationSpec = tween(75))
                    }
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onLogout: () -> Unit,
    onCartClick: () -> Unit,
    onClosetClick: () -> Unit,
    onNotificationClick: () -> Unit
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
                // `homeData`가 null이 아닐 때만 UI를 표시
                val homeData = uiState.homeData!!
                HomeContent(
                    homeData = homeData,
                    isEditingWeeklyGoal = uiState.isEditingWeeklyGoal,
                    weeklyGoalInput = uiState.weeklyGoalInput,
                    onWeeklyGoalInputChange = viewModel::onWeeklyGoalInputChange,
                    onStartEditingWeeklyGoal = viewModel::startEditingWeeklyGoal,
                    onSaveWeeklyGoal = viewModel::saveWeeklyGoal,
                    onCartClick = onCartClick,
                    onClosetClick = onClosetClick,
                    onNotificationClick = onNotificationClick
                )
            }
        }
    }
}

@Composable
fun HomeContent(
    homeData: HomeData,
    isEditingWeeklyGoal: Boolean,
    weeklyGoalInput: String,
    onWeeklyGoalInputChange: (String) -> Unit,
    onStartEditingWeeklyGoal: () -> Unit,
    onSaveWeeklyGoal: () -> Unit,
    onCartClick: () -> Unit,
    onClosetClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var showDitoFaceDialog by remember { mutableStateOf(false) }

    val coinInteractionSource = remember { MutableInteractionSource() }
    val isCoinPressed by coinInteractionSource.collectIsPressedAsState()
    val coinScale by animateFloatAsState(if (isCoinPressed) 0.8f else 1f, label = "coin_scale")
    val lemonRotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val lifecycleOwner = LocalLifecycleOwner.current
    var animationKey by remember { mutableStateOf(0) }

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

    LaunchedEffect(isEditingWeeklyGoal) {
        if (isEditingWeeklyGoal) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }
    // Frame 156: 메인 노란색 카드 (359x589)
    Column(
        modifier = Modifier
            .width(359.dp)
            .height(589.dp)
            .hardShadow(
                DitoHardShadow.Modal.copy(
                    cornerRadius = 0.dp,
                    offsetX = 6.dp,
                    offsetY = 6.dp
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
                            colorFilter = if (isPressed) ColorFilter.tint(Primary) else null,
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
                Image(
                    painter = painterResource(id = R.drawable.mail_home),
                    contentDescription = "missionNotification",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onNotificationClick() },
                    contentScale = ContentScale.Fit
                )
            }

            // Frame 162 - 내부 컨텐츠
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                if (!homeData.backgroundUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = homeData.backgroundUrl,
                        contentDescription = "Background",
                                                    modifier = Modifier
                                                        .fillMaxSize(),
                                                    contentScale = ContentScale.Crop,                        onError = { error ->
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(top = 16.dp, bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 주간 목표 입력 필드 (Frame 177)
                    Row(
                        modifier = Modifier
                            .softShadow(DitoSoftShadow.Low.copy(cornerRadius = 4.dp))
                            .width(261.dp)
                            .height(52.dp)
                            .background(Color.White, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (isEditingWeeklyGoal) {
                            // ✅ 편집 모드: TextField + 체크 아이콘
                            Box(
                                modifier = Modifier.weight(1f)
                            ) {
                                BasicTextField(
                                    value = weeklyGoalInput,
                                    onValueChange = onWeeklyGoalInputChange,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester),
                                    textStyle = DitoCustomTextStyles.titleDSmall.copy(color = Color.Black, textAlign = TextAlign.Center),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // 체크 아이콘
                            Image(
                                painter = painterResource(id = R.drawable.checkbox),
                                contentDescription = "Save",
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { onSaveWeeklyGoal() },
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            // ✅ 일반 모드: 텍스트 + 연필 아이콘 (목표 없을 때만)
                            Text(
                                text = if (homeData.weeklyGoal.isNullOrEmpty()) "주간 목표를 입력해주세요!" else homeData.weeklyGoal,
                                style = DitoCustomTextStyles.titleDSmall,
                                color = if (homeData.weeklyGoal.isNullOrEmpty()) Color(0xFFBDBDBD) else Color.Black,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )

                            // 연필 아이콘 (목표가 없을 때만 표시)
                            if (homeData.weeklyGoal.isNullOrEmpty()) {
                                Image(
                                    painter = painterResource(id = R.drawable.pencil),
                                    contentDescription = "Edit",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { onStartEditingWeeklyGoal() },
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp)) // Increased space

                    // 캐릭터 이미지 + 배경
                    WiggleClickable(
                        modifier = Modifier.size(110.dp),
                        onClick = {
                            playWiggleSound(context)
                            android.util.Log.d("HomeScreen", "Character clicked!")
                        }
                    ) {
                        // 캐릭터 이미지
                        if (homeData.costumeUrl.isNotEmpty()) {
                            AsyncImage(
                                model = homeData.costumeUrl,
                                contentDescription = "Character",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                                onError = { error ->
                                    android.util.Log.e(
                                        "HomeScreen", "이미지 로딩 실패: ${homeData.costumeUrl}",
                                        error.result.throwable
                                    )
                                },
                                onSuccess = {
                                    android.util.Log.d(
                                        "HomeScreen",
                                        "이미지 로딩 성공: ${homeData.costumeUrl}"
                                    )
                                }
                            )

                        } else {
                            // fallback 이미지
                            Image(
                                painter = painterResource(id = R.drawable.dito),
                                contentDescription = "Character",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp)) // Original space

                    // 코인 표시
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 코인 박스
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
                                                lemonRotation.animateTo(targetValue = -15f, animationSpec = tween(75))
                                                lemonRotation.animateTo(targetValue = 15f, animationSpec = tween(75))
                                            }
                                            lemonRotation.animateTo(targetValue = 0f, animationSpec = tween(75))
                                        }
                                    }
                                )
                                .softShadow(DitoSoftShadow.Low.copy(cornerRadius = 48.dp))
                                .widthIn(min = 97.dp) // 최소 너비 설정
                                .height(36.dp)
                                .background(Color.White, RoundedCornerShape(48.dp))
                                .border(1.dp, Color.Black, RoundedCornerShape(48.dp))
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = homeData.coinBalance.toString(),
                                style = DitoCustomTextStyles.titleDLarge, // 22sp
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
            }
            // 프로그레스 바 섹션
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(191.dp)
                    .background(Color.White),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // border-top
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
                    verticalArrangement = Arrangement.Center // Center the content vertically
                ) {
                    // Add a nested column to group the items with their own spacing
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProgressBarItem(label = "자기관리", progress = homeData.selfCareStatus / 100.0f, animationKey = animationKey)
                        ProgressBarItem(label = "집중", progress = homeData.focusStatus / 100.0f, animationKey = animationKey)
                        ProgressBarItem(label = "수면", progress = homeData.sleepStatus / 100.0f, animationKey = animationKey)
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
            // 닉네임 영역
            Column(
                modifier = Modifier
                    .width(252.dp)
                    .height(52.dp)
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = homeData.nickname,
                    style = DitoTypography.headlineMedium, // 28sp
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                // border-bottom
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Color.Black)
                )
            }

            // 원형 버튼
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

@Composable
private fun ProgressBarItem(label: String, progress: Float, animationKey: Any?) {
    var showValue by remember { mutableStateOf(false) }
    val animatedProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    // Animate on entry and when progress value changes
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
        // Frame 172/177/178
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
                                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
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
            // Frame 173
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
                    style = DitoCustomTextStyles.titleKMedium, // 16sp Bold
                    color = Color.White
                )
            }

            // Frame 174 - 프로그레스 바
            Box(
                modifier = Modifier
                    .width(171.dp)
                    .height(24.dp)
                    .border(1.dp, Color.White, RectangleShape),
                contentAlignment = Alignment.CenterStart // Align content to center start
            ) {
                // 눈금 선들 (0, 10, 20, ..., 100)
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(11) { index ->
                        Spacer(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(Color.White.copy(alpha = 0.5f))
                        )
                    }
                }

                // Line 1 - 프로그레스 (노란색)
                Box(
                    modifier = Modifier
                        .height(12.dp)
                        .fillMaxWidth(animatedProgress.value)
                        .background(Primary, RectangleShape)
                )
            }
        }

        // Tooltip that appears on press
        AnimatedVisibility(
            visible = showValue,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = 6.dp, x = (-10).dp),
            enter = fadeIn(animationSpec = tween(100)) + scaleIn(animationSpec = tween(100), initialScale = 0.8f),
            exit = fadeOut(animationSpec = tween(100)) + scaleOut(animationSpec = tween(100), targetScale = 0.8f)
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
    // Preview에서는 ViewModel을 직접 주입할 수 없으므로,
    // 실제 데이터가 있는 HomeContent를 직접 호출하거나 가짜 데이터를 만듭니다.
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
        isEditingWeeklyGoal = false,
        weeklyGoalInput = "",
        onWeeklyGoalInputChange = {},
        onStartEditingWeeklyGoal = {},
        onSaveWeeklyGoal = {},
        onCartClick = {},
        onClosetClick = {},
        onNotificationClick = {}
    )
}
