package com.dito.app.feature.settings

import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.dito.app.R
import com.dito.app.core.ui.designsystem.Background
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.OnSurface
import com.dito.app.core.ui.designsystem.Spacing

@Preview(showBackground = true)
@Composable
fun SettingTab() {
    var showNicknameDialog by remember { mutableStateOf(false) }
    var showNotiCountDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(
                    color = Background,
                )
        ){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ){
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(bottom = 1.dp,)
                        .fillMaxWidth()
                        .background(
                            color = Background,
                        )
                        .padding(vertical = 6.dp,)
                ){
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                    ){
                        Text("설정",
                            color = OnSurface,
                            style = DitoTypography.headlineLarge
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .padding(bottom = 1.dp,)
                        .fillMaxWidth()
                        .background(
                            color = Background,
                        )
                        .padding(16.dp)
                ){
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 22.dp,horizontal = 10.dp,)
                    ){
                        Text("내 정보",
                            color = OnSurface,
                            style = DitoCustomTextStyles.titleKMedium
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showNicknameDialog = true }
                            .padding(vertical = 10.dp,)
                    ){
                        Text("닉네임 변경",
                            color =  OnSurface,
                            style = DitoTypography.bodyLarge,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.right),
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = Spacing.s)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showNotiCountDialog = true }
                            .padding(vertical = 11.dp,)
                    ){
                        Text("미션 빈도 변경",
                            color =  OnSurface,
                            style = DitoTypography.bodyLarge,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.right),
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = Spacing.s)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTermsDialog = true }
                            .padding(vertical = 11.dp,)
                    ){
                        Text("서비스 이용약관",
                            color =  OnSurface,
                            style = DitoTypography.bodyLarge,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.right),
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = Spacing.s)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPrivacyDialog = true }
                            .padding(vertical = 11.dp,)
                    ){
                        Text("개인정보 처리방침",
                            color =  OnSurface,
                            style = DitoTypography.bodyLarge,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.right),
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = Spacing.s)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* TODO: 문의하기 기능 */ }
                            .padding(vertical = 11.dp,)
                    ){
                        Text("문의하기",
                            color =  OnSurface,
                            style = DitoTypography.bodyLarge,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.right),
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = Spacing.s)
                        )
                    }
                    Column(
                        modifier = Modifier
                            .padding(bottom = 1.dp,)
                            .fillMaxWidth()
                            .clickable { showLogoutDialog = true }
                            .padding(vertical = 19.dp,horizontal = 10.dp,)
                    ){
                        Text("로그아웃",
                            color =  OnSurface,
                            style = DitoTypography.bodyLarge,
                        )
                    }
                }
            }
        }

        // 다이얼로그들
        if (showNicknameDialog) {
            ChangeNickName(onDismiss = { showNicknameDialog = false })
        }

        if (showNotiCountDialog) {
            EditNotiCount(onDismiss = { showNotiCountDialog = false })
        }

        if (showTermsDialog) {
            TermsOfServiceDialog(onDismiss = { showTermsDialog = false })
        }

        if (showPrivacyDialog) {
            PrivacyPoicyDialog(onDismiss = { showPrivacyDialog = false })
        }

        if (showLogoutDialog) {
            LogoutDialog(
                onDismiss = { showLogoutDialog = false },
                onConfirm = {
                    showLogoutDialog = false
                }
            )
        }
    }
}