package com.dito.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dito.app.core.navigation.Route
import com.dito.app.core.ui.component.BottomTab
import com.dito.app.core.ui.component.DitoBottomAppBar
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
) {
    val innerNavController = rememberNavController()
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }

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
                SettingScreen(navController = innerNavController)
            }
            composable(Route.SettingEditNickname.path) {
                ChangeNickName(onDismiss = { innerNavController.popBackStack() })
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