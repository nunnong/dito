package com.dito.app.feature.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dito.app.R
import com.dito.app.core.ui.component.DitoModalContainer
import com.dito.app.core.ui.designsystem.Background
import com.dito.app.core.ui.designsystem.DitoCustomTextStyles
import com.dito.app.core.ui.designsystem.DitoShapes
import com.dito.app.core.ui.designsystem.DitoTypography
import com.dito.app.core.ui.designsystem.OnSurface
import com.dito.app.core.ui.designsystem.Spacing
import com.dito.app.core.ui.designsystem.hardShadow

@Preview(showBackground = true)
@Composable

fun PrivacyPoicyDialog(onDismiss: () -> Unit = {}) {
    val context = LocalContext.current
    val termsText = remember {
        context.resources.openRawResource(R.raw.privacy_policy)
            .bufferedReader()
            .use { it.readText() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = Spacing.l, vertical = Spacing.m),
        contentAlignment = Alignment.Center
    ) {
        DitoModalContainer(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color.White,
            borderColor = Color.Black,
            shadowColor = Color.Black,
            contentPadding = PaddingValues(vertical = Spacing.s)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.m, vertical = Spacing.m)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "뒤로가기",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.TopStart)
                            .clickable{onDismiss()}
                    )
                }

                Spacer(Modifier.height(Spacing.xl))

                Text(
                    text = "개인정보 처리방침 ",
                    color = OnSurface,
                    style = DitoCustomTextStyles.titleKLarge
                )

                Spacer(Modifier.height(Spacing.xl))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(300.dp)
                        .hardShadow(
                            offsetX = 4.dp,
                            offsetY = 4.dp,
                            cornerRadius = 8.dp,
                            color = Color.Black
                        )
                        .clip(DitoShapes.small)
                        .border(1.dp, Color.Black, DitoShapes.small)
                        .background(Color.White)
                        .padding(14.dp)
                ) {
                    Text(
                        text = termsText,
                        color = Color.Black,
                        style = DitoTypography.bodyMedium,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }

                Spacer(Modifier.height(Spacing.m))
            }
        }
    }
}
