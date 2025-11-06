package com.dito.app.feature.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.dito.app.core.ui.designsystem.Outline
import com.dito.app.core.ui.designsystem.Spacing
import com.dito.app.core.ui.designsystem.Spacing.m
import com.dito.app.core.ui.designsystem.hardShadow
import com.dito.app.core.ui.component.DitoModalContainer

@Preview(showBackground = true)
@Composable
fun JoinWithCodeDialog() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(color = Background)
    ) {
        DitoModalContainer(
            modifier = Modifier.fillMaxWidth(0.9f).widthIn(max = 360.dp),
            contentPadding = PaddingValues(horizontal = 48.dp, vertical = 24.dp)
        ) {
            Box {
                // 닫기 버튼을 가장 상단에 배치
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = Spacing.xl)
                ) {
                    Text(
                        text = "코드로 참여하기",
                        color = OnSurface,
                        style = DitoCustomTextStyles.titleKLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(40.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeat(4) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp)
                                    .border(
                                        1.dp, color = OnSurface, shape = DitoShapes.extraSmall
                                    )
                                    .background(color = Background)
                            )

                        }
                    }


                    Spacer(Modifier.height(Spacing.xl))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .hardShadow(
                                offsetX = 4.dp,
                                offsetY = 4.dp,
                                cornerRadius = 8.dp,
                                color = Color.Black
                            )
                            .clip(DitoShapes.small)
                            .border(1.dp, Outline, DitoShapes.small)
                            .background(Color.White)
                            .padding(vertical = m),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "입력",
                            color = Color.Black,
                            style = DitoCustomTextStyles.titleKMedium
                        )
                    }
                }
            }
        }
    }
}

