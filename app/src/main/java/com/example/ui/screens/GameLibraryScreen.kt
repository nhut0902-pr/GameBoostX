package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.launcher.data.InstalledGame
import com.example.ui.GameBoostViewModel
import com.example.ui.components.GlassCard
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameLibraryScreen(
    viewModel: GameBoostViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val games by viewModel.allGames.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val availableAppPackages by viewModel.installableApps.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var appsSearchQuery by remember { mutableStateOf("") }

    // Filter games list
    val filteredGames = remember(games, searchQuery, selectedFilter) {
        games.filter { game ->
            val matchesSearch = game.name.contains(searchQuery, ignoreCase = true) ||
                    game.packageName.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "FAVORITES" -> game.isFavorite
                "MANUAL" -> game.isManual
                "SCANNED" -> !game.isManual
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    val favorites = remember(filteredGames) { filteredGames.filter { it.isFavorite } }
    val others = remember(filteredGames) { filteredGames.filter { !it.isFavorite } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07080F))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Welcome and manual add buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "A C C E L E R A T O R",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00FFCC),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Games Library",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }

            IconButton(
                onClick = {
                    viewModel.loadInstallableApps()
                    showAddDialog = true
                },
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0x1F00FFCC), RoundedCornerShape(8.dp))
                    .testTag("add_game_dialog_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Game Manually",
                    tint = Color(0xFF00FFCC)
                )
            }
        }

        // Search Bar Setup
        TextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search package or game title...", color = Color.Gray, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0x3310121D),
                unfocusedContainerColor = Color(0x1A10121D),
                disabledContainerColor = Color(0x1A10121D),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF00FFCC),
                focusedIndicatorColor = Color(0xFF00FFCC)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("library_search_input")
        )

        // Horizontal filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "ALL" to "ALL",
                "FAVORITES" to "FAVORITES",
                "SCANNED" to "DETECTED",
                "MANUAL" to "MANUAL ADD"
            ).forEach { (filterType, label) ->
                val isSelected = selectedFilter == filterType
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setFilter(filterType) },
                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (filterType == "FAVORITES") Color(0x33FF007F) else Color(0x3300FFCC),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0x1A10111A),
                        labelColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = Color(0x33FFFFFF),
                        selectedBorderColor = if (filterType == "FAVORITES") Color(0xFFFF007F) else Color(0xFF00FFCC),
                        enabled = true,
                        selected = isSelected
                    ),
                    modifier = Modifier.weight(1f).testTag("filter_chip_$filterType")
                )
            }
        }

        // Empty library visual card
        if (filteredGames.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(
                    borderColor = listOf(Color(0xFF323545), Color(0xFF1D1F29)),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideogameAsset,
                            contentDescription = "Empty Library",
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "No games matched criteria",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Tap the '+' icon on the top right to manually map and accelerate any system utilities as virtual targets.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        } else {
            // Games Grid display
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredGames, key = { it.packageName }) { game ->
                    GameGridItemCard(
                        game = game,
                        iconProvider = { viewModel.getAppIcon(game.packageName) },
                        onLaunch = { viewModel.launchGame(game) },
                        onToggleFavorite = { viewModel.toggleFavorite(game.packageName, !game.isFavorite) },
                        onDelete = if (game.isManual) { { viewModel.removeDeletedGame(game) } } else null
                    )
                }
            }
        }
    }

    // Modal dialog to add packages manually
    if (showAddDialog) {
        val filteredApps = remember(availableAppPackages, appsSearchQuery) {
            availableAppPackages.filter {
                it.second.contains(appsSearchQuery, ignoreCase = true) ||
                        it.first.contains(appsSearchQuery, ignoreCase = true)
            }
        }

        Dialog(onDismissRequest = { showAddDialog = false }) {
            GlassCard(
                borderColor = listOf(Color(0xFF00FFCC), Color(0xFFFF007F)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Add Application Target",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = { showAddDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                        }
                    }

                    TextField(
                        value = appsSearchQuery,
                        onValueChange = { appsSearchQuery = it },
                        placeholder = { Text("Search installed packages...", color = Color.Gray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0x3310121D),
                            unfocusedContainerColor = Color(0x1A10121D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF00FFCC)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("add_dialog_search")
                    )

                    Divider(color = Color(0x33FFFFFF))

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        if (filteredApps.isEmpty()) {
                            Text(
                                "No matches found on device",
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            androidx.compose.foundation.lazy.LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(filteredApps) { (packageName, label) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.addManualGame(packageName, label)
                                                showAddDialog = false
                                                appsSearchQuery = ""
                                            }
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Retrieve and render icon safely in lists
                                        val drawableIcon = remember(packageName) { viewModel.getAppIcon(packageName) }
                                        DrawableImage(
                                            drawable = drawableIcon,
                                            contentDescription = label,
                                            modifier = Modifier.size(32.dp)
                                        )

                                        Column {
                                            Text(label, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                            Text(packageName, color = Color.Gray, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameGridItemCard(
    game: InstalledGame,
    iconProvider: () -> Drawable,
    onLaunch: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appIconDrawable = remember(game.packageName) { iconProvider() }

    GlassCard(
        borderColor = if (game.isFavorite) {
            listOf(Color(0xFFFF007F), Color(0xFFFF007F).copy(alpha = 0.2f))
        } else {
            listOf(Color(0xFF32364B), Color(0xFF1B1D28))
        },
        modifier = modifier
            .fillMaxWidth()
            .testTag("game_card_${game.packageName}")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Toolbar icons (Favorite & Manual delete triggers)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onDelete != null) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove Game",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(24.dp))
                }

                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (game.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Toggle Favorite",
                        tint = if (game.isFavorite) Color(0xFFFF007F) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Game Icon
            DrawableImage(
                drawable = appIconDrawable,
                contentDescription = game.name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x33FFFFFF))
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Game metadata
            Text(
                text = game.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 4.dp).testTag("game_name_${game.packageName}")
            )

            val lpString = remember(game.lastPlayedTime) {
                if (game.lastPlayedTime == 0L) {
                    "Never optimized"
                } else {
                    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                    "Played: " + sdf.format(Date(game.lastPlayedTime))
                }
            }
            Text(
                text = lpString,
                fontSize = 10.sp,
                color = Color.Gray,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Launcher trigger button
            Button(
                onClick = onLaunch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (game.isFavorite) Color(0xFFFF007F) else Color(0xFF00FFCC)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .testTag("launch_game_button_${game.packageName}"),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "ACCELERATE",
                    color = if (game.isFavorite) Color.White else Color(0xFF07080F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// Convert Android Drawable to Bitmap for loading inside Jetpack Compose Image
@Composable
fun DrawableImage(
    drawable: Drawable,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(drawable) {
        if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            // Fallback drawing vector to custom size bitmap
            val width = drawable.intrinsicWidth.coerceAtLeast(100)
            val height = drawable.intrinsicHeight.coerceAtLeast(100)
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bmp)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bmp
        }
    }

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}
