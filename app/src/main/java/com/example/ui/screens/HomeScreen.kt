package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booster.domain.GamingProfile
import com.example.ui.GameBoostViewModel
import com.example.ui.components.CircularGauge
import com.example.ui.components.GlassCard
import com.example.ui.components.TelemetryChart

@Composable
fun HomeScreen(
    viewModel: GameBoostViewModel,
    onNavigateToLibrary: () -> Unit,
    onNavigateToBooster: () -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics by viewModel.liveMetrics.collectAsState()
    val isRunning by viewModel.isServiceRunning.collectAsState()
    val isOverlayShowing by viewModel.isOverlayShowing.collectAsState()
    val activeProfile by viewModel.activeProfile.collectAsState()

    val fpsHistory by viewModel.fpsHistoryList.collectAsState()
    val cpuHistory by viewModel.cpuHistoryList.collectAsState()
    val ramHistory by viewModel.ramHistoryList.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07080F))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Master Header Panel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "G A M E B O O S T X",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFF007F),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "System Cockpit",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }

            // Small dynamic LED indicator
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .background(if (isRunning) Color(0x3300FFCC) else Color(0x33FF007F))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isRunning) Color(0xFF00FFCC) else Color(0xFFFF007F))
                    )
                    Text(
                        text = if (isRunning) "ENGINE ONLINE" else "IDLE",
                        color = if (isRunning) Color(0xFF00FFCC) else Color(0xFFFF007F),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Active Session Controller Widget
        AnimatedVisibility(
            visible = isRunning,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            GlassCard(
                borderColor = listOf(Color(0xFF00FFCC), Color(0xFFCCFF00)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ACTIVE SESSION DETECTED",
                            fontSize = 9.sp,
                            color = Color(0xFFCCFF00),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Optimizing Performance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "FPS: ${metrics.fps}",
                                color = Color(0xFF00FFCC),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Latency: ${metrics.netPingMs}ms",
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.stopSessionAndTelemetry() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("stop_session_button")
                    ) {
                        Text("STOP", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Quick Boost trigger bar
        GlassCard(
            borderColor = listOf(Color(0xFFFF007F), Color(0xFF00FFCC)),
            onClick = onNavigateToBooster,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x1AFF007F)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Ram Booster",
                            tint = Color(0xFFFF007F)
                        )
                    }
                    Column {
                        Text(
                            text = "ONE-TAP BOOSTER",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Analyze RAM & release background tasks",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Navigate to Booster",
                    tint = Color.Gray
                )
            }
        }

        // Realtime Hardware Dials Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GlassCard(modifier = Modifier.weight(1f)) {
                CircularGauge(
                    percentage = metrics.cpuUsage,
                    label = "CPU LOAD",
                    accentColor = Color(0xFFFF007F),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    subtext = "${metrics.cpuFreqGhz}GHz"
                )
            }

            GlassCard(modifier = Modifier.weight(1f)) {
                CircularGauge(
                    percentage = (metrics.ramUsedMb / metrics.ramTotalMb * 100f).coerceIn(0f, 100f),
                    label = "RAM ALLOC",
                    accentColor = Color(0xFF00FFCC),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    subtext = "${metrics.ramUsedMb.toInt()}/${metrics.ramTotalMb.toInt()}M"
                )
            }
        }

        // Gaming mode controller Profile Switcher Matrix
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "GAMING MODE STAGES",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.Default.Gamepad,
                    contentDescription = "Gamepad Mode",
                    tint = Color(0xFF00FFCC)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    GamingProfile.PERFORMANCE to "PRO MODE" to Color(0xFFFF007F),
                    GamingProfile.BALANCED to "BALANCED" to Color(0xFF00FFCC),
                    GamingProfile.BATTERY_SAVER to "ECO SAVER" to Color(0xFFCCFF00)
                ).forEach { (profileColor, label) ->
                    val (profile, name) = profileColor
                    val isSelected = activeProfile == profile
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("profile_${profile.name.lowercase()}"),
                        onClick = { viewModel.setGamingProfile(profile) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) label.copy(alpha = 0.2f) else Color(0x0F1C1F2B)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) label else Color(0x33FFFFFF)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = name,
                            color = if (isSelected) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Telemetry Curves Chart module
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "REALTIME TELEMETRY TRACKER",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(14.dp))

            TelemetryChart(
                data = if (fpsHistory.isEmpty()) listOf(60f, 60f) else fpsHistory,
                label = "FPS (TARGET REFRESH RATE)",
                accentColor = Color(0xFF00FFCC),
                maxValue = 60f
            )

            Spacer(modifier = Modifier.height(16.dp))

            TelemetryChart(
                data = if (cpuHistory.isEmpty()) listOf(10f, 15f) else cpuHistory,
                label = "CPU CORE LOAD %",
                accentColor = Color(0xFFFF007F),
                maxValue = 100f
            )

            Spacer(modifier = Modifier.height(16.dp))

            TelemetryChart(
                data = if (ramHistory.isEmpty()) listOf(1500f, 1600f) else ramHistory,
                label = "RAM ALLOCATED MEMORY (MB)",
                accentColor = Color(0xFFCCFF00),
                maxValue = if (metrics.ramTotalMb > 0f) metrics.ramTotalMb else 8000f
            )
        }

        // Nav shortcuts
        Button(
            onClick = onNavigateToLibrary,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("navigate_library_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF101321)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF32385E)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LibraryBooks,
                    contentDescription = "Library",
                    tint = Color(0xFF00FFCC)
                )
                Text("OPEN GAMES LIBRARY", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
