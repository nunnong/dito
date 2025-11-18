package com.dito.app.feature.shop

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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import com.dito.app.core.ui.util.SoundPlayer
import com.dito.app.R
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import kotlin.random.Random
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.rotate
import com.dito.app.core.ui.designsystem.StrokeText

/**
 * 상점 구매 확인 모달
 * @param itemImage 구매할 아이템 이미지
 * @param isCostume 의상 아이템인지 여부
 * @param onConfirm 구매 버튼 클릭 시 호출
 * @param onApply 적용하기 버튼 클릭 시 호출
 * @param onDismiss 취소 버튼 또는 모달 외부 클릭 시 호출
 */
@Composable
fun ShopConfirmDialog(
    itemImage: Painter,
    isCostume: Boolean = false,
    onConfirm: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        ShopConfirmDialogContent(
            itemImage = itemImage,
            isCostume = isCostume,
            onConfirm = onConfirm,
            onApply = onApply,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun ShopConfirmDialogContent(
    itemImage: Painter,
    isCostume: Boolean,
    onConfirm: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    var showLemonExplosion by remember { mutableStateOf(false) }
    var isPurchased by remember { mutableStateOf(false) }
    var flashFlipped by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 애니메이션이 끝나면 구매 완료 상태로 전환
    LaunchedEffect(showLemonExplosion) {
        if (showLemonExplosion) {
            kotlinx.coroutines.delay(900) // 애니메이션 시간 + 여유
            onConfirm()
            isPurchased = true
        }
    }

    // 구매 완료 시 flash 배경 좌우반전 애니메이션
    LaunchedEffect(isPurchased) {
        if (isPurchased) {
            // 구매 성공 사운드 재생
            SoundPlayer.playSound(context, R.raw.yay)

            while (true) {
                delay(500)
                flashFlipped = !flashFlipped
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .width(280.dp)
                .wrapContentHeight()
                .align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .hardShadow(DitoHardShadow.Modal.copy(cornerRadius = 16.dp))
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(1.dp, Color.Black, RoundedCornerShape(16.dp))
            ) {
                // 구매 완료 시 flash 배경
                if (isPurchased) {
                    Image(
                        painter = painterResource(id = R.drawable.flash),
                        contentDescription = "Flash background",
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Transparent, RoundedCornerShape(16.dp))
                            .graphicsLayer {
                                scaleX = if (flashFlipped) -1f else 1f
                                clip = true
                                shape = RoundedCornerShape(16.dp)
                            },
                        contentScale = ContentScale.Crop
                    )

                    // 반짝이는 파티클
                    SparkleParticles()
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
        // 제목 영역
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (isPurchased) {
                StrokeText(
                    text = "구매 성공!",
                    style = DitoCustomTextStyles.titleDLarge,
                    fillColor = Color.White,
                    strokeColor = Color.Black,
                    strokeWidth = 2.dp,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "구매하시겠습니까?",
                    style = DitoCustomTextStyles.titleDLarge,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }

        // 아이템 이미지 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .then(
                    if (isPurchased) Modifier.padding(bottom = 16.dp)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = itemImage,
                contentDescription = "구매할 아이템",
                modifier = Modifier
                    .widthIn(max = 160.dp)
                    .heightIn(max = 160.dp)
                    .then(
                        if (!isCostume) Modifier.border(1.dp, Color.Black)
                        else Modifier
                    ),
                contentScale = ContentScale.Fit
            )
        }

        // 버튼 영역
        if (!isPurchased) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
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
        } else {
            // 구매 완료 후 버튼들
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 적용하기 버튼 (노란색)
                ConfirmButton(
                    text = "적용하기",
                    onClick = {
                        onApply()
                        onDismiss()
                    }
                )

                // 돌아가기 버튼 (검은색)
                CancelButton(
                    text = "돌아가기",
                    onClick = onDismiss
                )
            }
        }
            }
        }

        // 레몬 폭죽 애니메이션
        if (showLemonExplosion) {
            LemonExplosion()
        }
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
            .width(80.dp)
            .height(40.dp)
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
            .width(80.dp)
            .height(40.dp)
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

/**
 * 반짝이는 파티클 애니메이션 (8-bit 게임 스타일)
 */
@Composable
private fun SparkleParticles() {
    val sparkleColors = listOf(
        Color.White,
        Color(0xFFFFEB3B), // 노란색
        Color(0xFFFF9800), // 주황색
        Color(0xFFFFFFFF), // 흰색
        Primary // 메인 컬러
    )

    val particles = remember {
        List(30) {
            ParticleState(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 12f + 6f,
                delay = Random.nextInt(500),
                color = sparkleColors.random(),
                isPlus = Random.nextBoolean()
            )
        }
    }

    particles.forEach { particle ->
        val alpha = remember { Animatable(0f) }
        val scale = remember { Animatable(0f) }

        LaunchedEffect(Unit) {
            delay(particle.delay.toLong())
            launch {
                alpha.animateTo(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }
            launch {
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }
        }

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val x = size.width * particle.x
            val y = size.height * particle.y
            val currentSize = particle.size * scale.value

            if (particle.isPlus) {
                // + 모양 (십자가)
                // 가로선
                drawRect(
                    color = particle.color,
                    topLeft = Offset(x - currentSize, y - currentSize / 4),
                    size = androidx.compose.ui.geometry.Size(currentSize * 2, currentSize / 2),
                    alpha = alpha.value
                )
                // 세로선
                drawRect(
                    color = particle.color,
                    topLeft = Offset(x - currentSize / 4, y - currentSize),
                    size = androidx.compose.ui.geometry.Size(currentSize / 2, currentSize * 2),
                    alpha = alpha.value
                )
            } else {
                // X 모양 (대각선)
                rotate(degrees = 45f, pivot = Offset(x, y)) {
                    // 가로선
                    drawRect(
                        color = particle.color,
                        topLeft = Offset(x - currentSize, y - currentSize / 4),
                        size = androidx.compose.ui.geometry.Size(currentSize * 2, currentSize / 2),
                        alpha = alpha.value
                    )
                    // 세로선
                    drawRect(
                        color = particle.color,
                        topLeft = Offset(x - currentSize / 4, y - currentSize),
                        size = androidx.compose.ui.geometry.Size(currentSize / 2, currentSize * 2),
                        alpha = alpha.value
                    )
                }
            }
        }
    }
}

private data class ParticleState(
    val x: Float,
    val y: Float,
    val size: Float,
    val delay: Int,
    val color: Color,
    val isPlus: Boolean
)

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
            itemImage = androidx.compose.ui.res.painterResource(R.drawable.ic_launcher_foreground),
            isCostume = false,
            onConfirm = {},
            onApply = {},
            onDismiss = {}
        )
    }
}