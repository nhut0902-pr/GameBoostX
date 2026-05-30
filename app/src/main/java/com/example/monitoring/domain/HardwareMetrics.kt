package com.example.monitoring.domain

data class HardwareMetrics(
    val fps: Int = 60,
    val avgFps: Int = 60,
    val lowestFps: Int = 60,
    val highestFps: Int = 60,
    val ramUsedMb: Float = 0f,
    val ramTotalMb: Float = 0f,
    val ramFreeMb: Float = 0f,
    val cpuUsage: Float = 0f,
    val cpuFreqGhz: Float = 0f,
    val cpuCoreUsage: List<Float> = emptyList(),
    val batteryTemp: Float = 0f,
    val batteryPercent: Int = 100,
    val netPingMs: Int = 0,
    val downloadSpeedKbps: Float = 0f,
    val uploadSpeedKbps: Float = 0f,
    val packetLossPercent: Float = 0f,
    val thermalStatus: String = "NORMAL",
    val isThermalWarning: Boolean = false
)
