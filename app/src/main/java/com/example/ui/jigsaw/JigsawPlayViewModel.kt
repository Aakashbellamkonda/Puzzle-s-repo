package com.example.ui.jigsaw

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.jigsaw.JigsawLevel
import com.example.data.jigsaw.JigsawPathGenerator
import com.example.data.jigsaw.JigsawPiece
import com.example.data.jigsaw.JigsawRepository
import com.example.data.jigsaw.JigsawWinCondition
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class TrayFilter {
    ALL,
    EDGES,
    INTERIOR
}

data class JigsawPlayUiState(
    val level: JigsawLevel? = null,
    val placedPieces: List<JigsawPiece> = emptyList(),
    val unplacedPieces: List<JigsawPiece> = emptyList(),
    val selectedPiece: JigsawPiece? = null,
    val trayFilter: TrayFilter = TrayFilter.ALL,
    val showGhostImage: Boolean = false,
    val elapsedSeconds: Int = 0,
    val remainingSeconds: Int = 0,
    val movesCount: Int = 0,
    val isWon: Boolean = false,
    val isFailed: Boolean = false,
    val starsAwarded: Int = 0,
    val failureReason: String? = null,
    val warningMessage: String? = null,
    val showWinDialog: Boolean = false,
    val showHintDialog: Boolean = false,
    val showShareDialog: Boolean = false
) {
    val totalPieces: Int
        get() = (level?.rows ?: 0) * (level?.cols ?: 0)

    val progressPercent: Float
        get() = if (totalPieces > 0) placedPieces.size.toFloat() / totalPieces else 0f

    val filteredTrayPieces: List<JigsawPiece>
        get() = when (trayFilter) {
            TrayFilter.ALL -> unplacedPieces
            TrayFilter.EDGES -> unplacedPieces.filter { it.isEdge }
            TrayFilter.INTERIOR -> unplacedPieces.filter { !it.isEdge }
        }
}

