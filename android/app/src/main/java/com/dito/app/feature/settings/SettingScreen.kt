package com.dito.app.feature.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.dito.app.R
import com.dito.app.core.navigation.Route
import com.dito.app.core.ui.designsystem.Background
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.OnSurface
import com.dito.app.core.ui.designsystem.OnSurfaceVariant
import com.dito.app.core.ui.designsystem.Spacing

@Composable
fun SettingScreen(
    navController: NavController? = null,
    onLogout: () -> Unit = {},
    authViewModel: com.dito.app.feature.auth.AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .verticalScroll(rememberScrollState())
        ) {
            // 헤더
            Text(
                text = "설정",
                color = OnSurface,
                style = DitoTypography.headlineLarge,
                modifier = Modifier.padding(
                    horizontal = Spacing.l,
                    vertical = Spacing.m
                )
            )

            Spacer(modifier = Modifier.height(Spacing.s))

            // 내 정보 섹션
            SettingSection(title = "내 정보")

            SettingItem(
                title = "닉네임 변경",
                onClick = { navController?.navigate(Route.SettingEditNickname.path) }
            )

            SettingItem(
                title = "미션 빈도 변경",
                onClick = { navController?.navigate(Route.SettingEditNotiCount.path) }
            )

            SettingDivider()

            // 약관 및 정책 섹션
            SettingSection(title = "약관 및 정책")

            SettingItem(
                title = "서비스 이용약관",
                onClick = { navController?.navigate(Route.SettingTermsOfService.path) }
            )

            SettingItem(
                title = "개인정보 처리방침",
                onClick = { navController?.navigate(Route.SettingPrivacyPolicy.path) }
            )

            SettingDivider()

            // 고객 지원 섹션
            SettingSection(title = "고객 지원")

            SettingItem(
                title = "문의하기",
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:a708official@gmail.com")
                        putExtra(Intent.EXTRA_SUBJECT, "[Dito 문의]")
                        putExtra(Intent.EXTRA_TEXT, "문의 내용을 작성해주세요.\n\n")
                    }
                    context.startActivity(Intent.createChooser(intent, "문의 메일 보내기"))
                }
            )

            SettingDivider()

            // 계정 관리 섹션
            SettingSection(title = "계정 관리")

            SettingItem(
                title = "로그아웃",
                onClick = { showLogoutDialog = true },
                showArrow = false,
                textColor = OnSurfaceVariant
            )

            SettingItem(
                title = "탈퇴하기",
                onClick = { showWithdrawDialog = true },
                showArrow = false,
                textColor = OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.xl))
        }

        // 다이얼로그들
        if (showLogoutDialog) {
            LogoutDialog(
                onDismiss = { showLogoutDialog = false },
                onConfirm = {
                    authViewModel.logout {
                        showLogoutDialog = false
                        onLogout()
                    }
                }
            )
        }

        if (showWithdrawDialog) {
            WithdrawDialog(
                onDismiss = { showWithdrawDialog = false },
                onConfirm = {
                    authViewModel.signOut()
                    showWithdrawDialog = false
                    onLogout()
                }
            )
        }
    }
}

@Composable
private fun SettingSection(title: String) {
    Text(
        text = title,
        color = OnSurface,
        style = DitoCustomTextStyles.titleKMedium,
        modifier = Modifier.padding(
            horizontal = Spacing.l,
            vertical = Spacing.m
        )
    )
}

@Composable
private fun SettingItem(
    title: String,
    onClick: () -> Unit,
    showArrow: Boolean = true,
    textColor: Color = OnSurface
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = Spacing.l,
                vertical = Spacing.m
            )
    ) {
        Text(
            text = title,
            color = textColor,
            style = DitoTypography.bodyLarge
        )

        if (showArrow) {
            Image(
                painter = painterResource(id = R.drawable.right),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = Spacing.m),
        thickness = 1.dp,
        color = OnSurfaceVariant.copy(alpha = 0.2f)
    )
}