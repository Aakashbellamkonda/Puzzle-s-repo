package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LevelEntity::class], version = 1, exportSchema = false)
abstract class PuzzleDatabase : RoomDatabase() {
    abstract fun levelDao(): LevelDao

    companion object {
        @Volatile
        private var INSTANCE: PuzzleDatabase? = null

        fun getDatabase(context: Context): PuzzleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PuzzleDatabase::class.java,
                    "puzzle_craft_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
