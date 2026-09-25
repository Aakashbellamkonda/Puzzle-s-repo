package com.example.data.jigsaw

import com.example.data.model.Difficulty

enum class EdgeType {
    FLAT,
    TAB_OUT,
    TAB_IN
}

data class PieceEdges(
    val top: EdgeType = EdgeType.FLAT,
    val right: EdgeType = EdgeType.FLAT,
    val bottom: EdgeType = EdgeType.FLAT,
    val left: EdgeType = EdgeType.FLAT
)

enum class JigsawWinCondition(val displayName: String, val description: String) {
    STANDARD("Complete Assembly", "Fit all pieces into the correct positions"),
    TIME_ATTACK("Time Attack", "Assemble the entire puzzle before the timer expires"),
    MOVE_LIMIT("Move Efficiency", "Assemble within a maximum piece-placement limit"),
    EDGES_FIRST("Border First", "Connect all edge pieces before interior pieces unlock")
}

enum class JigsawCutStyle(val displayName: String) {
    CLASSIC_JIGSAW("Classic Interlocking"),
    GEOMETRIC("Geometric Grid")
}

data class JigsawPiece(
    val id: Int,
    val row: Int,
    val col: Int,
    val edges: PieceEdges,
    var currentRotation: Int = 0, // 0, 90, 180, 270
    var isPlaced: Boolean = false,
    val isEdge: Boolean = (edges.top == EdgeType.FLAT || edges.right == EdgeType.FLAT || edges.bottom == EdgeType.FLAT || edges.left == EdgeType.FLAT),
    var isLockedAnchor: Boolean = false
)

data class JigsawLevel(
    val id: Long = 0,
    val title: String = "Untitled Jigsaw",
    val author: String = "Designer",
    val imageKey: String = "cosmic_nebula",
    val customImageUri: String? = null,
    val rows: Int = 3,
    val cols: Int = 3,
    val edgeSeed: Long = 12345L,
    val cutStyle: JigsawCutStyle = JigsawCutStyle.CLASSIC_JIGSAW,
    val winCondition: JigsawWinCondition = JigsawWinCondition.STANDARD,
    val timeLimitSeconds: Int = 180,
    val parTimeSeconds: Int = 90,
    val maxMoves: Int = 30,
    val rotatePieces: Boolean = false,
    val anchorPiecesCount: Int = 0,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val hint: String = "",
    val isBuiltIn: Boolean = false,
    val isFavorite: Boolean = false,
    val bestTimeSeconds: Int? = null,
    val isCompleted: Boolean = false,
    val playCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalPieces: Int
        get() = rows * cols
}
