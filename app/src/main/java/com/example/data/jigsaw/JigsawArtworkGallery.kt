package com.example.data.jigsaw

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

data class ArtworkTheme(
    val key: String,
    val name: String,
    val description: String,
    val primaryColor: Color
)

object JigsawArtworkGallery {

    val builtInThemes = listOf(
        ArtworkTheme("cosmic_nebula", "Cosmic Nebula", "Deep space galaxy with glowing celestial rings", Color(0xFF8B5CF6)),
        ArtworkTheme("sunset_mountains", "Sunset Ridge", "Golden twilight over alpine mountain silhouettes", Color(0xFFF59E0B)),
        ArtworkTheme("cyber_city", "Cyber City", "Futuristic synthwave neon skyline & grid", Color(0xFF06B6D4)),
        ArtworkTheme("forest_magic", "Enchanted Forest", "Mystical glowing woodland glade & mushrooms", Color(0xFF10B981)),
        ArtworkTheme("geometric_mandala", "Golden Mandala", "Sacred geometric lotus petals & radiant gold rings", Color(0xFFEAB308)),
        ArtworkTheme("tropical_reef", "Coral Kingdom", "Vibrant underwater ocean life & sea turtle", Color(0xFF0284C7))
    )

    fun getTheme(key: String): ArtworkTheme {
        return builtInThemes.firstOrNull { it.key == key } ?: builtInThemes[0]
    }

    /**
     * Renders the complete puzzle artwork within the given DrawScope bounds.
     */
    fun renderArtwork(scope: DrawScope, key: String, size: Size) {
        when (key) {
            "cosmic_nebula" -> renderCosmicNebula(scope, size)
            "sunset_mountains" -> renderSunsetMountains(scope, size)
            "cyber_city" -> renderCyberCity(scope, size)
            "forest_magic" -> renderEnchantedForest(scope, size)
            "geometric_mandala" -> renderGoldenMandala(scope, size)
            "tropical_reef" -> renderTropicalReef(scope, size)
            else -> renderCosmicNebula(scope, size)
        }
    }

