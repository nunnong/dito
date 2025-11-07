package com.dito.app.feature.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import com.dito.app.core.ui.designsystem.Primary
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
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
fun EditNotiCount(onDismiss: () -> Unit = {}) {

    var selectedOption by remember { mutableStateOf(1)}

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
                            .clickable{onDismiss()}
                    )
                }

                Spacer(Modifier.height(Spacing.s))
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

                Spacer(Modifier.height(Spacing.s))

                Text(
                    text = "일주일 단위로 [보통]이 기준점이 됩니다",
                    color = OnSurface.copy(alpha = 0.6f),
                    style = DitoCustomTextStyles.titleKSmall,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(Spacing.l))

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
                            .background(
                                if (selectedOption == 0) Primary else Color.White,  // 선택 시 노란색
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedOption = 0}
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "적게",
                            color = Color.Black,
                            style = DitoCustomTextStyles.titleKMedium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
                            .background(
                                if (selectedOption == 1) Primary else Color.White,  // 선택 시 노란색
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedOption = 1 }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "보통",
                            color = Color.Black,
                            style = DitoCustomTextStyles.titleKMedium
                        )
                    }

                    // Plus Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
                            .background(
                                if (selectedOption == 2) Primary else Color.White,  // 선택 시 노란색
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedOption = 2}
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "많이",
                            color = Color.Black,
                            style = DitoCustomTextStyles.titleKMedium
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.m))
            }
        }
    }
}
