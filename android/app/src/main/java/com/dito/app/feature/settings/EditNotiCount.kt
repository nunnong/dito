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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dito.app.R
import com.dito.app.core.ui.component.DitoModalContainer
import com.dito.app.core.ui.designsystem.Background
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoShapes
import com.dito.app.core.ui.designsystem.OnSurface
import com.dito.app.core.ui.designsystem.Spacing
import com.dito.app.core.ui.designsystem.hardShadow

@Composable
fun EditNotiCount(
    onDismiss: () -> Unit = {},
    viewModel: SettingViewModel = hiltViewModel()
) {
    // 저장된 빈도를 불러와서 초기값으로 설정
    val savedFrequency = remember { viewModel.getFrequency() }
    val initialOption = remember {
        when (savedFrequency) {
            "LOW" -> 0
            "NORMAL" -> 1
            "HIGH" -> 2
            else -> 1
        }
    }

    var selectedOption by remember { mutableStateOf(initialOption) }
    val uiState by viewModel.uiState.collectAsState()

    // 옵션이 변경되었는지 확인 (초기값과 비교)
    val hasChanged = selectedOption != initialOption

    fun getFrequencyString(option: Int): String {
        return when (option) {
            0 -> "LOW"
            1 -> "NORMAL"
            2 -> "HIGH"
            else -> "NORMAL"
        }
    }

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
                        .fillMaxWidth(0.7f)
                        .align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                            .clickable {
                                selectedOption = 0
                            }
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
                            .clickable {
                                selectedOption = 1
                            }
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
                            .clickable {
                                selectedOption = 2
                            }
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

                // 확인 버튼
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .hardShadow(
                            offsetX = 4.dp,
                            offsetY = 4.dp,
                            cornerRadius = 8.dp,
                            color = Color.Black
                        )
                        .clip(DitoShapes.small)
                        .border(1.dp, Color.Black, DitoShapes.small)
                        .background(if (hasChanged) Primary else Color.White)
                        .clickable {
                            if (hasChanged) {
                                val frequency = getFrequencyString(selectedOption)
                                viewModel.updateFrequency(frequency) {
                                    onDismiss()
                                }
                            } else {
                                onDismiss()
                            }
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "확인",
                        color = Color.Black,
                        style = DitoCustomTextStyles.titleKMedium
                    )
                }

                Spacer(Modifier.height(Spacing.l))
            }
        }
    }
}