    private fun renderCosmicNebula(s: DrawScope, size: Size) {
        val w = size.width
        val h = size.height

        // Deep cosmos background
        s.drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF3B0764), Color(0xFF1E1B4B), Color(0xFF050515)),
                center = Offset(w * 0.4f, h * 0.35f),
                radius = w * 0.8f
            ),
            size = size
        )

        // Nebula cloud sweeps
        s.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFE879F9).copy(alpha = 0.45f), Color.Transparent),
                center = Offset(w * 0.3f, h * 0.4f),
                radius = w * 0.45f
            ),
            radius = w * 0.45f,
            center = Offset(w * 0.3f, h * 0.4f)
        )
        s.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.4f), Color.Transparent),
                center = Offset(w * 0.7f, h * 0.6f),
                radius = w * 0.4f
            ),
            radius = w * 0.4f,
            center = Offset(w * 0.7f, h * 0.6f)
        )

        // Giant ringed planet
        val planetCenter = Offset(w * 0.72f, h * 0.32f)
        val planetR = w * 0.16f
        s.drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFFDE047), Color(0xFFF97316), Color(0xFF7C2D12)),
                start = Offset(planetCenter.x - planetR, planetCenter.y - planetR),
                end = Offset(planetCenter.x + planetR, planetCenter.y + planetR)
            ),
            radius = planetR,
            center = planetCenter
        )

        // Planet rings
        val ringPath = Path().apply {
            moveTo(planetCenter.x - planetR * 1.9f, planetCenter.y - planetR * 0.3f)
            cubicTo(
                planetCenter.x - planetR * 1.5f, planetCenter.y + planetR * 0.8f,
                planetCenter.x + planetR * 1.5f, planetCenter.y + planetR * 0.8f,
                planetCenter.x + planetR * 1.9f, planetCenter.y - planetR * 0.3f
            )
        }
        s.drawPath(
            path = ringPath,
            color = Color(0xFFFBBF24).copy(alpha = 0.85f),
            style = Stroke(width = w * 0.025f)
        )

        // Moon
        s.drawCircle(
            color = Color(0xFF67E8F9),
            radius = w * 0.045f,
            center = Offset(w * 0.22f, h * 0.68f)
        )

        // Constellation lines & stars
        val stars = listOf(
            Offset(w * 0.15f, h * 0.2f),
            Offset(w * 0.25f, h * 0.12f),
            Offset(w * 0.42f, h * 0.18f),
            Offset(w * 0.5f, h * 0.28f),
            Offset(w * 0.18f, h * 0.45f),
            Offset(w * 0.85f, h * 0.8f),
            Offset(w * 0.6f, h * 0.85f),
            Offset(w * 0.35f, h * 0.85f)
        )
        for (i in 0 until stars.size - 1) {
            s.drawLine(
                color = Color.White.copy(alpha = 0.3f),
                start = stars[i],
                end = stars[i + 1],
                strokeWidth = 1.5f
            )
        }
        stars.forEach { pos ->
            s.drawCircle(color = Color.White, radius = 3.5f, center = pos)
            s.drawCircle(color = Color(0xFFA78BFA).copy(alpha = 0.4f), radius = 7f, center = pos)
        }
    }

    private fun renderSunsetMountains(s: DrawScope, size: Size) {
        val w = size.width
        val h = size.height

        // Sky gradient
        s.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF311042), Color(0xFF7E22CE), Color(0xFFEA580C), Color(0xFFFACC15), Color(0xFFFED7AA))
            ),
            size = size
        )

        // Golden Sun
        s.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFFBEB), Color(0xFFFEF08A), Color(0xFFF59E0B).copy(alpha = 0.2f)),
                center = Offset(w * 0.5f, h * 0.42f),
                radius = w * 0.24f
            ),
            radius = w * 0.2f,
            center = Offset(w * 0.5f, h * 0.42f)
        )

        // Distant Mountains
        val m1 = Path().apply {
            moveTo(0f, h * 0.55f)
            lineTo(w * 0.25f, h * 0.42f)
            lineTo(w * 0.52f, h * 0.54f)
            lineTo(w * 0.78f, h * 0.4f)
            lineTo(w, h * 0.56f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        s.drawPath(m1, color = Color(0xFF6B21A8).copy(alpha = 0.85f))

        // Mid-range Mountains
        val m2 = Path().apply {
            moveTo(0f, h * 0.65f)
            lineTo(w * 0.35f, h * 0.5f)
            lineTo(w * 0.65f, h * 0.66f)
            lineTo(w * 0.88f, h * 0.52f)
            lineTo(w, h * 0.68f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        s.drawPath(m2, color = Color(0xFF431407))

        // Lake reflection in foreground
        s.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFB45309).copy(alpha = 0.7f), Color(0xFF1C1917)),
                startY = h * 0.75f,
                endY = h
            ),
            topLeft = Offset(0f, h * 0.75f),
            size = Size(w, h * 0.25f)
        )

        // Pine trees silhouette along shore
        for (i in 0..12) {
            val tx = (w / 12f) * i + w * 0.02f
            val th = h * (0.08f + (i % 3) * 0.03f)
            val base = h * 0.75f
            val tree = Path().apply {
                moveTo(tx, base - th)
                lineTo(tx + w * 0.035f, base)
                lineTo(tx - w * 0.035f, base)
                close()
            }
            s.drawPath(tree, color = Color(0xFF0F172A))
        }
    }

    private fun renderCyberCity(s: DrawScope, size: Size) {
        val w = size.width
        val h = size.height

        // Dark retro synthwave sky
        s.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF09090B), Color(0xFF18181B), Color(0xFF3F0A5E), Color(0xFF831843))
            ),
            size = size
        )

        // Neon Sun with horizontal slats
        val sunCenter = Offset(w * 0.5f, h * 0.45f)
        val sunRadius = w * 0.22f
        s.drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFDE047), Color(0xFFEC4899), Color(0xFF701A75)),
                startY = sunCenter.y - sunRadius,
                endY = sunCenter.y + sunRadius
            ),
            radius = sunRadius,
            center = sunCenter
        )

        // Sun cuts
        for (i in 1..5) {
            val yCut = sunCenter.y + sunRadius * (i * 0.16f)
            s.drawRect(
                color = Color(0xFF09090B),
                topLeft = Offset(sunCenter.x - sunRadius, yCut),
                size = Size(sunRadius * 2f, 4f)
            )
        }

        // City skyline buildings
        val buildingWidths = listOf(0.12f, 0.10f, 0.14f, 0.09f, 0.15f, 0.11f, 0.13f, 0.16f)
        var curX = 0f
        val heights = listOf(0.35f, 0.45f, 0.28f, 0.50f, 0.40f, 0.32f, 0.48f, 0.36f)

        buildingWidths.forEachIndexed { idx, bw ->
            val bh = heights[idx % heights.size] * h
            val bWidth = bw * w
            val topY = h * 0.65f - bh

            s.drawRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(curX, topY),
                size = Size(bWidth, bh + h * 0.35f)
            )

            // Neon roof outline
            s.drawLine(
                color = if (idx % 2 == 0) Color(0xFF06B6D4) else Color(0xFFF43F5E),
                start = Offset(curX, topY),
                end = Offset(curX + bWidth, topY),
                strokeWidth = 3f
            )

            // Glowing Windows
            for (row in 0..6) {
                for (col in 0..2) {
                    val wx = curX + bWidth * (0.25f + col * 0.28f)
                    val wy = topY + 20f + row * 22f
                    if (wy < h * 0.65f) {
                        s.drawRect(
                            color = if ((row + col + idx) % 2 == 0) Color(0xFF38BDF8) else Color(0xFFFDE047),
                            topLeft = Offset(wx, wy),
                            size = Size(4f, 6f)
                        )
                    }
                }
            }
            curX += bWidth
        }

        // Perspective Neon Grid Floor
        val horizonY = h * 0.68f
        s.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF020617), Color(0xFF0F172A)),
                startY = horizonY,
                endY = h
            ),
            topLeft = Offset(0f, horizonY),
            size = Size(w, h - horizonY)
        )

        // Grid lines
        for (i in 0..10) {
            val fx = w * (i / 10f)
            s.drawLine(
                color = Color(0xFF06B6D4).copy(alpha = 0.5f),
                start = Offset(w * 0.5f, horizonY),
                end = Offset(fx, h),
                strokeWidth = 2f
            )
        }
        for (j in 1..5) {
            val hy = horizonY + (h - horizonY) * (j * j / 25f)
            s.drawLine(
                color = Color(0xFFEC4899).copy(alpha = 0.6f),
                start = Offset(0f, hy),
                end = Offset(w, hy),
                strokeWidth = 2f
            )
        }
    }

    private fun renderEnchantedForest(s: DrawScope, size: Size) {
        val w = size.width
        val h = size.height

        // Lush deep forest background
        s.drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF14532D), Color(0xFF064E3B), Color(0xFF022C22)),
                center = Offset(w * 0.5f, h * 0.4f),
                radius = w * 0.8f
            ),
            size = size
        )

        // Sunbeams through canopy
        val beamPath = Path().apply {
            moveTo(w * 0.4f, 0f)
            lineTo(w * 0.6f, 0f)
            lineTo(w * 0.9f, h)
            lineTo(w * 0.1f, h)
            close()
        }
        s.drawPath(beamPath, color = Color(0xFFFEF08A).copy(alpha = 0.15f))

        // Giant Ancient Tree Trunk
        val trunk = Path().apply {
            moveTo(w * 0.45f, 0f)
            lineTo(w * 0.62f, 0f)
            lineTo(w * 0.72f, h)
            lineTo(w * 0.35f, h)
            close()
        }
        s.drawPath(trunk, color = Color(0xFF3E2723))

        // Fairy Treehouse on trunk
        s.drawRoundRect(
            color = Color(0xFF854D0E),
            topLeft = Offset(w * 0.38f, h * 0.35f),
            size = Size(w * 0.3f, h * 0.22f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f)
        )
        // Roof
        val roof = Path().apply {
            moveTo(w * 0.34f, h * 0.35f)
            lineTo(w * 0.53f, h * 0.25f)
            lineTo(w * 0.72f, h * 0.35f)
            close()
        }
        s.drawPath(roof, color = Color(0xFF065F46))

        // Glowing window
        s.drawCircle(
            color = Color(0xFFFDE047),
            radius = w * 0.05f,
            center = Offset(w * 0.53f, h * 0.44f)
        )

        // Glowing Mushrooms at base
        val shroomColors = listOf(Color(0xFFF43F5E), Color(0xFF38BDF8), Color(0xFFA855F7))
        for (i in 0..5) {
            val mx = w * (0.15f + i * 0.14f)
            val my = h * (0.85f + (i % 2) * 0.05f)
            s.drawCircle(color = shroomColors[i % 3], radius = 14f, center = Offset(mx, my))
            s.drawCircle(color = Color.White, radius = 4f, center = Offset(mx, my - 4f))
            s.drawCircle(color = shroomColors[i % 3].copy(alpha = 0.3f), radius = 24f, center = Offset(mx, my))
        }

        // Fireflies
        for (i in 0..15) {
            val fx = w * ((i * 37 % 100) / 100f)
            val fy = h * ((i * 53 % 100) / 100f)
            s.drawCircle(color = Color(0xFFFDE047), radius = 3f, center = Offset(fx, fy))
            s.drawCircle(color = Color(0xFFFDE047).copy(alpha = 0.25f), radius = 8f, center = Offset(fx, fy))
        }
    }

    private fun renderGoldenMandala(s: DrawScope, size: Size) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Dark teal & navy gradient
        s.drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF042F2E), Color(0xFF0F172A), Color(0xFF020617)),
                center = Offset(cx, cy),
                radius = w * 0.7f
            ),
            size = size
        )

        // Concentric radiant rings
        val radii = listOf(0.12f, 0.22f, 0.32f, 0.42f)
        radii.forEach { rFrac ->
            s.drawCircle(
                color = Color(0xFFFACC15).copy(alpha = 0.4f),
                radius = w * rFrac,
                center = Offset(cx, cy),
                style = Stroke(width = 2.5f)
            )
        }

        // Outer petals (12 petals)
        for (i in 0 until 12) {
            val angle = (i * 30.0) * Math.PI / 180.0
            val px = cx + cos(angle).toFloat() * w * 0.32f
            val py = cy + sin(angle).toFloat() * w * 0.32f
            s.drawCircle(
                color = Color(0xFF14B8A6).copy(alpha = 0.5f),
                radius = w * 0.08f,
                center = Offset(px, py)
            )
            s.drawCircle(
                color = Color(0xFFF59E0B),
                radius = w * 0.08f,
                center = Offset(px, py),
                style = Stroke(width = 2f)
            )
        }

        // Inner petals (8 petals)
        for (i in 0 until 8) {
            val angle = (i * 45.0) * Math.PI / 180.0
            val px = cx + cos(angle).toFloat() * w * 0.18f
            val py = cy + sin(angle).toFloat() * w * 0.18f
            s.drawCircle(
                color = Color(0xFFF43F5E).copy(alpha = 0.6f),
                radius = w * 0.05f,
                center = Offset(px, py)
            )
        }

        // Center Jewel
        s.drawCircle(color = Color(0xFFFDE047), radius = w * 0.06f, center = Offset(cx, cy))
        s.drawCircle(color = Color.White, radius = w * 0.02f, center = Offset(cx, cy))
    }

    private fun renderTropicalReef(s: DrawScope, size: Size) {
        val w = size.width
        val h = size.height

        // Deep ocean water gradient
        s.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0369A1), Color(0xFF0C4A6E), Color(0xFF082F49))
            ),
            size = size
        )

        // Sandy seabed with coral fans
        s.drawRect(
            color = Color(0xFFD97706).copy(alpha = 0.6f),
            topLeft = Offset(0f, h * 0.85f),
            size = Size(w, h * 0.15f)
        )

        // Sea turtle
        val tx = w * 0.38f
        val ty = h * 0.42f
        // Shell
        s.drawOval(
            color = Color(0xFF059669),
            topLeft = Offset(tx - w * 0.1f, ty - h * 0.07f),
            size = Size(w * 0.2f, h * 0.14f)
        )
        // Shell pattern
        s.drawOval(
            color = Color(0xFF10B981),
            topLeft = Offset(tx - w * 0.07f, ty - h * 0.05f),
            size = Size(w * 0.14f, h * 0.1f),
            style = Stroke(width = 3f)
        )
        // Flippers
        s.drawOval(color = Color(0xFF047857), topLeft = Offset(tx + w * 0.06f, ty - h * 0.12f), size = Size(w * 0.12f, h * 0.06f))
        s.drawOval(color = Color(0xFF047857), topLeft = Offset(tx + w * 0.06f, ty + h * 0.06f), size = Size(w * 0.12f, h * 0.06f))

        // Coral bushes at bottom
        for (i in 0..7) {
            val cx = w * (0.1f + i * 0.12f)
            val ch = h * (0.12f + (i % 3) * 0.05f)
            s.drawArc(
                color = if (i % 2 == 0) Color(0xFFF43F5E) else Color(0xFFA855F7),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(cx - w * 0.07f, h * 0.88f - ch),
                size = Size(w * 0.14f, ch * 2f)
            )
        }

        // Bubbles
        val bubbles = listOf(
            Offset(w * 0.15f, h * 0.6f),
            Offset(w * 0.18f, h * 0.5f),
            Offset(w * 0.72f, h * 0.7f),
            Offset(w * 0.75f, h * 0.55f),
            Offset(w * 0.73f, h * 0.35f),
            Offset(w * 0.82f, h * 0.25f)
        )
        bubbles.forEach { b ->
            s.drawCircle(color = Color.White.copy(alpha = 0.5f), radius = 6f, center = b, style = Stroke(width = 1.5f))
        }
    }
}
