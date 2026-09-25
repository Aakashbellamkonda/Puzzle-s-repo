package com.example.ui.jigsaw

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.jigsaw.JigsawLevel
import com.example.data.jigsaw.JigsawWinCondition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JigsawPlayScreen(
    viewModel: JigsawPlayViewModel,
    onNavigateBack: () -> Unit,
    onOpenEditor: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val level = uiState.level
    val context = LocalContext.current

    LaunchedEffect(uiState.warningMessage) {
        uiState.warningMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearWarning()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = level?.title ?: "Jigsaw Puzzle",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${level?.totalPieces ?: 0} pieces • ${level?.winCondition?.displayName ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("play_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Ghost reference image toggle
                    IconButton(onClick = { viewModel.toggleGhostImage() }, modifier = Modifier.testTag("toggle_ghost_button")) {
                        Icon(
                            imageVector = if (uiState.showGhostImage) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Ghost Reference",
                            tint = if (uiState.showGhostImage) Color(0xFF38BDF8) else Color.Gray
                        )
                    }

                    if (level?.hint?.isNotBlank() == true) {
                        IconButton(onClick = { viewModel.toggleHint(true) }) {
                            Icon(Icons.Default.Lightbulb, contentDescription = "Hint", tint = Color(0xFFFBBF24))
                        }
                    }

                    IconButton(onClick = { level?.let { onOpenEditor(it.id) } }) {
                        Icon(Icons.Default.Build, contentDescription = "Edit in Studio")
                    }

                    IconButton(onClick = { viewModel.toggleShare(true) }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        bottomBar = {
            if (level != null && !uiState.isWon && !uiState.isFailed) {
                JigsawPieceTray(
                    level = level,
                    trayPieces = uiState.filteredTrayPieces,
                    selectedPiece = uiState.selectedPiece,
                    trayFilter = uiState.trayFilter,
                    rotateEnabled = level.rotatePieces,
                    onSelectPiece = { viewModel.selectPiece(it) },
                    onRotatePiece = { viewModel.rotateSelectedPiece() },
                    onFilterChange = { viewModel.setTrayFilter(it) }
                )
            }
        },
        containerColor = Color(0xFF0A0F1D)
    ) { padding ->
        if (level == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading puzzle...")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // HUD: Progress, Timer, Move count
                JigsawHud(
                    placedCount = uiState.placedPieces.size,
                    totalCount = uiState.totalPieces,
                    progressPercent = uiState.progressPercent,
                    elapsedSec = uiState.elapsedSeconds,
                    remainingSec = uiState.remainingSeconds,
                    isTimeAttack = level.winCondition == JigsawWinCondition.TIME_ATTACK,
                    moves = uiState.movesCount,
                    onRestart = { viewModel.restart() }
                )

                // Assembly Board
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(max = 520.dp),
                    contentAlignment = Alignment.Center
                ) {
                    JigsawBoard(
                        level = level,
                        placedPieces = uiState.placedPieces,
                        showGhostImage = uiState.showGhostImage,
                        onBoardTap = { r, c ->
                            viewModel.tryPlacePieceAt(r, c)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Instruction helper badge
                uiState.selectedPiece?.let { sel ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Selected Piece (${if (sel.isEdge) "Border" else "Center"})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                        if (level.rotatePieces && sel.currentRotation != 0) {
                            Text(
                                text = "Rotated ${sel.currentRotation}°",
                                fontSize = 11.sp,
                                color = Color(0xFFFBBF24)
                            )
                        }
                        Text(
                            text = "• Tap board to place",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }

    // Hint Dialog
    if (uiState.showHintDialog && level?.hint?.isNotBlank() == true) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleHint(false) },
            icon = { Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(32.dp)) },
            title = { Text("Puzzle Hint") },
            text = { Text(level.hint, style = MaterialTheme.typography.bodyLarge, color = Color.LightGray) },
            confirmButton = {
                Button(onClick = { viewModel.toggleHint(false) }) { Text("Got It") }
            }
        )
    }

    // Win Dialog
    if (uiState.showWinDialog && level != null) {
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Row {
                    repeat(3) { index ->
                        val filled = index < uiState.starsAwarded
                        Icon(
                            imageVector = if (filled) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (filled) Color(0xFFFACC15) else Color.Gray,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "Puzzle Completed!",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Assembled in ${uiState.elapsedSeconds} seconds!",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF38BDF8)
                    )
                    Text(
                        text = "Moves: ${uiState.movesCount} • Par Time: ${level.parTimeSeconds}s",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    val praise = when (uiState.starsAwarded) {
                        3 -> "★★★ Flawless assembly! Perfect precision."
                        2 -> "★★ Superb effort! Well solved."
                        else -> "★ Solved! Try assembling even faster next time."
                    }
                    Text(
                        text = praise,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.LightGray
                    )
                }
            },
            confirmButton = {
                Button(onClick = onNavigateBack, modifier = Modifier.testTag("jigsaw_win_continue")) {
                    Text("Level Select")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.restart() }) {
                    Text("Play Again")
                }
            }
        )
    }

    // Fail Dialog
    if (uiState.isFailed && level != null) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Challenge Failed", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444)) },
            text = { Text(uiState.failureReason ?: "Try again!") },
            confirmButton = {
                Button(onClick = { viewModel.restart() }) { Text("Retry Puzzle") }
            },
            dismissButton = {
                TextButton(onClick = onNavigateBack) { Text("Exit") }
            }
        )
    }

    if (uiState.showShareDialog && level != null) {
        JigsawShareDialog(
            level = level,
            onDismiss = { viewModel.toggleShare(false) }
        )
    }
}

@Composable
fun JigsawHud(
    placedCount: Int,
    totalCount: Int,
    progressPercent: Float,
    elapsedSec: Int,
    remainingSec: Int,
    isTimeAttack: Boolean,
    moves: Int,
    onRestart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Placed count
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$placedCount/$totalCount",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color(0xFF38BDF8)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pieces Placed",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }

                // Timer
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isTimeAttack) Icons.Default.HourglassTop else Icons.Default.Timer,
                        contentDescription = null,
                        tint = if (isTimeAttack && remainingSec <= 15) Color(0xFFEF4444) else Color(0xFFFBBF24),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isTimeAttack) "${remainingSec}s" else "${elapsedSec}s",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isTimeAttack && remainingSec <= 15) Color(0xFFEF4444) else Color.White
                    )
                }

                // Restart
                IconButton(onClick = onRestart, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                }
            }

            LinearProgressIndicator(
                progress = { progressPercent },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF38BDF8),
                trackColor = Color(0xFF0F172A)
            )
        }
    }
}

