package com.dito.app.feature.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dito.app.R
import com.dito.app.core.navigation.Route
import com.dito.app.core.ui.component.DitoModalContainer
import com.dito.app.core.ui.designsystem.Background
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoShapes
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.Error
import com.dito.app.core.ui.designsystem.ErrorContainer
import com.dito.app.core.ui.designsystem.OnErrorContainer
import com.dito.app.core.ui.designsystem.OnSurface
import com.dito.app.core.ui.designsystem.Primary
import com.dito.app.core.ui.designsystem.Spacing
import com.dito.app.core.ui.designsystem.hardShadow

@Composable
fun ChangeNickName(
    onDismiss: () -> Unit = {},
    navController: NavController? = null,
    viewModel: SettingViewModel = hiltViewModel()
) {
    var nickName by remember { mutableStateOf("") }
    val isValid = nickName.length in 1..7 && nickName.matches("^[a-zA-Z가-힣]+$".toRegex())
    val uiState by viewModel.uiState.collectAsState()

    // 로그인 필요 에러 발생 시 로그인 페이지로 이동
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage == "로그인이 필요합니다") {
            onDismiss()
            navController?.navigate(Route.Login.path) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

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
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.s, vertical = Spacing.xs)
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

                Spacer(Modifier.height(Spacing.m))

                // 제목 텍스트
                Text(
                    text = "닉네임 변경",
                    color = OnSurface,
                    style = DitoCustomTextStyles.titleKLarge
                )

                Spacer(Modifier.height(Spacing.l))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .padding(horizontal = Spacing.m)
                ) {
                    BasicTextField(
                        value = nickName,
                        onValueChange = { input ->
                            // 길이만 체크하여 한글 조합형 입력 허용
                            if (input.length <= 7) {
                                nickName = input
                            }
                        },
                        textStyle = DitoTypography.bodyLarge.copy(color = OnSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                val strokeWidth = 1.dp.toPx()
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
                                if (nickName.isEmpty()) {
                                    Text(
                                        text = "영문/한글 1~7자",
                                        color = Color.Gray,
                                        style = DitoTypography.bodyMedium
                                    )
                                }
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
                            .clickable { nickName = "" }

                    )
                }

                // 에러 메시지 표시
                uiState.errorMessage?.let { errorMessage ->
                    Spacer(Modifier.height(Spacing.s))
                    Text(
                        text = errorMessage,
                        color = Error,
                        style = DitoTypography.bodySmall,
                        modifier = Modifier.padding(horizontal = Spacing.m)
                    )
                }

                Spacer(Modifier.height(Spacing.xl))

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
                        .background(if (isValid) Primary else ErrorContainer)
                        .clickable(enabled = isValid && !uiState.isLoading) {
                            viewModel.updateNickname(nickName) {
                                onDismiss()
                            }
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.isLoading) "변경 중..." else "변경하기",
                        color = if (isValid) Color.Black else OnErrorContainer,
                        style = DitoCustomTextStyles.titleKMedium
                    )
                }

                Spacer(Modifier.height(Spacing.s))
            }
        }
    }
}
