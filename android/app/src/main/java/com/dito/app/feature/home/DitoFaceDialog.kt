package com.dito.app.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dito.app.R
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles

/**
 * 캐릭터 얼굴 표정 안내 다이얼로그
 *
 * 사용자의 스탯에 따라 디토의 표정이 변한다는 것을 알려주는 정보 제공용 다이얼로그
 *
 * @param onDismiss 다이얼로그를 닫을 때 호출되는 콜백
 */
@Composable
fun DitoFaceDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        // 배경 클릭 영역 (모달 밖 클릭 시 닫힘)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            // 모달 컨텐츠 (내부 클릭 이벤트 전파 방지)
            Column(
                modifier = Modifier
                    .width(286.dp)
                    .height(140.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { /* 클릭 이벤트 전파 방지 */ }
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
            ) {
                // 안내 문구
                Text(
                    text = "내 스탯에 따라\n디토의 표정이 바뀌어요!",
                    style = DitoCustomTextStyles.titleDSmall,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // 얼굴 표정 5개 (가로로 나열)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(22.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 얼굴 1 (가장 슬픔)
                    Image(
                        painter = painterResource(id = R.drawable.face_1),
                        contentDescription = "가장 슬픈 표정",
                        modifier = Modifier.size(30.dp, 22.dp)
                    )

                    // 얼굴 2 (슬픔)
                    Image(
                        painter = painterResource(id = R.drawable.face_2),
                        contentDescription = "슬픈 표정",
                        modifier = Modifier.size(30.dp, 22.dp)
                    )

                    // 얼굴 3 (보통)
                    Image(
                        painter = painterResource(id = R.drawable.face_3),
                        contentDescription = "보통 표정",
                        modifier = Modifier.size(30.dp, 22.dp)
                    )

                    // 얼굴 4 (기쁨)
                    Image(
                        painter = painterResource(id = R.drawable.face_4),
                        contentDescription = "기쁜 표정",
                        modifier = Modifier.size(30.dp, 22.dp)
                    )

                    // 얼굴 5 (가장 행복)
                    Image(
                        painter = painterResource(id = R.drawable.face_5),
                        contentDescription = "가장 행복한 표정",
                        modifier = Modifier.size(30.dp, 22.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DitoFaceDialogPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEEEEE)),
        contentAlignment = Alignment.Center
    ) {
        DitoFaceDialog(
            onDismiss = {}
        )
    }
}