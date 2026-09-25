package com.example.data.db

import com.example.data.codec.LevelShareCodec
import com.example.data.model.LevelData
import com.example.data.sample.BuiltInLevels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class LevelRepository(private val dao: LevelDao) {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedBuiltInLevelsIfEmpty()
        }
    }

    private suspend fun seedBuiltInLevelsIfEmpty() {
        val count = dao.countBuiltInLevels()
        if (count == 0) {
            val entities = BuiltInLevels.getList().map { LevelEntity.fromLevelData(it) }
            dao.insertAll(entities)
        }
    }

    val allLevels: Flow<List<LevelData>> = dao.getAllLevels().map { list ->
        list.map { it.toLevelData() }
    }

    val builtInLevels: Flow<List<LevelData>> = dao.getBuiltInLevels().map { list ->
        list.map { it.toLevelData() }
    }

    val customLevels: Flow<List<LevelData>> = dao.getCustomLevels().map { list ->
        list.map { it.toLevelData() }
    }

    val favoriteLevels: Flow<List<LevelData>> = dao.getFavoriteLevels().map { list ->
        list.map { it.toLevelData() }
    }

    fun getLevel(id: Long): Flow<LevelData?> = dao.getLevelById(id).map { entity ->
        entity?.toLevelData()
    }

    suspend fun getLevelOnce(id: Long): LevelData? {
        return dao.getLevelByIdOnce(id)?.toLevelData()
    }

    suspend fun saveLevel(level: LevelData): Long {
        val entity = LevelEntity.fromLevelData(level)
        return dao.insertLevel(entity)
    }

    suspend fun deleteLevel(id: Long) {
        dao.deleteById(id)
    }

    suspend fun duplicateLevel(id: Long): Long? {
        val original = dao.getLevelByIdOnce(id) ?: return null
        val copyEntity = original.copy(
            id = 0,
            title = "${original.title} (Copy)",
            isBuiltIn = false,
            bestMoves = null,
            isCompleted = false,
            playCount = 0,
            createdAt = System.currentTimeMillis()
        )
        return dao.insertLevel(copyEntity)
    }

    suspend fun toggleFavorite(id: Long, isFav: Boolean) {
        dao.setFavorite(id, isFav)
    }

    suspend fun recordCompletion(id: Long, moves: Int) {
        dao.recordCompletion(id, moves)
    }

    suspend fun incrementPlayCount(id: Long) {
        dao.incrementPlayCount(id)
    }

    suspend fun importLevelFromCode(code: String): Result<LevelData> {
        return LevelShareCodec.decode(code).mapCatching { decoded ->
            val toSave = decoded.copy(id = 0, isBuiltIn = false, createdAt = System.currentTimeMillis())
            val newId = dao.insertLevel(LevelEntity.fromLevelData(toSave))
            toSave.copy(id = newId)
        }
    }
}
