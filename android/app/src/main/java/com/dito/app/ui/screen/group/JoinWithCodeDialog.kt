package com.dito.app.ui.screen.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dito.app.R
import com.dito.app.core.ui.designsystem.Background
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoShapes
import com.dito.app.core.ui.designsystem.OnSurface
import com.dito.app.core.ui.designsystem.hardShadow

@Preview(showBackground = true)
@Composable
fun JoinWithCodeDialog() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(color = Background)
    ) {
        // 내용 다이얼로그
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .hardShadow(
                    offsetX = 6.dp,
                    offsetY = 6.dp,
                    cornerRadius = 32.dp,
                    color = OnSurface
                )
                .clip(DitoShapes.extraLarge)
                .border(
                    width = 1.dp,
                    color = OnSurface,
                    shape = DitoShapes.extraLarge
                )
                .background(color = Background)
                .padding(horizontal = 48.dp, vertical = 56.dp)
        ) {
            // 닫기 버튼 (다이얼로그 우측 상단)
            Image(
                painter = painterResource(id = R.drawable.close),
                contentDescription = "창 닫기",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.TopEnd)
            )

            // 컨텐츠
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "코드로 참여하기",
                    color = OnSurface,
                    style = DitoCustomTextStyles.titleKLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(40.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .border(
                                    1.dp, color = OnSurface,
                                    shape = DitoShapes.extraSmall
                                )
                                .background(color = Background)
                        )
                    }
                }
            }
        }
    }
}
