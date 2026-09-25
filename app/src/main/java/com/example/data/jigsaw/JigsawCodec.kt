package com.example.data.jigsaw

import com.example.data.model.Difficulty
import org.json.JSONObject
import java.nio.charset.StandardCharsets

object JigsawCodec {
    private const val PREFIX = "JIGSAW:1:"

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

    fun encode(level: JigsawLevel): String {
        val json = JSONObject().apply {
            put("t", level.title)
            put("a", level.author)
            put("img", level.imageKey)
            put("uri", level.customImageUri ?: "")
            put("r", level.rows)
            put("c", level.cols)
            put("seed", level.edgeSeed)
            put("cut", level.cutStyle.name)
            put("wc", level.winCondition.name)
            put("tlim", level.timeLimitSeconds)
            put("par", level.parTimeSeconds)
            put("moves", level.maxMoves)
            put("rot", level.rotatePieces)
            put("anch", level.anchorPiecesCount)
            put("diff", level.difficulty.name)
            put("hint", level.hint)
        }

        val jsonBytes = json.toString().toByteArray(StandardCharsets.UTF_8)
        return PREFIX + encodeBase64(jsonBytes)
    }

    fun decode(rawCode: String): Result<JigsawLevel> {
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
            val json = JSONObject(String(decodedBytes, StandardCharsets.UTF_8))

            val uriStr = json.optString("uri", "")

            JigsawLevel(
                id = 0,
                title = json.optString("t", "Shared Jigsaw"),
                author = json.optString("a", "Community Artist"),
                imageKey = json.optString("img", "cosmic_nebula"),
                customImageUri = if (uriStr.isBlank()) null else uriStr,
                rows = json.optInt("r", 3),
                cols = json.optInt("c", 3),
                edgeSeed = json.optLong("seed", 12345L),
                cutStyle = runCatching { JigsawCutStyle.valueOf(json.optString("cut")) }.getOrDefault(JigsawCutStyle.CLASSIC_JIGSAW),
                winCondition = runCatching { JigsawWinCondition.valueOf(json.optString("wc")) }.getOrDefault(JigsawWinCondition.STANDARD),
                timeLimitSeconds = json.optInt("tlim", 180),
                parTimeSeconds = json.optInt("par", 90),
                maxMoves = json.optInt("moves", 30),
                rotatePieces = json.optBoolean("rot", false),
                anchorPiecesCount = json.optInt("anch", 0),
                difficulty = Difficulty.fromName(json.optString("diff")),
                hint = json.optString("hint", ""),
                isBuiltIn = false
            )
        }
    }
}
