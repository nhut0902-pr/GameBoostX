package com.example.analytics.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_records")
data class SessionRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gameName: String,
    val packageName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSecs: Long = 0,
    val avgFps: Int = 0,
    val peakFps: Int = 0,
    val minFps: Int = 0,
    val avgCpu: Float = 0f,
    val avgRam: Float = 0f,
    val avgTemp: Float = 0f,
    val avgPing: Int = 0,
    // Store timeline data as comma-separated strings for easy Room serialization without overhead
    val fpsHistory: String = "",
    val cpuHistory: String = "",
    val ramHistory: String = "",
    val tempHistory: String = "",
    val pingHistory: String = ""
)
