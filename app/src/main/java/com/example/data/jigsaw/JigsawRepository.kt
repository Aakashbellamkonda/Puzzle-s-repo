package com.example.data.jigsaw

import com.example.data.model.Difficulty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class JigsawRepository(private val dao: JigsawLevelDao) {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedBuiltInPuzzlesIfEmpty()
        }
    }

    private suspend fun seedBuiltInPuzzlesIfEmpty() {
        val count = dao.countBuiltInLevels()
        if (count == 0) {
            val builtIns = listOf(
                JigsawLevel(
                    title = "01: Cosmic Voyage",
                    author = "Studio",
                    imageKey = "cosmic_nebula",
                    rows = 2,
                    cols = 2,
                    edgeSeed = 101L,
                    winCondition = JigsawWinCondition.STANDARD,
                    parTimeSeconds = 30,
                    difficulty = Difficulty.EASY,
                    hint = "A gentle 4-piece starter puzzle showcasing the ringed celestial planet.",
                    isBuiltIn = true
                ),
                JigsawLevel(
                    title = "02: Sunset Peaks",
                    author = "Studio",
                    imageKey = "sunset_mountains",
                    rows = 3,
                    cols = 3,
                    edgeSeed = 202L,
                    winCondition = JigsawWinCondition.STANDARD,
                    parTimeSeconds = 60,
                    difficulty = Difficulty.EASY,
                    hint = "Assemble the warm sunset sky first, followed by the mountain ridges.",
                    isBuiltIn = true
                ),
                JigsawLevel(
                    title = "03: Cyber Neon Grid",
                    author = "Studio",
                    imageKey = "cyber_city",
                    rows = 3,
                    cols = 3,
                    edgeSeed = 303L,
                    rotatePieces = true,
                    winCondition = JigsawWinCondition.STANDARD,
                    parTimeSeconds = 90,
                    difficulty = Difficulty.MEDIUM,
                    hint = "Pieces can be rotated! Tap any piece in the tray or board to rotate it 90°.",
                    isBuiltIn = true
                ),
                JigsawLevel(
                    title = "04: Enchanted Treehouse",
                    author = "Studio",
                    imageKey = "forest_magic",
                    rows = 4,
                    cols = 3,
                    edgeSeed = 404L,
                    winCondition = JigsawWinCondition.STANDARD,
                    parTimeSeconds = 120,
                    difficulty = Difficulty.MEDIUM,
                    hint = "12 interlocking pieces with glowing mushrooms and fairy lights.",
                    isBuiltIn = true
                ),
                JigsawLevel(
                    title = "05: Golden Lotus Mandala",
                    author = "Studio",
                    imageKey = "geometric_mandala",
                    rows = 4,
                    cols = 4,
                    edgeSeed = 505L,
                    winCondition = JigsawWinCondition.TIME_ATTACK,
                    timeLimitSeconds = 150,
                    parTimeSeconds = 100,
                    difficulty = Difficulty.HARD,
                    hint = "Time Attack! Complete the intricate 16-piece sacred geometry before the timer runs out.",
                    isBuiltIn = true
                ),
                JigsawLevel(
                    title = "06: Coral Kingdom",
                    author = "Studio",
                    imageKey = "tropical_reef",
                    rows = 4,
                    cols = 4,
                    edgeSeed = 606L,
                    winCondition = JigsawWinCondition.EDGES_FIRST,
                    parTimeSeconds = 120,
                    difficulty = Difficulty.MASTER,
                    hint = "Edges First! You must assemble all 12 outer border pieces before interior pieces unlock.",
                    isBuiltIn = true
                )
            )
            dao.insertAll(builtIns.map { JigsawLevelEntity.fromJigsawLevel(it) })
        }
    }

    val allLevels: Flow<List<JigsawLevel>> = dao.getAllLevels().map { list ->
        list.map { it.toJigsawLevel() }
    }

    val builtInLevels: Flow<List<JigsawLevel>> = dao.getBuiltInLevels().map { list ->
        list.map { it.toJigsawLevel() }
    }

    val customLevels: Flow<List<JigsawLevel>> = dao.getCustomLevels().map { list ->
        list.map { it.toJigsawLevel() }
    }

    val favoriteLevels: Flow<List<JigsawLevel>> = dao.getFavoriteLevels().map { list ->
        list.map { it.toJigsawLevel() }
    }

    suspend fun getLevelOnce(id: Long): JigsawLevel? {
        return dao.getLevelByIdOnce(id)?.toJigsawLevel()
    }

    suspend fun saveLevel(level: JigsawLevel): Long {
        return dao.insertLevel(JigsawLevelEntity.fromJigsawLevel(level))
    }

    suspend fun deleteLevel(id: Long) {
        dao.deleteById(id)
    }

    suspend fun duplicateLevel(id: Long): Long? {
        val original = dao.getLevelByIdOnce(id) ?: return null
        val copy = original.copy(
            id = 0,
            title = "${original.title} (Copy)",
            isBuiltIn = false,
            bestTimeSeconds = null,
            isCompleted = false,
            playCount = 0,
            createdAt = System.currentTimeMillis()
        )
        return dao.insertLevel(copy)
    }

    suspend fun toggleFavorite(id: Long, isFav: Boolean) {
        dao.setFavorite(id, isFav)
    }

    suspend fun recordCompletion(id: Long, timeSec: Int) {
        dao.recordCompletion(id, timeSec)
    }

    suspend fun incrementPlayCount(id: Long) {
        dao.incrementPlayCount(id)
    }
}
