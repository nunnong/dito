package com.dito.app.feature.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dito.app.core.ui.designsystem.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.dito.app.R
import com.dito.app.core.ui.util.SoundPlayer
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * 상점 구매 확인 모달
 * @param onConfirm 구매 버튼 클릭 시 호출
 * @param onDismiss 취소 버튼 또는 모달 외부 클릭 시 호출
 */
@Composable
fun ShopConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        ShopConfirmDialogContent(
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun ShopConfirmDialogContent(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var showLemonExplosion by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // 애니메이션이 끝나면 onConfirm 호출
    LaunchedEffect(showLemonExplosion) {
        if (showLemonExplosion) {
            kotlinx.coroutines.delay(900) // 애니메이션 시간 + 여유
            onConfirm()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .width(228.dp)
                .height(128.dp)
                .hardShadow(DitoHardShadow.Modal.copy(cornerRadius = 16.dp))
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(1.dp, Color.Black, RoundedCornerShape(16.dp))
                .padding(horizontal = 32.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        // 제목 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(top = 24.dp, start = 4.dp, end = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "구매하시겠습니까?",
                style = DitoCustomTextStyles.titleKMedium, // KoPubDotum Bold 16sp
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }

        // 버튼 영역
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(top = 24.dp, bottom = 24.dp, start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 구매 버튼 (노란색)
            ConfirmButton(
                text = "구매",
                onClick = {
                    showLemonExplosion = true
                    SoundPlayer.playSound(context, R.raw.purchase)
                }
            )

            // 취소 버튼 (검은색)
            CancelButton(
                text = "취소",
                onClick = onDismiss
            )
        }
        }

        // 레몬 폭죽 애니메이션
        if (showLemonExplosion) {
            LemonExplosion()
        }
    }
}

/**
 * 레몬 폭죽 애니메이션
 */
@Composable
private fun LemonExplosion() {
    val lemonCount = 30 // 레몬 개수
    val squareParticleCount = 20 // 네모 파티클 개수

    Box(modifier = Modifier.fillMaxSize()) {
        // 레몬 파티클들
        repeat(lemonCount) { index ->
            val baseAngle = (360f / lemonCount) * index
            // 각도에 랜덤성 추가 (-15도 ~ +15도)
            val angleOffset = Random.nextFloat() * 30f - 15f
            val angle = baseAngle + angleOffset

            // 크기에 랜덤성 추가 (0.7배 ~ 1.3배)
            val sizeMultiplier = 0.7f + Random.nextFloat() * 0.6f

            LemonParticle(
                angle = angle,
                sizeMultiplier = sizeMultiplier
            )
        }

        // 노란색 네모 파티클들
        repeat(squareParticleCount) { index ->
            val angle = Random.nextFloat() * 360f
            val sizeMultiplier = 0.3f + Random.nextFloat() * 1.0f

            SquareParticle(
                angle = angle,
                sizeMultiplier = sizeMultiplier
            )
        }
    }
}

/**
 * 레몬 파티클
 */
@Composable
private fun LemonParticle(angle: Float, sizeMultiplier: Float) {
    // 거리에도 랜덤성 추가
    val targetDistance = 450f + Random.nextFloat() * 200f
    // 애니메이션 시간에도 랜덤성 추가 (700 ~ 900ms)
    val duration = (700 + Random.nextInt(200)).toInt()
    // 회전 방향 랜덤 (시계/반시계)
    val rotationDirection = if (Random.nextBoolean()) 1f else -1f

    val distance = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // 동시에 여러 애니메이션 실행
        launch {
            distance.animateTo(
                targetValue = targetDistance,
                animationSpec = tween(
                    durationMillis = duration,
                    easing = FastOutSlowInEasing
                )
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = duration,
                    easing = FastOutSlowInEasing
                )
            )
        }
        launch {
            rotation.animateTo(
                targetValue = rotationDirection * (360f + Random.nextFloat() * 360f), // 1~2바퀴 회전
                animationSpec = tween(
                    durationMillis = duration,
                    easing = FastOutSlowInEasing
                )
            )
        }
        launch {
            scale.animateTo(
                targetValue = 0.2f + Random.nextFloat() * 0.3f, // 0.2 ~ 0.5배로 축소
                animationSpec = tween(
                    durationMillis = duration,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    // 각도에 따른 x, y 위치 계산
    val radian = Math.toRadians(angle.toDouble())
    val offsetX = (cos(radian) * distance.value).toFloat()
    val offsetY = (sin(radian) * distance.value).toFloat()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        Image(
            painter = painterResource(id = R.drawable.lemon),
            contentDescription = "Lemon Particle",
            modifier = Modifier
                .size((45 * sizeMultiplier).dp)
                .graphicsLayer {
                    translationX = offsetX
                    translationY = offsetY
                    this.alpha = alpha.value
                    rotationZ = rotation.value
                    scaleX = scale.value
                    scaleY = scale.value
                }
        )
    }
}

/**
 * 노란색 네모 파티클
 */
@Composable
private fun SquareParticle(angle: Float, sizeMultiplier: Float) {
    // 거리에 랜덤성 추가 (200 ~ 450)
    val targetDistance = 400f + Random.nextFloat() * 250f
    // 애니메이션 시간에 랜덤성 추가 (600 ~ 1000ms)
    val duration = (600 + Random.nextInt(400)).toInt()
    // 회전 각도 랜덤
    val targetRotation = Random.nextFloat() * 720f

    val distance = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        launch {
            distance.animateTo(
                targetValue = targetDistance,
                animationSpec = tween(
                    durationMillis = duration,
                    easing = FastOutSlowInEasing
                )
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = duration,
                    easing = FastOutSlowInEasing
                )
            )
        }
        launch {
            rotation.animateTo(
                targetValue = targetRotation,
                animationSpec = tween(
                    durationMillis = duration,
                    easing = FastOutSlowInEasing
                )
            )
        }
        launch {
            scale.animateTo(
                targetValue = 0.1f,
                animationSpec = tween(
                    durationMillis = duration,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    val radian = Math.toRadians(angle.toDouble())
    val offsetX = (cos(radian) * distance.value).toFloat()
    val offsetY = (sin(radian) * distance.value).toFloat()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        Box(
            modifier = Modifier
                .size((8 * sizeMultiplier).dp)
                .graphicsLayer {
                    translationX = offsetX
                    translationY = offsetY
                    this.alpha = alpha.value
                    rotationZ = rotation.value
                    scaleX = scale.value
                    scaleY = scale.value
                }
                .background(Primary, RoundedCornerShape(2.dp))
        )
    }
}

/**
 * 구매 버튼 (노란색)
 */
@Composable
private fun ConfirmButton(
    text: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        label = "confirm_button_scale"
    )

    Box(
        modifier = Modifier
            .width(70.dp)
            .height(32.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .hardShadow(DitoHardShadow.ButtonSmall.copy(cornerRadius = 4.dp))
            .background(Primary, RoundedCornerShape(4.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = DitoTypography.labelMedium, // KoPubDotum Bold 12sp
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 취소 버튼 (검은색)
 */
@Composable
private fun CancelButton(
    text: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        label = "cancel_button_scale"
    )

    Box(
        modifier = Modifier
            .width(70.dp)
            .height(32.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .hardShadow(DitoHardShadow.ButtonSmall.copy(cornerRadius = 4.dp))
            .background(Color.Black, RoundedCornerShape(4.dp))
            .border(1.dp, Color.White, RoundedCornerShape(4.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = DitoTypography.labelMedium, // KoPubDotum Bold 12sp
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ShopConfirmDialogPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x80000000)), // 반투명 배경
        contentAlignment = Alignment.Center
    ) {
        ShopConfirmDialogContent(
            onConfirm = {},
            onDismiss = {}
        )
    }
}