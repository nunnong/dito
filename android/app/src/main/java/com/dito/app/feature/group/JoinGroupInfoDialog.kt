package com.dito.app.feature.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dito.app.R
import com.dito.app.core.ui.designsystem.*
import com.dito.app.core.ui.designsystem.hardShadow

@Preview(showBackground = true)
@Composable
fun JoinGroupInfoDialog() {
    var bet by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = Spacing.l, vertical = Spacing.xl),
        contentAlignment = Alignment.Center
    ) {
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
                .verticalScroll(rememberScrollState())
                .padding(vertical = Spacing.l)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = 32.dp,
                            topEnd = 32.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                        )
                    )
                    .background(Color.White)
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

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryContainer)
                    .drawBehind {
                        // 위쪽 선
                        drawLine(
                            color = Outline, // 선 색상
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                        // 아래쪽 선
                        drawLine(
                            color = Outline,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.dito),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(end = Spacing.m)
                )
                Column {
                    Text(
                        text = "방 정보",
                        color = Color.Black,
                        style = DitoCustomTextStyles.titleDMedium
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text = "챌린지 내용을 확인하고, 도전에 함께하세요!",
                        color = OnSurfaceVariant,
                        style = DitoTypography.labelSmall
                    )
                }
            }

            Spacer(Modifier.height(Spacing.l))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.m)
            ) {
                ChallengeInfoField(
                    title = "기간(일수)",
                    content = "7일",
                    iconRes = R.drawable.period
                )

                Spacer(Modifier.height(Spacing.m))

                ChallengeInfoField(
                    title = "목표",
                    content = "유튜브 하루 2시간 이하",
                    iconRes = R.drawable.goal
                )

                Spacer(Modifier.height(Spacing.m))

                ChallengeInfoField(
                    title = "벌칙",
                    content = "커피 사주기",
                    iconRes = R.drawable.penalty
                )

                Spacer(Modifier.height(Spacing.m))

                ChallengeInputField(
                    title = "배팅 금액(최소 10레몬)",
                    hint = "10",
                    iconRes = R.drawable.coin,
                    value = bet,
                    onValueChange = { bet = it }
                )

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
                        .border(1.dp, Color.Black, DitoShapes.small)
                        .background(Color.White)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "방 입장하기",
                        color = Color.Black,
                        style = DitoCustomTextStyles.titleDMedium
                    )
                }
            }

            Spacer(Modifier.height(Spacing.xl))
        }
    }
}

@Composable
fun ChallengeInfoField(
    title: String,
    content: String,
    iconRes: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            color = Color.Black,
            style = DitoTypography.labelLarge,
            modifier = Modifier.padding(bottom = Spacing.xs)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(DitoShapes.small)
                .border(1.dp, Color.Black, DitoShapes.small)
                .background(Color.White)
                .padding(horizontal = Spacing.s, vertical = Spacing.s)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
            )
            Spacer(modifier = Modifier.width(Spacing.xs))
            Text(
                text = content,
                color = Color.Black,
                style = DitoTypography.bodySmall
            )
        }
    }
}
