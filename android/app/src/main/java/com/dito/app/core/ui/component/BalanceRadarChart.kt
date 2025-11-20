package com.dito.app.core.ui.component

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dito.app.core.data.report.RadarChartData
import kotlin.math.cos
import kotlin.math.sin

/**
 * 3축 레이더 차트 컴포넌트 (수면, 집중, 조절력)
 *
 * @param data 차트에 표시할 데이터 (각 점수 0-100 범위, before/after 비교 포함)
 * @param modifier Modifier
 * @param labelColor 축 라벨 색상
 * @param gridColor 그리드 선 색상
 * @param fillColor 현재(after) 데이터 영역 채우기 색상
 * @param beforeColor 이전(before) 데이터 영역 색상
 */
@Composable
fun BalanceRadarChart(
    data: RadarChartData,
    modifier: Modifier = Modifier,
    labelColor: Color = Color.Black,
    gridColor: Color = Color.LightGray,
    fillColor: Color = Color(0xFFEC3E3E), // Red for after (current)
    beforeColor: Color = Color(0xFF0080FF) // Blue for before (previous)
) {
    // 현재(after) 점수 정규화 (0.0 ~ 1.0)
    val afterScores = listOf(
        data.sleepScore / 100f,       // Top (12시 방향)
        data.focusScore / 100f,       // Bottom Right (4시 방향)
        data.selfControlScore / 100f  // Bottom Left (8시 방향)
    )

    // 이전(before) 점수 정규화 (0.0 ~ 1.0)
    val beforeScores = listOf(
        data.sleepBefore / 100f,
        data.focusBefore / 100f,
        data.selfControlBefore / 100f
    )

    val labels = listOf("🌙 수면", "🎯 집중", "⚖️ 조절력")

    Box(modifier = modifier.aspectRatio(1f)) {
        Canvas(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2
            val angleStep = (2 * Math.PI / 3).toFloat() // 3축 (120도)

            // 1. 배경 그리드 그리기 (동심원 형태의 삼각형 4단계)
            val steps = 4
            for (i in 1..steps) {
                val stepRadius = radius * (i / steps.toFloat())
                val gridPath = Path()

                for (j in 0 until 3) {
                    val angle = (angleStep * j) - (Math.PI / 2).toFloat() // -90도에서 시작 (12시 방향)
                    val x = center.x + stepRadius * cos(angle)
                    val y = center.y + stepRadius * sin(angle)

                    if (j == 0) gridPath.moveTo(x, y)
                    else gridPath.lineTo(x, y)
                }
                gridPath.close()

                drawPath(
                    path = gridPath,
                    color = gridColor.copy(alpha = 0.5f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // 2. 축 그리기 (중심에서 뻗어나가는 선)
            for (j in 0 until 3) {
                val angle = (angleStep * j) - (Math.PI / 2).toFloat()
                val endX = center.x + radius * cos(angle)
                val endY = center.y + radius * sin(angle)

                drawLine(
                    color = gridColor.copy(alpha = 0.8f),
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 1.dp.toPx()
                )

                // 텍스트 라벨 그리기 (Native Canvas 사용)
                val labelRadius = radius + 20.dp.toPx()
                val labelX = center.x + labelRadius * cos(angle)
                val labelY = center.y + labelRadius * sin(angle)

                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        labels[j],
                        labelX,
                        labelY + 10f, // 수직 중앙 정렬 보정
                        Paint().apply {
                            color = labelColor.toArgb()
                            textSize = 14.sp.toPx()
                            textAlign = Paint.Align.CENTER
                            typeface = Typeface.DEFAULT_BOLD
                        }
                    )
                }
            }

            // 3. Before 데이터 영역 그리기 (회색, 반투명)
            val beforePath = Path()
            val beforePoints = mutableListOf<Offset>()

            beforeScores.forEachIndexed { index, score ->
                val angle = (angleStep * index) - (Math.PI / 2).toFloat()
                // 최소 5%는 보이게 하여 모양 유지
                val effectiveScore = score.coerceAtLeast(0.05f)
                val x = center.x + (radius * effectiveScore) * cos(angle)
                val y = center.y + (radius * effectiveScore) * sin(angle)
                val point = Offset(x, y)
                beforePoints.add(point)

                if (index == 0) beforePath.moveTo(point.x, point.y)
                else beforePath.lineTo(point.x, point.y)
            }
            beforePath.close()

            // Before 영역 채우기 (파란색, 70% 불투명)
            drawPath(
                path = beforePath,
                color = beforeColor.copy(alpha = 0.7f)
            )

            // Before 외곽선 (실선)
            drawPath(
                path = beforePath,
                color = beforeColor,
                style = Stroke(
                    width = 3.dp.toPx(),
                    join = StrokeJoin.Round,
                    cap = StrokeCap.Round
                )
            )

            // 4. After 데이터 영역 그리기 (노랑색)
            val afterPath = Path()
            val afterPoints = mutableListOf<Offset>()

            afterScores.forEachIndexed { index, score ->
                val angle = (angleStep * index) - (Math.PI / 2).toFloat()
                // 최소 5%는 보이게 하여 모양 유지
                val effectiveScore = score.coerceAtLeast(0.05f)
                val x = center.x + (radius * effectiveScore) * cos(angle)
                val y = center.y + (radius * effectiveScore) * sin(angle)
                val point = Offset(x, y)
                afterPoints.add(point)

                if (index == 0) afterPath.moveTo(point.x, point.y)
                else afterPath.lineTo(point.x, point.y)
            }
            afterPath.close()

            // After 내부 채우기 (빨강색, 70% 불투명)
            drawPath(
                path = afterPath,
                color = fillColor.copy(alpha = 0.7f)
            )

            // After 외곽선 그리기 (실선)
            drawPath(
                path = afterPath,
                color = fillColor,
                style = Stroke(
                    width = 3.dp.toPx(),
                    join = StrokeJoin.Round,
                    cap = StrokeCap.Round
                )
            )

            // 5. After 꼭짓점 점 그리기
            afterPoints.forEach { point ->
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = fillColor,
                    radius = 3.5.dp.toPx(),
                    center = point
                )
            }

            // Before 꼭짓점 점 그리기 (파란색)
            beforePoints.forEach { point ->
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = beforeColor,
                    radius = 3.dp.toPx(),
                    center = point
                )
            }
        }
    }
}
