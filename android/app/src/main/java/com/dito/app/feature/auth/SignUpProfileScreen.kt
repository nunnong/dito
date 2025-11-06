package com.dito.app.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dito.app.R
import com.dito.app.core.ui.designsystem.*

/** 회원가입 화면 - 2단계: 회원 정보 입력 (닉네임, 생년월일, 성별) */
@Composable
fun SignUpProfileScreen(
    username: String,
    password: String,
    onNavigateBack: () -> Unit,
    onNavigateToNext: (username: String, password: String, nickname: String, birthYear: Int, birthMonth: Int, birthDay: Int, gender: String) -> Unit,
    viewModel: SignUpProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var showYearPicker by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var showDayPicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.navigateToNext) {
        if (uiState.navigateToNext) {
            onNavigateToNext(
                username,
                password,
                uiState.nickname,
                uiState.birthYear,
                uiState.birthMonth,
                uiState.birthDay,
                uiState.gender
            )
            viewModel.onNavigated()
        }
    }

    val isFormValid = uiState.nickname.isNotBlank() &&
            uiState.birthYear > 0 &&
            uiState.birthMonth > 0 &&
            uiState.birthDay > 0 &&
            uiState.gender.isNotBlank() &&
            uiState.nicknameErrorMessage.isEmpty()

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                    .padding(start = 32.dp, end = 32.dp, bottom = 100.dp)
            ) {
                LargeSignUpButton(
                    text = "Next",
                    enabled = isFormValid,
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.onNextClicked()
                    },
                    modifier = Modifier
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
        // 상단 헤더 - 뒤로가기 버튼 + 제목
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.angle_left),
                contentDescription = "뒤로가기",
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onNavigateBack() }
            )

            Text(
                text = "회원정보",
                style = DitoTypography.headlineMedium,
                color = Color.Black
            )
        }

        // 입력 필드 영역
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 닉네임 입력 필드
            ProfileFieldSection(
                label = "닉네임",
                value = uiState.nickname,
                onValueChange = viewModel::onNicknameChange,
                placeholder = "",
                errorMessage = uiState.nicknameErrorMessage,
                onNicknameLostFocus = viewModel::validateNicknameOnBlur // Passed callback
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 생년월일 입력 필드
            BirthDateFieldSection(
                label = "생년월일",
                year = uiState.birthYear,
                month = uiState.birthMonth,
                day = uiState.birthDay,
                onYearClick = {
                    focusManager.clearFocus()
                    showYearPicker = true
                },
                onMonthClick = {
                    focusManager.clearFocus()
                    showMonthPicker = true
                },
                onDayClick = {
                    focusManager.clearFocus()
                    showDayPicker = true
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 성별 선택 필드
            GenderFieldSection(
                label = "성별",
                selectedGender = uiState.gender,
                onGenderSelect = {
                    focusManager.clearFocus()
                    viewModel.onGenderChange(it)
                }
            )
        }

        }
    }

    // 다이얼로그들
    if (showYearPicker) {
        NumberPickerDialog(
            title = "년도 선택",
            range = 1900..2020,
            initialValue = uiState.birthYear.takeIf { it > 0 } ?: 1990,
            onDismiss = { showYearPicker = false },
            onConfirm = { year ->
                viewModel.onBirthYearChange(year)
                showYearPicker = false
            }
        )
    }

    if (showMonthPicker) {
        NumberPickerDialog(
            title = "월 선택",
            range = 1..12,
            initialValue = uiState.birthMonth.takeIf { it > 0 } ?: 1,
            onDismiss = { showMonthPicker = false },
            onConfirm = { month ->
                viewModel.onBirthMonthChange(month)
                showMonthPicker = false
            }
        )
    }

    if (showDayPicker) {
        NumberPickerDialog(
            title = "일 선택",
            range = 1..31,
            initialValue = uiState.birthDay.takeIf { it > 0 } ?: 1,
            onDismiss = { showDayPicker = false },
            onConfirm = { day ->
                viewModel.onBirthDayChange(day)
                showDayPicker = false
            }
        )
    }
}

