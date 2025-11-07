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

@Composable
fun MainScreen(
    onLogout: () -> Unit = {},
    onNavigateToShop: () -> Unit = {},
    onNavigateToCloset: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }

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
            when (selectedTab) {
                BottomTab.GROUP -> GroupChallengeScreen()
                BottomTab.HOME -> HomeScreen(
                    onLogout = onLogout,
                    onCartClick = onNavigateToShop,
                    onClosetClick = onNavigateToCloset
                )
                BottomTab.SETTINGS -> {
                    // TODO: SettingsScreen 구현
                }
            }
        }
    }
}
