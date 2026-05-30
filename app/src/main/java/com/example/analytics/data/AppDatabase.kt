package com.example.analytics.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.launcher.data.GameDao
import com.example.launcher.data.InstalledGame

@Database(entities = [SessionRecord::class, InstalledGame::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val sessionDao: SessionDao
    abstract val gameDao: GameDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gameboostx_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
