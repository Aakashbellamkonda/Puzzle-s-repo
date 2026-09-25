package com.example.data.jigsaw

import androidx.compose.ui.graphics.Path
import java.util.Random

object JigsawPathGenerator {

    /**
     * Generates consistent interlocking edge definitions for a grid of size [rows] x [cols].
     */
    fun generatePieceEdges(rows: Int, cols: Int, seed: Long): List<PieceEdges> {
        val rand = Random(seed)

        // horizontalEdges[r][c]: edge between piece (r, c) bottom and (r+1, c) top
        // true = (r, c) has TAB_OUT and (r+1, c) has TAB_IN; false = vice versa
        val horizontalEdges = Array(rows - 1) { BooleanArray(cols) { rand.nextBoolean() } }

        // verticalEdges[r][c]: edge between piece (r, c) right and (r, c+1) left
        // true = (r, c) has TAB_OUT and (r, c+1) has TAB_IN; false = vice versa
        val verticalEdges = Array(rows) { BooleanArray(cols - 1) { rand.nextBoolean() } }

        val result = mutableListOf<PieceEdges>()

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val top = if (r == 0) {
                    EdgeType.FLAT
                } else {
                    if (horizontalEdges[r - 1][c]) EdgeType.TAB_IN else EdgeType.TAB_OUT
                }

                val bottom = if (r == rows - 1) {
                    EdgeType.FLAT
                } else {
                    if (horizontalEdges[r][c]) EdgeType.TAB_OUT else EdgeType.TAB_IN
                }

                val left = if (c == 0) {
                    EdgeType.FLAT
                } else {
                    if (verticalEdges[r][c - 1]) EdgeType.TAB_IN else EdgeType.TAB_OUT
                }

                val right = if (c == cols - 1) {
                    EdgeType.FLAT
                } else {
                    if (verticalEdges[r][c]) EdgeType.TAB_OUT else EdgeType.TAB_IN
                }

                result.add(PieceEdges(top = top, right = right, bottom = bottom, left = left))
            }
        }

        return result
    }

    /**
     * Builds the clipping path for a piece of dimensions (w x h).
     * The origin (0, 0) is the top-left of the piece's bounding grid cell.
     */
    fun createPiecePath(w: Float, h: Float, edges: PieceEdges, isGeometric: Boolean = false): Path {
        val path = Path()
        path.moveTo(0f, 0f)

        if (isGeometric) {
            path.lineTo(w, 0f)
            path.lineTo(w, h)
            path.lineTo(0f, h)
            path.close()
            return path
        }

        val tabDepthX = w * 0.20f
        val tabDepthY = h * 0.20f

        // Top Edge (0, 0) -> (w, 0)
        drawHorizontalEdge(path, startX = 0f, endX = w, y = 0f, edge = edges.top, depth = -tabDepthY)

        // Right Edge (w, 0) -> (w, h)
        drawVerticalEdge(path, x = w, startY = 0f, endY = h, edge = edges.right, depth = tabDepthX)

        // Bottom Edge (w, h) -> (0, h)
        drawHorizontalEdge(path, startX = w, endX = 0f, y = h, edge = edges.bottom, depth = tabDepthY)

        // Left Edge (0, h) -> (0, 0)
        drawVerticalEdge(path, x = 0f, startY = h, endY = 0f, edge = edges.left, depth = -tabDepthX)

        path.close()
        return path
    }

    private fun drawHorizontalEdge(path: Path, startX: Float, endX: Float, y: Float, edge: EdgeType, depth: Float) {
        if (edge == EdgeType.FLAT) {
            path.lineTo(endX, y)
            return
        }

        val sign = if (edge == EdgeType.TAB_OUT) 1f else -1f
        val d = depth * sign
        val len = endX - startX

        val p1x = startX + len * 0.35f
        val p2x = startX + len * 0.38f
        val p3x = startX + len * 0.44f
        val apexX = startX + len * 0.50f
        val p5x = startX + len * 0.56f
        val p6x = startX + len * 0.62f
        val p7x = startX + len * 0.65f

        path.lineTo(p1x, y)
        path.cubicTo(
            p1x + len * 0.02f, y - d * 0.15f,
            p2x, y + d * 0.6f,
            p3x, y + d * 0.95f
        )
        path.cubicTo(
            apexX - len * 0.05f, y + d * 1.15f,
            apexX + len * 0.05f, y + d * 1.15f,
            p5x, y + d * 0.95f
        )
        path.cubicTo(
            p6x, y + d * 0.6f,
            p7x - len * 0.02f, y - d * 0.15f,
            p7x, y
        )
        path.lineTo(endX, y)
    }

    private fun drawVerticalEdge(path: Path, x: Float, startY: Float, endY: Float, edge: EdgeType, depth: Float) {
        if (edge == EdgeType.FLAT) {
            path.lineTo(x, endY)
            return
        }

        val sign = if (edge == EdgeType.TAB_OUT) 1f else -1f
        val d = depth * sign
        val len = endY - startY

        val p1y = startY + len * 0.35f
        val p2y = startY + len * 0.38f
        val p3y = startY + len * 0.44f
        val apexY = startY + len * 0.50f
        val p5y = startY + len * 0.56f
        val p6y = startY + len * 0.62f
        val p7y = startY + len * 0.65f

        path.lineTo(x, p1y)
        path.cubicTo(
            x - d * 0.15f, p1y + len * 0.02f,
            x + d * 0.6f, p2y,
            x + d * 0.95f, p3y
        )
        path.cubicTo(
            x + d * 1.15f, apexY - len * 0.05f,
            x + d * 1.15f, apexY + len * 0.05f,
            x + d * 0.95f, p5y
        )
        path.cubicTo(
            x + d * 0.6f, p6y,
            x - d * 0.15f, p7y - len * 0.02f,
            x, p7y
        )
        path.lineTo(x, endY)
    }
}
