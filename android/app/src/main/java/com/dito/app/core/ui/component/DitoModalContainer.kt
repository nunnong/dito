package com.dito.app.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dito.app.core.ui.designsystem.Background
import com.dito.app.core.ui.designsystem.DitoShapes
import com.dito.app.core.ui.designsystem.OnSurface
import com.dito.app.core.ui.designsystem.Spacing.l
import com.dito.app.core.ui.designsystem.Spacing.xxl
import com.dito.app.core.ui.designsystem.hardShadow

/**
 * Dito 디자인 시스템의 모달 컨테이너 컴포넌트
 *
 * hard shadow, border, background가 적용된 재사용 가능한 모달 스타일 컴포넌트
 *
 * @param modifier 추가적인 Modifier (alignment 등)
 * @param shape 모달의 형태 (기본값: DitoShapes.extraLarge)
 * @param backgroundColor 배경색 (기본값: Background)
 * @param borderColor 테두리 색상 (기본값: OnSurface)
 * @param borderWidth 테두리 두께 (기본값: 1.dp)
 * @param shadowOffsetX 그림자 X 오프셋 (기본값: 6.dp)
 * @param shadowOffsetY 그림자 Y 오프셋 (기본값: 6.dp)
 * @param shadowColor 그림자 색상 (기본값: OnSurface)
 * @param cornerRadius 그림자 모서리 반경 (기본값: 32.dp)
 * @param contentPadding 내부 content의 padding (기본값: horizontal=48.dp, vertical=56.dp)
 * @param content 모달 내부에 표시할 컨텐츠
 */
@Composable
fun DitoModalContainer(
    modifier: Modifier = Modifier,
    shape: Shape = DitoShapes.extraLarge,
    backgroundColor: Color = Background,
    borderColor: Color = OnSurface,
    borderWidth: Dp = 1.dp,
    shadowOffsetX: Dp = 6.dp,
    shadowOffsetY: Dp = 6.dp,
    shadowColor: Color = OnSurface,
    cornerRadius: Dp = 32.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = xxl, vertical = l),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .hardShadow(
                offsetX = shadowOffsetX,
                offsetY = shadowOffsetY,
                cornerRadius = cornerRadius,
                color = shadowColor
            )
            .clip(shape)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = shape
            )
            .background(color = backgroundColor)
            .padding(contentPadding)

    ) {
        content()
    }
}

/**
 * Modifier extension으로 모달 스타일을 적용하는 함수
 *
 * hard shadow, clip, border, background를 한 번에 적용
 *
 * @param shape 모달의 형태 (기본값: DitoShapes.extraLarge)
 * @param backgroundColor 배경색 (기본값: Background)
 * @param borderColor 테두리 색상 (기본값: OnSurface)
 * @param borderWidth 테두리 두께 (기본값: 1.dp)
 * @param shadowOffsetX 그림자 X 오프셋 (기본값: 6.dp)
 * @param shadowOffsetY 그림자 Y 오프셋 (기본값: 6.dp)
 * @param shadowColor 그림자 색상 (기본값: OnSurface)
 * @param cornerRadius 그림자 모서리 반경 (기본값: 32.dp)
 */
fun Modifier.modalStyle(
    shape: Shape = DitoShapes.extraLarge,
    backgroundColor: Color = Background,
    borderColor: Color = OnSurface,
    borderWidth: Dp = 1.dp,
    shadowOffsetX: Dp = 6.dp,
    shadowOffsetY: Dp = 6.dp,
    shadowColor: Color = OnSurface,
    cornerRadius: Dp = 32.dp
) = this
    .hardShadow(
        offsetX = shadowOffsetX,
        offsetY = shadowOffsetY,
        cornerRadius = cornerRadius,
        color = shadowColor
    )
    .clip(shape)
    .border(
        width = borderWidth,
        color = borderColor,
        shape = shape
    )
    .background(color = backgroundColor)
