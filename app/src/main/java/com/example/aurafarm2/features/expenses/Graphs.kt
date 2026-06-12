package com.example.aurafarm2.features.expenses

import android.graphics.Paint
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.aurafarm2.core.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

data class ChartDataPoint(
    val dateEpochDay: Long,
    val amount: Double
)

@Composable
fun TrendLineChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = Primary,
    currencySymbol: String = "$"
) {
    if (data.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
            Text("No data for this period", color = OnSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val sortedData = data.sortedBy { it.dateEpochDay }
    val maxAmount = sortedData.maxOfOrNull { it.amount }?.coerceAtLeast(1.0) ?: 1.0
    
    // Animation for line drawing
    val transition = updateTransition(targetState = true, label = "lineChartTransition")
    val progress by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 1500, easing = FastOutSlowInEasing) },
        label = "progress",
        targetValueByState = { if (it) 1f else 0f }
    )

    val backgroundColor = Background

    // Optional tooltip state
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Box(modifier = modifier.fillMaxWidth().height(220.dp).padding(16.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(sortedData) {
                    detectTapGestures { offset ->
                        val itemWidth = size.width / (sortedData.size.coerceAtLeast(2) - 1).coerceAtLeast(1).toFloat()
                        val index = (offset.x / itemWidth).toInt().coerceIn(0, sortedData.size - 1)
                        selectedIndex = index
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            val path = Path()
            val fillPath = Path()
            
            val points = mutableListOf<Offset>()
            
            if (sortedData.size == 1) {
                points.add(Offset(width / 2f, height - (sortedData[0].amount.toFloat() / maxAmount.toFloat()) * height))
                path.moveTo(points.first().x, points.first().y)
                path.lineTo(points.first().x + 1, points.first().y)
            } else {
                val dx = width / (sortedData.size - 1)
                sortedData.forEachIndexed { index, point ->
                    val x = index * dx
                    val y = height - ((point.amount / maxAmount).toFloat() * height * 0.8f) // leave 20% top padding
                    points.add(Offset(x, y))
                }

                path.moveTo(points.first().x, points.first().y)
                fillPath.moveTo(points.first().x, height)
                fillPath.lineTo(points.first().x, points.first().y)

                for (i in 0 until points.size - 1) {
                    val p1 = points[i]
                    val p2 = points[i + 1]
                    
                    val cx1 = p1.x + dx / 2f
                    val cy1 = p1.y
                    val cx2 = p1.x + dx / 2f
                    val cy2 = p2.y
                    
                    path.cubicTo(cx1, cy1, cx2, cy2, p2.x, p2.y)
                    fillPath.cubicTo(cx1, cy1, cx2, cy2, p2.x, p2.y)
                }

                fillPath.lineTo(points.last().x, height)
                fillPath.close()
            }

            // Path measure to animate drawing
            val pathMeasure = android.graphics.PathMeasure(path.asAndroidPath(), false)
            val animatedPath = Path()
            pathMeasure.getSegment(0f, pathMeasure.length * progress, animatedPath.asAndroidPath(), true)

            // Draw Fill
            val fillGradient = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.3f * progress), Color.Transparent),
                startY = 0f,
                endY = height
            )
            drawPath(
                path = fillPath,
                brush = fillGradient
            )

            // Draw Line
            drawPath(
                path = animatedPath,
                color = lineColor,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw Points
            points.forEachIndexed { index, point ->
                val alpha = if (progress > (index.toFloat() / points.size)) 1f else 0f
                drawCircle(
                    color = backgroundColor,
                    radius = 5.dp.toPx(),
                    center = point,
                    alpha = alpha
                )
                drawCircle(
                    color = lineColor,
                    radius = 3.dp.toPx(),
                    center = point,
                    alpha = alpha
                )
                
                // Draw Selection
                if (selectedIndex == index) {
                    drawCircle(
                        color = lineColor.copy(alpha = 0.2f),
                        radius = 12.dp.toPx(),
                        center = point
                    )
                }
            }
        }

        // Tooltip Overlay
        selectedIndex?.let { index ->
            val point = sortedData[index]
            val dateStr = LocalDate.ofEpochDay(point.dateEpochDay)
                .format(DateTimeFormatter.ofPattern("MMM dd", Locale.ENGLISH))
            
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceContainerHigh.copy(alpha = 0.9f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "$dateStr • $currencySymbol${"%.2f".format(point.amount)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurface
                )
            }
        }
    }
}

data class DonutSlice(
    val value: Double,
    val color: Color,
    val label: String
)

@Composable
fun CategoryDonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    centerText: String = ""
) {
    if (slices.isEmpty()) return

    val total = slices.sumOf { it.value }
    var currentAngle = -90f

    val transition = updateTransition(targetState = true, label = "donutTransition")
    val sweepProgress by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 1500, easing = FastOutSlowInEasing) },
        label = "sweepProgress",
        targetValueByState = { if (it) 1f else 0f }
    )

    Box(modifier = modifier.size(200.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val thickness = 24.dp.toPx()
            
            slices.forEach { slice ->
                val sweepAngle = ((slice.value / total) * 360f).toFloat() * sweepProgress
                
                drawArc(
                    color = slice.color,
                    startAngle = currentAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = thickness, cap = StrokeCap.Round),
                    size = Size(size.width - thickness, size.height - thickness),
                    topLeft = Offset(thickness / 2f, thickness / 2f)
                )
                currentAngle += ((slice.value / total) * 360f).toFloat()
            }
        }
        
        if (centerText.isNotEmpty()) {
            Text(
                text = centerText,
                style = MaterialTheme.typography.titleMedium,
                color = OnSurface
            )
        }
    }
}
