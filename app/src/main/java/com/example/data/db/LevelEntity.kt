package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Difficulty
import com.example.data.model.LevelData
import com.example.data.model.TileType
import com.example.data.model.WinCondition

@Entity(tableName = "levels")
data class LevelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String,
    val width: Int,
    val height: Int,
    val gridData: String,
    val winCondition: String,
    val parMoves: Int = 20,
    val moveLimit: Int = 0,
    val difficulty: String = "MEDIUM",
    val hint: String = "",
    val isBuiltIn: Boolean = false,
    val isFavorite: Boolean = false,
    val bestMoves: Int? = null,
    val isCompleted: Boolean = false,
    val playCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toLevelData(): LevelData {
        val tiles = ArrayList<TileType>(width * height)
        for (ch in gridData) {
            tiles.add(TileType.fromCode(ch.toString()))
        }
        while (tiles.size < width * height) {
            tiles.add(TileType.EMPTY)
        }
        return LevelData(
            id = id,
            title = title,
            author = author,
            width = width,
            height = height,
            grid = tiles.take(width * height),
            winCondition = WinCondition.fromName(winCondition),
            parMoves = parMoves,
            moveLimit = moveLimit,
            difficulty = Difficulty.fromName(difficulty),
            hint = hint,
            isBuiltIn = isBuiltIn,
            isFavorite = isFavorite,
            bestMoves = bestMoves,
            isCompleted = isCompleted,
            playCount = playCount,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromLevelData(data: LevelData): LevelEntity {
            return LevelEntity(
                id = data.id,
                title = data.title,
                author = data.author,
                width = data.width,
                height = data.height,
                gridData = data.grid.joinToString(separator = "") { it.code },
                winCondition = data.winCondition.name,
                parMoves = data.parMoves,
                moveLimit = data.moveLimit,
                difficulty = data.difficulty.name,
                hint = data.hint,
                isBuiltIn = data.isBuiltIn,
                isFavorite = data.isFavorite,
                bestMoves = data.bestMoves,
                isCompleted = data.isCompleted,
                playCount = data.playCount,
                createdAt = data.createdAt
            )
        }
    }
}
