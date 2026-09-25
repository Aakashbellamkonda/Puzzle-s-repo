package com.example.ui.game

import com.example.data.model.LevelData
import com.example.data.model.TileType
import com.example.data.model.WinCondition

data class GridPos(val x: Int, val y: Int)

enum class Direction(val dx: Int, val dy: Int) {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0)
}

sealed class GameEvent {
    object Move : GameEvent()
    object PushCrate : GameEvent()
    object CollectStar : GameEvent()
    object CollectKey : GameEvent()
    object UnlockDoor : GameEvent()
    object TriggerSwitch : GameEvent()
    object WarpTeleport : GameEvent()
    object IceSlide : GameEvent()
    object Victory : GameEvent()
    data class HazardDeath(val reason: String) : GameEvent()
}

data class PlayState(
    val level: LevelData,
    val playerPos: GridPos,
    val crates: Set<GridPos>,
    val collectedStars: Set<GridPos>,
    val goldKeys: Int,
    val blueKeys: Int,
    val openedGoldDoors: Set<GridPos>,
    val openedBlueDoors: Set<GridPos>,
    val collectedGoldKeys: Set<GridPos>,
    val collectedBlueKeys: Set<GridPos>,
    val isSwitchActive: Boolean,
    val moves: Int,
    val isWon: Boolean,
    val isFailed: Boolean,
    val failureReason: String? = null,
    val lastEvent: GameEvent? = null
) {
    val totalStarsCount: Int by lazy {
        level.grid.indices.count { level.grid[it] == TileType.STAR_GEM }
    }

    val totalTargetsCount: Int by lazy {
        level.grid.indices.count { level.grid[it] == TileType.TARGET_PEDESTAL }
    }

    val targetsFilledCount: Int
        get() {
            var count = 0
            for (y in 0 until level.height) {
                for (x in 0 until level.width) {
                    if (level.getTile(x, y) == TileType.TARGET_PEDESTAL && crates.contains(GridPos(x, y))) {
                        count++
                    }
                }
            }
            return count
        }

    fun isDoorOpen(pos: GridPos, tile: TileType): Boolean {
        return when (tile) {
            TileType.DOOR_GOLD -> openedGoldDoors.contains(pos)
            TileType.DOOR_BLUE -> openedBlueDoors.contains(pos)
            TileType.TOGGLE_GATE_CLOSED -> isSwitchActive // Open if switch active
            TileType.TOGGLE_GATE_OPEN -> !isSwitchActive // Open if switch inactive
            else -> false
        }
    }

    fun isPassableForPlayer(pos: GridPos): Boolean {
        if (pos.x !in 0 until level.width || pos.y !in 0 until level.height) return false
        val tile = level.getTile(pos.x, pos.y)
        if (tile == TileType.WALL) return false
        if (tile == TileType.DOOR_GOLD && !openedGoldDoors.contains(pos)) return false
        if (tile == TileType.DOOR_BLUE && !openedBlueDoors.contains(pos)) return false
        if (tile == TileType.TOGGLE_GATE_CLOSED && !isSwitchActive) return false
        if (tile == TileType.TOGGLE_GATE_OPEN && isSwitchActive) return false
        return true
    }

    fun isPassableForCrate(pos: GridPos): Boolean {
        if (pos.x !in 0 until level.width || pos.y !in 0 until level.height) return false
        if (crates.contains(pos)) return false
        val tile = level.getTile(pos.x, pos.y)
        if (tile == TileType.WALL) return false
        if (tile == TileType.DOOR_GOLD && !openedGoldDoors.contains(pos)) return false
        if (tile == TileType.DOOR_BLUE && !openedBlueDoors.contains(pos)) return false
        if (tile == TileType.TOGGLE_GATE_CLOSED && !isSwitchActive) return false
        if (tile == TileType.TOGGLE_GATE_OPEN && isSwitchActive) return false
        if (tile == TileType.HAZARD_SPIKES) return false
        return true
    }
}

object GameEngine {

    fun initialize(level: LevelData): PlayState {
        var startPos = GridPos(1, 1)
        val initialCrates = mutableSetOf<GridPos>()

        for (y in 0 until level.height) {
            for (x in 0 until level.width) {
                when (level.getTile(x, y)) {
                    TileType.PLAYER_SPAWN -> startPos = GridPos(x, y)
                    TileType.CRATE -> initialCrates.add(GridPos(x, y))
                    else -> {}
                }
            }
        }

        return PlayState(
            level = level,
            playerPos = startPos,
            crates = initialCrates,
            collectedStars = emptySet(),
            goldKeys = 0,
            blueKeys = 0,
            openedGoldDoors = emptySet(),
            openedBlueDoors = emptySet(),
            collectedGoldKeys = emptySet(),
            collectedBlueKeys = emptySet(),
            isSwitchActive = false,
            moves = 0,
            isWon = false,
            isFailed = false,
            failureReason = null,
            lastEvent = null
        )
    }

