package com.example.data.jigsaw

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Difficulty

@Entity(tableName = "jigsaw_levels")
data class JigsawLevelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String,
    val imageKey: String,
    val customImageUri: String? = null,
    val rows: Int = 3,
    val cols: Int = 3,
    val edgeSeed: Long = 12345L,
    val cutStyle: String = "CLASSIC_JIGSAW",
    val winCondition: String = "STANDARD",
    val timeLimitSeconds: Int = 180,
    val parTimeSeconds: Int = 90,
    val maxMoves: Int = 30,
    val rotatePieces: Boolean = false,
    val anchorPiecesCount: Int = 0,
    val difficulty: String = "MEDIUM",
    val hint: String = "",
    val isBuiltIn: Boolean = false,
    val isFavorite: Boolean = false,
    val bestTimeSeconds: Int? = null,
    val isCompleted: Boolean = false,
    val playCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toJigsawLevel(): JigsawLevel {
        return JigsawLevel(
            id = id,
            title = title,
            author = author,
            imageKey = imageKey,
            customImageUri = customImageUri,
            rows = rows,
            cols = cols,
            edgeSeed = edgeSeed,
            cutStyle = runCatching { JigsawCutStyle.valueOf(cutStyle) }.getOrDefault(JigsawCutStyle.CLASSIC_JIGSAW),
            winCondition = runCatching { JigsawWinCondition.valueOf(winCondition) }.getOrDefault(JigsawWinCondition.STANDARD),
            timeLimitSeconds = timeLimitSeconds,
            parTimeSeconds = parTimeSeconds,
            maxMoves = maxMoves,
            rotatePieces = rotatePieces,
            anchorPiecesCount = anchorPiecesCount,
            difficulty = Difficulty.fromName(difficulty),
            hint = hint,
            isBuiltIn = isBuiltIn,
            isFavorite = isFavorite,
            bestTimeSeconds = bestTimeSeconds,
            isCompleted = isCompleted,
            playCount = playCount,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromJigsawLevel(l: JigsawLevel): JigsawLevelEntity {
            return JigsawLevelEntity(
                id = l.id,
                title = l.title,
                author = l.author,
                imageKey = l.imageKey,
                customImageUri = l.customImageUri,
                rows = l.rows,
                cols = l.cols,
                edgeSeed = l.edgeSeed,
                cutStyle = l.cutStyle.name,
                winCondition = l.winCondition.name,
                timeLimitSeconds = l.timeLimitSeconds,
                parTimeSeconds = l.parTimeSeconds,
                maxMoves = l.maxMoves,
                rotatePieces = l.rotatePieces,
                anchorPiecesCount = l.anchorPiecesCount,
                difficulty = l.difficulty.name,
                hint = l.hint,
                isBuiltIn = l.isBuiltIn,
                isFavorite = l.isFavorite,
                bestTimeSeconds = l.bestTimeSeconds,
                isCompleted = l.isCompleted,
                playCount = l.playCount,
                createdAt = l.createdAt
            )
        }
    }
}
