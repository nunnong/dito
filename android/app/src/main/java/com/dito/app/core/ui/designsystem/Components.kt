package com.dito.app.core.ui.designsystem

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BounceClickable(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (isPressed: Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = tween(durationMillis = 500), // Adjusted duration
        label = "scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content(isPressed)
    }
}


@Composable
fun StrokeText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    fillColor: Color,
    strokeColor: Color,
    strokeWidth: Dp,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 바깥쪽 4방향 외곽선 텍스트
        Text(
            text = text,
            style = style,
            color = strokeColor,
            textAlign = textAlign,
            maxLines = maxLines,
            modifier = Modifier.offset(x = (-1).dp, y = (-1).dp)
        )
        Text(
            text = text,
            style = style,
            color = strokeColor,
            textAlign = textAlign,
            maxLines = maxLines,
            modifier = Modifier.offset(x = (1).dp, y = (-1).dp)
        )
        Text(
            text = text,
            style = style,
            color = strokeColor,
            textAlign = textAlign,
            maxLines = maxLines,
            modifier = Modifier.offset(x = (-1).dp, y = (1).dp)
        )
        Text(
            text = text,
            style = style,
            color = strokeColor,
            textAlign = textAlign,
            maxLines = maxLines,
            modifier = Modifier.offset(x = (1).dp, y = (1).dp)
        )

        // 안쪽 실제 글자
        Text(
            text = text,
            style = style,
            color = fillColor,
            textAlign = textAlign,
            maxLines = maxLines
        )
    }
}