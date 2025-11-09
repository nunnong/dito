package com.dito.app.feature.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dito.app.R
import com.dito.app.core.ui.designsystem.*
import com.dito.app.core.ui.designsystem.hardShadow

@Composable
fun JoinGroupInfoDialog(
    groupName: String,
    goal: String,
    penalty: String,
    period: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var bet by remember { mutableIntStateOf(10) }
    var showBetPicker by remember { mutableStateOf(false) }

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
                        text = "${groupName} 정보",
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
                    content = "${period}일",
                    iconRes = R.drawable.period
                )

                Spacer(Modifier.height(Spacing.m))

                ChallengeInfoField(
                    title = "목표",
                    content = goal,
                    iconRes = R.drawable.goal
                )

                Spacer(Modifier.height(Spacing.m))

                ChallengeInfoField(
                    title = "벌칙",
                    content = penalty,
                    iconRes = R.drawable.penalty
                )

                Spacer(Modifier.height(Spacing.m))

                BettingAmountField(
                    value = bet,
                    onClick = { showBetPicker = true }
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
                        .background(Primary)
                        .clickable {
                            onConfirm(bet)
                        }
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

    // 배팅 금액 선택 다이얼로그
    if (showBetPicker) {
        NumberPickerDialog(
            title = "배팅 금액 선택",
            range = (10..100).toList(),
            initialValue = bet,
            onDismiss = { showBetPicker = false },
            onConfirm = { amount ->
                bet = amount
                showBetPicker = false
            }
        )
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
                .background(OnSurfaceVariant.copy(alpha = 0.1f))
                .padding(horizontal = Spacing.s, vertical = Spacing.s)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
            )
            Spacer(modifier = Modifier.width(Spacing.m))
            Text(
                text = content,
                color = Color.Black,
                style = DitoTypography.bodySmall
            )
        }
    }
}

/** 숫자 선택 다이얼로그 */
@Composable
private fun NumberPickerDialog(
    title: String,
    range: List<Int>,
    initialValue: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedValue by remember { mutableStateOf(initialValue) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(280.dp)
                .wrapContentHeight()
                .hardShadow(DitoHardShadow.Modal.copy(cornerRadius = 8.dp))
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                .clickable(enabled = false) { /* 내부 클릭 이벤트 차단 */ }
        ) {
            // 타이틀바
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(
                        Primary,
                        RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = DitoCustomTextStyles.titleDMedium,
                    color = Color.Black
                )

                Spacer(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .background(Color.Black)
                )
            }

            // 숫자 선택 영역
            run {
                val initialIndex = range.indexOf(selectedValue).coerceAtLeast(0)
                val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp),
                    state = listState,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(range.size) { index ->
                        val number = range[index]
                        Text(
                            text = number.toString(),
                            style = if (number == selectedValue) {
                                MaterialTheme.typography.titleLarge.copy(
                                    color = Primary
                                )
                            } else {
                                MaterialTheme.typography.bodyLarge
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedValue = number }
                                .padding(vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 버튼 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SmallDialogButton(
                    text = "취소",
                    onClick = onDismiss,
                    isPrimary = false,
                    modifier = Modifier.weight(1f)
                )

                SmallDialogButton(
                    text = "확인",
                    onClick = { onConfirm(selectedValue) },
                    isPrimary = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** 작은 다이얼로그 버튼 */
@Composable
private fun SmallDialogButton(
    text: String,
    onClick: () -> Unit,
    isPrimary: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .hardShadow(DitoHardShadow.ButtonSmall.copy(cornerRadius = 4.dp))
            .clip(RoundedCornerShape(4.dp))
            .background(if (isPrimary) Primary else Color.White)
            .border(1.5.dp, Color.Black, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = DitoCustomTextStyles.titleDMedium,
            color = Color.Black
        )
    }
}
