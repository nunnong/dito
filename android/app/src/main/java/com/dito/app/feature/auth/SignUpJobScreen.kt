package com.dito.app.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dito.app.R
import com.dito.app.core.ui.designsystem.*

/** 회원가입 화면 - 3단계: 직업 선택 */
@Composable
fun SignUpJobScreen(
    username: String,
    password: String,
    nickname: String,
    birthYear: Int,
    birthMonth: Int,
    birthDay: Int,
    gender: String,
    onNavigateBack: () -> Unit,
    onSignUpComplete: (username: String, password: String, nickname: String, birthYear: Int, birthMonth: Int, birthDay: Int, gender: String, job: String) -> Unit,
    viewModel: SignUpJobViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.navigateToNext) {
        if (uiState.navigateToNext) {
            onSignUpComplete(
                username,
                password,
                nickname,
                birthYear,
                birthMonth,
                birthDay,
                gender,
                uiState.selectedJob
            )
            viewModel.onNavigated()
        }
    }

    val isFormValid = uiState.selectedJob.isNotBlank()

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
                    text = "Complete",
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
                    text = "직업 선택",
                    style = DitoTypography.headlineMedium,
                    color = Color.Black
                )
            }

            // 직업 선택 영역
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 0.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "직업",
                    style = DitoCustomTextStyles.titleDLarge,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                JobButton(
                    text = "학생",
                    isSelected = uiState.selectedJob == "STUDENT",
                    onClick = { viewModel.onJobSelect("STUDENT") }
                )

                JobButton(
                    text = "직장인",
                    isSelected = uiState.selectedJob == "EMPLOYEE",
                    onClick = { viewModel.onJobSelect("EMPLOYEE") }
                )

                JobButton(
                    text = "프리랜서",
                    isSelected = uiState.selectedJob == "FREELANCER",
                    onClick = { viewModel.onJobSelect("FREELANCER") }
                )

                JobButton(
                    text = "주부",
                    isSelected = uiState.selectedJob == "HOMEMAKER",
                    onClick = { viewModel.onJobSelect("HOMEMAKER") }
                )

                JobButton(
                    text = "기타",
                    isSelected = uiState.selectedJob == "ETC",
                    onClick = { viewModel.onJobSelect("ETC") }
                )
            }
        }
    }
}

/** 직업 선택 버튼 */
@Composable
private fun JobButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color.Black else Color.White
    val textColor = if (isSelected) Primary else Color.Black
    val borderColor = if (isSelected) Primary else Color.Black

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .hardShadow(DitoHardShadow.ButtonLarge.copy(cornerRadius = 4.dp))
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = DitoCustomTextStyles.titleDLarge,
            color = textColor
        )
    }
}

/** 큰 Complete 버튼 */
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
