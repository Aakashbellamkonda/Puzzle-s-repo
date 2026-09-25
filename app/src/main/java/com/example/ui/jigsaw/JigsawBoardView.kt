package com.example.ui.jigsaw

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.data.jigsaw.JigsawArtworkGallery
import com.example.data.jigsaw.JigsawLevel
import com.example.data.jigsaw.JigsawPathGenerator
import com.example.data.jigsaw.JigsawPiece

@Composable
fun JigsawBoard(
    level: JigsawLevel,
    placedPieces: List<JigsawPiece>,
    showGhostImage: Boolean = false,
    selectedPiece: JigsawPiece? = null,
    dragOffset: Offset? = null,
    onBoardTap: ((row: Int, col: Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val pieceEdgesList = remember(level.rows, level.cols, level.edgeSeed) {
        JigsawPathGenerator.generatePieceEdges(level.rows, level.cols, level.edgeSeed)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
            .aspectRatio(level.cols.toFloat() / level.rows.toFloat())
            .shadow(16.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A))
            .border(2.dp, Color(0xFF334155), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        val boardW = constraints.maxWidth.toFloat()
        val boardH = constraints.maxHeight.toFloat()
        val pw = boardW / level.cols
        val ph = boardH / level.rows

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(level.rows, level.cols) {
                    if (onBoardTap != null) {
                        detectTapGestures { offset ->
                            val c = (offset.x / pw).toInt().coerceIn(0, level.cols - 1)
                            val r = (offset.y / ph).toInt().coerceIn(0, level.rows - 1)
                            onBoardTap(r, c)
                        }
                    }
                }
        ) {
            val boardSize = Size(boardW, boardH)

            // 1. Optional Faint Ghost Reference Image
            if (showGhostImage) {
                drawContext.canvas.save()
                JigsawArtworkGallery.renderArtwork(this, level.imageKey, boardSize)
                drawRect(
                    color = Color(0xFF0A0F1D).copy(alpha = 0.72f),
                    size = boardSize
                )
                drawContext.canvas.restore()
            }

            // 2. Background slot outlines & subtle grid
            for (r in 0 until level.rows) {
                for (c in 0 until level.cols) {
                    val idx = r * level.cols + c
                    val edges = pieceEdgesList[idx]
                    val path = JigsawPathGenerator.createPiecePath(
                        pw, ph, edges,
                        isGeometric = level.cutStyle == com.example.data.jigsaw.JigsawCutStyle.GEOMETRIC
                    )
                    translate(left = c * pw, top = r * ph) {
                        drawPath(
                            path = path,
                            color = Color(0xFF334155).copy(alpha = 0.45f),
                            style = Stroke(width = 1.5f)
                        )
                    }
                }
            }

            // 3. Draw Placed Pieces
            for (piece in placedPieces) {
                val idx = piece.row * level.cols + piece.col
                val edges = pieceEdgesList[idx]
                val path = JigsawPathGenerator.createPiecePath(
                    pw, ph, edges,
                    isGeometric = level.cutStyle == com.example.data.jigsaw.JigsawCutStyle.GEOMETRIC
                )

                translate(left = piece.col * pw, top = piece.row * ph) {
                    drawJigsawPiece(
                        path = path,
                        level = level,
                        boardSize = boardSize,
                        pieceW = pw,
                        pieceH = ph,
                        pieceCol = piece.col,
                        pieceRow = piece.row,
                        rotation = piece.currentRotation
                    )
                }
            }

            // 4. Draw Currently Dragged / Floating Piece (if any)
            if (selectedPiece != null && dragOffset != null) {
                val idx = selectedPiece.row * level.cols + selectedPiece.col
                val edges = pieceEdgesList[idx]
                val path = JigsawPathGenerator.createPiecePath(
                    pw, ph, edges,
                    isGeometric = level.cutStyle == com.example.data.jigsaw.JigsawCutStyle.GEOMETRIC
                )

                translate(left = dragOffset.x - pw / 2, top = dragOffset.y - ph / 2) {
                    // Floating drop shadow
                    drawPath(
                        path = path,
                        color = Color.Black.copy(alpha = 0.5f)
                    )
                    drawJigsawPiece(
                        path = path,
                        level = level,
                        boardSize = boardSize,
                        pieceW = pw,
                        pieceH = ph,
                        pieceCol = selectedPiece.col,
                        pieceRow = selectedPiece.row,
                        rotation = selectedPiece.currentRotation,
                        isFloating = true
                    )
                }
            }
        }
    }
}

/**
 * Draws a single piece in isolated or placed state with clipped artwork and 3D borders.
 */
fun DrawScope.drawJigsawPiece(
    path: Path,
    level: JigsawLevel,
    boardSize: Size,
    pieceW: Float,
    pieceH: Float,
    pieceCol: Int,
    pieceRow: Int,
    rotation: Int = 0,
    isFloating: Boolean = false
) {
    rotate(degrees = rotation.toFloat(), pivot = Offset(pieceW / 2, pieceH / 2)) {
        clipPath(path) {
            // Translate the artwork so the correct region shows
            translate(left = -pieceCol * pieceW, top = -pieceRow * pieceH) {
                JigsawArtworkGallery.renderArtwork(this, level.imageKey, boardSize)
            }
        }

        // Tactile 3D piece border
        drawPath(
            path = path,
            color = if (isFloating) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.45f),
            style = Stroke(width = if (isFloating) 2.5.dp.toPx() else 1.5.dp.toPx())
        )
    }
}

@Composable
fun JigsawPieceThumb(
    piece: JigsawPiece,
    level: JigsawLevel,
    modifier: Modifier = Modifier
) {
    val pieceEdgesList = remember(level.rows, level.cols, level.edgeSeed) {
        JigsawPathGenerator.generatePieceEdges(level.rows, level.cols, level.edgeSeed)
    }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val pw = size.width
            val ph = size.height
            val boardSize = Size(pw * level.cols, ph * level.rows)

            val idx = piece.row * level.cols + piece.col
            val edges = pieceEdgesList.getOrElse(idx) { piece.edges }
            val path = JigsawPathGenerator.createPiecePath(
                pw, ph, edges,
                isGeometric = level.cutStyle == com.example.data.jigsaw.JigsawCutStyle.GEOMETRIC
            )

            drawJigsawPiece(
                path = path,
                level = level,
                boardSize = boardSize,
                pieceW = pw,
                pieceH = ph,
                pieceCol = piece.col,
                pieceRow = piece.row,
                rotation = piece.currentRotation
            )
        }
    }
}