/** 닉네임 입력 필드 섹션 */
@Composable
private fun ProfileFieldSection(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    errorMessage: String = "",
    onNicknameLostFocus: () -> Unit // Added parameter
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = DitoCustomTextStyles.titleDLarge,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val focusRequester = remember { FocusRequester() }
        ProfileTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            hasError = errorMessage.isNotEmpty(),
            // Container should fill max width
            modifier = Modifier
                .fillMaxWidth(),
            // Apply focus handling to the actual text field
            textFieldModifier = Modifier
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) {
                        onNicknameLostFocus()
                    }
                }
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = Error,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

/** 프로필용 텍스트 입력 필드 */
@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    hasError: Boolean = false,
    modifier: Modifier = Modifier,
    textFieldModifier: Modifier = Modifier
) {
    val borderColor = if (hasError) Error else Color.Black

    Box(
        modifier = modifier
            .height(50.dp)
            .border(1.5.dp, borderColor, RectangleShape)
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .then(textFieldModifier),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = Color.Black,
                letterSpacing = 0.5.sp
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        if (value.isEmpty() && placeholder.isNotEmpty()) {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFBDBDBD)
            )
        }
    }
}

/** 생년월일 입력 필드 섹션 */
@Composable
private fun BirthDateFieldSection(
    label: String,
    year: Int,
    month: Int,
    day: Int,
    onYearClick: () -> Unit,
    onMonthClick: () -> Unit,
    onDayClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = DitoCustomTextStyles.titleDLarge,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BirthDatePickerBox(
                value = if (year > 0) year.toString() else "1990",
                onClick = onYearClick,
                modifier = Modifier.weight(1f)
            )

            BirthDatePickerBox(
                value = if (month > 0) month.toString().padStart(2, '0') else "01",
                onClick = onMonthClick,
                modifier = Modifier.weight(1f)
            )

            BirthDatePickerBox(
                value = if (day > 0) day.toString().padStart(2, '0') else "01",
                onClick = onDayClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** 생년월일 선택 박스 */
@Composable
private fun BirthDatePickerBox(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(47.dp)
            .border(2.dp, Color.Black, RectangleShape)
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                letterSpacing = 0.5.sp
            ),
            color = Color.Black
        )
    }
}

/** 성별 선택 필드 섹션 */
@Composable
private fun GenderFieldSection(
    label: String,
    selectedGender: String,
    onGenderSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = DitoCustomTextStyles.titleDLarge,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GenderButton(
                text = "남성",
                isSelected = selectedGender == "MALE",
                onClick = { onGenderSelect("MALE") },
                modifier = Modifier.weight(1f)
            )

            GenderButton(
                text = "여성",
                isSelected = selectedGender == "FEMALE",
                onClick = { onGenderSelect("FEMALE") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** 성별 선택 버튼 */
@Composable
private fun GenderButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Color.Black else Color.White
    val textColor = if (isSelected) Primary else Color.Black
    val borderColor = if (isSelected) Primary else Color.Black

    Box(
        modifier = modifier
            .height(50.dp)
            .hardShadow(DitoHardShadow.ButtonLarge.copy(cornerRadius = 4.dp))
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = DitoCustomTextStyles.titleDMedium,
            color = textColor
        )
    }
}

/** 큰 Next 버튼 */
@Composable
private fun LargeSignUpButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .hardShadow(DitoHardShadow.ButtonLarge.copy(cornerRadius = 8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(if (enabled) Primary else Color.White)
            .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = DitoTypography.headlineMedium,
            color = if (enabled) Color.Black else Color.Black.copy(alpha = 0.4f)
        )
    }
}

/** 숫자 선택 다이얼로그 */
@Composable
private fun NumberPickerDialog(
    title: String,
    range: IntRange,
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
                val initialIndex = (selectedValue - range.first).coerceIn(0, range.count() - 1)
                val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp),
                    state = listState,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(range.toList()) { number ->
                        Text(
                            text = number.toString().padStart(2, '0'),
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
