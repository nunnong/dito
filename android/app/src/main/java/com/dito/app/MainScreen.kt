package com.dito.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.dito.app.core.ui.component.BottomTab
import com.dito.app.core.ui.component.DitoBottomAppBar
import com.dito.app.feature.group.GroupChallengeScreen
import com.dito.app.feature.home.HomeScreen
import com.dito.app.feature.shop.ShopScreen

@Composable
fun MainScreen(
    onLogout: () -> Unit = {},
    onNavigateToShop: () -> Unit = {},
    initialShowShop: Boolean = false,
    onBackFromShop: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }
    var showShop by remember { mutableStateOf(initialShowShop) }

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
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showShop) {
                ShopScreen(
                    onBackClick = {
                        showShop = false
                        onBackFromShop()
                    }
                )
            } else {
                when (selectedTab) {
                    BottomTab.GROUP -> GroupChallengeScreen()
                    BottomTab.HOME -> HomeScreen(
                        onLogout = onLogout,
                        onCartClick = {
                            showShop = true
                            onNavigateToShop()
                        }
                    )
                    BottomTab.SETTINGS -> {
                        // TODO: SettingsScreen 구현
                    }
                }
            }
        }
    }
}
