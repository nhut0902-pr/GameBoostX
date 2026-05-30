package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.GameBoostViewModel
import com.example.ui.screens.*

class MainActivity : ComponentActivity() {

    private val viewModel: GameBoostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Re-apply local window optimizations (Keep screen awake & Lock brightness) reactively
            val isScreenAwake by viewModel.isScreenAwakeEnabled.collectAsState()
            val isBrightnessLocked by viewModel.isBrightnessLocked.collectAsState()
            val brightnessVal by viewModel.lockedBrightnessValue.collectAsState()

            LaunchedEffect(isScreenAwake, isBrightnessLocked, brightnessVal) {
                viewModel.applyLocalWindowOptimizations(window)
            }

            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: "home"

            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF00FFCC),
                    secondary = Color(0xFFFF007F),
                    tertiary = Color(0xFFCCFF00),
                    background = Color(0xFF07080F),
                    surface = Color(0xFF10121D),
                    onBackground = Color.White,
                    onSurface = Color.White
                )
            ) {
                // Global update alert check on startup
                val updateState by viewModel.updateState.collectAsState()
                var showUpdateDialog by remember { mutableStateOf(false) }

                LaunchedEffect(updateState) {
                    showUpdateDialog = when (updateState) {
                        is com.example.update.UpdateState.UpdateAvailable -> true
                        is com.example.update.UpdateState.Downloading -> true
                        is com.example.update.UpdateState.ReadyToInstall -> true
                        else -> false
                    }
                }

                if (showUpdateDialog) {
                    androidx.compose.ui.window.Dialog(onDismissRequest = { 
                        if (updateState !is com.example.update.UpdateState.Downloading) {
                            showUpdateDialog = false
                            viewModel.resetUpdateState()
                        }
                    }) {
                        com.example.ui.components.GlassCard(
                            borderColor = listOf(Color(0xFF00FFCC), Color(0xFFFF007F)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "N E W  U P D A T E  A V A I L A B L E",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF00FFCC),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                )

                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = "Update Available",
                                    tint = Color(0xFF00FFCC),
                                    modifier = Modifier.size(48.dp)
                                )

                                when (val state = updateState) {
                                    is com.example.update.UpdateState.UpdateAvailable -> {
                                        Column(
                                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "Version ${state.version} is ready",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = state.changelog,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray,
                                                maxLines = 5,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        val hasInstallPermission = viewModel.updateManager.canRequestPackageInstalls()

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            TextButton(
                                                onClick = {
                                                    viewModel.resetUpdateState()
                                                    showUpdateDialog = false
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("LATER", color = Color.Gray, fontWeight = FontWeight.Bold)
                                            }

                                            if (!hasInstallPermission) {
                                                Button(
                                                    onClick = { viewModel.updateManager.launchPackageInstallSettings() },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                                                    modifier = Modifier.weight(1.5f)
                                                ) {
                                                    Text("ENABLE INSTALL", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                Button(
                                                    onClick = { viewModel.downloadAndInstallUpdate(state.downloadUrl) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                                                    modifier = Modifier.weight(1.5f)
                                                ) {
                                                    Text("INSTALL NOW", color = Color(0xFF07080F), fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                    is com.example.update.UpdateState.Downloading -> {
                                        Column(
                                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Downloading Update: ${(state.progress * 100).toInt()}%",
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 13.sp
                                            )
                                            
                                            LinearProgressIndicator(
                                                progress = { state.progress },
                                                color = Color(0xFF00FFCC),
                                                trackColor = Color.DarkGray,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                            )
                                            
                                            Text(
                                                text = "Please do not leave the app during progress",
                                                color = Color.Gray,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    is com.example.update.UpdateState.ReadyToInstall -> {
                                        Column(
                                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Download Complete!",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Green
                                            )
                                            Text(
                                                text = "The update is ready to be loaded to key system nodes.",
                                                color = Color.Gray,
                                                fontSize = 11.sp,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                            
                                            Spacer(modifier = Modifier.height(8.dp))

                                            Button(
                                                onClick = { viewModel.installUpdate(state.apkFile) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("ENGAGE SYSTEM INSTALLER", color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color(0xFF0A0C16),
                            tonalElevation = 8.dp,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            listOf(
                                NavigationTabItem("home", "Dashboard", Icons.Default.Home),
                                NavigationTabItem("library", "Library", Icons.Default.SportsEsports),
                                NavigationTabItem("booster", "Booster", Icons.Default.Bolt),
                                NavigationTabItem("analytics", "Analytics", Icons.Default.Analytics),
                                NavigationTabItem("settings", "Settings", Icons.Default.Settings)
                            ).forEach { item ->
                                val isSelected = currentRoute == item.route
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        if (currentRoute != item.route) {
                                            navController.navigate(item.route) {
                                                popUpTo("home") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label,
                                            tint = if (isSelected) Color(0xFF00FFCC) else Color.Gray,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = item.label,
                                            fontSize = 10.sp,
                                            color = if (isSelected) Color.White else Color.Gray
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = Color(0x2200FFCC)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${item.route}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(Color(0xFF07080F))
                    ) {
                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToLibrary = { navController.navigate("library") },
                                onNavigateToBooster = { navController.navigate("booster") }
                            )
                        }
                        composable("library") {
                            GameLibraryScreen(viewModel = viewModel)
                        }
                        composable("booster") {
                            BoosterScreen(viewModel = viewModel)
                        }
                        composable("analytics") {
                            AnalyticsScreen(viewModel = viewModel)
                        }
                        composable("settings") {
                            SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

data class NavigationTabItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
