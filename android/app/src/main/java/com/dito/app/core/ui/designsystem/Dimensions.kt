package com.dito.app.core.ui.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Hard Shadow
data class HardShadowStyle(
    val offsetX: Dp = 0.dp,
    val offsetY: Dp,
    val color: Color,
    val cornerRadius: Dp = 0.dp
)

fun Modifier.hardShadow(
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp,
    cornerRadius: Dp = 0.dp,
    color: Color = Color.Black
) = this.drawBehind {
    drawRoundRect(
        color = color,
        topLeft = Offset(offsetX.toPx(), offsetY.toPx()),
        size = size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx())
    )
}

fun Modifier.hardShadow(shadowStyle: HardShadowStyle) = this.drawBehind {
    drawRoundRect(
        color = shadowStyle.color,
        topLeft = Offset(shadowStyle.offsetX.toPx(), shadowStyle.offsetY.toPx()),
        size = size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(shadowStyle.cornerRadius.toPx())
    )
}

// Soft Shadow
data class SoftShadowStyle(
    val offsetX: Dp = 0.dp,
    val offsetY: Dp,
    val blurRadius: Dp,
    val color: Color,
    val cornerRadius: Dp = 0.dp
)

fun Modifier.softShadow(
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp,
    blurRadius: Dp = 4.dp,
    cornerRadius: Dp = 0.dp,
    color: Color = Color.Black
) = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            this.color = color
        }
        paint.asFrameworkPaint().apply {
            this.color = color.toArgb()
            setShadowLayer(blurRadius.toPx(), offsetX.toPx(), offsetY.toPx(), color.toArgb())
        }
        canvas.drawRoundRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height,
            radiusX = cornerRadius.toPx(),
            radiusY = cornerRadius.toPx(),
            paint = paint
        )
    }
}

fun Modifier.softShadow(shadowStyle: SoftShadowStyle) = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            this.color = shadowStyle.color
        }
        paint.asFrameworkPaint().apply {
            this.color = shadowStyle.color.toArgb()
            setShadowLayer(
                shadowStyle.blurRadius.toPx(),
                shadowStyle.offsetX.toPx(),
                shadowStyle.offsetY.toPx(),
                shadowStyle.color.toArgb()
            )
        }
        canvas.drawRoundRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height,
            radiusX = shadowStyle.cornerRadius.toPx(),
            radiusY = shadowStyle.cornerRadius.toPx(),
            paint = paint
        )
    }
}

// Hard Shadow Elevation Styles
object DitoHardShadow {

    val ButtonLarge = HardShadowStyle(
        offsetX = 4.dp,
        offsetY = 4.dp,
        color = Color.Black.copy(alpha = 1f)
    )

    val ButtonSmall = HardShadowStyle(
        offsetX = 2.dp,
        offsetY = 2.dp,
        color = Color.Black.copy(alpha = 1f)
    )

    val Modal = HardShadowStyle(
        offsetX = 6.dp,
        offsetY = 6.dp,
        color = Color.Black.copy(alpha = 1f)
    )
}

// Soft Shadow Elevation Styles
object DitoSoftShadow {
    val Low = SoftShadowStyle(
        offsetY = 2.dp,
        blurRadius = 4.dp,
        color = Color.Black.copy(alpha = 0.12f)
    )

    val Medium = SoftShadowStyle(
        offsetY = 4.dp,
        blurRadius = 8.dp,
        color = Color.Black.copy(alpha = 0.15f)
    )

    val High = SoftShadowStyle(
        offsetY = 6.dp,
        blurRadius = 12.dp,
        color = Color.Black.copy(alpha = 0.2f)
    )
}
