package com.example.ui.editor

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BorderAll
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TileCategory
import com.example.data.model.TileType
import com.example.ui.components.InteractivePuzzleBoard
import com.example.ui.components.ShareLevelDialog
import com.example.ui.game.Direction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelEditorScreen(
    viewModel: LevelEditorViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showGridSizeMenu by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveSuccessMessage) {
        uiState.saveSuccessMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.dismissSaveSuccessMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.clickable { viewModel.togglePropertiesDialog(true) }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = uiState.level.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Title",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "${uiState.level.width}x${uiState.level.height} • ${uiState.level.winCondition.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!uiState.isTesting) {
                        IconButton(
                            onClick = { viewModel.undo() },
                            enabled = uiState.canUndo,
                            modifier = Modifier.testTag("editor_undo_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                        }

                        IconButton(
                            onClick = { viewModel.redo() },
                            enabled = uiState.canRedo,
                            modifier = Modifier.testTag("editor_redo_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
                        }

                        // Grid size dropdown
                        Box {
                            IconButton(onClick = { showGridSizeMenu = true }) {
                                Icon(Icons.Default.GridOn, contentDescription = "Resize Grid")
                            }
                            DropdownMenu(
                                expanded = showGridSizeMenu,
                                onDismissRequest = { showGridSizeMenu = false }
                            ) {
                                listOf(6 to 6, 7 to 7, 8 to 8, 9 to 9, 10 to 10).forEach { (w, h) ->
                                    DropdownMenuItem(
                                        text = { Text("${w}x${h} Grid") },
                                        onClick = {
                                            viewModel.resizeGrid(w, h)
                                            showGridSizeMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { viewModel.togglePropertiesDialog(true) },
                            modifier = Modifier.testTag("editor_settings_button")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Rules & Properties")
                        }

                        IconButton(
                            onClick = { viewModel.toggleShareDialog(true) },
                            modifier = Modifier.testTag("editor_share_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share Level")
                        }

                        IconButton(
                            onClick = { viewModel.saveLevel() },
                            modifier = Modifier.testTag("editor_save_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Save Level", tint = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        // In Testing mode
                        Button(
                            onClick = { viewModel.stopTestPlay() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop Test")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        },
        bottomBar = {
            if (!uiState.isTesting) {
                EditorPaletteBottomBar(
                    selectedCategory = uiState.selectedCategory,
                    selectedTile = uiState.selectedTile,
                    activeTool = uiState.activeTool,
                    onCategorySelected = { viewModel.selectCategory(it) },
                    onTileSelected = { viewModel.selectTile(it) },
                    onToolSelected = { viewModel.setTool(it) }
                )
            }
        },
        containerColor = Color(0xFF0A0F1D)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Testing Banner or Validation / Quick Tools Bar
            if (uiState.isTesting) {
                TestPlayHeader(
                    playState = uiState.testPlayState,
                    onReset = { viewModel.resetTestPlay() }
                )
            } else {
                EditorHelperBar(
                    isValid = uiState.isValid,
                    errorCount = uiState.validationErrors.size,
                    onValidateClick = { viewModel.toggleValidationDialog(true) },
                    onAddBorders = { viewModel.applyBorderWalls() },
                    onClear = { viewModel.clearAllTiles() },
                    onTestPlay = {
                        if (uiState.isValid) {
                            viewModel.startTestPlay()
                        } else {
                            viewModel.toggleValidationDialog(true)
                        }
                    }
                )
            }

            // Interactive Grid Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 500.dp),
                contentAlignment = Alignment.Center
            ) {
                InteractivePuzzleBoard(
                    level = uiState.level,
                    playState = if (uiState.isTesting) uiState.testPlayState else null,
                    isEditorMode = !uiState.isTesting,
                    selectedEditorPos = uiState.selectedPos,
                    onTileClicked = { x, y -> viewModel.onTileClick(x, y) },
                    onTileDragged = { x, y -> viewModel.onTileDrag(x, y) }
                )
            }

            // In Testing mode, show on-screen D-pad controls
            if (uiState.isTesting) {
                EditorTestControls(
                    onMove = { viewModel.testMove(it) },
                    onReset = { viewModel.resetTestPlay() }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }

    // Dialogs
    if (uiState.showPropertiesDialog) {
        LevelPropertiesDialog(
            level = uiState.level,
            onDismiss = { viewModel.togglePropertiesDialog(false) },
            onSave = { t, a, wc, par, lim, diff, h ->
                viewModel.updateProperties(t, a, wc, par, lim, diff, h)
            }
        )
    }

    if (uiState.showValidationDialog) {
        ValidationDialog(
            errors = uiState.validationErrors,
            onDismiss = { viewModel.toggleValidationDialog(false) }
        )
    }

    if (uiState.showShareDialog) {
        ShareLevelDialog(
            level = uiState.level,
            onDismiss = { viewModel.toggleShareDialog(false) }
        )
    }
}

@Composable
fun EditorHelperBar(
    isValid: Boolean,
    errorCount: Int,
    onValidateClick: () -> Unit,
    onAddBorders: () -> Unit,
    onClear: () -> Unit,
    onTestPlay: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Validation Status Chip
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isValid) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f))
                .clickable { onValidateClick() }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isValid) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isValid) Color(0xFF10B981) else Color(0xFFEF4444),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isValid) "Valid Puzzle" else "$errorCount Issues",
                color = if (isValid) Color(0xFF10B981) else Color(0xFFEF4444),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        // Action Buttons: Border, Clear, Test
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(
                onClick = onAddBorders,
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Default.BorderAll, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Borders", fontSize = 11.sp)
            }

            Button(
                onClick = onTestPlay,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("editor_test_play_button")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Test Run", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TestPlayHeader(
    playState: com.example.ui.game.PlayState?,
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TEST RUN ACTIVE",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp
                )
                Text(
                    text = "Moves: ${playState?.moves ?: 0} • Par: ${playState?.level?.parMoves}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (playState?.isWon == true) {
                Text(
                    text = "★ PUZZLE SOLVED! ★",
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            } else if (playState?.isFailed == true) {
                Text(
                    text = playState.failureReason ?: "Failed!",
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            IconButton(onClick = onReset) {
                Icon(Icons.Default.Refresh, contentDescription = "Restart Test", tint = Color.LightGray)
            }
        }
    }
}

@Composable
fun EditorPaletteBottomBar(
    selectedCategory: TileCategory,
    selectedTile: TileType,
    activeTool: EditorTool,
    onCategorySelected: (TileCategory) -> Unit,
    onTileSelected: (TileType) -> Unit,
    onToolSelected: (EditorTool) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF1E293B))
            .navigationBarsPadding()
            .padding(bottom = 6.dp)
    ) {
        // Mode & Category Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Tool Toggle (Paint / Erase)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilterChip(
                    selected = activeTool == EditorTool.PAINT,
                    onClick = { onToolSelected(EditorTool.PAINT) },
                    label = { Text("Paint", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.height(30.dp)
                )
                FilterChip(
                    selected = activeTool == EditorTool.ERASE,
                    onClick = { onToolSelected(EditorTool.ERASE) },
                    label = { Text("Eraser", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFEF4444).copy(alpha = 0.25f),
                        selectedLabelColor = Color(0xFFEF4444)
                    ),
                    modifier = Modifier.height(30.dp)
                )
            }

            // Category Chips
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TileCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { onCategorySelected(cat) },
                        label = { Text(cat.title, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF38BDF8).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFF38BDF8)
                        ),
                        modifier = Modifier.height(30.dp)
                    )
                }
            }
        }

        // Horizontal list of tiles for current category
        val tilesInCat = remember(selectedCategory) {
            TileType.entries.filter { it.category == selectedCategory }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tilesInCat.forEach { tile ->
                val isSelected = selectedTile == tile && activeTool == EditorTool.PAINT
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onTileSelected(tile) }
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = tile.code,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = if (isSelected) Color(0xFF38BDF8) else Color.White
                    )
                    Text(
                        text = tile.displayName,
                        fontSize = 10.sp,
                        color = if (isSelected) Color.White else Color.Gray,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun EditorTestControls(
    onMove: (Direction) -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // D-Pad for test run
        IconButton(
            onClick = { onMove(Direction.UP) },
            modifier = Modifier
                .size(46.dp)
                .background(Color(0xFF1E293B), CircleShape)
        ) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = Color.White)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onMove(Direction.LEFT) },
                modifier = Modifier
                    .size(46.dp)
                    .background(Color(0xFF1E293B), CircleShape)
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Left", tint = Color.White)
            }
            IconButton(
                onClick = onReset,
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFF334155), CircleShape)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.LightGray)
            }
            IconButton(
                onClick = { onMove(Direction.RIGHT) },
                modifier = Modifier
                    .size(46.dp)
                    .background(Color(0xFF1E293B), CircleShape)
            ) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Right", tint = Color.White)
            }
        }
        IconButton(
            onClick = { onMove(Direction.DOWN) },
            modifier = Modifier
                .size(46.dp)
                .background(Color(0xFF1E293B), CircleShape)
        ) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = Color.White)
        }
    }
}
