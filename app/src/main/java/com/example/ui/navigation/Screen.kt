package com.example.ui.navigation

import com.example.data.jigsaw.JigsawLevel

sealed class Screen {
    object JigsawHub : Screen()
    data class JigsawEditor(val levelId: Long = 0) : Screen()
    data class JigsawPlay(val levelId: Long = 0, val testLevel: JigsawLevel? = null) : Screen()
}