@Composable
fun JigsawPieceTray(
    level: JigsawLevel,
    trayPieces: List<com.example.data.jigsaw.JigsawPiece>,
    selectedPiece: com.example.data.jigsaw.JigsawPiece?,
    trayFilter: TrayFilter,
    rotateEnabled: Boolean,
    onSelectPiece: (com.example.data.jigsaw.JigsawPiece) -> Unit,
    onRotatePiece: () -> Unit,
    onFilterChange: (TrayFilter) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF1E293B))
            .navigationBarsPadding()
            .padding(bottom = 6.dp)
    ) {
        // Tray Controls: Filters + Rotate Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    TrayFilter.ALL to "All (${level.totalPieces})",
                    TrayFilter.EDGES to "Borders",
                    TrayFilter.INTERIOR to "Center"
                ).forEach { (f, label) ->
                    FilterChip(
                        selected = trayFilter == f,
                        onClick = { onFilterChange(f) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF38BDF8).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFF38BDF8)
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                }
            }

            // Rotate Piece Button (if rotation challenge enabled)
            if (rotateEnabled) {
                OutlinedButton(
                    onClick = onRotatePiece,
                    modifier = Modifier.height(30.dp).testTag("rotate_piece_button")
                ) {
                    Icon(Icons.Default.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rotate 90°", fontSize = 11.sp)
                }
            }
        }

        // Horizontal Carousel of unplaced loose pieces
        if (trayPieces.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No pieces in this filter",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                trayPieces.forEach { piece ->
                    val isSelected = selectedPiece?.id == piece.id
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (isSelected && rotateEnabled) {
                                    onRotatePiece()
                                } else {
                                    onSelectPiece(piece)
                                }
                            }
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(if (isSelected) Color(0xFF1E293B) else Color(0xFF131C2E))
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        JigsawPieceThumb(
                            piece = piece,
                            level = level,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Rotation badge if rotated
                        if (rotateEnabled && piece.currentRotation != 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFBBF24))
                                    .padding(2.dp)
                            ) {
                                Text(
                                    text = "${piece.currentRotation}°",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
