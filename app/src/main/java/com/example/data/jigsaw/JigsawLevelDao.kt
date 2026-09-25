package com.example.data.jigsaw

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface JigsawLevelDao {
    @Query("SELECT * FROM jigsaw_levels ORDER BY isBuiltIn DESC, createdAt DESC")
    fun getAllLevels(): Flow<List<JigsawLevelEntity>>

    @Query("SELECT * FROM jigsaw_levels WHERE isBuiltIn = 1 ORDER BY id ASC")
    fun getBuiltInLevels(): Flow<List<JigsawLevelEntity>>

    @Query("SELECT * FROM jigsaw_levels WHERE isBuiltIn = 0 ORDER BY createdAt DESC")
    fun getCustomLevels(): Flow<List<JigsawLevelEntity>>

    @Query("SELECT * FROM jigsaw_levels WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteLevels(): Flow<List<JigsawLevelEntity>>

    @Query("SELECT * FROM jigsaw_levels WHERE id = :id LIMIT 1")
    fun getLevelById(id: Long): Flow<JigsawLevelEntity?>

    @Query("SELECT * FROM jigsaw_levels WHERE id = :id LIMIT 1")
    suspend fun getLevelByIdOnce(id: Long): JigsawLevelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevel(level: JigsawLevelEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(levels: List<JigsawLevelEntity>)

    @Update
    suspend fun updateLevel(level: JigsawLevelEntity)

    @Delete
    suspend fun deleteLevel(level: JigsawLevelEntity)

    @Query("DELETE FROM jigsaw_levels WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM jigsaw_levels WHERE isBuiltIn = 1")
    suspend fun countBuiltInLevels(): Int

    @Query("UPDATE jigsaw_levels SET playCount = playCount + 1 WHERE id = :id")
    suspend fun incrementPlayCount(id: Long)

    @Query("UPDATE jigsaw_levels SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE jigsaw_levels SET isCompleted = 1, bestTimeSeconds = CASE WHEN bestTimeSeconds IS NULL OR :timeSec < bestTimeSeconds THEN :timeSec ELSE bestTimeSeconds END WHERE id = :id")
    suspend fun recordCompletion(id: Long, timeSec: Int)
}
