package com.dito.app.core.ui.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ShadowStyle(
    val offsetX: Dp = 0.dp,
    val offsetY: Dp,
    val blur: Dp = 0.dp,
    val color: Color
)

object DitoElevation {
    val Low = ShadowStyle(
        offsetY = 2.dp,
        blur = 4.dp,
        color = Color.Black.copy(alpha = 0.12f)
    )

    val Medium = ShadowStyle(
        offsetY = 4.dp,
        blur = 8.dp,
        color = Color.Black.copy(alpha = 0.15f)
    )

    val High = ShadowStyle(
        offsetY = 6.dp,
        blur = 16.dp,
        color = Color.Black.copy(alpha = 0.2f)
    )

    val ButtonLarge = ShadowStyle(
        offsetX = 4.dp,
        offsetY = 4.dp,
        color = Color.Black.copy(alpha = 1f)
    )

    val ButtonSmall = ShadowStyle(
        offsetX = 2.dp,
        offsetY = 2.dp,
        color = Color.Black.copy(alpha = 1f)
    )

    val Modal = ShadowStyle(
        offsetX = 6.dp,
        offsetY = 6.dp,
        blur = 0.dp,
        color = Color.Black.copy(alpha = 1f)
    )
}
