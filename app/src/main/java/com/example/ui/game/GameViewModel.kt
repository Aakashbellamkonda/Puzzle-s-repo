package com.example.ui.game

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.LevelRepository
import com.example.data.model.LevelData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameScreenUiState(
    val level: LevelData? = null,
    val playState: PlayState? = null,
    val canUndo: Boolean = false,
    val showHintDialog: Boolean = false,
    val showWinDialog: Boolean = false,
    val showFailDialog: Boolean = false,
    val starsAwarded: Int = 0,
    val showShareDialog: Boolean = false
)

class GameViewModel(
    private val repository: LevelRepository,
    private val context: Context,
    private val levelId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameScreenUiState())
    val uiState: StateFlow<GameScreenUiState> = _uiState.asStateFlow()

    private val historyStack = mutableListOf<PlayState>()

    init {
        loadLevel()
    }

    private fun loadLevel() {
        viewModelScope.launch {
            val level = repository.getLevelOnce(levelId)
            if (level != null) {
                repository.incrementPlayCount(levelId)
                val initialPlay = GameEngine.initialize(level)
                _uiState.update {
                    it.copy(
                        level = level,
                        playState = initialPlay,
                        canUndo = false,
                        showWinDialog = false,
                        showFailDialog = false
                    )
                }
            }
        }
    }

    fun move(direction: Direction) {
        val current = _uiState.value.playState ?: return
        if (current.isWon || current.isFailed) return

        val next = GameEngine.step(current, direction)
        if (next != current) {
            historyStack.add(current)
            triggerHaptic(next.lastEvent)

            var starsAwarded = 0
            if (next.isWon) {
                starsAwarded = calculateStars(next.moves, next.level.parMoves)
                viewModelScope.launch {
                    repository.recordCompletion(next.level.id, next.moves)
                }
            }

            _uiState.update {
                it.copy(
                    playState = next,
                    canUndo = historyStack.isNotEmpty(),
                    showWinDialog = next.isWon,
                    showFailDialog = next.isFailed,
                    starsAwarded = starsAwarded
                )
            }
        }
    }

    fun undo() {
        if (historyStack.isNotEmpty()) {
            val previous = historyStack.removeAt(historyStack.lastIndex)
            _uiState.update {
                it.copy(
                    playState = previous,
                    canUndo = historyStack.isNotEmpty(),
                    showWinDialog = false,
                    showFailDialog = false
                )
            }
        }
    }

    fun reset() {
        val level = _uiState.value.level ?: return
        historyStack.clear()
        _uiState.update {
            it.copy(
                playState = GameEngine.initialize(level),
                canUndo = false,
                showWinDialog = false,
                showFailDialog = false
            )
        }
    }

    fun toggleHint(show: Boolean) {
        _uiState.update { it.copy(showHintDialog = show) }
    }

    fun toggleShare(show: Boolean) {
        _uiState.update { it.copy(showShareDialog = show) }
    }

    private fun calculateStars(moves: Int, par: Int): Int {
        return when {
            moves <= par -> 3
            moves <= (par * 1.4).toInt() -> 2
            else -> 1
        }
    }

    private fun triggerHaptic(event: GameEvent?) {
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
                    val duration = when (event) {
                        is GameEvent.PushCrate -> 25L
                        is GameEvent.CollectStar, is GameEvent.CollectKey -> 40L
                        is GameEvent.UnlockDoor -> 60L
                        is GameEvent.WarpTeleport -> 80L
                        is GameEvent.Victory -> 120L
                        is GameEvent.HazardDeath -> 150L
                        else -> 12L
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(duration)
                    }
                }
            }
        } catch (_: Exception) {
            // Graceful fallback if permission or hardware absent
        }
    }
}
