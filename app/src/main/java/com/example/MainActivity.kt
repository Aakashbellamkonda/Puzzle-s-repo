package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.db.LevelRepository
import com.example.data.db.PuzzleDatabase
import com.example.ui.editor.LevelEditorScreen
import com.example.ui.editor.LevelEditorViewModel
import com.example.ui.game.GameScreen
import com.example.ui.game.GameViewModel
import com.example.ui.hub.LevelHubScreen
import com.example.ui.hub.LevelHubViewModel
import com.example.ui.navigation.Screen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = PuzzleDatabase.getDatabase(applicationContext)
        val repository = LevelRepository(database.levelDao())

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PuzzleCraftApp(repository = repository)
                }
            }
        }
    }
}

@Composable
fun PuzzleCraftApp(repository: LevelRepository) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Hub) }
    val backStack = remember { mutableStateListOf<Screen>() }

    fun navigateTo(screen: Screen) {
        backStack.add(currentScreen)
        currentScreen = screen
    }

    fun navigateBack() {
        if (backStack.isNotEmpty()) {
            currentScreen = backStack.removeAt(backStack.lastIndex)
        } else {
            currentScreen = Screen.Hub
        }
    }

    BackHandler(enabled = currentScreen !is Screen.Hub) {
        navigateBack()
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            is Screen.Hub -> {
                val hubViewModel = remember { LevelHubViewModel(repository) }
                LevelHubScreen(
                    viewModel = hubViewModel,
                    onPlayLevel = { levelId -> navigateTo(Screen.Game(levelId)) },
                    onEditLevel = { levelId -> navigateTo(Screen.Editor(levelId)) },
                    onCreateNewLevel = { navigateTo(Screen.Editor(0L)) }
                )
            }
            is Screen.Editor -> {
                val editorViewModel = remember(screen.levelId) {
                    LevelEditorViewModel(repository, screen.levelId)
                }
                LevelEditorScreen(
                    viewModel = editorViewModel,
                    onNavigateBack = { navigateBack() }
                )
            }
            is Screen.Game -> {
                val gameViewModel = remember(screen.levelId) {
                    GameViewModel(repository, context, screen.levelId)
                }
                GameScreen(
                    viewModel = gameViewModel,
                    onNavigateBack = { navigateBack() },
                    onOpenEditor = { levelId -> navigateTo(Screen.Editor(levelId)) }
                )
            }
        }
    }
}
