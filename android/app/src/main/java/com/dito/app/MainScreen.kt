package com.dito.app

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
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

@Composable
fun MainScreen(
    onLogout: () -> Unit = {},
    onNavigateToShop: () -> Unit = {},
    initialShowShop: Boolean = false,
    onBackFromShop: () -> Unit = {},
    outerNavController: NavController? = null,
    initialNavigateTo: String? = null,
    initialMissionId: String? = null,
    initialOpenMissionDetail: Boolean = false
) {
    val innerNavController = rememberNavController()
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }
    var showShop by remember { mutableStateOf(initialShowShop) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 권한 체크 상태 (무한 네비게이션 방지)
    var isCheckingPermissions by remember { mutableStateOf(false) }

    var handledMissionId by remember { mutableStateOf<String?>(null) }

    // FCM 알림 처리 완료 플래그
    var hasHandledNotification by remember { mutableStateOf(false) }

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

    LaunchedEffect(Unit){
        if(initialNavigateTo == null || initialMissionId == null){
            selectedTab = BottomTab.HOME
            innerNavController.navigate("home"){
                popUpTo("home") { inclusive = true}
                launchSingleTop = true
            }
        }
    }


    LaunchedEffect(initialNavigateTo, initialMissionId, initialOpenMissionDetail) {
        val missionId = initialMissionId

        if (initialNavigateTo == "mission_notifications" && !missionId.isNullOrEmpty()) {

            if (initialOpenMissionDetail) {
                // 평가 알림: 같은 missionId라도 항상 미션 화면으로 이동해서 모달을 띄우도록 함
                Log.d("MainScreen", "📊 평가 알림 딥링크 감지")
                Log.d("MainScreen", "   mission_id: $missionId")
                Log.d("MainScreen", "   openDetail: $initialOpenMissionDetail")

                // Home/내부 Nav 준비 시간
                kotlinx.coroutines.delay(500)

                selectedTab = BottomTab.MISSION

                innerNavController.navigate("mission_notification") {
                    launchSingleTop = true
                }

                handledMissionId = missionId

                Log.d("MainScreen", "✅ 평가 알림 → 미션 화면 이동 완료")

            } else if (handledMissionId != missionId) {
                // 개입 알림: 새로운 missionId일 때만 이동 (중복 방지)
                Log.d("MainScreen", "🎯 개입 알림 딥링크 감지 (새 미션)")
                Log.d("MainScreen", "   mission_id: $missionId")

                kotlinx.coroutines.delay(500)

                selectedTab = BottomTab.MISSION

                innerNavController.navigate("mission_notification") {
                    launchSingleTop = true
                }

                handledMissionId = missionId   // 이번 미션은 처리 완료

                Log.d("MainScreen", "✅ 개입 알림 → 미션 화면 이동 완료")
            }
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

                    if (it == BottomTab.HOME) {
                        innerNavController.navigate("home") {
                            launchSingleTop = true
                            popUpTo("home") { inclusive = false }
                        }
                    }

                    if (it == BottomTab.GROUP) {
                        innerNavController.navigate(Route.GroupRoot.path) {
                            launchSingleTop = true
                        }
                    }

                    if (it == BottomTab.MISSION) {
                        innerNavController.navigate("mission_notification") {
                            launchSingleTop = true
                        }
                    }

                    if (it == BottomTab.REPORT) {
                        innerNavController.navigate(Route.Report.path) {
                            launchSingleTop = true
                        }
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
