package com.example.booster.domain

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.Window
import android.view.WindowManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class GamingProfile {
    PERFORMANCE,
    BALANCED,
    BATTERY_SAVER
}

class GamingModeManager(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private val _activeProfile = MutableStateFlow(GamingProfile.BALANCED)
        val activeProfile: StateFlow<GamingProfile> = _activeProfile

        private val _isDndEnabled = MutableStateFlow(false)
        val isDndEnabled: StateFlow<Boolean> = _isDndEnabled

        private val _isScreenAwakeEnabled = MutableStateFlow(true)
        val isScreenAwakeEnabled: StateFlow<Boolean> = _isScreenAwakeEnabled

        private val _isBrightnessLocked = MutableStateFlow(false)
        val isBrightnessLocked: StateFlow<Boolean> = _isBrightnessLocked

        private val _lockedBrightnessValue = MutableStateFlow(0.7f) // 70% default lock value
        val lockedBrightnessValue: StateFlow<Float> = _lockedBrightnessValue
    }

    fun setProfile(profile: GamingProfile) {
        _activeProfile.value = profile
    }

    fun toggleScreenAwake(enabled: Boolean) {
        _isScreenAwakeEnabled.value = enabled
    }

    fun toggleBrightnessLock(enabled: Boolean, value: Float = 0.7f) {
        _isBrightnessLocked.value = enabled
        _lockedBrightnessValue.value = value
    }

    fun hasDndPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.isNotificationPolicyAccessGranted
        } else {
            true
        }
    }

    fun toggleDnd(enable: Boolean): Boolean {
        if (!hasDndPermission()) return false

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (enable) {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                    _isDndEnabled.value = true
                } else {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                    _isDndEnabled.value = false
                }
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    // Applies local window-level optimizations (Screen Awake keeper & Brightness locking)
    // without requiring dangerous global WRITE_SETTINGS system policy permissions.
    fun applyWindowOptimizations(window: Window) {
        // 1. Keep Screen Awake
        if (_isScreenAwakeEnabled.value) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        // 2. Lock screen brightness locally
        if (_isBrightnessLocked.value) {
            val lp = window.attributes
            lp.screenBrightness = _lockedBrightnessValue.value
            window.attributes = lp
        } else {
            val lp = window.attributes
            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            window.attributes = lp
        }
    }
}
