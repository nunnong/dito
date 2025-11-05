package com.dito.app.feature.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dito.app.R
import com.dito.app.core.ui.designsystem.*
import kotlinx.coroutines.delay

@Preview
@Composable
fun SplashScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .width(272.dp)
                .wrapContentHeight()
        ) {
            // Modal Box with Character
            LoadingModalBox()

            // Title Text
            TitleSection()
        }
    }
}

@Composable
private fun LoadingModalBox() {
    Box(
        modifier = Modifier
            .width(255.dp)
            .height(225.dp)
            .hardShadow(DitoHardShadow.Modal.copy(cornerRadius = 8.dp))
            .background(
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 2.dp,
                color = Color.Black,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Yellow Header with Close Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Primary,
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    // Close Button
                    Image(
                        painter = painterResource(id = R.drawable.close),
                        contentDescription = "Close",
                        modifier = Modifier
                            .padding(end = 11.dp)
                            .size(24.dp)
                    )
                }
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .background(Color.Black)
                )
            }

            // Character and Loading Text
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Character Image
                Image(
                    painter = painterResource(id = R.drawable.dito),
                    contentDescription = "Dito Character",
                    modifier = Modifier.size(125.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Loading Text
                var loadingText by remember { mutableStateOf("LOADING   ") } // Initialize with spaces for "..."
                LaunchedEffect(Unit) {
                    val baseText = "LOADING"
                    val maxDots = 3
                    var currentDots = 0
                    while (true) {
                        loadingText = baseText + ".".repeat(currentDots) + " ".repeat(maxDots - currentDots)
                        currentDots = (currentDots + 1) % (maxDots + 1) // Cycle 0, 1, 2, 3
                        delay(500)
                    }
                }
                Text(
                    text = loadingText,
                    style = DitoTypography.titleLarge,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun TitleSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .width(272.dp)
            .wrapContentHeight()
    ) {
        // "DIGITAL DETOX" Text - DI만 노란색
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "DI",
                style = DitoTypography.displaySmall,
                color = Primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "GITAL DETOX",
                style = DitoTypography.displaySmall,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }

        // "TOGETHER" Text - TO만 노란색
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "TO",
                style = DitoTypography.displayMedium,
                color = Primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "GETHER",
                style = DitoTypography.displayMedium,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen()
}