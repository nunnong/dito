package com.dito.app

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dito.app.core.navigation.Route
import com.dito.app.core.ui.component.BottomTab
import com.dito.app.core.ui.component.DitoBottomAppBar
import com.dito.app.core.util.PermissionHelper
import com.dito.app.core.wearable.WearableMessageService
import com.dito.app.feature.closet.ClosetScreen
import com.dito.app.feature.group.GroupScreen
import com.dito.app.feature.group.GroupWaitingScreen
import com.dito.app.feature.home.HomeScreen
import com.dito.app.feature.missionNotification.MissionNotificationScreen
import com.dito.app.feature.report.DailyReportScreen
import com.dito.app.feature.settings.SettingScreen
import com.dito.app.feature.settings.EditNotiCount
import com.dito.app.feature.settings.ChangeNickName
import com.dito.app.feature.settings.TermsOfServiceDialog
import com.dito.app.feature.settings.PrivacyPoicyDialog
import com.dito.app.feature.shop.ShopScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    onLogout: () -> Unit = {},
    onNavigateToShop: () -> Unit = {},
    initialShowShop: Boolean = false,
    onBackFromShop: () -> Unit = {},
    outerNavController: NavController? = null,
    // FCM 알림에서 전달된 navigation 정보
    initialNavigateTo: String? = null,
    initialMissionId: String? = null,
    initialMissionType: String? = null,
    initialOpenMissionDetail: Boolean = false,  // 평가 알림 구분용
    wearableMessageService: WearableMessageService? = null
) {
    val innerNavController = rememberNavController()
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }
    var showShop by remember { mutableStateOf(initialShowShop) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 권한 체크 상태 (무한 네비게이션 방지)
    var isCheckingPermissions by remember { mutableStateOf(false) }

    // FCM 알림 처리 완료 플래그 (missionId가 변경되면 자동으로 리셋)
    var hasHandledNotification by remember(initialMissionId) { mutableStateOf(false) }

    // 화면이 다시 보일 때마다 권한 상태 확인
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && !isCheckingPermissions) {
                val hasAccessibility = PermissionHelper.isAccessibilityPermissionGranted(context)
                val hasUsageStats = PermissionHelper.isUsageStatsPermissionGranted(context)
                val hasNotification = PermissionHelper.isNotificationPermissionGranted(context)
                val hasNotificationListener =
                    PermissionHelper.isNotificationListenerPermissionGranted(context)

                if (!hasAccessibility || !hasUsageStats || !hasNotification || !hasNotificationListener) {
                    isCheckingPermissions = true
                    outerNavController?.navigate(Route.PermissionRecheck.path) {
                        launchSingleTop = true
                    }
                }
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                isCheckingPermissions = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // MainScreen이 처음 로드될 때 항상 home으로 초기화
    LaunchedEffect(Unit) {
        selectedTab = BottomTab.HOME
        innerNavController.navigate("home") {
            popUpTo("home") { inclusive = true }
            launchSingleTop = true
        }
    }

    // FCM 알림에서 전달된 navigation 처리
    LaunchedEffect(initialNavigateTo, initialMissionId, initialMissionType) {
        Log.d("MainScreen", "🔍 LaunchedEffect 호출됨")
        Log.d("MainScreen", "   initialNavigateTo: $initialNavigateTo")
        Log.d("MainScreen", "   initialMissionId: $initialMissionId")
        Log.d("MainScreen", "   initialMissionType: $initialMissionType")
        Log.d("MainScreen", "   hasHandledNotification: $hasHandledNotification")
        Log.d("MainScreen", "   wearableMessageService null 여부: ${wearableMessageService == null}")

        if (!hasHandledNotification && initialNavigateTo == "mission_notifications") {
            Log.d("MainScreen", "🎯 FCM 알림 감지: mission_id=$initialMissionId, type=$initialMissionType")

            // MEDITATION 미션일 때 워치 앱 자동 실행
            if (initialMissionType == "MEDITATION" && wearableMessageService != null) {
                Log.d("MainScreen", "🧘 명상 미션 감지 - 워치 앱 실행 시작")
                CoroutineScope(Dispatchers.IO).launch {
                    val result = wearableMessageService.startBreathingOnWatch()
                    if (result.isSuccess) {
                        Log.d("MainScreen", "✅ 워치 앱 실행 성공")
                    } else {
                        Log.e("MainScreen", "❌ 워치 앱 실행 실패: ${result.exceptionOrNull()?.message}")
                    }
                }
            }

            // Home 화면이 완전히 로드된 후 mission_notification으로 이동
            // 약간의 딜레이를 주어 innerNavController가 준비되도록 함
            kotlinx.coroutines.delay(500)

            innerNavController.navigate("mission_notification") {
                launchSingleTop = true
            }

            hasHandledNotification = true
            Log.d("MainScreen", "✅ 미션 알림 화면으로 이동 완료")
        } else {
            Log.d("MainScreen", "❌ 알림 처리 조건 불만족")
        }
    }

    // selectedTab이 변경되면 showShop을 false로 설정
    LaunchedEffect(selectedTab) {
        if (showShop) {
            showShop = false
        }
    }

    Scaffold(
        bottomBar = {
            DitoBottomAppBar(
                selectedTab = selectedTab,
                onTabSelected = {
                    selectedTab = it
                    if (it == BottomTab.HOME) innerNavController.navigate(Route.Home.path) {
                        launchSingleTop = true; popUpTo("home") { inclusive = false }
                    }
                    if (it == BottomTab.GROUP) innerNavController.navigate(Route.GroupRoot.path) {
                        launchSingleTop = true
                    }
                    if (it == BottomTab.MISSION) innerNavController.navigate(Route.MissionNotification.path) {
                        launchSingleTop = true
                    }
                    if (it == BottomTab.REPORT) innerNavController.navigate(Route.Report.path) {
                        launchSingleTop = true
                    }

                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = innerNavController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    onLogout = onLogout,
                    onCartClick = { innerNavController.navigate(Route.Shop.path) },
                    onClosetClick = { innerNavController.navigate(Route.Closet.path) },
                    onSettingsClick = { innerNavController.navigate(Route.SettingRoot.path) }
                )
            }
            composable("shop") {
                ShopScreen(onBackClick = { innerNavController.popBackStack() })
            }
            composable("closet") {
                ClosetScreen(onBackClick = { innerNavController.popBackStack() })
            }
            composable("mission_notification") {
                MissionNotificationScreen(
                    onBackClick = { innerNavController.popBackStack() },
                    initialMissionId = initialMissionId,  // 딥링크 missionId 전달
                    initialOpenDetail = initialOpenMissionDetail  // openDetail 파라미터 전달
                )
            }
            composable(Route.GroupRoot.path) {
                GroupScreen(navController = innerNavController)
            }
            composable(Route.Report.path) {
                DailyReportScreen()
            }
            composable(Route.SettingRoot.path) {
                SettingScreen(
                    navController = innerNavController,
                    onLogout = onLogout
                )
            }
            composable(Route.SettingEditNickname.path) {
                ChangeNickName(
                    onDismiss = { innerNavController.popBackStack() },
                    navController = outerNavController
                )
            }
            composable(Route.SettingEditNotiCount.path) {
                EditNotiCount(onDismiss = { innerNavController.popBackStack() })
            }
            composable(Route.SettingTermsOfService.path) {
                TermsOfServiceDialog(onDismiss = { innerNavController.popBackStack() })
            }
            composable(Route.SettingPrivacyPolicy.path) {
                PrivacyPoicyDialog(onDismiss = { innerNavController.popBackStack() })
            }
        }
    }
}
