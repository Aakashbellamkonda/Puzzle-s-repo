package com.example.ui.jigsaw

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.jigsaw.JigsawLevel
import com.example.data.jigsaw.JigsawRepository
import com.example.data.model.Difficulty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class JigsawTab(val label: String) {
    GALLERY("Gallery"),
    MY_CREATIONS("My Creations"),
    FAVORITES("Favorites")
}

data class JigsawHubUiState(
    val selectedTab: JigsawTab = JigsawTab.GALLERY,
    val searchQuery: String = "",
    val difficultyFilter: Difficulty? = null,
    val showImportDialog: Boolean = false,
    val shareTarget: JigsawLevel? = null,
    val deleteTarget: JigsawLevel? = null,
    val toastMessage: String? = null
)

class JigsawHubViewModel(
    private val repository: JigsawRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JigsawHubUiState())
    val uiState: StateFlow<JigsawHubUiState> = _uiState.asStateFlow()

    val levels: StateFlow<List<JigsawLevel>> = combine(
        repository.allLevels,
        _uiState
    ) { all, state ->
        val tabFiltered = when (state.selectedTab) {
            JigsawTab.GALLERY -> all.filter { it.isBuiltIn }
            JigsawTab.MY_CREATIONS -> all.filter { !it.isBuiltIn }
            JigsawTab.FAVORITES -> all.filter { it.isFavorite }
        }

        tabFiltered.filter { level ->
            val matchesQuery = state.searchQuery.isBlank() ||
                level.title.contains(state.searchQuery, ignoreCase = true) ||
                level.author.contains(state.searchQuery, ignoreCase = true)

            val matchesDiff = state.difficultyFilter == null || level.difficulty == state.difficultyFilter

            matchesQuery && matchesDiff
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectTab(tab: JigsawTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setDifficultyFilter(diff: Difficulty?) {
        _uiState.update { it.copy(difficultyFilter = diff) }
    }

    fun toggleFavorite(level: JigsawLevel) {
        viewModelScope.launch {
            repository.toggleFavorite(level.id, !level.isFavorite)
        }
    }

    fun toggleImportDialog(show: Boolean) {
        _uiState.update { it.copy(showImportDialog = show) }
    }

    fun setShareTarget(level: JigsawLevel?) {
        _uiState.update { it.copy(shareTarget = level) }
    }

    fun setDeleteTarget(level: JigsawLevel?) {
        _uiState.update { it.copy(deleteTarget = level) }
    }

    fun deleteLevel(id: Long) {
        viewModelScope.launch {
            repository.deleteLevel(id)
            _uiState.update { it.copy(deleteTarget = null, toastMessage = "Puzzle deleted") }
        }
    }

    fun duplicateLevel(id: Long) {
        viewModelScope.launch {
            repository.duplicateLevel(id)
            _uiState.update { it.copy(toastMessage = "Duplicated to My Creations") }
        }
    }

    fun importLevel(level: JigsawLevel, onImported: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.saveLevel(level.copy(id = 0, isBuiltIn = false))
            _uiState.update { it.copy(showImportDialog = false, toastMessage = "Puzzle imported!") }
            onImported(id)
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
