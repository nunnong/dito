package com.dito.app.feature.group.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dito.app.R
import com.dito.app.core.ui.designsystem.*

@Composable
fun ChallengeModal() {
    var period by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    var penalty by remember { mutableStateOf("") }
    var bet by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.l, vertical = Spacing.m)
    ) {

        // 상단 배너
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(DitoShapes.extraSmall)
                .border(1.dp, Color.Black, DitoShapes.extraSmall)
                .background(Color(0xFFFFDC5A))
                .padding(horizontal = Spacing.m, vertical = Spacing.s)
        ) {
            Image(
                painter = painterResource(id = R.drawable.dito),
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .padding(end = Spacing.m)
            )

            Column {
                Text(
                    text = "팀원들과 함께 도전해봐요!",
                    color = Color.Black,
                    style = DitoCustomTextStyles.titleDMedium
                )
                Text(
                    text = "1등이 되면 모든 참가자의 배팅 금액을 가져요!",
                    color = PrimaryContainer,
                    style = DitoTypography.labelSmall
                )
            }
        }

        Spacer(Modifier.height(Spacing.l))

        ChallengePixelField(
            title = "기간(일수)",
            hint = "최소 1일 이상으로 입력해주세요.",
            iconRes = R.drawable.period,
            value = period,
            onValueChange = { period = it }
        )

        Spacer(Modifier.height(Spacing.m))

        ChallengePixelField(
            title = "목표",
            hint = "예 : 유튜브 하루 2시간 이하",
            iconRes = R.drawable.goal,
            value = goal,
            onValueChange = { goal = it }
        )

        Spacer(Modifier.height(Spacing.m))

        ChallengePixelField(
            title = "벌칙",
            hint = "예 : 커피 사주기",
            iconRes = R.drawable.penalty,
            value = penalty,
            onValueChange = { penalty = it }
        )

        Spacer(Modifier.height(Spacing.m))

        ChallengePixelField(
            title = "배팅 금액(최소 10레몬)",
            hint = "10",
            iconRes = R.drawable.coin,
            value = bet,
            onValueChange = { bet = it }
        )

        Spacer(Modifier.height(Spacing.xl))

        // 🔸 픽셀 감성 버튼 (Offset 그림자)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = 2.dp, y = 2.dp)
                .clip(DitoShapes.extraSmall)
                .border(1.dp, Color.Black, DitoShapes.extraSmall)
                .background(Color.White)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "챌린지 방 만들기",
                color = Color.Black,
                style = DitoCustomTextStyles.titleDMedium
            )
        }

        Spacer(Modifier.height(Spacing.xl))
    }
}

@Composable
fun ChallengePixelField(
    title: String,
    hint: String,
    iconRes: Int,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 🔹 Label
        Text(
            text = title,
            color = Color.Black,
            style = DitoTypography.labelLarge,
            modifier = Modifier.padding(bottom = Spacing.xs)
        )

        // 🔹 Input Box
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(DitoShapes.extraSmall)
                .border(1.dp, Color.Black, DitoShapes.extraSmall)
                .background(Color.White)
                .padding(horizontal = Spacing.m, vertical = Spacing.s)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = Spacing.s)
            )
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = hint,
                        color = Color(0xFF707479),
                        style = DitoTypography.bodySmall
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = DitoTypography.bodySmall.copy(color = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
