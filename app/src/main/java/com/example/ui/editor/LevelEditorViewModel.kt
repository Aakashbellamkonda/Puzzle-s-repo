package com.example.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.LevelRepository
import com.example.data.model.Difficulty
import com.example.data.model.LevelData
import com.example.data.model.TileCategory
import com.example.data.model.TileType
import com.example.data.model.WinCondition
import com.example.ui.game.GameEngine
import com.example.ui.game.GridPos
import com.example.ui.game.PlayState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class EditorTool {
    PAINT,
    ERASE
}

data class LevelEditorUiState(
    val level: LevelData = LevelData(),
    val selectedTile: TileType = TileType.WALL,
    val selectedCategory: TileCategory = TileCategory.TERRAIN,
    val activeTool: EditorTool = EditorTool.PAINT,
    val selectedPos: GridPos? = null,
    val isTesting: Boolean = false,
    val testPlayState: PlayState? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val showPropertiesDialog: Boolean = false,
    val showValidationDialog: Boolean = false,
    val showShareDialog: Boolean = false,
    val saveSuccessMessage: String? = null,
    val isDirty: Boolean = false
) {
    val validationErrors: List<String>
        get() = level.validate()

    val isValid: Boolean
        get() = validationErrors.isEmpty()
}

class LevelEditorViewModel(
    private val repository: LevelRepository,
    initialLevelId: Long = 0
) : ViewModel() {

    private val _uiState = MutableStateFlow(LevelEditorUiState())
    val uiState: StateFlow<LevelEditorUiState> = _uiState.asStateFlow()

    private val undoStack = mutableListOf<LevelData>()
    private val redoStack = mutableListOf<LevelData>()

    init {
        if (initialLevelId > 0) {
            viewModelScope.launch {
                val loaded = repository.getLevelOnce(initialLevelId)
                if (loaded != null) {
                    _uiState.update { it.copy(level = loaded.copy(isBuiltIn = false)) }
                }
            }
        } else {
            // Setup default starter canvas with border walls and player start
            setupStarterLevel()
        }
    }

    private fun setupStarterLevel() {
        var base = LevelData(
            title = "My New Puzzle",
            author = "Designer",
            width = 8,
            height = 8,
            winCondition = WinCondition.REACH_EXIT,
            parMoves = 15,
            difficulty = Difficulty.MEDIUM
        )
        // Add border walls
        val mutable = base.grid.toMutableList()
        for (y in 0 until 8) {
            for (x in 0 until 8) {
                if (x == 0 || x == 7 || y == 0 || y == 7) {
                    mutable[y * 8 + x] = TileType.WALL
                }
            }
        }
        mutable[1 * 8 + 1] = TileType.PLAYER_SPAWN
        mutable[6 * 8 + 6] = TileType.GOAL_EXIT
        base = base.copy(grid = mutable)
        _uiState.update { it.copy(level = base) }
    }

    private fun pushHistory(newLevel: LevelData) {
        undoStack.add(_uiState.value.level)
        if (undoStack.size > 40) undoStack.removeAt(0)
        redoStack.clear()
        _uiState.update {
            it.copy(
                level = newLevel,
                canUndo = undoStack.isNotEmpty(),
                canRedo = false,
                isDirty = true
            )
        }
    }

    fun onTileClick(x: Int, y: Int) {
        if (_uiState.value.isTesting) return
        val currentLevel = _uiState.value.level
        val tool = _uiState.value.activeTool
        val tileToPlace = if (tool == EditorTool.ERASE) TileType.EMPTY else _uiState.value.selectedTile

        if (currentLevel.getTile(x, y) != tileToPlace) {
            val updated = currentLevel.withTile(x, y, tileToPlace)
            pushHistory(updated)
        }
        _uiState.update { it.copy(selectedPos = GridPos(x, y)) }
    }

    fun onTileDrag(x: Int, y: Int) {
        if (_uiState.value.isTesting) return
        val currentLevel = _uiState.value.level
        val tool = _uiState.value.activeTool
        val tileToPlace = if (tool == EditorTool.ERASE) TileType.EMPTY else _uiState.value.selectedTile

        if (currentLevel.getTile(x, y) != tileToPlace) {
            val updated = currentLevel.withTile(x, y, tileToPlace)
            pushHistory(updated)
        }
        _uiState.update { it.copy(selectedPos = GridPos(x, y)) }
    }

    fun selectTile(tile: TileType) {
        _uiState.update {
            it.copy(
                selectedTile = tile,
                selectedCategory = tile.category,
                activeTool = EditorTool.PAINT
            )
        }
    }

    fun selectCategory(category: TileCategory) {
        _uiState.update {
            val defaultTile = TileType.entries.first { t -> t.category == category }
            it.copy(selectedCategory = category, selectedTile = defaultTile)
        }
    }

    fun setTool(tool: EditorTool) {
        _uiState.update { it.copy(activeTool = tool) }
    }

    fun resizeGrid(newWidth: Int, newHeight: Int) {
        val current = _uiState.value.level
        if (current.width == newWidth && current.height == newHeight) return
        val resized = current.resized(newWidth, newHeight)
        pushHistory(resized)
    }

    fun applyBorderWalls() {
        val current = _uiState.value.level
        val mutable = current.grid.toMutableList()
        for (y in 0 until current.height) {
            for (x in 0 until current.width) {
                if (x == 0 || x == current.width - 1 || y == 0 || y == current.height - 1) {
                    mutable[y * current.width + x] = TileType.WALL
                }
            }
        }
        pushHistory(current.copy(grid = mutable))
    }

    fun clearAllTiles() {
        val current = _uiState.value.level
        val cleared = current.copy(grid = List(current.width * current.height) { TileType.EMPTY })
        pushHistory(cleared)
    }

    fun fillFloor() {
        val current = _uiState.value.level
        val updated = current.copy(grid = current.grid.map { if (it == TileType.WALL) TileType.EMPTY else it })
        pushHistory(updated)
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val prev = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(_uiState.value.level)
            _uiState.update {
                it.copy(
                    level = prev,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = true
                )
            }
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(_uiState.value.level)
            _uiState.update {
                it.copy(
                    level = next,
                    canUndo = true,
                    canRedo = redoStack.isNotEmpty()
                )
            }
        }
    }

    fun updateProperties(
        title: String,
        author: String,
        winCondition: WinCondition,
        parMoves: Int,
        moveLimit: Int,
        difficulty: Difficulty,
        hint: String
    ) {
        val updated = _uiState.value.level.copy(
            title = title.ifBlank { "Untitled Puzzle" },
            author = author.ifBlank { "Designer" },
            winCondition = winCondition,
            parMoves = parMoves.coerceAtLeast(1),
            moveLimit = moveLimit.coerceAtLeast(0),
            difficulty = difficulty,
            hint = hint
        )
        pushHistory(updated)
        _uiState.update { it.copy(showPropertiesDialog = false) }
    }

    fun togglePropertiesDialog(show: Boolean) {
        _uiState.update { it.copy(showPropertiesDialog = show) }
    }

    fun toggleValidationDialog(show: Boolean) {
        _uiState.update { it.copy(showValidationDialog = show) }
    }

    fun toggleShareDialog(show: Boolean) {
        _uiState.update { it.copy(showShareDialog = show) }
    }

    fun dismissSaveSuccessMessage() {
        _uiState.update { it.copy(saveSuccessMessage = null) }
    }

    fun startTestPlay() {
        val level = _uiState.value.level
        val testPlay = GameEngine.initialize(level)
        _uiState.update {
            it.copy(
                isTesting = true,
                testPlayState = testPlay
            )
        }
    }

    fun stopTestPlay() {
        _uiState.update {
            it.copy(
                isTesting = false,
                testPlayState = null
            )
        }
    }

    fun testMove(direction: com.example.ui.game.Direction) {
        val currentPlay = _uiState.value.testPlayState ?: return
        val next = GameEngine.step(currentPlay, direction)
        _uiState.update { it.copy(testPlayState = next) }
    }

    fun resetTestPlay() {
        val level = _uiState.value.level
        _uiState.update {
            it.copy(testPlayState = GameEngine.initialize(level))
        }
    }

    fun saveLevel(onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val levelToSave = _uiState.value.level.copy(
                isBuiltIn = false,
                createdAt = if (_uiState.value.level.id == 0L) System.currentTimeMillis() else _uiState.value.level.createdAt
            )
            val newId = repository.saveLevel(levelToSave)
            _uiState.update {
                it.copy(
                    level = levelToSave.copy(id = newId),
                    isDirty = false,
                    saveSuccessMessage = "Level saved to My Creations!"
                )
            }
            onSaved(newId)
        }
    }
}
