package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analytics.data.SessionRecord
import com.example.ui.GameBoostViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.TelemetryChart
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnalyticsScreen(
    viewModel: GameBoostViewModel,
    modifier: Modifier = Modifier
) {
    val sessions by viewModel.allSessions.collectAsState()
    var expandedSessionId by remember { mutableStateOf<Int?>(null) }
    
    // Compute aggregations
    val totalSessions = sessions.size
    val avgFps = remember(sessions) {
        if (sessions.isNotEmpty()) sessions.map { it.avgFps }.average().toInt() else 60
    }
    val avgPing = remember(sessions) {
        if (sessions.isNotEmpty()) sessions.map { it.avgPing }.average().toInt() else 24
    }
    val maxFps = remember(sessions) {
        if (sessions.isNotEmpty()) sessions.maxOf { it.peakFps } else 60
    }
    val avgTemp = remember(sessions) {
        if (sessions.isNotEmpty()) sessions.map { it.avgTemp }.average().toFloat() else 32f
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07080F))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "A N A L Y T I C S",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFCCFF00),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Performance Logs",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }

            if (sessions.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.clearAnalytics() },
                    modifier = Modifier
                        .background(Color(0x1AFFF000), RoundedCornerShape(8.dp))
                        .testTag("clear_analytics_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Clear All Sessions",
                        tint = Color(0xFFCCFF00)
                    )
                }
            }
        }

        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(borderColor = listOf(Color(0xFF333544), Color(0xFF1D1F28))) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueryStats,
                            contentDescription = "No sessions yet",
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "No recorded sessions",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Launch any and play games directly from the Game Launcher grid to start capturing hardware diagnostics summaries.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Summary Dashboard Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryBlock(
                    label = "A V G  F P S",
                    value = "$avgFps",
                    accentColor = Color(0xFF00FFCC),
                    modifier = Modifier.weight(1f)
                )
                SummaryBlock(
                    label = "P E A K  F P S",
                    value = "$maxFps",
                    accentColor = Color(0xFFFF007F),
                    modifier = Modifier.weight(1f)
                )
                SummaryBlock(
                    label = "A V G  P I N G",
                    value = "${avgPing}ms",
                    accentColor = Color(0xFFCCFF00),
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "SESSION HISTORY RECORDS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Dynamic Session List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("sessions_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sessions, key = { it.id }) { session ->
                    val isExpanded = expandedSessionId == session.id
                    SessionLogCard(
                        session = session,
                        isExpanded = isExpanded,
                        onToggle = {
                            expandedSessionId = if (isExpanded) null else session.id
                        },
                        onDelete = { viewModel.deleteSessionRecord(session.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryBlock(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(
        borderColor = listOf(accentColor.copy(alpha = 0.3f), Color.Transparent),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(label, fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = Color.White
            )
        }
    }
}

@Composable
fun SessionLogCard(
    session: SessionRecord,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateString = remember(session.timestamp) {
        val sdf = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault())
        sdf.format(Date(session.timestamp))
    }

    GlassCard(
        borderColor = if (isExpanded) {
            listOf(Color(0xFFCCFF00).copy(alpha = 0.7f), Color(0xFFFF007F).copy(alpha = 0.7f))
        } else {
            listOf(Color(0xFF33354A), Color(0xFF1C1D2A))
        },
        modifier = modifier
            .fillMaxWidth()
            .testTag("session_card_${session.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.gameName,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp,
                        maxLines = 1
                    )
                    Text(
                        text = "$dateString • ${session.durationSecs}s duration",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Log Record",
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand Charts",
                        tint = Color.Gray
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Divider(color = Color(0x1FFFFFFF))

                    // Average and limits details dashboard row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetailTextStat(label = "AVG FPS", value = "${session.avgFps}", color = Color(0xFF00FFCC))
                        DetailTextStat(label = "PEAK FPS", value = "${session.peakFps}", color = Color.White)
                        DetailTextStat(label = "MIN FPS", value = "${session.minFps}", color = Color.Gray)
                        DetailTextStat(label = "AVG CPU", value = String.format("%.0f%%", session.avgCpu), color = Color(0xFFFF007F))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetailTextStat(label = "AVG RAM", value = String.format("%.0fM", session.avgRam), color = Color(0xFFCCFF00))
                        DetailTextStat(label = "AVG TEMP", value = String.format("%.1f°C", session.avgTemp), color = Color.Green)
                        DetailTextStat(label = "AVG PING", value = "${session.avgPing}ms", color = Color.White)
                        DetailTextStat(label = "STABILITY", value = if (session.isFpsStable()) "HIGH" else "STRETCH", color = if (session.isFpsStable()) Color.Green else Color.Yellow)
                    }

                    Divider(color = Color(0x1FFFFFFF))

                    // Parse histories vectors and draw charts
                    val fpsHist = remember(session.fpsHistory) { session.fpsHistory.parseCsvFloats() }
                    val cpuHist = remember(session.cpuHistory) { session.cpuHistory.parseCsvFloats() }
                    val ramHist = remember(session.ramHistory) { session.ramHistory.parseCsvFloats() }
                    val pingHist = remember(session.pingHistory) { session.pingHistory.parseCsvFloats() }

                    if (fpsHist.isNotEmpty()) {
                        TelemetryChart(
                            data = fpsHist,
                            label = "FPS (RECONSTRUCTED TIMELINE)",
                            accentColor = Color(0xFF00FFCC),
                            maxValue = 60f
                        )
                    }

                    if (cpuHist.isNotEmpty()) {
                        TelemetryChart(
                            data = cpuHist,
                            label = "CPU CORE LOAD % PROFILE",
                            accentColor = Color(0xFFFF007F),
                            maxValue = 100f
                        )
                    }

                    if (ramHist.isNotEmpty()) {
                        TelemetryChart(
                            data = ramHist,
                            label = "RAM METRIC (MB)",
                            accentColor = Color(0xFFCCFF00),
                            maxValue = (ramHist.maxOrNull() ?: 1000f) * 1.2f
                        )
                    }

                    if (pingHist.isNotEmpty()) {
                        TelemetryChart(
                            data = pingHist,
                            label = "PING NETWORK DELAY (MS)",
                            accentColor = Color.White,
                            maxValue = (pingHist.maxOrNull() ?: 100f) * 1.2f
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailTextStat(
    label: String,
    value: String,
    color: Color
) {
    Column {
        Text(label, fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = color)
    }
}

// Helpers extension to easily parsing comma serialized lists
fun String.parseCsvFloats(): List<Float> {
    if (this.isEmpty()) return emptyList()
    return try {
        this.split(",").map { it.toFloat() }
    } catch (e: Exception) {
        emptyList()
    }
}

// Simple stability index
fun SessionRecord.isFpsStable(): Boolean {
    val variance = peakFps - minFps
    return variance <= 10
}
