package com.example.ui.navigation

sealed class Screen {
    object Hub : Screen()
    data class Editor(val levelId: Long = 0) : Screen()
    data class Game(val levelId: Long) : Screen()
}
