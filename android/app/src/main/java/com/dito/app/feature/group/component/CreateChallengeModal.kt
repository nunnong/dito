package com.dito.app.feature.group.component

import DitoShapes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dito.app.R
import com.dito.ui.theme.Background
import com.dito.ui.theme.Outline
import com.dito.ui.theme.PrimaryContainer
import com.dito.ui.theme.Spacing

/**
 * 방 생성 모달의 최상위 컴포저블.
 * - onDismiss: 바깥 클릭/뒤로가기 등으로 모달을 닫고 싶을 때 호출되는 콜백
 * - onCreate : "챌린지 방 만들기" 버튼 클릭 시 호출되는 콜백
 */

@Composable
fun CreateChallengeModal(
    onDismiss: () -> Unit = {},
    onCreate: () -> Unit = {}
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = DitoShapes.extraLarge,
            color = Background,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            CreateChallengeContent(onDismiss = onDismiss, onCreate = onCreate)
        }
    }
}

// 모달 내부
@Composable
private fun CreateChallengeContent(onCreate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(horizontal = Spacing.l, vertical = Spacing.xl)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .border(1.dp, Outline, DitoShapes.extraLarge)
                .clip(DitoShapes.extraLarge)
                .background(Surface)
        ) {
            Image(
                painter = painterResource(id = R.drawable.dito),
                contentDescription = "banner",
                modifier = Modifier
                    .padding(top = Spacing.m, bottom = Spacing.s)
                    .clip(DitoShapes.extraLarge)
                    .height(80.dp)
                    .fillMaxWidth(),
                colorFilter = null,
                contentScale = ContentScale.Crop // 가로에 맞춰 자르기
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = Spacing.s)
                    .border(1.dp, Outline)
                    .fillMaxWidth()
                    .background(
                        PrimaryContainer
                    ).padding(vertical = Spacing.s, horizontal = Spacing.m)
            ) {

            }

        }
    }
}