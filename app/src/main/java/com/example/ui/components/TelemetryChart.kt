package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TelemetryChart(
    data: List<Float>,
    label: String,
    accentColor: Color,
    maxValue: Float = 100f,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.Gray,
                fontSize = 11.sp,
                style = MaterialTheme.typography.labelMedium
            )
            val latestVal = data.lastOrNull() ?: 0f
            Text(
                text = if (latestVal % 1f == 0f) "${latestVal.toInt()}" else String.format("%.1f", latestVal),
                color = accentColor,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.titleSmall
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Draw background horizontal grids
                val gridLines = 3
                for (i in 0..gridLines) {
                    val y = height * (i.toFloat() / gridLines)
                    drawLine(
                        color = Color(0x1AFFFFFF),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                }

                if (data.size < 2) return@Canvas

                val stepX = width / (data.size - 1)
                val path = Path()
                val fillPath = Path()

                data.forEachIndexed { index, valRaw ->
                    val value = valRaw.coerceIn(0f, maxValue)
                    // Invert Y coordinate because canvas 0,0 is top-left
                    val x = index * stepX
                    val y = height - (value / maxValue) * height

                    if (index == 0) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, height)
                        fillPath.lineTo(x, y)
                    } else {
                        // Smooth cubic bezier curves or simple line curves
                        val prevValRaw = data[index - 1].coerceIn(0f, maxValue)
                        val prevX = (index - 1) * stepX
                        val prevY = height - (prevValRaw / maxValue) * height
                        
                        val controlX1 = prevX + stepX / 2f
                        val controlY1 = prevY
                        val controlX2 = prevX + stepX / 2f
                        val controlY2 = y

                        path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                        fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                    }

                    if (index == data.size - 1) {
                        fillPath.lineTo(x, height)
                        fillPath.close()
                    }
                }

                // Draw translucent glowing gradient fill
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(accentColor.copy(alpha = 0.3f), Color.Transparent)
                    )
                )

                // Draw neon curve stroke
                drawPath(
                    path = path,
                    color = accentColor,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Optional: Draw glow point on latest item
                val lastVal = data.last().coerceIn(0f, maxValue)
                val lastX = width
                val lastY = height - (lastVal / maxValue) * height
                drawCircle(
                    color = accentColor,
                    radius = 4.dp.toPx(),
                    center = Offset(lastX, lastY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 1.5.dp.toPx(),
                    center = Offset(lastX, lastY)
                )
            }
        }
    }
}
