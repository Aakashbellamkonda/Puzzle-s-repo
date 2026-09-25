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
import com.example.data.db.PuzzleDatabase
import com.example.data.jigsaw.JigsawRepository
import com.example.ui.jigsaw.JigsawEditorScreen
import com.example.ui.jigsaw.JigsawEditorViewModel
import com.example.ui.jigsaw.JigsawHubScreen
import com.example.ui.jigsaw.JigsawHubViewModel
import com.example.ui.jigsaw.JigsawPlayScreen
import com.example.ui.jigsaw.JigsawPlayViewModel
import com.example.ui.navigation.Screen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = PuzzleDatabase.getDatabase(applicationContext)
        val jigsawRepository = JigsawRepository(database.jigsawLevelDao())

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    JigsawApp(repository = jigsawRepository)
                }
            }
        }
    }
}

@Composable
fun JigsawApp(repository: JigsawRepository) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf<Screen>(Screen.JigsawHub) }
    val backStack = remember { mutableStateListOf<Screen>() }

    fun navigateTo(screen: Screen) {
        backStack.add(currentScreen)
        currentScreen = screen
    }

    fun navigateBack() {
        if (backStack.isNotEmpty()) {
            currentScreen = backStack.removeAt(backStack.lastIndex)
        } else {
            currentScreen = Screen.JigsawHub
        }
    }

    BackHandler(enabled = currentScreen !is Screen.JigsawHub) {
        navigateBack()
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "JigsawScreenTransition"
    ) { screen ->
        when (screen) {
            is Screen.JigsawHub -> {
                val hubViewModel = remember { JigsawHubViewModel(repository) }
                JigsawHubScreen(
                    viewModel = hubViewModel,
                    onPlayLevel = { levelId -> navigateTo(Screen.JigsawPlay(levelId)) },
                    onEditLevel = { levelId -> navigateTo(Screen.JigsawEditor(levelId)) },
                    onCreateNewLevel = { navigateTo(Screen.JigsawEditor(0L)) }
                )
            }
            is Screen.JigsawEditor -> {
                val editorViewModel = remember(screen.levelId) {
                    JigsawEditorViewModel(repository, screen.levelId)
                }
                JigsawEditorScreen(
                    viewModel = editorViewModel,
                    onNavigateBack = { navigateBack() },
                    onTestPlay = { testLevel ->
                        navigateTo(Screen.JigsawPlay(levelId = 0L, testLevel = testLevel))
                    }
                )
            }
            is Screen.JigsawPlay -> {
                val playViewModel = remember(screen.levelId, screen.testLevel) {
                    JigsawPlayViewModel(repository, context, screen.levelId, screen.testLevel)
                }
                JigsawPlayScreen(
                    viewModel = playViewModel,
                    onNavigateBack = { navigateBack() },
                    onOpenEditor = { id -> navigateTo(Screen.JigsawEditor(id)) }
                )
            }
        }
    }
}
