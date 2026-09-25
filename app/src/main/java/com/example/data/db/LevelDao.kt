package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelDao {
    @Query("SELECT * FROM levels ORDER BY isBuiltIn DESC, createdAt DESC")
    fun getAllLevels(): Flow<List<LevelEntity>>

    @Query("SELECT * FROM levels WHERE isBuiltIn = 1 ORDER BY id ASC")
    fun getBuiltInLevels(): Flow<List<LevelEntity>>

    @Query("SELECT * FROM levels WHERE isBuiltIn = 0 ORDER BY createdAt DESC")
    fun getCustomLevels(): Flow<List<LevelEntity>>

    @Query("SELECT * FROM levels WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteLevels(): Flow<List<LevelEntity>>

    @Query("SELECT * FROM levels WHERE id = :id LIMIT 1")
    fun getLevelById(id: Long): Flow<LevelEntity?>

    @Query("SELECT * FROM levels WHERE id = :id LIMIT 1")
    suspend fun getLevelByIdOnce(id: Long): LevelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevel(level: LevelEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(levels: List<LevelEntity>)

    @Update
    suspend fun updateLevel(level: LevelEntity)

    @Delete
    suspend fun deleteLevel(level: LevelEntity)

    @Query("DELETE FROM levels WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM levels WHERE isBuiltIn = 1")
    suspend fun countBuiltInLevels(): Int

    @Query("UPDATE levels SET playCount = playCount + 1 WHERE id = :id")
    suspend fun incrementPlayCount(id: Long)

    @Query("UPDATE levels SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE levels SET isCompleted = 1, bestMoves = CASE WHEN bestMoves IS NULL OR :moves < bestMoves THEN :moves ELSE bestMoves END WHERE id = :id")
    suspend fun recordCompletion(id: Long, moves: Int)
}
