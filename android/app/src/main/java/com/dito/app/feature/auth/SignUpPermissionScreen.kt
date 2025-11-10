package com.dito.app.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dito.app.R
import com.dito.app.core.ui.designsystem.*
import com.dito.app.core.util.PermissionHelper

enum class PermissionScreenMode {
    SIGNUP,      // 회원가입 모드
    RECHECK      // 권한 재확인 모드
}

/** 회원가입 화면 - 4단계: 사용 권한 허용 / 권한 재확인 화면 */
@Composable
fun SignUpPermissionScreen(
    mode: PermissionScreenMode = PermissionScreenMode.SIGNUP,
    username: String = "",
    password: String = "",
    nickname: String = "",
    birthYear: Int = 0,
    birthMonth: Int = 0,
    birthDay: Int = 0,
    gender: String = "",
    job: String = "",
    onNavigateBack: () -> Unit = {},
    onPermissionsGranted: (username: String, password: String, nickname: String, birthYear: Int, birthMonth: Int, birthDay: Int, gender: String, job: String) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onPermissionsRecheckComplete: () -> Unit = {},
    viewModel: SignUpPermissionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 화면이 다시 보일 때마다 권한 상태 확인
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissions(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 초기 권한 상태 확인
    LaunchedEffect(Unit) {
        viewModel.checkPermissions(context)
    }

    // SIGNUP 모드: 회원가입 완료 후 다음으로 이동
    LaunchedEffect(uiState.navigateToNext) {
        if (mode == PermissionScreenMode.SIGNUP && uiState.navigateToNext) {
            onPermissionsGranted(
                username,
                password,
                nickname,
                birthYear,
                birthMonth,
                birthDay,
                gender,
                job
            )
            viewModel.onNavigated()
        }
    }

    // RECHECK 모드: 권한이 모두 부여되면 자동으로 완료
    LaunchedEffect(uiState.accessibilityPermission, uiState.usageStatsPermission, uiState.notificationPermission) {
        if (mode == PermissionScreenMode.RECHECK &&
            uiState.accessibilityPermission &&
            uiState.usageStatsPermission &&
            uiState.notificationPermission) {
            onPermissionsRecheckComplete()
        }
    }

    // 에러 메시지 표시
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            // TODO: 토스트나 스낵바로 에러 표시
            android.util.Log.e("SignUpPermissionScreen", "회원가입 실패: $message")
        }
    }

    val isFormValid = uiState.accessibilityPermission &&
            uiState.usageStatsPermission &&
            uiState.notificationPermission

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            if (mode == PermissionScreenMode.SIGNUP) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                        .padding(start = 32.dp, end = 32.dp, bottom = 90.dp)
                ) {
                    LargeStartButton(
                        text = if (uiState.isLoading) "가입 중..." else "Let's Start!",
                        enabled = isFormValid && !uiState.isLoading,
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.onLetsStartClicked(
                                username = username,
                                password = password,
                                nickname = nickname,
                                birthYear = birthYear,
                                birthMonth = birthMonth,
                                birthDay = birthDay,
                                gender = gender,
                                job = job
                            )
                        },
                        modifier = Modifier
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp)
                .padding(innerPadding)
        ) {
            // 상단 헤더 - 뒤로가기 버튼 + 제목
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 뒤로가기 버튼 (SIGNUP 모드일 때만 표시)
                if (mode == PermissionScreenMode.SIGNUP) {
                    Image(
                        painter = painterResource(id = R.drawable.angle_left),
                        contentDescription = "뒤로가기",
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onNavigateBack() }
                    )
                }

                Text(
                    text = if (mode == PermissionScreenMode.SIGNUP) "사용권한" else "권한 재확인",
                    style = DitoTypography.headlineMedium,
                    color = Color.Black
                )
            }

            // 설명 텍스트
            Text(
                text = if (mode == PermissionScreenMode.SIGNUP)
                    "디토 이용을 위해 꼭 필요한 권한만 모았어요."
                else
                    "앱 사용에 필요한 권한이 해제되어 있습니다.\n권한을 다시 허용해주세요.",
                style = DitoCustomTextStyles.titleDMedium,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 권한 목록
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                PermissionItem(
                    title = "접근성 권한 허용",
                    description = "앱 사용 패턴을 자동으로 분석하기 위해 필요해요.",
                    isGranted = uiState.accessibilityPermission,
                    onToggle = {
                        if (!uiState.accessibilityPermission) {
                            PermissionHelper.openAccessibilitySettings(context)
                        }
                    },
                    showTopBorder = true
                )

                PermissionItem(
                    title = "사용정보 접근 허용",
                    description = "스크린 타임과 앱 사용 기록을 추적하기 위해 필요해요.",
                    isGranted = uiState.usageStatsPermission,
                    onToggle = {
                        if (!uiState.usageStatsPermission) {
                            PermissionHelper.openUsageStatsSettings(context)
                        }
                    },
                    showTopBorder = true
                )

                PermissionItem(
                    title = "알림 허용",
                    description = "디토 AI의 맞춤 조언과 미션 알림을 받을 수 있어요.",
                    isGranted = uiState.notificationPermission,
                    onToggle = {
                        if (!uiState.notificationPermission) {
                            PermissionHelper.openNotificationSettings(context)
                        }
                    },
                    showTopBorder = true
                )
            }
        }
    }
}

/** 권한 항목 */
@Composable
private fun PermissionItem(
    title: String,
    description: String,
    isGranted: Boolean,
    onToggle: (Boolean) -> Unit,
    showTopBorder: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(94.dp)
            .background(Color.White)
            .then(
                if (showTopBorder) Modifier.border(
                    width = 1.5.dp,
                    color = Color.Black,
                    shape = RoundedCornerShape(0.dp)
                ).padding(top = 1.5.dp) else Modifier
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.width(36.dp))

        CustomSwitch(
            checked = isGranted,
            onCheckedChange = onToggle
        )
    }
}

/** 커스텀 스위치 */
@Composable
private fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = Modifier.size(width = 52.dp, height = 32.dp),
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = Color.Black,
            checkedBorderColor = Color.Black,
            uncheckedThumbColor = Color.Black,
            uncheckedTrackColor = Color(0xFFFFFFEE),
            uncheckedBorderColor = Color(0xFF79747E)
        )
    )
}

/** 큰 Let's Start 버튼 */
@Composable
private fun LargeStartButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp)
            .hardShadow(DitoHardShadow.ButtonLarge.copy(cornerRadius = 8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(if (enabled) Primary else Color.White)
            .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = DitoTypography.headlineLarge,
            color = if (enabled) Color.Black else Color(0xFFC9C4CE)
        )
    }
}
