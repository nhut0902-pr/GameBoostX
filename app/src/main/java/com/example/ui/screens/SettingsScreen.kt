package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GameBoostViewModel
import com.example.ui.components.GlassCard

@Composable
fun SettingsScreen(
    viewModel: GameBoostViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Config state binders
    val isOverlayShowing by viewModel.isOverlayShowing.collectAsState()
    val isDndEnabled by viewModel.isDndEnabled.collectAsState()
    val isScreenAwakeEnabled by viewModel.isScreenAwakeEnabled.collectAsState()
    val isBrightnessLocked by viewModel.isBrightnessLocked.collectAsState()
    val lockedBrightnessVal by viewModel.lockedBrightnessValue.collectAsState()

    // Realtime permissions checks
    var hasOverlayPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else true
        )
    }

    var hasDndPermission by remember {
        mutableStateOf(viewModel.hasDndPermission())
    }

    // Refresh permissions on screen resume / interactive actions
    LaunchedEffect(Unit) {
        hasOverlayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true
        hasDndPermission = viewModel.hasDndPermission()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07080F))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "P A R A M E T E R S",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFFF007F),
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = "Gaming Sandbox",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
        }

        // Section 1: Overlay controls
        Text(
            text = "FLOATING HUD SETTINGS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            fontFamily = FontFamily.Monospace
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Display Game Overlay", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Draw floating widget over game screens to monitor metrics", color = Color.Gray, fontSize = 12.sp)
                }

                Switch(
                    checked = isOverlayShowing,
                    onCheckedChange = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                            // Onboarding trigger
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } else {
                            viewModel.toggleOverlayWidget()
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00FFCC),
                        checkedTrackColor = Color(0x3300FFCC)
                    ),
                    modifier = Modifier.testTag("overlay_toggle_switch")
                )
            }

            if (!hasOverlayPermission) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x1AFFFF00))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "ATTENTION: Floating telemetry overlay requires the 'Draw over other apps' permission to function.",
                        color = Color(0xFFCCFF00),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Section 2: Hardware Optimizers
        Text(
            text = "HARDWARE ENGINE REGULATORS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            fontFamily = FontFamily.Monospace
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            // Keep Screen Awake
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Keep Screen Awake", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Locks display active during gameplay sessions", color = Color.Gray, fontSize = 12.sp)
                }

                Switch(
                    checked = isScreenAwakeEnabled,
                    onCheckedChange = { viewModel.toggleScreenAwake(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00FFCC),
                        checkedTrackColor = Color(0x3300FFCC)
                    ),
                    modifier = Modifier.testTag("awake_toggle_switch")
                )
            }

            Divider(color = Color(0x19FFFFFF), modifier = Modifier.padding(vertical = 12.dp))

            // Lock Display Brightness
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Lock Display Brightness", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Prevents automatic adaptive brightness shifts", color = Color.Gray, fontSize = 12.sp)
                }

                Switch(
                    checked = isBrightnessLocked,
                    onCheckedChange = { viewModel.toggleBrightnessLock(it, lockedBrightnessVal) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00FFCC),
                        checkedTrackColor = Color(0x3300FFCC)
                    ),
                    modifier = Modifier.testTag("brightness_lock_switch")
                )
            }

            if (isBrightnessLocked) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Locked Intensity", fontSize = 12.sp, color = Color.Gray)
                        Text("${(lockedBrightnessVal * 100).toInt()}%", fontSize = 12.sp, color = Color(0xFF00FFCC), fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = lockedBrightnessVal,
                        onValueChange = { viewModel.toggleBrightnessLock(isBrightnessLocked, it) },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00FFCC),
                            activeTrackColor = Color(0xFF00FFCC),
                            inactiveTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier.testTag("brightness_slider")
                    )
                }
            }

            Divider(color = Color(0x19FFFFFF), modifier = Modifier.padding(vertical = 12.dp))

            // Do Not Disturb Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Do Not Disturb", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Mute all incoming notifications and interruptions", color = Color.Gray, fontSize = 12.sp)
                }

                Switch(
                    checked = isDndEnabled,
                    onCheckedChange = { enable ->
                        if (!viewModel.hasDndPermission()) {
                            // Onboarding trigger
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }
                        } else {
                            viewModel.toggleDndMode(enable)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00FFCC),
                        checkedTrackColor = Color(0x3300FFCC)
                    ),
                    modifier = Modifier.testTag("dnd_toggle_switch")
                )
            }

            if (!hasDndPermission) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x1AFFFFFF)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(36.dp)
                ) {
                    Text("Grant DND Muting Permission", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Section: GitHub Auto-Update Configuration
        var isAutoCheckEnabled by remember { mutableStateOf(viewModel.updateManager.isAutoCheckEnabled()) }
        val updateState by viewModel.updateState.collectAsState()

        Text(
            text = "GITHUB AUTO-UPDATE SETTINGS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            fontFamily = FontFamily.Monospace
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Check on App Startup", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Automatically queries GitHub for updates when opened", color = Color.Gray, fontSize = 12.sp)
                    }

                    Switch(
                        checked = isAutoCheckEnabled,
                        onCheckedChange = {
                            isAutoCheckEnabled = it
                            viewModel.saveGitHubSettings("", "", it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00FFCC),
                            checkedTrackColor = Color(0x3300FFCC)
                        ),
                        modifier = Modifier.testTag("update_auto_check_switch")
                    )
                }

                Divider(color = Color(0x19FFFFFF), modifier = Modifier.padding(vertical = 4.dp))

                val packageInfo = remember(context) { context.packageManager.getPackageInfo(context.packageName, 0) }
                val currentVersion = packageInfo.versionName ?: "1.0"

                Text(
                    text = "Current App Version: v$currentVersion",
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold
                )

                when (val state = updateState) {
                    is com.example.update.UpdateState.Idle -> {
                        Button(
                            onClick = { viewModel.checkForUpdates() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(40.dp).testTag("check_updates_button")
                        ) {
                            Text("CHECK FOR UPDATES NOW", color = Color(0xFF07080F), fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                    is com.example.update.UpdateState.Checking -> {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(color = Color(0xFF00FFCC), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Checking GitHub Releases...", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                    is com.example.update.UpdateState.UpdateAvailable -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x1A00FFCC), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text("Update Available: ${state.version}", color = Color(0xFF00FFCC), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(state.changelog, color = Color.Gray, fontSize = 11.sp, maxLines = 4)
                            
                            val hasInstallPermission = viewModel.updateManager.canRequestPackageInstalls()
                            if (!hasInstallPermission) {
                                Button(
                                    onClick = { viewModel.updateManager.launchPackageInstallSettings() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Grant Install Unknown Apps Permission", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.downloadAndInstallUpdate(state.downloadUrl) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("DOWNLOAD & AUTO UPDATE", color = Color.DarkGray, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    is com.example.update.UpdateState.UpToDate -> {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Your app is fully up-to-date!", color = Color.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            TextButton(onClick = { viewModel.resetUpdateState() }) {
                                Text("Dismiss", color = Color(0xFF00FFCC), fontSize = 11.sp)
                            }
                        }
                    }
                    is com.example.update.UpdateState.Downloading -> {
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Downloading update: ${(state.progress * 100).toInt()}%", fontSize = 12.sp, color = Color.White)
                            LinearProgressIndicator(
                                progress = { state.progress },
                                color = Color(0xFF00FFCC),
                                trackColor = Color.DarkGray,
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                    is com.example.update.UpdateState.ReadyToInstall -> {
                        Button(
                            onClick = { viewModel.installUpdate(state.apkFile) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        ) {
                            Text("TAP TO RE-INSTALL / ENGAGE UPDATE", color = Color.White, fontWeight = FontWeight.Black)
                        }
                    }
                    is com.example.update.UpdateState.Error -> {
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Error: ${state.message}", color = Color.Red, fontSize = 12.sp)
                            Button(
                                onClick = { viewModel.resetUpdateState() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Retry", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Onboarding Permissions Information Guides
        Text(
            text = "REGULATORY PRIVACY ONBOARDING",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            fontFamily = FontFamily.Monospace
        )

        OnboardingItem(
            title = "SYSTEM OVERLAY (Draw Over Other Apps)",
            descr = "Required to display the draggable in-game real-time HUD showing critical CPU load, RAM use, temperature levels, and online latency over gameplay surfaces.",
            icon = Icons.Default.FlipToFront,
            tint = Color(0xFF00FFCC)
        )

        OnboardingItem(
            title = "PACKAGE USAGE STATISTICS ACCESS",
            descr = "Required to scan installed files and identify custom application catalogs dynamically, allowing precise game launchers population.",
            icon = Icons.Default.Apps,
            tint = Color(0xFFFF007F)
        )

        OnboardingItem(
            title = "FOREGROUND SERVICE & NOTIFICATION POLICY",
            descr = "Required to preserve background telemetry coroutines and refresh notifications. Grants authority to engage priority silent filters (DND).",
            icon = Icons.Default.Rule,
            tint = Color(0xFFCCFF00)
        )

        OnboardingItem(
            title = "WAKE_LOCK & SYSTEM CONFIGS",
            descr = "Permits GameBoostX to lock the device screen awake and screen brightness level locally, preventing auto-dim sleep interruptions during matching active gameplay.",
            icon = Icons.Default.LockClock,
            tint = Color.White
        )
    }
}

@Composable
fun OnboardingItem(
    title: String,
    descr: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    GlassCard(borderColor = listOf(tint.copy(alpha = 0.2f), Color.Transparent)) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(tint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = tint, modifier = Modifier.size(18.dp))
            }

            Column {
                Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(descr, color = Color.Gray, fontSize = 11.sp)
            }
        }
    }
}
