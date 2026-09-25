package com.example

import com.example.data.model.LevelData
import com.example.data.model.TileType
import com.example.data.model.WinCondition
import com.example.ui.game.Direction
import com.example.ui.game.GameEngine
import com.example.ui.game.GridPos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testLevelValidation() {
        val invalidLevel = LevelData(
            title = "Invalid Test",
            width = 4,
            height = 4,
            grid = List(16) { TileType.EMPTY }
        )
        val errors = invalidLevel.validate()
        assertTrue("Should report missing player spawn", errors.any { it.contains("Player Start") })

        val validLevel = invalidLevel
            .withTile(0, 0, TileType.PLAYER_SPAWN)
            .withTile(3, 3, TileType.GOAL_EXIT)
        assertTrue("Should be valid", validLevel.validate().isEmpty())
    }

    @Test
    fun testGameEngineCratePushAndWin() {
        val level = LevelData(
            width = 5,
            height = 5,
            winCondition = WinCondition.PUSH_ALL_TARGETS
        ).withTile(1, 2, TileType.PLAYER_SPAWN)
            .withTile(2, 2, TileType.CRATE)
            .withTile(3, 2, TileType.TARGET_PEDESTAL)

        var state = GameEngine.initialize(level)
        assertEquals(GridPos(1, 2), state.playerPos)
        assertEquals(setOf(GridPos(2, 2)), state.crates)
        assertFalse(state.isWon)

        state = GameEngine.step(state, Direction.RIGHT)
        assertEquals(GridPos(2, 2), state.playerPos)
        assertEquals(setOf(GridPos(3, 2)), state.crates)
        assertEquals(1, state.moves)
        assertTrue("Player should have won by pushing crate onto target pad", state.isWon)
    }

    @Test
    fun testGameEngineIceSlide() {
        val level = LevelData(
            width = 6,
            height = 3,
            winCondition = WinCondition.REACH_EXIT
        ).withTile(1, 1, TileType.PLAYER_SPAWN)
            .withTile(2, 1, TileType.ICE)
            .withTile(3, 1, TileType.ICE)
            .withTile(4, 1, TileType.WALL)

        var state = GameEngine.initialize(level)
        state = GameEngine.step(state, Direction.RIGHT)
        assertEquals(GridPos(3, 1), state.playerPos)
    }
}
