package com.example.launcher.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "installed_games")
data class InstalledGame(
    @PrimaryKey val packageName: String,
    val name: String,
    val isFavorite: Boolean = false,
    val isManual: Boolean = false,
    val lastPlayedTime: Long = 0L
)
