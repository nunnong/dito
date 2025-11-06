package com.dito.app.feature.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dito.app.R
import com.dito.app.core.ui.component.DitoModalContainer
import com.dito.app.core.ui.designsystem.Background
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.OnSurface
import com.dito.app.core.ui.designsystem.Spacing

@Preview(showBackground = true)
@Composable
fun EditNotiCount() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = Spacing.l, vertical = Spacing.xl),
        contentAlignment = Alignment.Center
    ) {
        DitoModalContainer(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color.White,
            borderColor = Color.Black,
            shadowColor = Color.Black,
            contentPadding = PaddingValues(vertical = Spacing.s)
        ) {
            // Column: 전체 컨텐츠를 세로로 배치
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Box: 뒤로가기 버튼을 왼쪽에 정렬하기 위해
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.m, vertical = Spacing.s)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "뒤로가기",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.TopStart)
                    )
                }

                Spacer(Modifier.height(Spacing.xl))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.s, vertical = Spacing.s),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.slide),
                        contentDescription = "변경하기",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(24.dp)

                    )
                }

                // 제목 텍스트
                Text(
                    text = "미션 빈도 설정",
                    color = OnSurface,
                    style = DitoCustomTextStyles.titleKLarge
                )

                Spacer(Modifier.height(Spacing.xxl))

                // 카운터 컨트롤 Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.l),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.m),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Minus Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .clickable { /* TODO: Decrease count */ }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "−",
                            color = Color.Black,
                            style = DitoCustomTextStyles.titleKLarge
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "유지",
                            color = Color.Black,
                            style = DitoCustomTextStyles.titleKMedium
                        )
                    }

                    // Plus Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .clickable { /* TODO: Increase count */ }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            color = Color.Black,
                            style = DitoCustomTextStyles.titleKLarge
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.m))
            }
        }
    }
}
