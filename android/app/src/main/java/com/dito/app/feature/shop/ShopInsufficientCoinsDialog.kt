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

/**
 * 상점 레몬 부족 알림 모달
 * @param onDismiss 확인 버튼 또는 모달 외부 클릭 시 호출
 */
@Composable
fun ShopInsufficientCoinsDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        ShopInsufficientCoinsDialogContent(onDismiss = onDismiss)
    }
}

@Composable
private fun ShopInsufficientCoinsDialogContent(
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(228.dp)
            .wrapContentHeight()
            .hardShadow(DitoHardShadow.Modal.copy(cornerRadius = 16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(16.dp))
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 메시지 영역
        Text(
            text = "레몬이 부족합니다.",
            style = DitoCustomTextStyles.titleKMedium, // KoPubDotum Bold 16sp
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // 확인 버튼 (노란색)
        Box(
            modifier = Modifier
                .width(70.dp)
                .height(32.dp)
                .hardShadow(DitoHardShadow.ButtonSmall.copy(cornerRadius = 4.dp))
                .background(Primary, RoundedCornerShape(4.dp))
                .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "확인",
                style = DitoTypography.labelMedium, // KoPubDotum Bold 12sp
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShopInsufficientCoinsDialogPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x80000000)), // 반투명 배경
        contentAlignment = Alignment.Center
    ) {
        ShopInsufficientCoinsDialogContent(onDismiss = {})
    }
}