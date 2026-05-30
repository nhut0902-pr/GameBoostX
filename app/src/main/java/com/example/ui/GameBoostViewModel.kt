package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.analytics.data.AppDatabase
import com.example.analytics.data.SessionRecord
import com.example.analytics.domain.SessionRepository
import com.example.booster.domain.BoostStats
import com.example.booster.domain.GamingModeManager
import com.example.booster.domain.GamingProfile
import com.example.booster.domain.MemoryBooster
import com.example.launcher.data.InstalledGame
import com.example.launcher.domain.GameRepository
import com.example.monitoring.domain.HardwareMetrics
import com.example.monitoring.domain.MonitoringService
import com.example.update.UpdateManager
import com.example.update.UpdateState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GameBoostViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    
    // Core Managers & Repositories
    private val database = AppDatabase.getDatabase(context)
    private val sessionRepository = SessionRepository(database.sessionDao)
    private val gameRepository = GameRepository(context, database.gameDao)
    private val booster = MemoryBooster(context)
    private val gamingModeManager = GamingModeManager(context)

    // GitHub Update Manager
    val updateManager = UpdateManager(context)
    val updateState: StateFlow<UpdateState> = updateManager.updateState

    // Observable states fetched from repositories
    val allGames: StateFlow<List<InstalledGame>> = gameRepository.allGames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessions: StateFlow<List<SessionRecord>> = sessionRepository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Game selection list for manual addition
    private val _installableApps = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val installableApps: StateFlow<List<Pair<String, String>>> = _installableApps

    // Subscribed telemetry state from the service
    val liveMetrics: StateFlow<HardwareMetrics> = MonitoringService.metricsState
    val isServiceRunning: StateFlow<Boolean> = MonitoringService.isServiceRunning
    val isOverlayShowing: StateFlow<Boolean> = MonitoringService.isOverlayShowing

    // Manual/Gaming mode properties mapped from their managers
    val activeProfile: StateFlow<GamingProfile> = GamingModeManager.activeProfile
    val isDndEnabled: StateFlow<Boolean> = GamingModeManager.isDndEnabled
    val isScreenAwakeEnabled: StateFlow<Boolean> = GamingModeManager.isScreenAwakeEnabled
    val isBrightnessLocked: StateFlow<Boolean> = GamingModeManager.isBrightnessLocked
    val lockedBrightnessValue: StateFlow<Float> = GamingModeManager.lockedBrightnessValue

    // Live Booster trigger states
    private val _lastBoostStats = MutableStateFlow<BoostStats?>(null)
    val lastBoostStats: StateFlow<BoostStats?> = _lastBoostStats

    private val _isBoosting = MutableStateFlow(false)
    val isBoosting: StateFlow<Boolean> = _isBoosting

    // Navigation search states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedFilter = MutableStateFlow("ALL") // ALL, FAVORITES, SCANNED, MANUAL
    val selectedFilter: StateFlow<String> = _selectedFilter

    // Local buffers to plot historic lines on Home Screens
    private val _fpsHistoryList = MutableStateFlow<List<Float>>(emptyList())
    val fpsHistoryList: StateFlow<List<Float>> = _fpsHistoryList

    private val _cpuHistoryList = MutableStateFlow<List<Float>>(emptyList())
    val cpuHistoryList: StateFlow<List<Float>> = _cpuHistoryList

    private val _ramHistoryList = MutableStateFlow<List<Float>>(emptyList())
    val ramHistoryList: StateFlow<List<Float>> = _ramHistoryList

    init {
        // Initial auto-scans & Update Check
        viewModelScope.launch {
            gameRepository.scanGames()
            loadInstallableApps()
            if (updateManager.isAutoCheckEnabled()) {
                updateManager.checkForUpdates()
            }
        }

        // Periodically track some history buffers locally in ViewModel for real-time Home charts
        viewModelScope.launch {
            liveMetrics.collect { metrics ->
                // FPS History
                val fList = _fpsHistoryList.value.toMutableList()
                fList.add(metrics.fps.toFloat())
                if (fList.size > 24) fList.removeAt(0)
                _fpsHistoryList.value = fList

                // CPU History
                val cList = _cpuHistoryList.value.toMutableList()
                cList.add(metrics.cpuUsage)
                if (cList.size > 24) cList.removeAt(0)
                _cpuHistoryList.value = cList

                // RAM History
                val rList = _ramHistoryList.value.toMutableList()
                rList.add(metrics.ramUsedMb)
                if (rList.size > 24) rList.removeAt(0)
                _ramHistoryList.value = rList
            }
        }
    }

    // --- Actions ---

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun loadInstallableApps() {
        viewModelScope.launch {
            _installableApps.value = gameRepository.getInstalledAppsForSelection()
        }
    }

    fun scanGames() {
        viewModelScope.launch {
            gameRepository.scanGames()
            loadInstallableApps()
        }
    }

    fun toggleFavorite(packageName: String, isFavorite: Boolean) {
        viewModelScope.launch {
            gameRepository.updateFavorite(packageName, isFavorite)
        }
    }

    fun addManualGame(packageName: String, name: String) {
        viewModelScope.launch {
            gameRepository.addManualGame(packageName, name)
        }
    }

    fun removeDeletedGame(game: InstalledGame) {
        viewModelScope.launch {
            gameRepository.removeGame(game)
        }
    }

    // Launch Game session with Monitoring service tracking
    fun launchGame(installedGame: InstalledGame): Boolean {
        viewModelScope.launch {
            gameRepository.updateLastPlayed(installedGame.packageName)
        }

        // Trigger start of monitoring service associated with game
        val startServiceIntent = Intent(context, MonitoringService::class.java).apply {
            action = MonitoringService.ACTION_START
            putExtra("GAME_PACKAGE", installedGame.packageName)
            putExtra("GAME_NAME", installedGame.name)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(startServiceIntent)
        } else {
            context.startService(startServiceIntent)
        }

        // Launch game activity
        return gameRepository.launchGame(installedGame.packageName)
    }

    fun stopSessionAndTelemetry() {
        val stopIntent = Intent(context, MonitoringService::class.java).apply {
            action = MonitoringService.ACTION_STOP
        }
        context.startService(stopIntent)
    }

    fun toggleOverlayWidget() {
        val intent = Intent(context, MonitoringService::class.java).apply {
            action = MonitoringService.ACTION_TOGGLE_OVERLAY
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun performOneTapBoost() {
        viewModelScope.launch {
            _isBoosting.value = true
            val stats = booster.performBoost()
            _lastBoostStats.value = stats
            _isBoosting.value = false
        }
    }

    // --- Optimization Settings mapping ---

    fun setGamingProfile(profile: GamingProfile) {
        gamingModeManager.setProfile(profile)
    }

    fun toggleScreenAwake(enabled: Boolean) {
        gamingModeManager.toggleScreenAwake(enabled)
    }

    fun toggleBrightnessLock(enabled: Boolean, value: Float = 0.7f) {
        gamingModeManager.toggleBrightnessLock(enabled, value)
    }

    fun toggleDndMode(enabled: Boolean): Boolean {
        return gamingModeManager.toggleDnd(enabled)
    }

    fun hasDndPermission(): Boolean {
        return gamingModeManager.hasDndPermission()
    }

    fun applyLocalWindowOptimizations(window: android.view.Window) {
        gamingModeManager.applyWindowOptimizations(window)
    }

    // Clear analytics history
    fun clearAnalytics() {
        viewModelScope.launch {
            sessionRepository.clearSessions()
        }
    }

    fun deleteSessionRecord(id: Int) {
        viewModelScope.launch {
            sessionRepository.deleteSession(id)
        }
    }

    fun getAppIcon(packageName: String) = gameRepository.getAppIcon(packageName)

    // --- GitHub Updates Actions ---
    fun checkForUpdates() {
        viewModelScope.launch {
            updateManager.checkForUpdates()
        }
    }

    fun downloadAndInstallUpdate(url: String) {
        viewModelScope.launch {
            updateManager.downloadAndInstallApk(url)
        }
    }

    fun installUpdate(file: java.io.File) {
        updateManager.triggerInstall(file)
    }

    fun saveGitHubSettings(owner: String, repo: String, autoCheck: Boolean) {
        updateManager.saveGitHubSettings(owner, repo, autoCheck)
    }

    fun resetUpdateState() {
        updateManager.resetIdle()
    }
}
