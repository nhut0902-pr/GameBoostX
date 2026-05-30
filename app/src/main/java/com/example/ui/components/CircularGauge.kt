package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CircularGauge(
    percentage: Float,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    subtext: String = ""
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val radius = diameter / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            val rect = Size(diameter, diameter)
            val topLeft = Offset(center.x - radius, center.y - radius)

            // Draw full background track
            drawArc(
                color = Color(0x1F222437),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = rect,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Draw running filled progress arc
            val sweepGradient = Brush.sweepGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.5f),
                    accentColor,
                    accentColor
                )
            )
            drawArc(
                brush = sweepGradient,
                startAngle = 135f,
                sweepAngle = (percentage / 100f) * 270f,
                useCenter = false,
                topLeft = topLeft,
                size = rect,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${percentage.toInt()}%",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color.White
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
            if (subtext.isNotEmpty()) {
                Text(
                    text = subtext,
                    fontSize = 9.sp,
                    color = accentColor,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
