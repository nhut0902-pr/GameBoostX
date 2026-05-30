package com.example.analytics.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM session_records ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<SessionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionRecord): Long

    @Query("DELETE FROM session_records WHERE id = :id")
    suspend fun deleteSessionById(id: Int)

    @Query("DELETE FROM session_records")
    suspend fun clearAll()
}