class JigsawPlayViewModel(
    private val repository: JigsawRepository,
    private val context: Context,
    private val levelId: Long,
    customLevel: JigsawLevel? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(JigsawPlayUiState())
    val uiState: StateFlow<JigsawPlayUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        if (customLevel != null) {
            setupGame(customLevel)
        } else if (levelId > 0) {
            viewModelScope.launch {
                val loaded = repository.getLevelOnce(levelId)
                if (loaded != null) {
                    repository.incrementPlayCount(levelId)
                    setupGame(loaded)
                }
            }
        }
    }

    private fun setupGame(level: JigsawLevel) {
        val edgeList = JigsawPathGenerator.generatePieceEdges(level.rows, level.cols, level.edgeSeed)
        val allPieces = mutableListOf<JigsawPiece>()

        var idCounter = 0
        for (r in 0 until level.rows) {
            for (c in 0 until level.cols) {
                val edges = edgeList[r * level.cols + c]
                val initRot = if (level.rotatePieces) (0..3).random() * 90 else 0
                allPieces.add(
                    JigsawPiece(
                        id = idCounter++,
                        row = r,
                        col = c,
                        edges = edges,
                        currentRotation = initRot,
                        isPlaced = false
                    )
                )
            }
        }

        // Anchor Clues pre-placed
        val placed = mutableListOf<JigsawPiece>()
        val unplaced = mutableListOf<JigsawPiece>()

        allPieces.shuffle()

        var anchorsLeft = level.anchorPiecesCount
        for (p in allPieces) {
            if (anchorsLeft > 0 && p.isEdge) {
                p.isPlaced = true
                p.currentRotation = 0
                p.isLockedAnchor = true
                placed.add(p)
                anchorsLeft--
            } else {
                unplaced.add(p)
            }
        }

        _uiState.update {
            it.copy(
                level = level,
                placedPieces = placed,
                unplacedPieces = unplaced,
                selectedPiece = unplaced.firstOrNull(),
                elapsedSeconds = 0,
                remainingSeconds = level.timeLimitSeconds,
                movesCount = 0,
                isWon = false,
                isFailed = false,
                showWinDialog = false,
                warningMessage = null
            )
        }

        startTimer(level)
    }

    private fun startTimer(level: JigsawLevel) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val curr = _uiState.value
                if (curr.isWon || curr.isFailed) break

                val newElapsed = curr.elapsedSeconds + 1
                val newRemaining = curr.remainingSeconds - 1

                val timeFailed = level.winCondition == JigsawWinCondition.TIME_ATTACK && newRemaining <= 0

                _uiState.update {
                    it.copy(
                        elapsedSeconds = newElapsed,
                        remainingSeconds = newRemaining.coerceAtLeast(0),
                        isFailed = timeFailed,
                        failureReason = if (timeFailed) "Time's up! The clock ran out." else null
                    )
                }

                if (timeFailed) {
                    triggerHaptic(long = true)
                    break
                }
            }
        }
    }

    fun selectPiece(piece: JigsawPiece) {
        _uiState.update { it.copy(selectedPiece = piece) }
    }

    fun rotateSelectedPiece() {
        val sel = _uiState.value.selectedPiece ?: return
        val newRot = (sel.currentRotation + 90) % 360
        sel.currentRotation = newRot

        // Update list reference to trigger recomposition
        _uiState.update { state ->
            state.copy(
                unplacedPieces = state.unplacedPieces.map { if (it.id == sel.id) it.copy(currentRotation = newRot) else it },
                selectedPiece = sel.copy(currentRotation = newRot)
            )
        }
        triggerHaptic(long = false)
    }

    fun setTrayFilter(filter: TrayFilter) {
        _uiState.update { it.copy(trayFilter = filter) }
    }

    fun toggleGhostImage() {
        _uiState.update { it.copy(showGhostImage = !it.showGhostImage) }
    }

    fun toggleHint(show: Boolean) {
        _uiState.update { it.copy(showHintDialog = show) }
    }

    fun toggleShare(show: Boolean) {
        _uiState.update { it.copy(showShareDialog = show) }
    }

    fun clearWarning() {
        _uiState.update { it.copy(warningMessage = null) }
    }

    fun tryPlacePieceAt(targetRow: Int, targetCol: Int) {
        val state = _uiState.value
        val level = state.level ?: return
        val selected = state.selectedPiece ?: return

        // Check if target slot is already occupied
        if (state.placedPieces.any { it.row == targetRow && it.col == targetCol }) {
            return
        }

        val newMoves = state.movesCount + 1

        // Check if this piece belongs to (targetRow, targetCol)
        if (selected.row == targetRow && selected.col == targetCol) {
            // Check rotation
            if (level.rotatePieces && selected.currentRotation != 0) {
                _uiState.update {
                    it.copy(
                        warningMessage = "Piece is rotated! Tap it to orient it correctly (needs 0°).",
                        movesCount = newMoves
                    )
                }
                return
            }

            // Check Edges First condition
            if (level.winCondition == JigsawWinCondition.EDGES_FIRST && !selected.isEdge) {
                val allEdgesPlaced = state.unplacedPieces.none { it.isEdge }
                if (!allEdgesPlaced) {
                    _uiState.update {
                        it.copy(
                            warningMessage = "Border First! Connect all outer edge pieces before placing interior pieces.",
                            movesCount = newMoves
                        )
                    }
                    return
                }
            }

            // SNAP INTO PLACE!
            triggerHaptic(long = false)
            val updatedPlaced = state.placedPieces + selected.copy(isPlaced = true, currentRotation = 0)
            val updatedUnplaced = state.unplacedPieces.filterNot { it.id == selected.id }
            val nextSelected = updatedUnplaced.firstOrNull()

            val isComplete = updatedPlaced.size >= state.totalPieces

            var stars = 0
            if (isComplete) {
                triggerHaptic(long = true)
                stars = calculateStars(state.elapsedSeconds, level.parTimeSeconds)
                viewModelScope.launch {
                    repository.recordCompletion(level.id, state.elapsedSeconds)
                }
            }

            _uiState.update {
                it.copy(
                    placedPieces = updatedPlaced,
                    unplacedPieces = updatedUnplaced,
                    selectedPiece = nextSelected,
                    movesCount = newMoves,
                    isWon = isComplete,
                    showWinDialog = isComplete,
                    starsAwarded = stars,
                    warningMessage = null
                )
            }
        } else {
            // Wrong slot
            val moveFailed = level.winCondition == JigsawWinCondition.MOVE_LIMIT && newMoves >= level.maxMoves
            _uiState.update {
                it.copy(
                    movesCount = newMoves,
                    isFailed = moveFailed,
                    failureReason = if (moveFailed) "Move limit of ${level.maxMoves} reached!" else null,
                    warningMessage = if (!moveFailed) "Doesn't fit there! Look closely at the edges and colors." else null
                )
            }
        }
    }

    fun restart() {
        _uiState.value.level?.let { setupGame(it) }
    }

    private fun calculateStars(elapsed: Int, par: Int): Int {
        return when {
            elapsed <= par -> 3
            elapsed <= (par * 1.5).toInt() -> 2
            else -> 1
        }
    }

    private fun triggerHaptic(long: Boolean) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    val duration = if (long) 100L else 35L
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(duration)
                    }
                }
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