    fun step(state: PlayState, direction: Direction): PlayState {
        if (state.isWon || state.isFailed) return state

        val target = GridPos(state.playerPos.x + direction.dx, state.playerPos.y + direction.dy)

        // Boundary check
        if (target.x !in 0 until state.level.width || target.y !in 0 until state.level.height) {
            return state
        }

        var newCrates = state.crates.toMutableSet()
        var newStars = state.collectedStars.toMutableSet()
        var newGoldKeys = state.goldKeys
        var newBlueKeys = state.blueKeys
        var newOpenedGoldDoors = state.openedGoldDoors.toMutableSet()
        var newOpenedBlueDoors = state.openedBlueDoors.toMutableSet()
        var newCollectedGoldKeys = state.collectedGoldKeys.toMutableSet()
        var newCollectedBlueKeys = state.collectedBlueKeys.toMutableSet()
        var newSwitchActive = state.isSwitchActive
        var event: GameEvent = GameEvent.Move

        val targetTile = state.level.getTile(target.x, target.y)

        // Check if pushing a crate
        if (newCrates.contains(target)) {
            val crateTarget = GridPos(target.x + direction.dx, target.y + direction.dy)
            if (!state.isPassableForCrate(crateTarget)) {
                // Blocked crate
                return state
            }

            // Move crate
            newCrates.remove(target)
            val finalCratePos = resolveCrateSlide(state, crateTarget, direction, newCrates)
            newCrates.add(finalCratePos)
            event = GameEvent.PushCrate
        } else {
            // Check doors / barriers
            if (targetTile == TileType.WALL) return state

            if (targetTile == TileType.DOOR_GOLD && !state.openedGoldDoors.contains(target)) {
                if (newGoldKeys > 0) {
                    newGoldKeys--
                    newOpenedGoldDoors.add(target)
                    event = GameEvent.UnlockDoor
                } else {
                    return state // locked
                }
            }

            if (targetTile == TileType.DOOR_BLUE && !state.openedBlueDoors.contains(target)) {
                if (newBlueKeys > 0) {
                    newBlueKeys--
                    newOpenedBlueDoors.add(target)
                    event = GameEvent.UnlockDoor
                } else {
                    return state // locked
                }
            }

            if (targetTile == TileType.TOGGLE_GATE_CLOSED && !state.isSwitchActive) {
                return state // locked barrier
            }
            if (targetTile == TileType.TOGGLE_GATE_OPEN && state.isSwitchActive) {
                return state // closed barrier
            }
        }

        // Move player
        var finalPlayerPos = target

        // If player stepped on ice, slide until non-ice or obstacle
        if (state.level.getTile(finalPlayerPos.x, finalPlayerPos.y) == TileType.ICE) {
            val slideResult = resolvePlayerSlide(state, finalPlayerPos, direction, newCrates)
            finalPlayerPos = slideResult
            event = GameEvent.IceSlide
        }

        // If player stepped on conveyor
        val conveyorTile = state.level.getTile(finalPlayerPos.x, finalPlayerPos.y)
        when (conveyorTile) {
            TileType.CONVEYOR_UP -> {
                val next = GridPos(finalPlayerPos.x, finalPlayerPos.y - 1)
                if (isPosValidForPlayer(state, next, newCrates)) finalPlayerPos = next
            }
            TileType.CONVEYOR_DOWN -> {
                val next = GridPos(finalPlayerPos.x, finalPlayerPos.y + 1)
                if (isPosValidForPlayer(state, next, newCrates)) finalPlayerPos = next
            }
            TileType.CONVEYOR_LEFT -> {
                val next = GridPos(finalPlayerPos.x - 1, finalPlayerPos.y)
                if (isPosValidForPlayer(state, next, newCrates)) finalPlayerPos = next
            }
            TileType.CONVEYOR_RIGHT -> {
                val next = GridPos(finalPlayerPos.x + 1, finalPlayerPos.y)
                if (isPosValidForPlayer(state, next, newCrates)) finalPlayerPos = next
            }
            else -> {}
        }

        // Teleporter logic
        val landTile = state.level.getTile(finalPlayerPos.x, finalPlayerPos.y)
        if (landTile == TileType.TELEPORTER_A || landTile == TileType.TELEPORTER_B) {
            val destination = findTeleportPair(state.level, finalPlayerPos, landTile)
            if (destination != null && !newCrates.contains(destination)) {
                finalPlayerPos = destination
                event = GameEvent.WarpTeleport
            }
        }

        // Check hazard
        if (state.level.getTile(finalPlayerPos.x, finalPlayerPos.y) == TileType.HAZARD_SPIKES) {
            return state.copy(
                playerPos = finalPlayerPos,
                crates = newCrates,
                moves = state.moves + 1,
                isFailed = true,
                failureReason = "Fell into lethal spike trap!",
                lastEvent = GameEvent.HazardDeath("Fell into spike trap!")
            )
        }

        // Items at landing spot
        if (state.level.getTile(finalPlayerPos.x, finalPlayerPos.y) == TileType.STAR_GEM && !newStars.contains(finalPlayerPos)) {
            newStars.add(finalPlayerPos)
            event = GameEvent.CollectStar
        }

        if (state.level.getTile(finalPlayerPos.x, finalPlayerPos.y) == TileType.KEY_GOLD && !newCollectedGoldKeys.contains(finalPlayerPos)) {
            newCollectedGoldKeys.add(finalPlayerPos)
            newGoldKeys++
            event = GameEvent.CollectKey
        }

        if (state.level.getTile(finalPlayerPos.x, finalPlayerPos.y) == TileType.KEY_BLUE && !newCollectedBlueKeys.contains(finalPlayerPos)) {
            newCollectedBlueKeys.add(finalPlayerPos)
            newBlueKeys++
            event = GameEvent.CollectKey
        }

        // Pressure switch toggling (player or any crate on switch)
        var anyOnSwitch = state.level.getTile(finalPlayerPos.x, finalPlayerPos.y) == TileType.BUTTON_SWITCH
        if (!anyOnSwitch) {
            for (crate in newCrates) {
                if (state.level.getTile(crate.x, crate.y) == TileType.BUTTON_SWITCH) {
                    anyOnSwitch = true
                    break
                }
            }
        }
        newSwitchActive = anyOnSwitch

        val newMoves = state.moves + 1

        // Check Win Condition
        val isWon = checkWin(
            level = state.level,
            playerPos = finalPlayerPos,
            crates = newCrates,
            collectedStars = newStars
        )

        // Check Move Limit
        val isFailed = !isWon && state.level.moveLimit > 0 && newMoves >= state.level.moveLimit

        return PlayState(
            level = state.level,
            playerPos = finalPlayerPos,
            crates = newCrates,
            collectedStars = newStars,
            goldKeys = newGoldKeys,
            blueKeys = newBlueKeys,
            openedGoldDoors = newOpenedGoldDoors,
            openedBlueDoors = newOpenedBlueDoors,
            collectedGoldKeys = newCollectedGoldKeys,
            collectedBlueKeys = newCollectedBlueKeys,
            isSwitchActive = newSwitchActive,
            moves = newMoves,
            isWon = isWon,
            isFailed = isFailed,
            failureReason = if (isFailed) "Out of moves! Par limit reached." else null,
            lastEvent = if (isWon) GameEvent.Victory else event
        )
    }

