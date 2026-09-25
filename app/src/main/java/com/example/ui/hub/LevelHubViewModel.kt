package com.example.ui.hub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.LevelRepository
import com.example.data.model.Difficulty
import com.example.data.model.LevelData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HubTab(val label: String) {
    CAMPAIGN("Campaign"),
    MY_CREATIONS("My Creations"),
    FAVORITES("Favorites")
}

data class LevelHubUiState(
    val selectedTab: HubTab = HubTab.CAMPAIGN,
    val searchQuery: String = "",
    val difficultyFilter: Difficulty? = null,
    val showImportDialog: Boolean = false,
    val shareLevelTarget: LevelData? = null,
    val deleteConfirmTarget: LevelData? = null,
    val snackbarMessage: String? = null
)

class LevelHubViewModel(
    private val repository: LevelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LevelHubUiState())
    val uiState: StateFlow<LevelHubUiState> = _uiState.asStateFlow()

    val levels: StateFlow<List<LevelData>> = combine(
        repository.allLevels,
        _uiState
    ) { all, state ->
        val tabFiltered = when (state.selectedTab) {
            HubTab.CAMPAIGN -> all.filter { it.isBuiltIn }
            HubTab.MY_CREATIONS -> all.filter { !it.isBuiltIn }
            HubTab.FAVORITES -> all.filter { it.isFavorite }
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

    fun selectTab(tab: HubTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setDifficultyFilter(diff: Difficulty?) {
        _uiState.update { it.copy(difficultyFilter = diff) }
    }

    fun toggleFavorite(level: LevelData) {
        viewModelScope.launch {
            repository.toggleFavorite(level.id, !level.isFavorite)
        }
    }

    fun toggleImportDialog(show: Boolean) {
        _uiState.update { it.copy(showImportDialog = show) }
    }

    fun setShareTarget(level: LevelData?) {
        _uiState.update { it.copy(shareLevelTarget = level) }
    }

    fun setDeleteConfirmTarget(level: LevelData?) {
        _uiState.update { it.copy(deleteConfirmTarget = level) }
    }

    fun deleteLevel(id: Long) {
        viewModelScope.launch {
            repository.deleteLevel(id)
            _uiState.update { it.copy(deleteConfirmTarget = null, snackbarMessage = "Level deleted") }
        }
    }

    fun duplicateLevel(id: Long) {
        viewModelScope.launch {
            repository.duplicateLevel(id)
            _uiState.update { it.copy(snackbarMessage = "Level duplicated into My Creations") }
        }
    }

    fun importLevel(level: LevelData, onImported: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.saveLevel(level.copy(id = 0, isBuiltIn = false))
            _uiState.update { it.copy(showImportDialog = false, snackbarMessage = "Level imported successfully!") }
            onImported(id)
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
