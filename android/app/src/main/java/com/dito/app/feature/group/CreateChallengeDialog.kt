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
import androidx.compose.foundation.text.BasicTextField
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
import com.dito.app.core.ui.component.DitoModalContainer
import com.dito.app.core.ui.designsystem.Background
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoHardShadow
import com.dito.app.core.ui.designsystem.DitoShapes
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.OnSurfaceVariant
import com.dito.app.core.ui.designsystem.Outline
import com.dito.app.core.ui.designsystem.Primary
import com.dito.app.core.ui.designsystem.PrimaryContainer
import com.dito.app.core.ui.designsystem.Spacing
import com.dito.app.core.ui.designsystem.hardShadow

@Composable
fun CreateChallengeDialog(
    groupName: String,
    onDismiss: () -> Unit,
    onCreateChallenge: (String, String, String, Int, Int) -> Unit = { _, _, _, _, _ -> }
) {
    var period by remember { mutableIntStateOf(1) }
    var goal by remember { mutableStateOf("") }
    var penalty by remember { mutableStateOf("") }
    var bet by remember { mutableIntStateOf(10) }
    var showPeriodPicker by remember { mutableStateOf(false) }
    var showBetPicker by remember { mutableStateOf(false) }

    // 옵션 선택 상태 (1: 총 사용 시간만 공유, 2: 상세 앱별 시간 공유)
    var selectedOption by remember { mutableIntStateOf(1) }

    // 옵션2 관련 상태
    var selectedApp by remember { mutableStateOf("유튜브") }
    var selectedTimeValue by remember { mutableIntStateOf(60) } // 기본값 60분 (1시간)
    var showAppPicker by remember { mutableStateOf(false) }
    var showTimeValuePicker by remember { mutableStateOf(false) }

    // 시간 값에 따라 단위를 자동으로 결정하는 함수
    fun getTimeDisplay(minutes: Int): Pair<Int, String> {
        return when {
            minutes < 60 -> Pair(minutes, "분")
            minutes % 60 == 0 -> Pair(minutes / 60, "시간")
            else -> Pair(minutes, "분")
        }
    }

    val isFormValid = if (selectedOption == 1) {
        goal.isNotEmpty() && penalty.isNotEmpty()
    } else {
        penalty.isNotEmpty()
    }

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
                PeriodPickerField(
                    value = period,
                    onClick = { showPeriodPicker = true }
                )

                Spacer(Modifier.height(Spacing.m))

                // 옵션 선택 UI
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "공유 옵션",
                        color = Color.Black,
                        style = DitoTypography.labelLarge,
                        modifier = Modifier.padding(bottom = Spacing.xs)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.s)
                    ) {
                        OptionButton(
                            text = "총 사용 시간만 공유",
                            isSelected = selectedOption == 1,
                            onClick = { selectedOption = 1 },
                            modifier = Modifier.weight(1f)
                        )

                        OptionButton(
                            text = "상세 앱별 시간 공유",
                            isSelected = selectedOption == 2,
                            onClick = { selectedOption = 2 },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.m))

                // 옵션에 따른 목표 입력 필드
                if (selectedOption == 1) {
                    ChallengeInputField(
                        title = "목표",
                        hint = "예 : 유튜브 하루 2시간 이하",
                        iconRes = R.drawable.goal,
                        value = goal,
                        onValueChange = { newValue ->
                            if (newValue.length <= 50) {
                                goal = newValue
                            }
                        }
                    )
                } else {
                    // 옵션2: 앱 선택 및 시간 설정
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "목표",
                            color = Color.Black,
                            style = DitoTypography.labelLarge,
                            modifier = Modifier.padding(bottom = Spacing.xs)
                        )

                        // 고정된 형식으로 표시
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(DitoShapes.small)
                                .border(1.dp, Color.Black, DitoShapes.small)
                                .background(Background)
                                .padding(horizontal = Spacing.s, vertical = Spacing.s)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.goal),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(Modifier.width(Spacing.xs))

                            // 앱 선택 드롭다운
                            Box(
                                modifier = Modifier
                                    .clip(DitoShapes.small)
                                    .background(Color.White)
                                    .border(1.dp, Color.Black, DitoShapes.small)
                                    .clickable { showAppPicker = true }
                                    .padding(horizontal = Spacing.xs, vertical = 4.dp)
                            ) {
                                Text(
                                    text = selectedApp,
                                    color = Color.Black,
                                    style = DitoTypography.bodySmall
                                )
                            }

                            Spacer(Modifier.width(Spacing.xs))

                            Text(
                                text = "하루",
                                color = Color.Black,
                                style = DitoTypography.bodySmall
                            )

                            Spacer(Modifier.width(Spacing.xs))

                            // 시간 값 및 단위 드롭다운 (통합)
                            Box(
                                modifier = Modifier
                                    .clip(DitoShapes.small)
                                    .background(Color.White)
                                    .border(1.dp, Color.Black, DitoShapes.small)
                                    .clickable { showTimeValuePicker = true }
                                    .padding(horizontal = Spacing.xs, vertical = 4.dp)
                            ) {
                                val (displayValue, displayUnit) = getTimeDisplay(selectedTimeValue)
                                Text(
                                    text = "$displayValue$displayUnit",
                                    color = Color.Black,
                                    style = DitoTypography.bodySmall
                                )
                            }

                            Spacer(Modifier.width(Spacing.xs))

                            Text(
                                text = "이하 사용",
                                color = Color.Black,
                                style = DitoTypography.bodySmall
                            )
                        }
                    }

                    // 백엔드에는 기존 goal 형식으로 전달하도록 설정
                    LaunchedEffect(selectedApp, selectedTimeValue) {
                        val (displayValue, displayUnit) = getTimeDisplay(selectedTimeValue)
                        goal = "$selectedApp 하루 $displayValue$displayUnit 이하"
                    }
                }

                Spacer(Modifier.height(Spacing.m))

                ChallengeInputField(
                    title = "벌칙",
                    hint = "예 : 커피 사주기",
                    iconRes = R.drawable.penalty,
                    value = penalty,
                    onValueChange = { newValue ->
                        if (newValue.length <= 50) {
                            penalty = newValue
                        }
                    }
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
                        .background(if (isFormValid) Primary else Background)
                        .clickable(enabled = isFormValid) {
                            if (isFormValid && period > 0 && bet >= 10) {
                                onCreateChallenge(groupName, goal, penalty, period, bet)
                            }
                        }
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

    // 기간 선택 다이얼로그
    if (showPeriodPicker) {
        NumberPickerDialog(
            title = "기간 선택",
            range = (1..100).toList(),
            initialValue = period,
            onDismiss = { showPeriodPicker = false },
            onConfirm = { days ->
                period = days
                showPeriodPicker = false
            }
        )
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

    // 앱 선택 다이얼로그
    if (showAppPicker) {
        StringPickerDialog(
            title = "앱 선택",
            options = listOf("유튜브", "인스타그램", "트위터", "크롬", "틱톡"),
            initialValue = selectedApp,
            onDismiss = { showAppPicker = false },
            onConfirm = { app ->
                selectedApp = app
                showAppPicker = false
            }
        )
    }

    // 시간 값 선택 다이얼로그 (분과 시간 통합)
    if (showTimeValuePicker) {
        TimePickerDialog(
            initialValue = selectedTimeValue,
            onDismiss = { showTimeValuePicker = false },
            onConfirm = { minutes ->
                selectedTimeValue = minutes
                showTimeValuePicker = false
            }
        )
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
            Spacer(Modifier.width(Spacing.m))
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

@Composable
fun PeriodPickerField(
    value: Int,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "기간(일수)",
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
                .clickable(onClick = onClick)
                .padding(horizontal = Spacing.s, vertical = Spacing.s)
        ) {
            Image(
                painter = painterResource(id = R.drawable.period),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Spacer(Modifier.width(Spacing.m))

            Text(
                text = "${value}일",
                color = Color.Black,
                style = DitoTypography.bodySmall,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun BettingAmountField(
    value: Int,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "배팅 금액(최소 10레몬)",
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
                .clickable(onClick = onClick)
                .padding(horizontal = Spacing.s, vertical = Spacing.s)
        ) {
            Image(
                painter = painterResource(id = R.drawable.coin),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Spacer(Modifier.width(Spacing.m))

            Text(
                text = value.toString(),
                color = Color.Black,
                style = DitoTypography.bodySmall,
                modifier = Modifier.weight(1f)
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

/** 옵션 선택 버튼 */
@Composable
private fun OptionButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(DitoShapes.small)
            .background(if (isSelected) Primary else Color.White)
            .border(1.dp, Color.Black, DitoShapes.small)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.xs, vertical = Spacing.xs),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.Black,
            style = DitoTypography.labelSmall,
            textAlign = TextAlign.Center
        )
    }
}

/** 문자열 선택 다이얼로그 */
@Composable
private fun StringPickerDialog(
    title: String,
    options: List<String>,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
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

            // 선택 영역
            run {
                val initialIndex = options.indexOf(selectedValue).coerceAtLeast(0)
                val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp),
                    state = listState,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(options.size) { index ->
                        val option = options[index]
                        Text(
                            text = option,
                            style = if (option == selectedValue) {
                                MaterialTheme.typography.titleLarge.copy(
                                    color = Primary
                                )
                            } else {
                                MaterialTheme.typography.bodyLarge
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedValue = option }
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

/** 시간 선택 다이얼로그 (분과 시간 통합) */
@Composable
private fun TimePickerDialog(
    initialValue: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedMinutes by remember { mutableIntStateOf(initialValue) }

    // 선택 가능한 시간 목록 생성 (분 단위: 10~50분, 시간 단위: 1~12시간)
    val timeOptions = buildList {
        // 분 단위 (10분 단위로 10~50분)
        for (i in 1..5) {
            add(Pair(i * 10, "${i * 10}분"))
        }
        // 시간 단위 (1~12시간)
        for (i in 1..12) {
            add(Pair(i * 60, "${i}시간"))
        }
    }

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
                    text = "시간 선택",
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

            // 선택 영역
            run {
                val initialIndex = timeOptions.indexOfFirst { it.first == selectedMinutes }
                    .coerceAtLeast(0)
                val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp),
                    state = listState,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(timeOptions.size) { index ->
                        val (minutes, displayText) = timeOptions[index]
                        Text(
                            text = displayText,
                            style = if (minutes == selectedMinutes) {
                                MaterialTheme.typography.titleLarge.copy(
                                    color = Primary
                                )
                            } else {
                                MaterialTheme.typography.bodyLarge
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMinutes = minutes }
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
                    onClick = { onConfirm(selectedMinutes) },
                    isPrimary = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
