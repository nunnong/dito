package com.dito.app.feature.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.dito.app.R
import com.dito.app.core.ui.component.BottomTab
import com.dito.app.core.ui.component.DitoBottomAppBar

// 입장 후 start 버튼 화면
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GroupLeaderScreen(
    onNavigateToTab: (BottomTab) -> Unit = {}
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            DitoBottomAppBar(
                selectedTab = BottomTab.GROUP,
                onTabSelected = onNavigateToTab
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Image(
                painter = painterResource(id = R.drawable.race2),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            Column(
                modifier = Modifier.fillMaxSize()
            ) {

            }
        }
    }
}
