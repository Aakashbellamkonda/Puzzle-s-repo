package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PuzzleCraft Studio", appName)
  }

  @Test
  fun `level share codec round trip`() {
    val original = com.example.data.model.LevelData(
        title = "Mystery Chamber",
        author = "Alex",
        width = 5,
        height = 5,
        winCondition = com.example.data.model.WinCondition.COLLECT_ALL_STARS,
        parMoves = 12,
        moveLimit = 20,
        difficulty = com.example.data.model.Difficulty.HARD,
        hint = "Follow the stars"
    ).withTile(0, 0, com.example.data.model.TileType.PLAYER_SPAWN)
        .withTile(2, 2, com.example.data.model.TileType.STAR_GEM)
        .withTile(4, 4, com.example.data.model.TileType.ICE)

    val code = com.example.data.codec.LevelShareCodec.encode(original)
    org.junit.Assert.assertTrue(code.startsWith("PZLCRAFT:1:"))

    val decodedResult = com.example.data.codec.LevelShareCodec.decode(code)
    org.junit.Assert.assertTrue(decodedResult.isSuccess)
    val decoded = decodedResult.getOrThrow()

    assertEquals(original.title, decoded.title)
    assertEquals(original.author, decoded.author)
    assertEquals(original.width, decoded.width)
    assertEquals(original.height, decoded.height)
    assertEquals(original.winCondition, decoded.winCondition)
    assertEquals(original.parMoves, decoded.parMoves)
    assertEquals(original.difficulty, decoded.difficulty)
    assertEquals(original.hint, decoded.hint)
    assertEquals(com.example.data.model.TileType.PLAYER_SPAWN, decoded.getTile(0, 0))
    assertEquals(com.example.data.model.TileType.STAR_GEM, decoded.getTile(2, 2))
    assertEquals(com.example.data.model.TileType.ICE, decoded.getTile(4, 4))
  }
}
