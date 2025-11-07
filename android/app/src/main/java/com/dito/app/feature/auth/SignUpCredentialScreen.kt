package com.dito.app.feature.auth

import androidx.hilt.navigation.compose.hiltViewModel

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dito.app.R
import com.dito.app.core.ui.designsystem.*

/** 회원가입 화면 - 1단계: 아이디/비밀번호 입력 (자격증명) */
@Composable
fun SignUpCredentialsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToNext: (username: String, password: String) -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val confirmFocusRequester = remember { FocusRequester() }

    LaunchedEffect(uiState.navigateToNext) {
        if (uiState.navigateToNext) {
            onNavigateToNext(uiState.username, uiState.password)
            viewModel.onNavigated() // Reset navigation trigger
        }
    }

    Scaffold(
        containerColor = Color.White
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
            // 뒤로가기 버튼
            Image(
                painter = painterResource(id = R.drawable.angle_left), // angle_left 아이콘 사용
                contentDescription = "뒤로가기",
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onNavigateBack() }
            )

            Text(
                text = "회원가입",
                style = DitoTypography.headlineMedium,
                color = Color.Black
            )
        }

        // 입력 필드 영역
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 46.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // 아이디 입력 필드
            SignUpFieldSection(
                label = "아이디",
                value = uiState.username,
                onValueChange = viewModel::onUsernameChange,
                placeholder = "4~20자의 영문 소문자, 숫자",
                errorMessage = uiState.usernameErrorMessage,
                successMessage = if (uiState.isUsernameValid == true) "사용 가능한 아이디입니다." else "",
                showDuplicateCheck = true,
                onDuplicateCheck = viewModel::checkUsernameAvailability,
                iconRes = R.drawable.user, // user 아이콘
                isCheckButtonEnabled = uiState.username.isNotBlank() && !uiState.isCheckingUsername  && !uiState.isUsernameChecked
            )

            // 비밀번호 입력 필드
            SignUpFieldSection(
                label = "비밀번호",
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = "8~20자의 영문 소문자 + 숫자",
                errorMessage = uiState.passwordErrorMessage,
                isPassword = true,
                iconRes = R.drawable.lock // lock 아이콘
            )

            // 비밀번호 확인 입력 필드
            SignUpFieldSection(
                label = "비밀번호 확인",
                value = uiState.passwordConfirm,
                onValueChange = viewModel::onPasswordConfirmChange,
                placeholder = "비밀번호와 동일하게 입력",
                errorMessage = uiState.passwordConfirmErrorMessage,
                isPassword = true,
                iconRes = R.drawable.lock_open, // lock_open 아이콘
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.clearFocus() }
            )
        }

        // Next 버튼
        val isFormValid = uiState.isUsernameChecked &&
                uiState.username.isNotBlank() &&
                uiState.password.isNotBlank() &&
                uiState.passwordConfirm.isNotBlank() &&
                uiState.password == uiState.passwordConfirm &&
                uiState.passwordErrorMessage.isEmpty() &&
                uiState.passwordConfirmErrorMessage.isEmpty()

        LargeSignUpButton(
            text = "Next",
            enabled = isFormValid,
            onClick = viewModel::onNextClicked,
            modifier = Modifier.padding(vertical = 48.dp, horizontal = 4.dp)
        )
        }
    }
}

/** 회원가입 입력 필드 섹션 */
@Composable
private fun SignUpFieldSection(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    errorMessage: String = "",
    successMessage: String = "",
    isPassword: Boolean = false,
    showDuplicateCheck: Boolean = false,
    onDuplicateCheck: () -> Unit = {},
    iconRes: Int,
    isCheckButtonEnabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: (() -> Unit)? = null
) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {        // 라벨
        Text(
            text = label,
            style = DitoCustomTextStyles.titleDMedium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // 입력 필드 + 내부 버튼
        SignUpTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            isPassword = isPassword,
            iconRes = iconRes,
            hasError = errorMessage.isNotEmpty(),
            hasSuccess = successMessage.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            imeAction = imeAction,
            onImeAction = onImeAction,
            trailingContent = {
                if (showDuplicateCheck) {
                    Box(modifier = Modifier) { // Add padding for the button
                        SmallCheckButton(
                            text = "중복확인",
                            enabled = isCheckButtonEnabled,
                            isSuccess = successMessage.isNotEmpty(), // Pass success state
                            onClick = onDuplicateCheck
                        )
                    }
                }
            }
        )

        // 에러 메시지 (에러가 있을 때만 표시)
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = Error,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

/** 회원가입용 텍스트 입력 필드 */
@Composable
private fun SignUpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    iconRes: Int,
    hasError: Boolean = false,
    hasSuccess: Boolean = false,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    val localFocusManager = LocalFocusManager.current
    val borderColor = when {
        hasError -> Error
        hasSuccess -> Color(0xFF4CAF50)
        else -> Color.Black
    }

    Row(
        modifier = modifier
            .height(50.dp)
            .border(1.5.dp, borderColor, RectangleShape)
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 아이콘
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
        )

        // 텍스트 입력
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp), // Added padding here
            contentAlignment = Alignment.CenterStart
        ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = Color.Black,
                letterSpacing = 0.5.sp
            ),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { onImeAction?.invoke() ?: run { localFocusManager.moveFocus(FocusDirection.Down) } },
                onDone = { onImeAction?.invoke() ?: run { localFocusManager.clearFocus() } }
            )
        )

            // 플레이스홀더
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFBDBDBD)
                )
            }
        }

        // 내부 우측 컨텐츠 (e.g., 버튼)
        trailingContent?.invoke()
    }
}

/** 작은 중복확인 버튼 */
@Composable
private fun SmallCheckButton(
    text: String,
    enabled: Boolean,
    isSuccess: Boolean, // Add success state parameter
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(56.dp)
            .height(32.dp)
            .hardShadow(DitoHardShadow.ButtonSmall.copy(cornerRadius = 4.dp))
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSuccess) Primary else Color.White) // Change background based on success state
            .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (enabled) Color.Black else Color.Black.copy(alpha = 0.4f)
        )
    }
}

/** 큰 Next 버튼 */
    @Composable
    private fun LargeSignUpButton(
        text: String,
        enabled: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier // Added modifier parameter
    ) {
        Box(
            modifier = modifier // Applied modifier parameter
                .fillMaxWidth()
                .height(68.dp)            .hardShadow(DitoHardShadow.ButtonLarge.copy(cornerRadius = 8.dp))
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
