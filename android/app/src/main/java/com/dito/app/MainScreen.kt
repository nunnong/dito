package com.dito.app

import androidx.compose.foundation.layout.Box
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
import com.dito.app.feature.home.HomeScreen
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
    outerNavController: NavController? = null
) {
    val innerNavController = rememberNavController()
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }
    var showShop by remember { mutableStateOf(initialShowShop) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 권한 체크 상태 (무한 네비게이션 방지)
    var isCheckingPermissions by remember { mutableStateOf(false) }

    // 화면이 다시 보일 때마다 권한 상태 확인
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && !isCheckingPermissions) {
                val hasAccessibility = PermissionHelper.isAccessibilityPermissionGranted(context)
                val hasUsageStats = PermissionHelper.isUsageStatsPermissionGranted(context)
                val hasNotification = PermissionHelper.isNotificationPermissionGranted(context)

                if (!hasAccessibility || !hasUsageStats || !hasNotification) {
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
                    if (it == BottomTab.HOME) innerNavController.navigate("home") {
                        launchSingleTop = true; popUpTo("home") { inclusive = false }
                    }
                    if (it == BottomTab.GROUP) innerNavController.navigate(Route.GroupRoot.path) {
                        launchSingleTop = true
                    }
                    if (it == BottomTab.SETTINGS) innerNavController.navigate(Route.SettingRoot.path) {
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
                    onCartClick  = { innerNavController.navigate("shop") },
                    onClosetClick = { innerNavController.navigate("closet") }
                )
            }
            composable("shop") {
                ShopScreen(onBackClick = { innerNavController.popBackStack() })
            }
            composable("closet") {
                ClosetScreen(onBackClick = { innerNavController.popBackStack() })
            }
            composable(Route.GroupRoot.path) {
                GroupScreen(navController = innerNavController)
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