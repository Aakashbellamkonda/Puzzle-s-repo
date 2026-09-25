package com.example.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.InteractivePuzzleBoard
import com.example.ui.components.ShareLevelDialog
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    onOpenEditor: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val level = uiState.level
    val playState = uiState.playState

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = level?.title ?: "Puzzle",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "by ${level?.author ?: "Unknown"} • ${level?.difficulty?.label ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("game_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (level?.hint?.isNotBlank() == true) {
                        IconButton(
                            onClick = { viewModel.toggleHint(true) },
                            modifier = Modifier.testTag("game_hint_button")
                        ) {
                            Icon(Icons.Default.Lightbulb, contentDescription = "Hint", tint = Color(0xFFFBBF24))
                        }
                    }

                    IconButton(
                        onClick = { level?.let { onOpenEditor(it.id) } },
                        modifier = Modifier.testTag("game_open_editor_button")
                    ) {
                        Icon(Icons.Default.Build, contentDescription = "Edit in Studio")
                    }

                    IconButton(
                        onClick = { viewModel.toggleShare(true) },
                        modifier = Modifier.testTag("game_share_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        },
        containerColor = Color(0xFF0A0F1D)
    ) { padding ->
        if (level == null || playState == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading puzzle...")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Game HUD: Moves, Par Target, Keys, Stars
                GameHud(
                    playState = playState,
                    canUndo = uiState.canUndo,
                    onUndo = { viewModel.undo() },
                    onReset = { viewModel.reset() }
                )

                // Swipe Gesture Tracker on Grid
                var totalDragX by remember { mutableFloatStateOf(0f) }
                var totalDragY by remember { mutableFloatStateOf(0f) }
                val swipeThreshold = 55f

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(max = 520.dp)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragEnd = {
                                    if (abs(totalDragX) > abs(totalDragY)) {
                                        if (abs(totalDragX) > swipeThreshold) {
                                            if (totalDragX > 0) viewModel.move(Direction.RIGHT)
                                            else viewModel.move(Direction.LEFT)
                                        }
                                    } else {
                                        if (abs(totalDragY) > swipeThreshold) {
                                            if (totalDragY > 0) viewModel.move(Direction.DOWN)
                                            else viewModel.move(Direction.UP)
                                        }
                                    }
                                    totalDragX = 0f
                                    totalDragY = 0f
                                },
                                onDragCancel = {
                                    totalDragX = 0f
                                    totalDragY = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    totalDragX += dragAmount.x
                                    totalDragY += dragAmount.y
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    InteractivePuzzleBoard(
                        level = level,
                        playState = playState,
                        isEditorMode = false
                    )
                }

                // D-Pad and Control Pad
                GameControls(
                    onMove = { viewModel.move(it) },
                    onUndo = { viewModel.undo() },
                    onReset = { viewModel.reset() },
                    canUndo = uiState.canUndo
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Hint Dialog
    if (uiState.showHintDialog && level?.hint?.isNotBlank() == true) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleHint(false) },
            icon = {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(32.dp))
            },
            title = {
                Text("Creator's Hint", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = level.hint,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.LightGray
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.toggleHint(false) }) {
                    Text("Got It")
                }
            }
        )
    }

    // Win Dialog
    if (uiState.showWinDialog && playState != null && level != null) {
        AlertDialog(
            onDismissRequest = { /* keep open */ },
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
                    text = "Puzzle Solved!",
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Completed in ${playState.moves} moves!",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF38BDF8)
                    )
                    Text(
                        text = "Par Target: ${level.parMoves} moves",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    val ratingMsg = when (uiState.starsAwarded) {
                        3 -> "★★★ Perfect Efficiency! True Mastermind!"
                        2 -> "★★ Great job! Near par score."
                        else -> "★ Solved! Can you beat it with fewer moves?"
                    }
                    Text(
                        text = ratingMsg,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.LightGray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("win_continue_button")
                ) {
                    Text("Level Select")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.reset() }
                ) {
                    Text("Play Again")
                }
            }
        )
    }

    // Fail Dialog
    if (uiState.showFailDialog && playState != null) {
        AlertDialog(
            onDismissRequest = { /* keep open */ },
            title = {
                Text(text = "Level Failed", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
            },
            text = {
                Text(
                    text = playState.failureReason ?: "Try again with a different approach!",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.reset() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Try Again")
                }
            },
            dismissButton = {
                if (uiState.canUndo) {
                    TextButton(onClick = { viewModel.undo() }) {
                        Text("Undo Move")
                    }
                }
            }
        )
    }

    // Share Dialog
    if (uiState.showShareDialog && level != null) {
        ShareLevelDialog(
            level = level,
            onDismiss = { viewModel.toggleShare(false) }
        )
    }
}

@Composable
fun GameHud(
    playState: PlayState,
    canUndo: Boolean,
    onUndo: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Moves & Par
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${playState.moves}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color(0xFF38BDF8)
                    )
                    Text(
                        text = " / ${playState.level.parMoves} par",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
                Text(
                    text = playState.level.winCondition.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
            }

            // Collectibles & Targets
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                if (playState.totalStarsCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFACC15), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${playState.collectedStars.size}/${playState.totalStarsCount}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }

                if (playState.totalTargetsCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ViewInAr, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${playState.targetsFilledCount}/${playState.totalTargetsCount}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }

                if (playState.goldKeys > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFFFACC15), modifier = Modifier.size(16.dp))
                        Text(text = "${playState.goldKeys}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    }
                }
            }

            // Quick Actions: Undo & Reset
            Row {
                IconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier.testTag("game_undo_button")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) Color.White else Color.DarkGray
                    )
                }
                IconButton(
                    onClick = onReset,
                    modifier = Modifier.testTag("game_reset_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Level", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun GameControls(
    onMove: (Direction) -> Unit,
    onUndo: () -> Unit,
    onReset: () -> Unit,
    canUndo: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(top = 4.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Up
        IconButton(
            onClick = { onMove(Direction.UP) },
            modifier = Modifier
                .size(54.dp)
                .background(Color(0xFF1E293B), CircleShape)
                .border(1.dp, Color(0xFF334155), CircleShape)
                .testTag("dpad_up")
        ) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Up", tint = Color.White, modifier = Modifier.size(32.dp))
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Middle Row (Left, Center Action, Right)
        Row(
            horizontalArrangement = Arrangement.spacedBy(36.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onMove(Direction.LEFT) },
                modifier = Modifier
                    .size(54.dp)
                    .background(Color(0xFF1E293B), CircleShape)
                    .border(1.dp, Color(0xFF334155), CircleShape)
                    .testTag("dpad_left")
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Move Left", tint = Color.White, modifier = Modifier.size(32.dp))
            }

            // Center indicator
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF0F172A), CircleShape)
                    .border(1.dp, Color(0xFF334155), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("D-PAD", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            }

            IconButton(
                onClick = { onMove(Direction.RIGHT) },
                modifier = Modifier
                    .size(54.dp)
                    .background(Color(0xFF1E293B), CircleShape)
                    .border(1.dp, Color(0xFF334155), CircleShape)
                    .testTag("dpad_right")
            ) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Move Right", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Down
        IconButton(
            onClick = { onMove(Direction.DOWN) },
            modifier = Modifier
                .size(54.dp)
                .background(Color(0xFF1E293B), CircleShape)
                .border(1.dp, Color(0xFF334155), CircleShape)
                .testTag("dpad_down")
        ) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Down", tint = Color.White, modifier = Modifier.size(32.dp))
        }
    }
}
