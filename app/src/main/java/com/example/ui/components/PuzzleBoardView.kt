package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.data.model.LevelData
import com.example.data.model.TileType
import com.example.ui.game.GridPos
import com.example.ui.game.PlayState

// Distinctive Palette
val TileEmptyBg = Color(0xFF0F172A)
val TileEmptyGridLine = Color(0xFF1E293B)
val WallBase = Color(0xFF334155)
val WallHighlight = Color(0xFF475569)
val WallShadow = Color(0xFF1E293B)
val IceColor = Color(0xFF38BDF8)
val IceBg = Color(0xFF0C4A6E)
val ConveyorBeltBg = Color(0xFF1E293B)
val ConveyorArrow = Color(0xFFFBBF24)
val SpikeColor = Color(0xFFEF4444)
val TargetPadActive = Color(0xFF10B981)
val TargetPadInactive = Color(0xFF0D9488)
val CrateColor = Color(0xFFD97706)
val CrateHighlight = Color(0xFFF59E0B)
val GoldKeyColor = Color(0xFFFACC15)
val GoldDoorColor = Color(0xFFCA8A04)
val BlueKeyColor = Color(0xFF38BDF8)
val BlueDoorColor = Color(0xFF0284C7)
val PortalAColor = Color(0xFF06B6D4)
val PortalBColor = Color(0xFFA855F7)
val SwitchActiveColor = Color(0xFF10B981)
val SwitchInactiveColor = Color(0xFF64748B)
val StarGemColor = Color(0xFFFDE047)
val PlayerBody = Color(0xFF6366F1)
val PlayerEyes = Color(0xFF38BDF8)

