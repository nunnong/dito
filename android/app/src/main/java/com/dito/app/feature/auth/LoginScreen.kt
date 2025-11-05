package com.dito.app.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dito.app.R
import com.dito.app.core.ui.designsystem.*

/** 로그인 화면(단일 버전) — 상태/네비/디자인 모두 여기서 처리 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // 로그인 성공 시 네비게이션
    LaunchedEffect(uiState.isLoggedIn) { if (uiState.isLoggedIn) onLoginSuccess() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 45.dp, bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 로고
        Image(
            painter = painterResource(id = R.drawable.dito_logo),
            contentDescription = "Dito Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .width(154.dp)
                .height(77.dp)
        )

        Spacer(Modifier.height(Spacing.l))

        // 픽셀 윈도우 카드
        PixelWindowCard {
            // 입력 영역
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, start = 32.dp, end = 32.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LabeledFieldRow(
                    label = "ID",
                    value = username,
                    onValueChange = { username = it },
                    placeholder = "아이디",
                    enabled = !uiState.isLoading
                )
                Spacer(Modifier.height(Spacing.m))
                LabeledFieldRow(
                    label = "PW",
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "비밀번호",
                    enabled = !uiState.isLoading,
                    isPassword = true
                )
            }

            // 에러 메시지
            uiState.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(vertical = 4.dp)
                )
            }

            // 버튼 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, end = 32.dp, top = 8.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(Spacing.l),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PixelButtonYellow(
                    text = "로그인",
                    enabled = !uiState.isLoading && username.isNotBlank() && password.isNotBlank()
                ) {
                    viewModel.signIn(username, password)
                }

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.size(100.dp, 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.Black
                        )
                    }
                } else {
                    PixelButtonBlack(
                        text = "회원가입",
                        enabled = true,
                        onClick = onNavigateToSignUp
                    )
                }
            }
        }
    }
}

/* ======================= 공용 UI 컴포넌트 ======================= */

@Composable
private fun PixelWindowCard(content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = Modifier
            .width(302.dp)
            .wrapContentHeight()
            .hardShadow(DitoHardShadow.Modal.copy(cornerRadius = 8.dp))
            .background(Color.White, shape)
            .border(2.dp, Color.Black, shape)
    ) {
        // 상단 노란 타이틀바 + close.png + 하단 1.5dp 라인
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Primary, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .padding(end = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Image(
                    painter = painterResource(id = R.drawable.close), // 요청한 리소스 사용
                    contentDescription = "Close",
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(Color.Black)
            )
        }
        content()
    }
}

@Composable
private fun LabeledFieldRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    isPassword: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.l),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DitoText(
            text = label,
            style = DitoCustomTextStyles.titleDLarge, // 둥근모 22sp
            color = Color.Black,
            modifier = Modifier.width(22.dp)
        )
        PixelTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            enabled = enabled,
            isPassword = isPassword
        )
    }
}

@Composable
private fun PixelTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    isPassword: Boolean
) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier = Modifier
            .width(180.dp)
            .height(30.dp)
            .clip(shape)
            .border(1.5.dp, Color.Black, shape)
            .background(Color.White),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 8.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = Color.Black,
                letterSpacing = 0.25.sp
            ),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Done)
        )
        if (value.isEmpty() && placeholder.isNotEmpty()) {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFBDBDBD),
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

/* ---- 100×40 버튼 + 4dp 하드 섀도(디자인 토큰) ---- */

@Composable
private fun PixelButtonYellow(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    PixelButtonBase(
        bg = Primary,
        borderColor = Color.Black,
        textColor = Color.Black,
        enabled = enabled,
        onClick = onClick,
        label = text
    )
}

@Composable
private fun PixelButtonBlack(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    PixelButtonBase(
        bg = Color.Black,
        borderColor = Primary,
        textColor = Primary,
        enabled = enabled,
        onClick = onClick,
        label = text
    )
}

@Composable
private fun PixelButtonBase(
    bg: Color,
    borderColor: Color,
    textColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    label: String
) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier = Modifier
            .size(width = 100.dp, height = 40.dp)
            .hardShadow(DitoHardShadow.ButtonLarge.copy(cornerRadius = 4.dp))
            .clip(shape)
            .background(bg)
            .border(1.5.dp, borderColor, shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = DitoCustomTextStyles.titleDMedium, // 둥근모 16sp
            color = if (enabled) textColor else textColor.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun DitoText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    align: TextAlign = TextAlign.Start
) {
    Text(text = text, style = style, color = color, modifier = modifier, textAlign = align)
}
