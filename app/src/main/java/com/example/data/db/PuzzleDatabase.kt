package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.jigsaw.JigsawLevelDao
import com.example.data.jigsaw.JigsawLevelEntity

@Database(entities = [LevelEntity::class, JigsawLevelEntity::class], version = 2, exportSchema = false)
abstract class PuzzleDatabase : RoomDatabase() {
    abstract fun levelDao(): LevelDao
    abstract fun jigsawLevelDao(): JigsawLevelDao

    companion object {
        @Volatile
        private var INSTANCE: PuzzleDatabase? = null

        fun getDatabase(context: Context): PuzzleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PuzzleDatabase::class.java,
                    "puzzle_craft_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