@Composable
fun InteractivePuzzleBoard(
    level: LevelData,
    playState: PlayState?,
    isEditorMode: Boolean = false,
    selectedEditorPos: GridPos? = null,
    onTileClicked: ((x: Int, y: Int) -> Unit)? = null,
    onTileDragged: ((x: Int, y: Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .aspectRatio(level.width.toFloat() / level.height.toFloat())
            .shadow(12.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF090D16))
            .border(2.dp, Color(0xFF334155), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val tileSize = minOf(widthPx / level.width, heightPx / level.height)

        val pointerModifier = if (onTileClicked != null || onTileDragged != null) {
            Modifier.pointerInput(level.width, level.height, tileSize) {
                detectTapGestures { offset ->
                    val x = (offset.x / tileSize).toInt().coerceIn(0, level.width - 1)
                    val y = (offset.y / tileSize).toInt().coerceIn(0, level.height - 1)
                    onTileClicked?.invoke(x, y)
                }
            }.pointerInput(level.width, level.height, tileSize) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val x = (change.position.x / tileSize).toInt().coerceIn(0, level.width - 1)
                    val y = (change.position.y / tileSize).toInt().coerceIn(0, level.height - 1)
                    onTileDragged?.invoke(x, y)
                }
            }
        } else {
            Modifier
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(pointerModifier)
        ) {
            val totalW = level.width * tileSize
            val totalH = level.height * tileSize
            val offsetX = (size.width - totalW) / 2f
            val offsetY = (size.height - totalH) / 2f

            // 1. Draw base floor tiles
            for (y in 0 until level.height) {
                for (x in 0 until level.width) {
                    val tileX = offsetX + x * tileSize
                    val tileY = offsetY + y * tileSize
                    val tile = level.getTile(x, y)

                    drawFloorTile(
                        x = tileX,
                        y = tileY,
                        size = tileSize,
                        tile = tile,
                        playState = playState,
                        pos = GridPos(x, y)
                    )
                }
            }

            // 2. Draw static barriers / walls / mechanisms
            for (y in 0 until level.height) {
                for (x in 0 until level.width) {
                    val tileX = offsetX + x * tileSize
                    val tileY = offsetY + y * tileSize
                    val tile = level.getTile(x, y)

                    drawStructureTile(
                        x = tileX,
                        y = tileY,
                        size = tileSize,
                        tile = tile,
                        playState = playState,
                        pos = GridPos(x, y),
                        isEditor = isEditorMode
                    )
                }
            }

            // 3. Draw Crates
            if (playState != null) {
                for (cratePos in playState.crates) {
                    val cx = offsetX + cratePos.x * tileSize
                    val cy = offsetY + cratePos.y * tileSize
                    val isCoveringTarget = level.getTile(cratePos.x, cratePos.y) == TileType.TARGET_PEDESTAL
                    drawCrate(cx, cy, tileSize, isCoveringTarget)
                }
            } else if (isEditorMode) {
                // In editor, crates are stored on the grid
                for (y in 0 until level.height) {
                    for (x in 0 until level.width) {
                        if (level.getTile(x, y) == TileType.CRATE) {
                            val cx = offsetX + x * tileSize
                            val cy = offsetY + y * tileSize
                            drawCrate(cx, cy, tileSize, false)
                        }
                    }
                }
            }

            // 4. Draw Player
            val playerPosition = playState?.playerPos ?: run {
                var found: GridPos? = null
                for (y in 0 until level.height) {
                    for (x in 0 until level.width) {
                        if (level.getTile(x, y) == TileType.PLAYER_SPAWN) {
                            found = GridPos(x, y)
                            break
                        }
                    }
                }
                found
            }

            if (playerPosition != null) {
                val px = offsetX + playerPosition.x * tileSize
                val py = offsetY + playerPosition.y * tileSize
                drawPlayer(px, py, tileSize)
            }

            // 5. Draw selection cursor in editor
            if (isEditorMode && selectedEditorPos != null) {
                val sx = offsetX + selectedEditorPos.x * tileSize
                val sy = offsetY + selectedEditorPos.y * tileSize
                drawRect(
                    color = Color(0xFF38BDF8),
                    topLeft = Offset(sx, sy),
                    size = Size(tileSize, tileSize),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }
    }
}

private fun DrawScope.drawFloorTile(
    x: Float,
    y: Float,
    size: Float,
    tile: TileType,
    playState: PlayState?,
    pos: GridPos
) {
    // Base floor background
    val bg = when (tile) {
        TileType.ICE -> IceBg
        TileType.CONVEYOR_UP, TileType.CONVEYOR_DOWN,
        TileType.CONVEYOR_LEFT, TileType.CONVEYOR_RIGHT -> ConveyorBeltBg
        else -> TileEmptyBg
    }

    drawRect(
        color = bg,
        topLeft = Offset(x, y),
        size = Size(size, size)
    )

    // Subtle grid border
    drawRect(
        color = TileEmptyGridLine,
        topLeft = Offset(x, y),
        size = Size(size, size),
        style = Stroke(width = 1f)
    )

    // Ice shimmer
    if (tile == TileType.ICE) {
        drawLine(
            color = IceColor.copy(alpha = 0.4f),
            start = Offset(x + size * 0.2f, y + size * 0.8f),
            end = Offset(x + size * 0.8f, y + size * 0.2f),
            strokeWidth = 2f
        )
        drawLine(
            color = IceColor.copy(alpha = 0.2f),
            start = Offset(x + size * 0.4f, y + size * 0.9f),
            end = Offset(x + size * 0.9f, y + size * 0.4f),
            strokeWidth = 1.5f
        )
    }

    // Conveyor arrows
    when (tile) {
        TileType.CONVEYOR_UP -> drawConveyorArrow(x, y, size, 0)
        TileType.CONVEYOR_RIGHT -> drawConveyorArrow(x, y, size, 90)
        TileType.CONVEYOR_DOWN -> drawConveyorArrow(x, y, size, 180)
        TileType.CONVEYOR_LEFT -> drawConveyorArrow(x, y, size, 270)
        else -> {}
    }

    // Target pad
    if (tile == TileType.TARGET_PEDESTAL) {
        val hasCrate = playState?.crates?.contains(pos) == true
        val padColor = if (hasCrate) TargetPadActive else TargetPadInactive
        drawCircle(
            color = padColor.copy(alpha = 0.3f),
            radius = size * 0.35f,
            center = Offset(x + size / 2, y + size / 2)
        )
        drawCircle(
            color = padColor,
            radius = size * 0.25f,
            center = Offset(x + size / 2, y + size / 2),
            style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(
            color = padColor,
            radius = size * 0.08f,
            center = Offset(x + size / 2, y + size / 2)
        )
    }

    // Hazard spikes
    if (tile == TileType.HAZARD_SPIKES) {
        drawSpikes(x, y, size)
    }

    // Pressure button switch
    if (tile == TileType.BUTTON_SWITCH) {
        val active = playState?.isSwitchActive == true
        val col = if (active) SwitchActiveColor else SwitchInactiveColor
        drawRoundRect(
            color = col.copy(alpha = 0.25f),
            topLeft = Offset(x + size * 0.2f, y + size * 0.2f),
            size = Size(size * 0.6f, size * 0.6f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
        drawCircle(
            color = col,
            radius = size * 0.2f,
            center = Offset(x + size / 2, y + size / 2)
        )
    }
}

private fun DrawScope.drawStructureTile(
    x: Float,
    y: Float,
    size: Float,
    tile: TileType,
    playState: PlayState?,
    pos: GridPos,
    isEditor: Boolean
) {
    when (tile) {
        TileType.WALL -> {
            // 3D block appearance
            drawRoundRect(
                color = WallShadow,
                topLeft = Offset(x + 2f, y + 2f),
                size = Size(size - 4f, size - 4f),
                cornerRadius = CornerRadius(6.dp.toPx())
            )
            drawRoundRect(
                color = WallBase,
                topLeft = Offset(x + 3f, y + 3f),
                size = Size(size - 6f, size - 7f),
                cornerRadius = CornerRadius(5.dp.toPx())
            )
            drawRoundRect(
                color = WallHighlight,
                topLeft = Offset(x + 4f, y + 4f),
                size = Size(size - 8f, size * 0.4f),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
        }
        TileType.GOAL_EXIT -> {
            // Glowing Portal
            drawCircle(
                color = Color(0xFF10B981).copy(alpha = 0.25f),
                radius = size * 0.42f,
                center = Offset(x + size / 2, y + size / 2)
            )
            drawCircle(
                color = Color(0xFF34D399),
                radius = size * 0.32f,
                center = Offset(x + size / 2, y + size / 2),
                style = Stroke(width = 3.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF6EE7B7),
                radius = size * 0.16f,
                center = Offset(x + size / 2, y + size / 2)
            )
        }
        TileType.STAR_GEM -> {
            val collected = playState?.collectedStars?.contains(pos) == true
            if (!collected || isEditor) {
                drawStarGem(x, y, size)
            }
        }
        TileType.KEY_GOLD -> {
            val collected = playState?.collectedGoldKeys?.contains(pos) == true
            if (!collected || isEditor) {
                drawKey(x, y, size, GoldKeyColor)
            }
        }
        TileType.DOOR_GOLD -> {
            val opened = playState?.openedGoldDoors?.contains(pos) == true
            if (!opened || isEditor) {
                drawDoor(x, y, size, GoldDoorColor, "G")
            }
        }
        TileType.KEY_BLUE -> {
            val collected = playState?.collectedBlueKeys?.contains(pos) == true
            if (!collected || isEditor) {
                drawKey(x, y, size, BlueKeyColor)
            }
        }
        TileType.DOOR_BLUE -> {
            val opened = playState?.openedBlueDoors?.contains(pos) == true
            if (!opened || isEditor) {
                drawDoor(x, y, size, BlueDoorColor, "B")
            }
        }
        TileType.TOGGLE_GATE_CLOSED -> {
            val isOpen = playState?.isSwitchActive == true
            if (!isOpen || isEditor) {
                drawToggleBarrier(x, y, size, isBlocked = true)
            }
        }
        TileType.TOGGLE_GATE_OPEN -> {
            val isClosed = playState?.isSwitchActive == true
            if (isClosed || isEditor) {
                drawToggleBarrier(x, y, size, isBlocked = isClosed)
            }
        }
        TileType.TELEPORTER_A -> {
            drawTeleporter(x, y, size, PortalAColor, "A")
        }
        TileType.TELEPORTER_B -> {
            drawTeleporter(x, y, size, PortalBColor, "B")
        }
        TileType.PLAYER_SPAWN -> {
            if (isEditor) {
                // Ghost outline in editor
                drawCircle(
                    color = PlayerBody.copy(alpha = 0.4f),
                    radius = size * 0.35f,
                    center = Offset(x + size / 2, y + size / 2),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
        else -> {}
    }
}

private fun DrawScope.drawCrate(x: Float, y: Float, size: Float, onTarget: Boolean) {
    val base = if (onTarget) TargetPadActive else CrateColor
    val highlight = if (onTarget) Color(0xFF6EE7B7) else CrateHighlight

    // Outer drop shadow
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.4f),
        topLeft = Offset(x + size * 0.1f + 2f, y + size * 0.1f + 3f),
        size = Size(size * 0.8f, size * 0.8f),
        cornerRadius = CornerRadius(6.dp.toPx())
    )

    // Crate Body
    drawRoundRect(
        color = base,
        topLeft = Offset(x + size * 0.1f, y + size * 0.1f),
        size = Size(size * 0.8f, size * 0.8f),
        cornerRadius = CornerRadius(6.dp.toPx())
    )

    // Highlight border
    drawRoundRect(
        color = highlight,
        topLeft = Offset(x + size * 0.15f, y + size * 0.15f),
        size = Size(size * 0.7f, size * 0.7f),
        cornerRadius = CornerRadius(4.dp.toPx()),
        style = Stroke(width = 2.dp.toPx())
    )

    // Diagonal Cross brace
    drawLine(
        color = highlight,
        start = Offset(x + size * 0.2f, y + size * 0.2f),
        end = Offset(x + size * 0.8f, y + size * 0.8f),
        strokeWidth = 2.dp.toPx()
    )
    drawLine(
        color = highlight,
        start = Offset(x + size * 0.8f, y + size * 0.2f),
        end = Offset(x + size * 0.2f, y + size * 0.8f),
        strokeWidth = 2.dp.toPx()
    )
}

private fun DrawScope.drawPlayer(x: Float, y: Float, size: Float) {
    val cx = x + size / 2
    val cy = y + size / 2

    // Shadow
    drawCircle(
        color = Color.Black.copy(alpha = 0.5f),
        radius = size * 0.32f,
        center = Offset(cx, cy + size * 0.1f)
    )

    // Outer Glow
    drawCircle(
        color = PlayerBody.copy(alpha = 0.3f),
        radius = size * 0.42f,
        center = Offset(cx, cy)
    )

    // Bot Body
    drawRoundRect(
        color = PlayerBody,
        topLeft = Offset(cx - size * 0.3f, cy - size * 0.3f),
        size = Size(size * 0.6f, size * 0.6f),
        cornerRadius = CornerRadius(8.dp.toPx())
    )

    // Visor / Screen
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(cx - size * 0.22f, cy - size * 0.15f),
        size = Size(size * 0.44f, size * 0.3f),
        cornerRadius = CornerRadius(4.dp.toPx())
    )

    // Cute glowing cyan eyes
    drawCircle(
        color = PlayerEyes,
        radius = size * 0.05f,
        center = Offset(cx - size * 0.1f, cy)
    )
    drawCircle(
        color = PlayerEyes,
        radius = size * 0.05f,
        center = Offset(cx + size * 0.1f, cy)
    )
}

private fun DrawScope.drawStarGem(x: Float, y: Float, size: Float) {
    val cx = x + size / 2
    val cy = y + size / 2
    val path = Path().apply {
        moveTo(cx, cy - size * 0.32f)
        lineTo(cx + size * 0.26f, cy)
        lineTo(cx, cy + size * 0.32f)
        lineTo(cx - size * 0.26f, cy)
        close()
    }
    // Glow
    drawCircle(
        color = StarGemColor.copy(alpha = 0.3f),
        radius = size * 0.35f,
        center = Offset(cx, cy)
    )
    drawPath(path, color = StarGemColor)
    // Shine center
    drawCircle(
        color = Color.White,
        radius = size * 0.07f,
        center = Offset(cx, cy)
    )
}

private fun DrawScope.drawKey(x: Float, y: Float, size: Float, color: Color) {
    val cx = x + size / 2
    val cy = y + size / 2
    drawCircle(
        color = color,
        radius = size * 0.18f,
        center = Offset(cx - size * 0.1f, cy - size * 0.1f),
        style = Stroke(width = 3.dp.toPx())
    )
    drawLine(
        color = color,
        start = Offset(cx, cy),
        end = Offset(cx + size * 0.25f, cy + size * 0.25f),
        strokeWidth = 3.dp.toPx()
    )
    drawLine(
        color = color,
        start = Offset(cx + size * 0.18f, cy + size * 0.18f),
        end = Offset(cx + size * 0.24f, cy + size * 0.12f),
        strokeWidth = 2.5.dp.toPx()
    )
}

private fun DrawScope.drawDoor(x: Float, y: Float, size: Float, color: Color, label: String) {
    drawRoundRect(
        color = color,
        topLeft = Offset(x + size * 0.1f, y + size * 0.1f),
        size = Size(size * 0.8f, size * 0.8f),
        cornerRadius = CornerRadius(6.dp.toPx())
    )
    drawCircle(
        color = Color(0xFF0F172A),
        radius = size * 0.12f,
        center = Offset(x + size / 2, y + size * 0.45f)
    )
    drawRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(x + size * 0.44f, y + size * 0.45f),
        size = Size(size * 0.12f, size * 0.2f)
    )
}

private fun DrawScope.drawToggleBarrier(x: Float, y: Float, size: Float, isBlocked: Boolean) {
    if (isBlocked) {
        val col = Color(0xFFEF4444)
        drawRect(
            color = col.copy(alpha = 0.2f),
            topLeft = Offset(x + size * 0.1f, y + size * 0.1f),
            size = Size(size * 0.8f, size * 0.8f)
        )
        drawLine(
            color = col,
            start = Offset(x + size * 0.2f, y + size * 0.5f),
            end = Offset(x + size * 0.8f, y + size * 0.5f),
            strokeWidth = 4.dp.toPx()
        )
        drawLine(
            color = col,
            start = Offset(x + size * 0.5f, y + size * 0.2f),
            end = Offset(x + size * 0.5f, y + size * 0.8f),
            strokeWidth = 4.dp.toPx()
        )
    } else {
        // Open passage marker
        drawRect(
            color = Color(0xFF10B981).copy(alpha = 0.15f),
            topLeft = Offset(x + size * 0.15f, y + size * 0.15f),
            size = Size(size * 0.7f, size * 0.7f),
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}

private fun DrawScope.drawTeleporter(x: Float, y: Float, size: Float, color: Color, label: String) {
    val cx = x + size / 2
    val cy = y + size / 2
    drawCircle(
        color = color.copy(alpha = 0.2f),
        radius = size * 0.38f,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = color,
        radius = size * 0.28f,
        center = Offset(cx, cy),
        style = Stroke(width = 2.5.dp.toPx())
    )
    drawCircle(
        color = color,
        radius = size * 0.12f,
        center = Offset(cx, cy)
    )
}

private fun DrawScope.drawSpikes(x: Float, y: Float, size: Float) {
    val p1 = Path().apply {
        moveTo(x + size * 0.25f, y + size * 0.75f)
        lineTo(x + size * 0.35f, y + size * 0.3f)
        lineTo(x + size * 0.45f, y + size * 0.75f)
        close()
    }
    val p2 = Path().apply {
        moveTo(x + size * 0.55f, y + size * 0.75f)
        lineTo(x + size * 0.65f, y + size * 0.3f)
        lineTo(x + size * 0.75f, y + size * 0.75f)
        close()
    }
    drawPath(p1, color = SpikeColor)
    drawPath(p2, color = SpikeColor)
}

private fun DrawScope.drawConveyorArrow(x: Float, y: Float, size: Float, angleDeg: Int) {
    val cx = x + size / 2
    val cy = y + size / 2
    val arrowPath = Path()

    when (angleDeg) {
        0 -> { // UP
            arrowPath.moveTo(cx, cy - size * 0.25f)
            arrowPath.lineTo(cx + size * 0.25f, cy + size * 0.15f)
            arrowPath.lineTo(cx, cy + size * 0.05f)
            arrowPath.lineTo(cx - size * 0.25f, cy + size * 0.15f)
            arrowPath.close()
        }
        90 -> { // RIGHT
            arrowPath.moveTo(cx + size * 0.25f, cy)
            arrowPath.lineTo(cx - size * 0.15f, cy + size * 0.25f)
            arrowPath.lineTo(cx - size * 0.05f, cy)
            arrowPath.lineTo(cx - size * 0.15f, cy - size * 0.25f)
            arrowPath.close()
        }
        180 -> { // DOWN
            arrowPath.moveTo(cx, cy + size * 0.25f)
            arrowPath.lineTo(cx + size * 0.25f, cy - size * 0.15f)
            arrowPath.lineTo(cx, cy - size * 0.05f)
            arrowPath.lineTo(cx - size * 0.25f, cy - size * 0.15f)
            arrowPath.close()
        }
        270 -> { // LEFT
            arrowPath.moveTo(cx - size * 0.25f, cy)
            arrowPath.lineTo(cx + size * 0.15f, cy + size * 0.25f)
            arrowPath.lineTo(cx + size * 0.05f, cy)
            arrowPath.lineTo(cx + size * 0.15f, cy - size * 0.25f)
            arrowPath.close()
        }
    }
    drawPath(arrowPath, color = ConveyorArrow)
}

@Composable
fun PuzzleThumbnail(
    level: LevelData,
    modifier: Modifier = Modifier
) {
    InteractivePuzzleBoard(
        level = level,
        playState = null,
        isEditorMode = false,
        modifier = modifier
    )
}
