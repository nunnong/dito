package com.dito.app.feature.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dito.app.R
import com.dito.app.core.ui.designsystem.*
import com.dito.app.core.ui.designsystem.hardShadow

@Preview(showBackground = true)
@Composable
fun CreateGroupNameDialog() {
    var groupName by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = Spacing.l, vertical = Spacing.xl),
        contentAlignment = Alignment.Center
    ) {
        // Column: 전체 컨텐츠를 세로로 배치
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .hardShadow(
                    offsetX = 6.dp,
                    offsetY = 6.dp,
                    cornerRadius = 32.dp,
                    color = Color.Black
                )
                .clip(DitoShapes.extraLarge)
                .border(1.dp, Color.Black, DitoShapes.extraLarge)
                .background(Color.White)
                .padding(vertical = Spacing.l),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Box: 뒤로가기 버튼을 왼쪽에 정렬하기 위해
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.s, vertical = Spacing.m)
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

            // 제목 텍스트
            Text(
                text = "방의 이름을\n정해주세요",
                color = OnSurface,
                style = DitoTypography.headlineMedium
            )

            Spacer(Modifier.height(Spacing.xl))

            // 캐릭터 이미지
            Image(
                painter = painterResource(id = R.drawable.dito),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(115.dp)
            )

            Spacer(Modifier.height(Spacing.xl))

            // Box: 입력 필드에 밑줄을 그리기 위해
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(horizontal = Spacing.m)
            ) {
                BasicTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    textStyle = DitoTypography.bodyLarge.copy(color = OnSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            val strokeWidth = 2.dp.toPx()
                            val y = size.height - strokeWidth / 2
                            drawLine(
                                color = Color.Black,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        }
                        .padding(vertical = Spacing.s),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            innerTextField()
                        }

                    }
                )
                Image(
                    painter = painterResource(id = R.drawable.x),
                    contentDescription = "방 이름 삭제",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd)

                )
            }

            Spacer(Modifier.height(Spacing.xxl))

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
                    .background(Color.White)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "계속하기",
                    color = Color.Black,
                    style = DitoCustomTextStyles.titleDMedium
                )
            }

            Spacer(Modifier.height(Spacing.m))
        }
    }
}
