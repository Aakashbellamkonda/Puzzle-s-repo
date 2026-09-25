package com.example

import com.example.data.jigsaw.EdgeType
import com.example.data.jigsaw.JigsawCodec
import com.example.data.jigsaw.JigsawCutStyle
import com.example.data.jigsaw.JigsawLevel
import com.example.data.jigsaw.JigsawPathGenerator
import com.example.data.jigsaw.JigsawWinCondition
import com.example.data.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testJigsawEdgeGenerationInterlocking() {
        val rows = 3
        val cols = 4
        val edges = JigsawPathGenerator.generatePieceEdges(rows, cols, seed = 42L)

        assertEquals(rows * cols, edges.size)

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val idx = r * cols + c
                val piece = edges[idx]

                // Outer border boundaries must be FLAT
                if (r == 0) assertEquals(EdgeType.FLAT, piece.top)
                if (r == rows - 1) assertEquals(EdgeType.FLAT, piece.bottom)
                if (c == 0) assertEquals(EdgeType.FLAT, piece.left)
                if (c == cols - 1) assertEquals(EdgeType.FLAT, piece.right)

                // Internal horizontal connections must mate (one TAB_OUT, one TAB_IN)
                if (r < rows - 1) {
                    val bottomPiece = edges[(r + 1) * cols + c]
                    val mates = (piece.bottom == EdgeType.TAB_OUT && bottomPiece.top == EdgeType.TAB_IN) ||
                        (piece.bottom == EdgeType.TAB_IN && bottomPiece.top == EdgeType.TAB_OUT)
                    assertTrue("Vertical adjacent pieces must interlock", mates)
                }

                // Internal vertical connections must mate
                if (c < cols - 1) {
                    val rightPiece = edges[r * cols + (c + 1)]
                    val mates = (piece.right == EdgeType.TAB_OUT && rightPiece.left == EdgeType.TAB_IN) ||
                        (piece.right == EdgeType.TAB_IN && rightPiece.left == EdgeType.TAB_OUT)
                    assertTrue("Horizontal adjacent pieces must interlock", mates)
                }
            }
        }
    }

    @Test
    fun testJigsawCodecRoundTrip() {
        val original = JigsawLevel(
            title = "Enchanted Waterfall",
            author = "Marina",
            imageKey = "sunset_mountains",
            rows = 4,
            cols = 4,
            edgeSeed = 999L,
            cutStyle = JigsawCutStyle.CLASSIC_JIGSAW,
            winCondition = JigsawWinCondition.TIME_ATTACK,
            timeLimitSeconds = 120,
            parTimeSeconds = 75,
            rotatePieces = true,
            difficulty = Difficulty.HARD,
            hint = "Look for the red sunset sky first"
        )

        val code = JigsawCodec.encode(original)
        assertTrue(code.startsWith("JIGSAW:1:"))

        val decodedResult = JigsawCodec.decode(code)
        assertTrue(decodedResult.isSuccess)
        val decoded = decodedResult.getOrThrow()

        assertEquals(original.title, decoded.title)
        assertEquals(original.author, decoded.author)
        assertEquals(original.imageKey, decoded.imageKey)
        assertEquals(original.rows, decoded.rows)
        assertEquals(original.cols, decoded.cols)
        assertEquals(original.edgeSeed, decoded.edgeSeed)
        assertEquals(original.cutStyle, decoded.cutStyle)
        assertEquals(original.winCondition, decoded.winCondition)
        assertEquals(original.timeLimitSeconds, decoded.timeLimitSeconds)
        assertEquals(original.parTimeSeconds, decoded.parTimeSeconds)
        assertEquals(original.rotatePieces, decoded.rotatePieces)
        assertEquals(original.difficulty, decoded.difficulty)
        assertEquals(original.hint, decoded.hint)
    }
}