    private fun isPosValidForPlayer(state: PlayState, pos: GridPos, crates: Set<GridPos>): Boolean {
        if (pos.x !in 0 until state.level.width || pos.y !in 0 until state.level.height) return false
        if (crates.contains(pos)) return false
        return state.isPassableForPlayer(pos)
    }

    private fun resolvePlayerSlide(state: PlayState, start: GridPos, dir: Direction, crates: Set<GridPos>): GridPos {
        var current = start
        while (true) {
            val next = GridPos(current.x + dir.dx, current.y + dir.dy)
            if (!isPosValidForPlayer(state, next, crates)) {
                return current
            }
            current = next
            // If next tile is not ice, stop sliding
            if (state.level.getTile(current.x, current.y) != TileType.ICE) {
                return current
            }
        }
    }

    private fun resolveCrateSlide(state: PlayState, start: GridPos, dir: Direction, otherCrates: Set<GridPos>): GridPos {
        var current = start
        if (state.level.getTile(current.x, current.y) != TileType.ICE) return current

        while (true) {
            val next = GridPos(current.x + dir.dx, current.y + dir.dy)
            if (!state.isPassableForCrate(next) || otherCrates.contains(next)) {
                return current
            }
            current = next
            if (state.level.getTile(current.x, current.y) != TileType.ICE) {
                return current
            }
        }
    }

    private fun findTeleportPair(level: LevelData, from: GridPos, type: TileType): GridPos? {
        for (y in 0 until level.height) {
            for (x in 0 until level.width) {
                val candidate = GridPos(x, y)
                if (candidate != from && level.getTile(x, y) == type) {
                    return candidate
                }
            }
        }
        return null
    }

    private fun checkWin(level: LevelData, playerPos: GridPos, crates: Set<GridPos>, collectedStars: Set<GridPos>): Boolean {
        val onExit = level.getTile(playerPos.x, playerPos.y) == TileType.GOAL_EXIT
        val totalStars = level.grid.count { it == TileType.STAR_GEM }
        val allStarsCollected = totalStars > 0 && collectedStars.size >= totalStars

        val targetPadPositions = mutableListOf<GridPos>()
        for (y in 0 until level.height) {
            for (x in 0 until level.width) {
                if (level.getTile(x, y) == TileType.TARGET_PEDESTAL) {
                    targetPadPositions.add(GridPos(x, y))
                }
            }
        }
        val allTargetsCovered = targetPadPositions.isNotEmpty() && targetPadPositions.all { crates.contains(it) }

        return when (level.winCondition) {
            WinCondition.REACH_EXIT -> onExit
            WinCondition.COLLECT_ALL_STARS -> allStarsCollected
            WinCondition.PUSH_ALL_TARGETS -> allTargetsCovered
            WinCondition.COLLECT_AND_EXIT -> allStarsCollected && onExit
        }
    }
}
