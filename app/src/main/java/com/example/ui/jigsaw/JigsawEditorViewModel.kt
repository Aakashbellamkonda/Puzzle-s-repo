package com.example.ui.jigsaw

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.jigsaw.JigsawCutStyle
import com.example.data.jigsaw.JigsawLevel
import com.example.data.jigsaw.JigsawRepository
import com.example.data.jigsaw.JigsawWinCondition
import com.example.data.model.Difficulty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class JigsawEditorUiState(
    val level: JigsawLevel = JigsawLevel(),
    val isTesting: Boolean = false,
    val showRulesDialog: Boolean = false,
    val showShareDialog: Boolean = false,
    val saveMessage: String? = null,
    val isDirty: Boolean = false
)

class JigsawEditorViewModel(
    private val repository: JigsawRepository,
    initialLevelId: Long = 0
) : ViewModel() {

    private val _uiState = MutableStateFlow(JigsawEditorUiState())
    val uiState: StateFlow<JigsawEditorUiState> = _uiState.asStateFlow()

    init {
        if (initialLevelId > 0) {
            viewModelScope.launch {
                val loaded = repository.getLevelOnce(initialLevelId)
                if (loaded != null) {
                    _uiState.update { it.copy(level = loaded.copy(isBuiltIn = false)) }
                }
            }
        }
    }

    fun selectArtwork(key: String) {
        _uiState.update {
            it.copy(
                level = it.level.copy(imageKey = key, customImageUri = null),
                isDirty = true
            )
        }
    }

    fun setCustomImageUri(uriString: String) {
        _uiState.update {
            it.copy(
                level = it.level.copy(customImageUri = uriString),
                isDirty = true
            )
        }
    }

    fun setGrid(rows: Int, cols: Int) {
        _uiState.update {
            it.copy(
                level = it.level.copy(
                    rows = rows,
                    cols = cols,
                    parTimeSeconds = (rows * cols * 8).coerceAtLeast(30),
                    timeLimitSeconds = (rows * cols * 14).coerceAtLeast(60)
                ),
                isDirty = true
            )
        }
    }

    fun setCutStyle(style: JigsawCutStyle) {
        _uiState.update {
            it.copy(level = it.level.copy(cutStyle = style), isDirty = true)
        }
    }

    fun randomizeTabs() {
        val newSeed = Random.nextLong()
        _uiState.update {
            it.copy(level = it.level.copy(edgeSeed = newSeed), isDirty = true)
        }
    }

    fun toggleRotatePieces(enabled: Boolean) {
        _uiState.update {
            it.copy(level = it.level.copy(rotatePieces = enabled), isDirty = true)
        }
    }

    fun setAnchorCount(count: Int) {
        _uiState.update {
            it.copy(level = it.level.copy(anchorPiecesCount = count.coerceIn(0, 4)), isDirty = true)
        }
    }

    fun updateRules(
        title: String,
        author: String,
        winCondition: JigsawWinCondition,
        timeLimit: Int,
        parTime: Int,
        maxMoves: Int,
        difficulty: Difficulty,
        hint: String
    ) {
        _uiState.update {
            it.copy(
                level = it.level.copy(
                    title = title.ifBlank { "My Custom Jigsaw" },
                    author = author.ifBlank { "Designer" },
                    winCondition = winCondition,
                    timeLimitSeconds = timeLimit,
                    parTimeSeconds = parTime,
                    maxMoves = maxMoves,
                    difficulty = difficulty,
                    hint = hint
                ),
                showRulesDialog = false,
                isDirty = true
            )
        }
    }

    fun toggleRulesDialog(show: Boolean) {
        _uiState.update { it.copy(showRulesDialog = show) }
    }

    fun toggleShareDialog(show: Boolean) {
        _uiState.update { it.copy(showShareDialog = show) }
    }

    fun clearSaveMessage() {
        _uiState.update { it.copy(saveMessage = null) }
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
                    saveMessage = "Puzzle saved to My Creations!"
                )
            }
            onSaved(newId)
        }
    }
}
