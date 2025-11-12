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
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer

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
    Column(
        modifier = Modifier
            .width(228.dp)
            .height(128.dp)
            .hardShadow(DitoHardShadow.Modal.copy(cornerRadius = 16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(16.dp))
            .padding(horizontal = 32.dp),
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
                onClick = onConfirm
            )

            // 취소 버튼 (검은색)
            CancelButton(
                text = "취소",
                onClick = onDismiss
            )
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
        animationSpec = tween(durationMillis = 50),
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
        animationSpec = tween(durationMillis = 50),
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