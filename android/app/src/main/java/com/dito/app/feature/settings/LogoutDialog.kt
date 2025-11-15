package com.dito.app.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dito.app.core.ui.component.DitoModalContainer
import com.dito.app.core.ui.designsystem.Background
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoShapes
import com.dito.app.core.ui.designsystem.ErrorContainer
import com.dito.app.core.ui.designsystem.OnSurface
import com.dito.app.core.ui.designsystem.Spacing
import com.dito.app.core.ui.designsystem.Surface
import com.dito.app.core.ui.designsystem.hardShadow

@Composable
fun LogoutDialog(
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(horizontal = Spacing.l, vertical = Spacing.xl),
        contentAlignment = Alignment.Center
    ) {
        DitoModalContainer(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color.White,
            borderColor = Color.Black,
            shadowColor = Color.Black,
            contentPadding = PaddingValues(vertical = Spacing.m, horizontal = Spacing.m)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(Spacing.xl)) // 상단 여백 추가

                // 제목 텍스트
                Text(
                    text = "정말 로그아웃하시겠어요?",
                    color = OnSurface,
                    style = DitoCustomTextStyles.titleKLarge
                )

                Spacer(Modifier.height(Spacing.xxl))

                // 버튼 Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.m)
                ) {
                    // 취소 버튼
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .hardShadow(
                                offsetX = 4.dp,
                                offsetY = 4.dp,
                                cornerRadius = 8.dp,
                                color = Color.Black
                            )
                            .clip(DitoShapes.small)
                            .border(1.dp, Color.Black, DitoShapes.small)
                            .background(Color.White)
                            .clickable { onDismiss() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "취소",
                            color = Color.Black,
                            style = DitoCustomTextStyles.titleKMedium
                        )
                    }

                    // 로그아웃 버튼
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .hardShadow(
                                offsetX = 4.dp,
                                offsetY = 4.dp,
                                cornerRadius = 8.dp,
                                color = Color.Black
                            )
                            .clip(DitoShapes.small)
                            .border(1.dp, Color.Black, DitoShapes.small)
                            .background(Surface)
                            .clickable { onConfirm() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "로그아웃",
                            color = Color.Black,
                            style = DitoCustomTextStyles.titleKMedium
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.s)) // 하단 여백 추가
            }
        }
    }
}
