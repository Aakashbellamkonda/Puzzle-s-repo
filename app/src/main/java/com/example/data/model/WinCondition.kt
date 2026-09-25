package com.example.data.model

enum class WinCondition(val displayName: String, val shortDesc: String) {
    REACH_EXIT("Reach Exit", "Reach the glowing exit portal"),
    COLLECT_ALL_STARS("Collect Gems", "Gather every star gem on the grid"),
    PUSH_ALL_TARGETS("Fill Targets", "Push all crates onto glowing target pads"),
    COLLECT_AND_EXIT("Collect & Exit", "Collect all stars and enter the exit portal");

    companion object {
        fun fromName(name: String?): WinCondition {
            return entries.firstOrNull { it.name == name } ?: REACH_EXIT
        }
    }
}

enum class Difficulty(val label: String, val colorHex: Long) {
    EASY("Easy", 0xFF10B981),
    MEDIUM("Medium", 0xFFF59E0B),
    HARD("Hard", 0xFFEF4444),
    MASTER("Master", 0xFF8B5CF6);

    companion object {
        fun fromName(name: String?): Difficulty {
            return entries.firstOrNull { it.name == name } ?: MEDIUM
        }
    }
}
