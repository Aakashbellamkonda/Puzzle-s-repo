package com.example.data.model

data class LevelData(
    val id: Long = 0,
    val title: String = "Untitled Puzzle",
    val author: String = "Designer",
    val width: Int = 8,
    val height: Int = 8,
    val grid: List<TileType> = List(width * height) { TileType.EMPTY },
    val winCondition: WinCondition = WinCondition.REACH_EXIT,
    val parMoves: Int = 15,
    val moveLimit: Int = 0, // 0 = unlimited
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val hint: String = "",
    val isBuiltIn: Boolean = false,
    val isFavorite: Boolean = false,
    val bestMoves: Int? = null,
    val isCompleted: Boolean = false,
    val playCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getTile(x: Int, y: Int): TileType {
        if (x !in 0 until width || y !in 0 until height) return TileType.WALL
        val index = y * width + x
        return grid.getOrElse(index) { TileType.EMPTY }
    }

    fun withTile(x: Int, y: Int, tile: TileType): LevelData {
        if (x !in 0 until width || y !in 0 until height) return this
        val index = y * width + x
        val mutable = grid.toMutableList()
        // If placing player spawn, remove any previous player spawn
        if (tile == TileType.PLAYER_SPAWN) {
            val oldIdx = mutable.indexOf(TileType.PLAYER_SPAWN)
            if (oldIdx != -1) mutable[oldIdx] = TileType.EMPTY
        }
        mutable[index] = tile
        return copy(grid = mutable)
    }

    fun resized(newWidth: Int, newHeight: Int): LevelData {
        val newGrid = MutableList(newWidth * newHeight) { TileType.EMPTY }
        for (y in 0 until minOf(height, newHeight)) {
            for (x in 0 until minOf(width, newWidth)) {
                newGrid[y * newWidth + x] = getTile(x, y)
            }
        }
        return copy(width = newWidth, height = newHeight, grid = newGrid)
    }

    fun countTiles(type: TileType): Int = grid.count { it == type }

    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        val playerCount = countTiles(TileType.PLAYER_SPAWN)
        if (playerCount == 0) {
            errors.add("Please place a Player Start position (P)")
        } else if (playerCount > 1) {
            errors.add("Only one Player Start is permitted")
        }

        val exitCount = countTiles(TileType.GOAL_EXIT)
        val starCount = countTiles(TileType.STAR_GEM)
        val crateCount = countTiles(TileType.CRATE)
        val targetCount = countTiles(TileType.TARGET_PEDESTAL)

        when (winCondition) {
            WinCondition.REACH_EXIT -> {
                if (exitCount == 0) errors.add("Reach Exit condition requires at least one Exit Portal (E)")
            }
            WinCondition.COLLECT_ALL_STARS -> {
                if (starCount == 0) errors.add("Collect Gems condition requires at least one Star Gem (*)")
            }
            WinCondition.PUSH_ALL_TARGETS -> {
                if (targetCount == 0) {
                    errors.add("Push Targets condition requires at least one Target Pad (O)")
                }
                if (crateCount < targetCount) {
                    errors.add("Need at least as many Pushable Crates ($crateCount) as Target Pads ($targetCount)")
                }
            }
            WinCondition.COLLECT_AND_EXIT -> {
                if (starCount == 0) errors.add("Requires at least one Star Gem (*)")
                if (exitCount == 0) errors.add("Requires an Exit Portal (E)")
            }
        }

        val teleA = countTiles(TileType.TELEPORTER_A)
        if (teleA == 1) errors.add("Teleporter A needs a matching pair (found only 1)")
        if (teleA > 2) errors.add("Teleporter A currently has more than 2 portals ($teleA)")

        val teleB = countTiles(TileType.TELEPORTER_B)
        if (teleB == 1) errors.add("Teleporter B needs a matching pair (found only 1)")
        if (teleB > 2) errors.add("Teleporter B currently has more than 2 portals ($teleB)")

        val goldKeys = countTiles(TileType.KEY_GOLD)
        val goldDoors = countTiles(TileType.DOOR_GOLD)
        if (goldDoors > 0 && goldKeys == 0) errors.add("Gold Door exists without a Gold Key")

        val blueKeys = countTiles(TileType.KEY_BLUE)
        val blueDoors = countTiles(TileType.DOOR_BLUE)
        if (blueDoors > 0 && blueKeys == 0) errors.add("Azure Door exists without an Azure Key")

        return errors
    }
}
