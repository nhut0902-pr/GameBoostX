package com.example.launcher.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM installed_games ORDER BY name ASC")
    fun getAllGames(): Flow<List<InstalledGame>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGames(games: List<InstalledGame>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: InstalledGame)

    @Query("UPDATE installed_games SET isFavorite = :favorite WHERE packageName = :packageName")
    suspend fun updateFavorite(packageName: String, favorite: Boolean)

    @Query("UPDATE installed_games SET lastPlayedTime = :timestamp WHERE packageName = :packageName")
    suspend fun updateLastPlayed(packageName: String, timestamp: Long)

    @Delete
    suspend fun deleteGame(game: InstalledGame)
}
