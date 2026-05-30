package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GameBoostViewModel
import com.example.ui.components.GlassCard

@Composable
fun BoosterScreen(
    viewModel: GameBoostViewModel,
    modifier: Modifier = Modifier
) {
    val isBoosting by viewModel.isBoosting.collectAsState()
    val boostStats by viewModel.lastBoostStats.collectAsState()
    val liveMetrics by viewModel.liveMetrics.collectAsState()

    // Configure loop rotations for active neon loader
    val infiniteTransition = rememberInfiniteTransition(label = "Booster Loop")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Spinner Rotation"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07080F))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "P E R F O R M A N C E",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFFF007F),
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = "One Tap Booster",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
        }

        // Master Active Booster Spinner Widget
        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Glowing border
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(if (isBoosting) rotationAngle else 0f)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            colors = if (isBoosting) {
                                listOf(Color(0xFFFF007F), Color(0xFF00FFCC), Color(0xFFFF007F))
                            } else {
                                listOf(Color(0x3300FFCC), Color(0x33FF007F), Color(0x3300FFCC))
                            }
                        )
                    )
            )

            // Inner circle
            Box(
                modifier = Modifier
                    .fillMaxSize(0.92f)
                    .clip(CircleShape)
                    .background(Color(0xFF0A0C16)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(12.dp)
                ) {
                    if (isBoosting) {
                        Text(
                            text = "KILLING TASK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF007F),
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "BOOSTING",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "RAM release...",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    } else if (boostStats != null) {
                        Text(
                            text = "OPTIMIZED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00FFCC),
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("+%.1fG", boostStats!!.reclaimedRamMb / 1024f),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "reclaimed",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    } else {
                        // Standing idle
                        val ramPct = (liveMetrics.ramUsedMb / liveMetrics.ramTotalMb * 100f).coerceIn(0f, 100f)
                        Text(
                            text = "RAM ALLOC",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${ramPct.toInt()}%",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${liveMetrics.ramUsedMb.toInt()}MB used",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Trigger button
        Button(
            onClick = { viewModel.performOneTapBoost() },
            enabled = !isBoosting,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF007F),
                disabledContainerColor = Color(0x33FF007F)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("boost_now_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Trigger Boost",
                    tint = Color.White
                )
                Text(
                    text = if (isBoosting) "OPTIMIZING PROCESSES..." else "ENGAGE ONE TAP BOOST",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Diagnostic summary card listing stats
        Divider(color = Color(0x1AFFFFFF))

        Text(
            text = "OPTIMIZATION METRICS REPORT",
            fontSize = 11.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.align(Alignment.Start)
        )

        AnimatedContent(
            targetState = boostStats,
            transitionSpec = {
                fadeIn(animationSpec = tween(220, delayMillis = 90)) togetherWith fadeOut(animationSpec = tween(90))
            },
            label = "Report Swap"
        ) { stats ->
            if (stats == null) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Pending Stats",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Ready to booster check.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray
                        )
                        Text(
                            text = "Tap the booster button to close processes, clean system cache overhead, and release hardware RAM nodes.",
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        BoostStatBlock(
                            label = "RAM RECLAIMED",
                            value = String.format("%.0f MB", stats.reclaimedRamMb),
                            tint = Color(0xFF00FFCC),
                            icon = Icons.Default.Memory,
                            modifier = Modifier.weight(1f)
                        )
                        BoostStatBlock(
                            label = "APPS KILLED",
                            value = "${stats.processesKilled} tasks",
                            tint = Color(0xFFFF007F),
                            icon = Icons.Default.Cancel,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "TELEMETRY SPECS SHIFT",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        StatCompareRow(
                            label = "Available System RAM",
                            before = String.format("%.0fM", stats.initialAvailableRamMb),
                            after = String.format("%.0fM", stats.finalAvailableRamMb),
                            color = Color(0xFF00FFCC)
                        )
                        
                        StatCompareRow(
                            label = "Active RAM Used",
                            before = String.format("%.0fM", stats.initialUsedRamMb),
                            after = String.format("%.0fM", stats.finalUsedRamMb),
                            color = Color(0xFFFF007F)
                        )

                        StatCompareRow(
                            label = "Running System Tasks",
                            before = "${stats.initialProcessCount} tasks",
                            after = "${stats.finalProcessCount} tasks",
                            color = Color(0xFFCCFF00)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BoostStatBlock(
    label: String,
    value: String,
    tint: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    GlassCard(
        borderColor = listOf(tint.copy(alpha = 0.4f), Color.Transparent),
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Text(label, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 16.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = Color.White)
        }
    }
}

@Composable
fun StatCompareRow(
    label: String,
    before: String,
    after: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = Color.White)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(before, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.Gray)
            Icon(
                imageVector = Icons.Default.TrendingFlat,
                contentDescription = "Improved",
                tint = Color.LightGray,
                modifier = Modifier.size(12.dp)
            )
            Text(after, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = color, fontWeight = FontWeight.Bold)
        }
    }
}
