package com.example.data.sample

import com.example.data.model.Difficulty
import com.example.data.model.LevelData
import com.example.data.model.TileType
import com.example.data.model.WinCondition

object BuiltInLevels {
    fun getList(): List<LevelData> = listOf(
        createLevel1(),
        createLevel2(),
        createLevel3(),
        createLevel4(),
        createLevel5(),
        createLevel6()
    )

    private fun parseAscii(
        title: String,
        author: String,
        winCondition: WinCondition,
        parMoves: Int,
        difficulty: Difficulty,
        hint: String,
        rows: List<String>
    ): LevelData {
        val height = rows.size
        val width = rows[0].length
        val tiles = ArrayList<TileType>(width * height)
        for (row in rows) {
            for (ch in row) {
                tiles.add(TileType.fromCode(ch.toString()))
            }
        }
        return LevelData(
            id = 0,
            title = title,
            author = author,
            width = width,
            height = height,
            grid = tiles,
            winCondition = winCondition,
            parMoves = parMoves,
            difficulty = difficulty,
            hint = hint,
            isBuiltIn = true
        )
    }

    private fun createLevel1(): LevelData {
        return parseAscii(
            title = "01: First Steps",
            author = "PuzzleCraft",
            winCondition = WinCondition.REACH_EXIT,
            parMoves = 8,
            difficulty = Difficulty.EASY,
            hint = "Navigate past the walls to reach the glowing exit portal.",
            rows = listOf(
                "######",
                "#P...#",
                "###..#",
                "#...##",
                "#..E.#",
                "######"
            )
        )
    }

    private fun createLevel2(): LevelData {
        return parseAscii(
            title = "02: Box Pusher Alley",
            author = "PuzzleCraft",
            winCondition = WinCondition.PUSH_ALL_TARGETS,
            parMoves = 14,
            difficulty = Difficulty.EASY,
            hint = "Push both wooden crates onto the glowing target pads.",
            rows = listOf(
                "#######",
                "#..O..#",
                "#..B..#",
                "#.P...#",
                "#..B..#",
                "#..O..#",
                "#######"
            )
        )
    }

    private fun createLevel3(): LevelData {
        return parseAscii(
            title = "03: Frost Slide Cavern",
            author = "PuzzleCraft",
            winCondition = WinCondition.COLLECT_AND_EXIT,
            parMoves = 12,
            difficulty = Difficulty.MEDIUM,
            hint = "Ice tiles cause you to slide continuously until you hit a wall or normal floor!",
            rows = listOf(
                "########",
                "#P~~~~*#",
                "#.~~~#.#",
                "#.###..#",
                "#~~~~~~#",
                "#*#~~~~#",
                "#....E.#",
                "########"
            )
        )
    }

    private fun createLevel4(): LevelData {
        return parseAscii(
            title = "04: The Golden Vault",
            author = "PuzzleCraft",
            winCondition = WinCondition.REACH_EXIT,
            parMoves = 18,
            difficulty = Difficulty.MEDIUM,
            hint = "Step on the pressure switch (S) to toggle barriers, and find the Gold Key (K) for the Gold Door (D).",
            rows = listOf(
                "########",
                "#P...#K#",
                "#.#G##D#",
                "#.S....#",
                "####g###",
                "#......#",
                "#...E..#",
                "########"
            )
        )
    }

    private fun createLevel5(): LevelData {
        return parseAscii(
            title = "05: Warp Zone Nexus",
            author = "PuzzleCraft",
            winCondition = WinCondition.COLLECT_ALL_STARS,
            parMoves = 16,
            difficulty = Difficulty.HARD,
            hint = "Step into Portal 1 to emerge at its pair! Collect all three cosmic star gems.",
            rows = listOf(
                "#########",
                "#P...#.*#",
                "#..1.#..#",
                "###..####",
                "#*...1..#",
                "####..###",
                "#..2.#.2#",
                "#.*..#..#",
                "#########"
            )
        )
    }

    private fun createLevel6(): LevelData {
        return parseAscii(
            title = "06: Conveyor Express",
            author = "PuzzleCraft",
            winCondition = WinCondition.PUSH_ALL_TARGETS,
            parMoves = 22,
            difficulty = Difficulty.HARD,
            hint = "Conveyor arrows push objects along their direction. Use them to maneuver crates!",
            rows = listOf(
                "########",
                "#P.....#",
                "#.>>>>v#",
                "#.^B..v#",
                "#.^..Bv#",
                "#.^<<<<#",
                "#..OO..#",
                "########"
            )
        )
    }
}
