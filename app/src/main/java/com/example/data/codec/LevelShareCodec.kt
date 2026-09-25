package com.example.data.codec

import com.example.data.model.Difficulty
import com.example.data.model.LevelData
import com.example.data.model.TileType
import com.example.data.model.WinCondition
import org.json.JSONObject
import java.nio.charset.StandardCharsets

object LevelShareCodec {
    private const val PREFIX = "PZLCRAFT:1:"

    private fun encodeBase64(bytes: ByteArray): String {
        return try {
            val encoder = java.util.Base64.getUrlEncoder().withoutPadding()
            encoder.encodeToString(bytes)
        } catch (_: Throwable) {
            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP or android.util.Base64.URL_SAFE)
        }
    }

    private fun decodeBase64(str: String): ByteArray {
        return try {
            val decoder = java.util.Base64.getUrlDecoder()
            decoder.decode(str)
        } catch (_: Throwable) {
            android.util.Base64.decode(str, android.util.Base64.NO_WRAP or android.util.Base64.URL_SAFE)
        }
    }

    fun encode(level: LevelData): String {
        val json = JSONObject().apply {
            put("t", level.title)
            put("a", level.author)
            put("w", level.width)
            put("h", level.height)
            put("wc", level.winCondition.name)
            put("par", level.parMoves)
            put("lim", level.moveLimit)
            put("diff", level.difficulty.name)
            put("hint", level.hint)
            val gridString = level.grid.joinToString(separator = "") { it.code }
            put("g", gridString)
        }

        val jsonBytes = json.toString().toByteArray(StandardCharsets.UTF_8)
        val b64 = encodeBase64(jsonBytes)
        return PREFIX + b64
    }

    fun decode(rawCode: String): Result<LevelData> {
        return runCatching {
            val trimmed = rawCode.trim()
            val b64 = if (trimmed.startsWith(PREFIX)) {
                trimmed.removePrefix(PREFIX)
            } else if (trimmed.contains(":")) {
                trimmed.substringAfterLast(":")
            } else {
                trimmed
            }

            val decodedBytes = decodeBase64(b64)
            val jsonString = String(decodedBytes, StandardCharsets.UTF_8)
            val json = JSONObject(jsonString)

            val width = json.getInt("w")
            val height = json.getInt("h")
            val gridString = json.getString("g")

            val grid = ArrayList<TileType>(width * height)
            for (ch in gridString) {
                grid.add(TileType.fromCode(ch.toString()))
            }
            while (grid.size < width * height) {
                grid.add(TileType.EMPTY)
            }

            LevelData(
                id = 0,
                title = json.optString("t", "Shared Puzzle"),
                author = json.optString("a", "Community Creator"),
                width = width,
                height = height,
                grid = grid.take(width * height),
                winCondition = WinCondition.fromName(json.optString("wc")),
                parMoves = json.optInt("par", 20),
                moveLimit = json.optInt("lim", 0),
                difficulty = Difficulty.fromName(json.optString("diff")),
                hint = json.optString("hint", ""),
                isBuiltIn = false
            )
        }
    }
}
