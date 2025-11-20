package com.dito.app.core.ui.designsystem

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun BounceClickable(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (isPressed: Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    onClick()
                    scope.launch {
                        scale.animateTo(
                            targetValue = 0.8f,
                            animationSpec = tween(75)
                        )
                        scale.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(75)
                        )
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        content(isPressed)
    }
}

@Composable
fun WiggleClickable(
    modifier: Modifier = Modifier, onClick: () -> Unit, content: @Composable () -> Unit
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
                }), contentAlignment = Alignment.Center
    ) {
        content()
    }
}


@Composable
fun StrokeText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    fillColor: Color,
    strokeColor: Color,
    strokeWidth: Dp = 1.dp,      // 기본 1dp 정도로
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 🔥 얇게 둘러줄 오프셋
        val o = strokeWidth

        // 대각선 4방향만 사용
        val offsets = listOf(
            -o to -o,   // 왼쪽 위
            o to -o,    // 오른쪽 위
            -o to o,    // 왼쪽 아래
            o to o      // 오른쪽 아래
        )

        offsets.forEach { (dx, dy) ->
            Text(
                text = text,
                style = style,
                color = strokeColor,
                textAlign = textAlign,
                maxLines = maxLines,
                modifier = Modifier.offset(dx, dy)
            )
        }

        // 가운데 실제 글자
        Text(
            text = text,
            style = style,
            color = fillColor,
            textAlign = textAlign,
            maxLines = maxLines
        )
    }
}

/**
 * Pop 효과음 재생 함수
 * @param context Context
 */
fun playPopSound(context: Context) {
    val mediaPlayer = MediaPlayer.create(context, com.dito.app.R.raw.pop)
    mediaPlayer?.start()
    mediaPlayer?.setOnCompletionListener { mp ->
        mp.release()
    }
}

/**
 * Wiggle 효과음 재생 함수 (볼륨 20%)
 * @param context Context
 */
fun playWiggleSound(context: Context) {
    val mediaPlayer = MediaPlayer.create(context, com.dito.app.R.raw.wiggle)
    mediaPlayer?.setVolume(0.2f, 0.2f)
    mediaPlayer?.start()
    mediaPlayer?.setOnCompletionListener { mp ->
        mp.release()
    }
}

/**
 * 레몬 폭죽 애니메이션
 * @param lemonCount 레몬 파티클 개수 (기본값: 30)
 * @param squareParticleCount 네모 파티클 개수 (기본값: 20)
 */
@Composable
fun LemonExplosion(
    lemonCount: Int = 30,
    squareParticleCount: Int = 20
) {
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
            painter = painterResource(id = com.dito.app.R.drawable.lemon),
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