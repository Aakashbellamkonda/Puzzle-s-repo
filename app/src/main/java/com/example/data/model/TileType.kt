package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class TileCategory(val title: String) {
    TERRAIN("Terrain"),
    ENTITIES("Entities"),
    MECHANISMS("Mechanics")
}

enum class TileType(
    val code: String,
    val displayName: String,
    val category: TileCategory,
    val description: String,
    val isWalkableDefault: Boolean = true,
    val isFloorLayer: Boolean = false
) {
    EMPTY(".", "Empty Floor", TileCategory.TERRAIN, "Plain walkable floor tile", isWalkableDefault = true, isFloorLayer = true),
    WALL("#", "Wall", TileCategory.TERRAIN, "Solid obstacle that blocks movement", isWalkableDefault = false),
    ICE("~", "Ice Tile", TileCategory.TERRAIN, "Slippery surface; slides until hitting an obstacle", isWalkableDefault = true, isFloorLayer = true),
    CONVEYOR_UP("^", "Conveyor Up", TileCategory.TERRAIN, "Pushes objects upward", isWalkableDefault = true, isFloorLayer = true),
    CONVEYOR_DOWN("v", "Conveyor Down", TileCategory.TERRAIN, "Pushes objects downward", isWalkableDefault = true, isFloorLayer = true),
    CONVEYOR_LEFT("<", "Conveyor Left", TileCategory.TERRAIN, "Pushes objects leftward", isWalkableDefault = true, isFloorLayer = true),
    CONVEYOR_RIGHT(">", "Conveyor Right", TileCategory.TERRAIN, "Pushes objects rightward", isWalkableDefault = true, isFloorLayer = true),
    HAZARD_SPIKES("X", "Spike Trap", TileCategory.TERRAIN, "Lethal hazard; resets the level if stepped on", isWalkableDefault = true, isFloorLayer = true),

    PLAYER_SPAWN("P", "Player Start", TileCategory.ENTITIES, "Starting position for the player", isWalkableDefault = true),
    GOAL_EXIT("E", "Exit Portal", TileCategory.ENTITIES, "Reach this to finish the level", isWalkableDefault = true),
    STAR_GEM("*", "Star Gem", TileCategory.ENTITIES, "Collectible crystal needed for win or par", isWalkableDefault = true),
    CRATE("B", "Pushable Crate", TileCategory.ENTITIES, "Box that can be pushed by the player", isWalkableDefault = false),
    TARGET_PEDESTAL("O", "Target Pad", TileCategory.ENTITIES, "Place a crate here to activate", isWalkableDefault = true, isFloorLayer = true),

    BUTTON_SWITCH("S", "Pressure Switch", TileCategory.MECHANISMS, "Toggles connected barriers when stepped on", isWalkableDefault = true, isFloorLayer = true),
    TOGGLE_GATE_CLOSED("G", "Closed Barrier", TileCategory.MECHANISMS, "Blocked barrier; opens with switch", isWalkableDefault = false),
    TOGGLE_GATE_OPEN("g", "Open Barrier", TileCategory.MECHANISMS, "Open passage; closes with switch", isWalkableDefault = true),

    KEY_GOLD("K", "Gold Key", TileCategory.MECHANISMS, "Unlocks the matching gold door", isWalkableDefault = true),
    DOOR_GOLD("D", "Gold Door", TileCategory.MECHANISMS, "Requires Gold Key to open", isWalkableDefault = false),
    KEY_BLUE("k", "Azure Key", TileCategory.MECHANISMS, "Unlocks the matching azure door", isWalkableDefault = true),
    DOOR_BLUE("d", "Azure Door", TileCategory.MECHANISMS, "Requires Azure Key to open", isWalkableDefault = false),

    TELEPORTER_A("1", "Warp Portal A", TileCategory.MECHANISMS, "Paired portal; instantly warps between matching portals", isWalkableDefault = true),
    TELEPORTER_B("2", "Warp Portal B", TileCategory.MECHANISMS, "Paired portal; instantly warps between matching portals", isWalkableDefault = true);

    companion object {
        private val codeMap = entries.associateBy { it.code }

        fun fromCode(code: String): TileType = codeMap[code] ?: EMPTY
    }
}
