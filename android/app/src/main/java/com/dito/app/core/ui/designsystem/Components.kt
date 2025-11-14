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
        animationSpec = tween(durationMillis = 150),
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
    strokeWidth: Dp = 1.dp,      // 기본 1dp 정도로
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 🔥 얇게 둘러줄 오프셋
        val o = strokeWidth

        // 대각선 4방향만 사용
        val offsets = listOf(
            -o to -o,   // 왼쪽 위
            o to -o,    // 오른쪽 위
            -o to o,    // 왼쪽 아래
            o to o      // 오른쪽 아래
        )

        offsets.forEach { (dx, dy) ->
            Text(
                text = text,
                style = style,
                color = strokeColor,
                textAlign = textAlign,
                maxLines = maxLines,
                modifier = Modifier.offset(dx, dy)
            )
        }

        // 가운데 실제 글자
        Text(
            text = text,
            style = style,
            color = fillColor,
            textAlign = textAlign,
            maxLines = maxLines
        )
    }
}