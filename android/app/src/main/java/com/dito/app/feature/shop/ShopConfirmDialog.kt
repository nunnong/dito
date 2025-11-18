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

/**
 * 상점 구매 확인 모달
 * @param itemImage 구매할 아이템 이미지
 * @param onConfirm 구매 버튼 클릭 시 호출
 * @param onDismiss 취소 버튼 또는 모달 외부 클릭 시 호출
 */
@Composable
fun ShopConfirmDialog(
    itemImage: Painter,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        ShopConfirmDialogContent(
            itemImage = itemImage,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun ShopConfirmDialogContent(
    itemImage: Painter,
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
                .width(280.dp)
                .wrapContentHeight()
                .hardShadow(DitoHardShadow.Modal.copy(cornerRadius = 16.dp))
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(1.dp, Color.Black, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 32.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        // 제목 영역
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "구매하시겠습니까?",
                style = DitoCustomTextStyles.titleKMedium, // KoPubDotum Bold 16sp
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }

        // 아이템 이미지 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = itemImage,
                contentDescription = "구매할 아이템",
                modifier = Modifier.size(160.dp)
            )
        }

        // 버튼 영역
        Row(
            modifier = Modifier
                .fillMaxWidth(),
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
            onConfirm = {},
            onDismiss = {}
        )
    }
}