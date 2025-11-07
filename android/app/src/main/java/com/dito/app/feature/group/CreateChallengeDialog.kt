package com.dito.app.feature.group
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.unit.dp
import com.dito.app.R
import com.dito.app.core.ui.component.DitoModalContainer
import com.dito.app.core.ui.designsystem.Background
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoShapes
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.OnSurfaceVariant
import com.dito.app.core.ui.designsystem.Outline
import com.dito.app.core.ui.designsystem.PrimaryContainer
import com.dito.app.core.ui.designsystem.Spacing
import com.dito.app.core.ui.designsystem.hardShadow

@Composable
fun CreateChallengeDialog(
    onDismiss: () -> Unit
) {
    var period by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    var penalty by remember { mutableStateOf("") }
    var bet by remember { mutableStateOf("") }

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
            contentPadding = PaddingValues(vertical = Spacing.l)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
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
                        .clickable { onDismiss() }
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
                        text = "팀원들과 함께 도전해봐요!",
                        color = Color.Black,
                        style = DitoCustomTextStyles.titleDMedium
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text = "1등이 되면 모든 참가자의 배팅 금액을 가져요!",
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
                ChallengeInputField(
                    title = "기간(일수)",
                    hint = "최소 1일 이상으로 입력해주세요.",
                    iconRes = R.drawable.period,
                    value = period,
                    onValueChange = { period = it }
                )

                Spacer(Modifier.height(Spacing.m))

                ChallengeInputField(
                    title = "목표",
                    hint = "예 : 유튜브 하루 2시간 이하",
                    iconRes = R.drawable.goal,
                    value = goal,
                    onValueChange = { goal = it }
                )

                Spacer(Modifier.height(Spacing.m))

                ChallengeInputField(
                    title = "벌칙",
                    hint = "예 : 커피 사주기",
                    iconRes = R.drawable.penalty,
                    value = penalty,
                    onValueChange = { penalty = it }
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
                        .background(Background)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "챌린지 방 만들기",
                        color = Color.Black,
                        style = DitoCustomTextStyles.titleDMedium
                    )
                }
            }

                Spacer(Modifier.height(Spacing.xl))
            }
        }
    }
}

@Composable
fun ChallengeInputField(
    title: String,
    hint: String,
    iconRes: Int,
    value: String,
    onValueChange: (String) -> Unit
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
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = hint,
                        color = OnSurfaceVariant,
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
