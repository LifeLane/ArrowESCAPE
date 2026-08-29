package com.mitsara.arrowescape.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp
import com.mitsara.arrowescape.model.Arrow
import com.mitsara.arrowescape.model.Direction
import com.mitsara.arrowescape.model.GridPoint
import kotlin.math.*
import kotlin.random.Random

/**
 * Live Animated Procedural Background Renderer for all 10 Background Cosmetics
 */
@Composable
fun CosmeticBackgroundCanvas(
    backgroundId: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CosmeticBackground")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "BgTime"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        when (backgroundId) {
            "BG_CYBER_GRID_WARP" -> drawCyberGridWarp(w, h, time)
            "BG_AURORA_BOREALIS" -> drawAuroraBorealis(w, h, time)
            "BG_MATRIX_CODE" -> drawMatrixCode(w, h, time)
            "BG_OBSIDIAN_CHASM" -> drawObsidianChasm(w, h, time)
            "BG_RETRO_SYNTHWAVE" -> drawRetroSynthwave(w, h, time)
            "BG_ZEN_BAMBOO_MIST" -> drawZenBambooMist(w, h, time)
            "BG_LAVA_FORGE" -> drawLavaForge(w, h, time)
            "BG_GOLDEN_PANTHEON" -> drawGoldenPantheon(w, h, time)
            "BG_PRISMATIC_CARNIVAL" -> drawPrismaticCarnival(w, h, time)
            else -> drawDeepCosmos(w, h, time) // Default "BG_DEEP_COSMOS"
        }
    }
}

// =========================================================================
// BACKGROUND DRAWING PROCEDURAL IMPLEMENTATIONS
// =========================================================================

private fun DrawScope.drawDeepCosmos(w: Float, h: Float, t: Float) {
    // Deep dark galaxy base
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF030712), Color(0xFF0B1120), Color(0xFF020617))
        )
    )
    // Swirling nebula clouds
    val nebulaCenter = Offset(w * 0.5f + sin(t * 0.1f) * 60f, h * 0.4f + cos(t * 0.08f) * 40f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.18f), Color(0xFF818CF8).copy(alpha = 0.08f), Color.Transparent),
            center = nebulaCenter,
            radius = w * 0.75f
        ),
        center = nebulaCenter,
        radius = w * 0.75f
    )
    val secondNebula = Offset(w * 0.7f, h * 0.75f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFC084FC).copy(alpha = 0.14f), Color.Transparent),
            center = secondNebula,
            radius = w * 0.6f
        ),
        center = secondNebula,
        radius = w * 0.6f
    )
    // Twinkling star field
    val rand = Random(4242)
    for (i in 0..45) {
        val sx = rand.nextFloat() * w
        val sy = rand.nextFloat() * h
        val twinkle = (sin(t * 0.8f + i) * 0.5f + 0.5f)
        drawCircle(
            color = Color.White.copy(alpha = 0.2f + twinkle * 0.7f),
            radius = (1.5f + rand.nextFloat() * 2f),
            center = Offset(sx, sy)
        )
    }
}

private fun DrawScope.drawCyberGridWarp(w: Float, h: Float, t: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0A0414), Color(0xFF1E0836), Color(0xFF05020A))
        )
    )
    // Glowing Horizon Line
    val horizonY = h * 0.55f
    drawLine(
        color = Color(0xFFFF007F).copy(alpha = 0.7f),
        start = Offset(0f, horizonY),
        end = Offset(w, horizonY),
        strokeWidth = 3.dp.toPx()
    )
    // Perspective Grid Lines
    val vpX = w * 0.5f
    val numLines = 14
    for (i in 0..numLines) {
        val targetX = (w / numLines) * i
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(Color.Transparent, Color(0xFF00E5FF).copy(alpha = 0.45f)),
                start = Offset(vpX, horizonY),
                end = Offset(targetX, h)
            ),
            start = Offset(vpX, horizonY),
            end = Offset(targetX, h),
            strokeWidth = 1.5.dp.toPx()
        )
    }
    // Horizontal Moving Perspective Lines
    val numHorizontal = 8
    val scroll = (t * 40f) % (h - horizonY)
    for (j in 0 until numHorizontal) {
        val progress = ((j.toFloat() / numHorizontal) + (t * 0.05f)) % 1f
        val lineY = horizonY + (progress * progress) * (h - horizonY)
        val alpha = (progress * 0.6f).coerceIn(0f, 0.6f)
        drawLine(
            color = Color(0xFFFF007F).copy(alpha = alpha),
            start = Offset(0f, lineY),
            end = Offset(w, lineY),
            strokeWidth = (1f + progress * 2.5f).dp.toPx()
        )
    }
}

private fun DrawScope.drawAuroraBorealis(w: Float, h: Float, t: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF03131D), Color(0xFF05232E), Color(0xFF020B10))
        )
    )
    // Flowing aurora ribbons
    val path1 = Path()
    path1.moveTo(0f, h * 0.35f)
    for (x in 0..w.toInt() step 20) {
        val xf = x.toFloat()
        val yf = h * 0.35f + sin((xf * 0.008f) + t * 0.15f) * 60f + cos((xf * 0.004f) + t * 0.1f) * 40f
        path1.lineTo(xf, yf)
    }
    path1.lineTo(w, h * 0.65f)
    path1.lineTo(0f, h * 0.65f)
    path1.close()

    drawPath(
        path = path1,
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color(0xFF10B981).copy(alpha = 0.25f), Color(0xFF06B6D4).copy(alpha = 0.15f), Color.Transparent)
        )
    )

    val path2 = Path()
    path2.moveTo(0f, h * 0.2f)
    for (x in 0..w.toInt() step 20) {
        val xf = x.toFloat()
        val yf = h * 0.22f + cos((xf * 0.007f) - t * 0.12f) * 70f
        path2.lineTo(xf, yf)
    }
    path2.lineTo(w, h * 0.5f)
    path2.lineTo(0f, h * 0.5f)
    path2.close()

    drawPath(
        path = path2,
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color(0xFF8B5CF6).copy(alpha = 0.2f), Color.Transparent)
        )
    )
}

private fun DrawScope.drawMatrixCode(w: Float, h: Float, t: Float) {
    drawRect(color = Color(0xFF02130C))
    val rand = Random(12345)
    val columns = 16
    val colW = w / columns
    for (col in 0 until columns) {
        val speed = 0.5f + rand.nextFloat() * 0.8f
        val offset = rand.nextFloat() * h
        val headY = ((t * speed * 80f + offset) % (h + 300f)) - 100f
        val streamLength = 220f + rand.nextFloat() * 120f

        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color(0xFF00FF99).copy(alpha = 0.15f), Color(0xFF00FF99).copy(alpha = 0.7f), Color.White),
                startY = headY - streamLength,
                endY = headY
            ),
            start = Offset(col * colW + colW * 0.5f, headY - streamLength),
            end = Offset(col * colW + colW * 0.5f, headY),
            strokeWidth = 2.dp.toPx()
        )
    }
}

private fun DrawScope.drawObsidianChasm(w: Float, h: Float, t: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF140F0A), Color(0xFF241408), Color(0xFF0C0804))
        )
    )
    // Geometric crystal facets
    val rand = Random(999)
    for (i in 0..8) {
        val cx = rand.nextFloat() * w
        val cy = rand.nextFloat() * h
        val rad = 90f + rand.nextFloat() * 80f
        val path = Path().apply {
            moveTo(cx, cy - rad)
            lineTo(cx + rad * 0.8f, cy - rad * 0.3f)
            lineTo(cx + rad * 0.6f, cy + rad * 0.8f)
            lineTo(cx - rad * 0.7f, cy + rad * 0.5f)
            close()
        }
        drawPath(
            path = path,
            color = Color(0xFF2C1A0C).copy(alpha = 0.4f)
        )
        drawPath(
            path = path,
            color = Color(0xFFF59E0B).copy(alpha = 0.15f),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

private fun DrawScope.drawRetroSynthwave(w: Float, h: Float, t: Float) {
    // Dusk sky
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF1F0638), Color(0xFF4C0E5F), Color(0xFFFF5722).copy(alpha = 0.8f), Color(0xFF0D021A))
        )
    )
    // Giant Sun with horizontal segment cutouts
    val sunCenter = Offset(w * 0.5f, h * 0.45f)
    val sunRadius = w * 0.28f
    drawCircle(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFEE55), Color(0xFFFF007F)),
            startY = sunCenter.y - sunRadius,
            endY = sunCenter.y + sunRadius
        ),
        radius = sunRadius,
        center = sunCenter
    )
    // Sun horizontal stripes
    for (s in 1..5) {
        val stripY = sunCenter.y + (s * sunRadius * 0.16f)
        val stripH = 3.dp.toPx() * s * 0.7f
        drawRect(
            color = Color(0xFF2A093D),
            topLeft = Offset(sunCenter.x - sunRadius, stripY),
            size = Size(sunRadius * 2, stripH)
        )
    }
}

private fun DrawScope.drawZenBambooMist(w: Float, h: Float, t: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF1E1915), Color(0xFF2D2520), Color(0xFF14100C))
        )
    )
    // Drifting mist layers
    for (i in 0..3) {
        val mistY = h * (0.25f + i * 0.2f)
        val shift = sin(t * 0.08f + i) * 50f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFD4C3A3).copy(alpha = 0.08f), Color.Transparent),
                center = Offset(w * 0.5f + shift, mistY),
                radius = w * 0.7f
            ),
            center = Offset(w * 0.5f + shift, mistY),
            radius = w * 0.7f
        )
    }
}

private fun DrawScope.drawLavaForge(w: Float, h: Float, t: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF1A0502), Color(0xFF3B0B04), Color(0xFF0F0201))
        )
    )
    // Molten magma heat bottom
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFF3D00).copy(alpha = 0.35f), Color(0xFFDC2626).copy(alpha = 0.15f), Color.Transparent),
            center = Offset(w * 0.5f, h * 0.9f),
            radius = w * 0.8f
        ),
        center = Offset(w * 0.5f, h * 0.9f),
        radius = w * 0.8f
    )
    // Rising sparks
    val rand = Random(777)
    for (i in 0..30) {
        val sx = rand.nextFloat() * w
        val sy = (h - ((t * 60f * (0.8f + rand.nextFloat() * 0.5f) + rand.nextFloat() * h) % h))
        drawCircle(
            color = Color(0xFFFF9100).copy(alpha = (sy / h).coerceIn(0.2f, 0.9f)),
            radius = 2.dp.toPx(),
            center = Offset(sx, sy)
        )
    }
}

private fun DrawScope.drawGoldenPantheon(w: Float, h: Float, t: Float) {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF302206), Color(0xFF181002), Color(0xFF0A0701)),
            center = Offset(w * 0.5f, h * 0.35f),
            radius = w * 0.9f
        )
    )
    // Divine sunbeams
    val center = Offset(w * 0.5f, h * 0.35f)
    val rays = 12
    for (r in 0 until rays) {
        val angle = (r * (360f / rays) + t * 0.05f) * (PI.toFloat() / 180f)
        val ex = center.x + cos(angle) * w
        val ey = center.y + sin(angle) * h
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFFDE047).copy(alpha = 0.15f), Color.Transparent),
                start = center,
                end = Offset(ex, ey)
            ),
            start = center,
            end = Offset(ex, ey),
            strokeWidth = 14.dp.toPx()
        )
    }
}

private fun DrawScope.drawPrismaticCarnival(w: Float, h: Float, t: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF1B0626), Color(0xFF2F0A44), Color(0xFF0E0214))
        )
    )
    // Chromatic colorful glowing wave orbs
    val rainbowColors = listOf(Color(0xFFFF007F), Color(0xFFFFB800), Color(0xFF00E676), Color(0xFF00E5FF), Color(0xFFAA00FF))
    for ((idx, color) in rainbowColors.withIndex()) {
        val ox = w * (0.2f + idx * 0.15f) + sin(t * 0.1f + idx) * 30f
        val oy = h * (0.3f + (idx % 3) * 0.2f) + cos(t * 0.08f + idx) * 40f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.2f), Color.Transparent),
                center = Offset(ox, oy),
                radius = w * 0.45f
            ),
            center = Offset(ox, oy),
            radius = w * 0.45f
        )
    }
}

// =========================================================================
// BOARD SURFACE PROCEDURAL TEXTURES
// =========================================================================

fun DrawScope.drawCosmeticBoardSurface(boardId: String, size: Size, baseColor: Color) {
    val w = size.width
    val h = size.height

    when (boardId) {
        "BOARD_CARBON_FIBER" -> {
            drawRoundRect(
                color = Color(0xFF111418),
                size = size,
                cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
            )
            // Carbon fiber diagonal weave lines
            val step = 14f
            for (x in 0..(w + h).toInt() step step.toInt()) {
                drawLine(
                    color = Color(0xFF1F242C).copy(alpha = 0.4f),
                    start = Offset(x.toFloat(), 0f),
                    end = Offset(0f, x.toFloat()),
                    strokeWidth = 2f
                )
            }
        }
        "BOARD_HOLO_GLASS" -> {
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E2640).copy(alpha = 0.85f), Color(0xFF0F1524).copy(alpha = 0.95f))
                ),
                size = size,
                cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
            )
            // Chromatic aberration glass reflection
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color(0xFF818CF8).copy(alpha = 0.4f), Color.Transparent),
                    start = Offset(0f, 0f),
                    end = Offset(w, h * 0.4f)
                ),
                start = Offset(0f, 0f),
                end = Offset(w, h * 0.4f),
                strokeWidth = 8.dp.toPx()
            )
        }
        "BOARD_ROYAL_MARBLE" -> {
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))
                ),
                size = size,
                cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
            )
            // Golden marble veins
            val veinPath = Path().apply {
                moveTo(w * 0.2f, 0f)
                cubicTo(w * 0.35f, h * 0.3f, w * 0.15f, h * 0.6f, w * 0.6f, h)
                moveTo(w * 0.7f, 0f)
                cubicTo(w * 0.55f, h * 0.4f, w * 0.85f, h * 0.7f, w * 0.9f, h)
            }
            drawPath(
                path = veinPath,
                color = Color(0xFFD97706).copy(alpha = 0.25f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        "BOARD_RETRO_ARCADE" -> {
            drawRoundRect(
                color = Color(0xFF12121A),
                size = size,
                cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
            )
            // CRT scanlines
            for (y in 0..h.toInt() step 6) {
                drawLine(
                    color = Color.Black.copy(alpha = 0.35f),
                    start = Offset(0f, y.toFloat()),
                    end = Offset(w, y.toFloat()),
                    strokeWidth = 1.5f
                )
            }
        }
        "BOARD_DEEP_AMETHYST" -> {
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF2E1A4E), Color(0xFF140B24)),
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = w * 0.7f
                ),
                size = size,
                cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
            )
        }
        "BOARD_STEAMPUNK_COPPER" -> {
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF4A2818), Color(0xFF2E170C)),
                    start = Offset(0f, 0f),
                    end = Offset(w, h)
                ),
                size = size,
                cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
            )
        }
        "BOARD_NEON_ACRYLIC" -> {
            drawRoundRect(
                color = Color(0xFF081220).copy(alpha = 0.95f),
                size = size,
                cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
            )
            // Inner cyan edge luminescence
            drawRoundRect(
                color = Color(0xFF00F0FF).copy(alpha = 0.2f),
                size = size,
                cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                style = Stroke(width = 4.dp.toPx())
            )
        }
        "BOARD_ZEN_CEDAR" -> {
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF3D2B1F), Color(0xFF2A1C13))
                ),
                size = size,
                cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
            )
            // Wood plank slats
            for (x in 0..w.toInt() step (w / 6).toInt()) {
                drawLine(
                    color = Color(0xFF1A1009).copy(alpha = 0.45f),
                    start = Offset(x.toFloat(), 0f),
                    end = Offset(x.toFloat(), h),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
        "BOARD_COSMIC_MIRROR" -> {
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF1B1033), Color(0xFF080512)),
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = w * 0.65f
                ),
                size = size,
                cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
            )
        }
        else -> {
            // Default Obsidian Slate
            drawRoundRect(
                color = baseColor,
                size = size,
                cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
            )
        }
    }
}

// =========================================================================
// GRID MATRIX PROCEDURAL STYLES
// =========================================================================

fun DrawScope.drawCosmeticGridSlots(
    gridId: String,
    gridWidth: Int,
    gridHeight: Int,
    cellW: Float,
    cellH: Float,
    validCells: Set<GridPoint>?,
    themeDotColor: Color
) {
    for (x in 0 until gridWidth) {
        for (y in 0 until gridHeight) {
            val p = GridPoint(x, y)
            if (validCells == null || validCells.contains(p)) {
                val left = x * cellW + 4f
                val top = y * cellH + 4f
                val w = cellW - 8f
                val h = cellH - 8f
                val centerX = x * cellW + cellW / 2
                val centerY = y * cellH + cellH / 2

                when (gridId) {
                    "GRID_HOLO_DOTS" -> {
                        // Micro crosshair targeting dot
                        drawCircle(
                            color = themeDotColor.copy(alpha = 0.7f),
                            radius = 2.5.dp.toPx(),
                            center = Offset(centerX, centerY)
                        )
                        drawLine(
                            color = themeDotColor.copy(alpha = 0.35f),
                            start = Offset(centerX - 6.dp.toPx(), centerY),
                            end = Offset(centerX + 6.dp.toPx(), centerY),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawLine(
                            color = themeDotColor.copy(alpha = 0.35f),
                            start = Offset(centerX, centerY - 6.dp.toPx()),
                            end = Offset(centerX, centerY + 6.dp.toPx()),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    "GRID_CIRCUIT_TRACES" -> {
                        // PCB gold trace node
                        drawRoundRect(
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                            topLeft = Offset(left, top),
                            size = Size(w, h),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                        drawCircle(
                            color = Color(0xFFF59E0B).copy(alpha = 0.85f),
                            radius = 3.dp.toPx(),
                            center = Offset(centerX, centerY)
                        )
                    }
                    "GRID_HONEYCOMB_HEX" -> {
                        // Hexagonal capsule slot
                        drawRoundRect(
                            color = themeDotColor.copy(alpha = 0.2f),
                            topLeft = Offset(left, top),
                            size = Size(w, h),
                            cornerRadius = CornerRadius(24f, 24f)
                        )
                        drawCircle(
                            color = themeDotColor.copy(alpha = 0.6f),
                            radius = 2.dp.toPx(),
                            center = Offset(centerX, centerY)
                        )
                    }
                    "GRID_DIAMOND_PRISM" -> {
                        val rad = minOf(w, h) * 0.28f
                        val diamond = Path().apply {
                            moveTo(centerX, centerY - rad)
                            lineTo(centerX + rad, centerY)
                            lineTo(centerX, centerY + rad)
                            lineTo(centerX - rad, centerY)
                            close()
                        }
                        drawPath(
                            path = diamond,
                            color = Color(0xFFA78BFA).copy(alpha = 0.22f)
                        )
                    }
                    "GRID_RADAR_RETICLE" -> {
                        drawRoundRect(
                            color = Color(0xFFEF4444).copy(alpha = 0.12f),
                            topLeft = Offset(left, top),
                            size = Size(w, h),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        // Corner brackets
                        drawRect(
                            color = Color(0xFFEF4444).copy(alpha = 0.6f),
                            topLeft = Offset(left, top),
                            size = Size(6f, 6f)
                        )
                        drawRect(
                            color = Color(0xFFEF4444).copy(alpha = 0.6f),
                            topLeft = Offset(left + w - 6f, top + h - 6f),
                            size = Size(6f, 6f)
                        )
                    }
                    "GRID_RETRO_SCAN" -> {
                        drawRect(
                            color = Color(0xFFFFB000).copy(alpha = 0.18f),
                            topLeft = Offset(left, top),
                            size = Size(w, h)
                        )
                        drawCircle(
                            color = Color(0xFFFFB000),
                            radius = 2.5.dp.toPx(),
                            center = Offset(centerX, centerY)
                        )
                    }
                    "GRID_CELESTIAL_STARS" -> {
                        // 4-point star node
                        val starPath = Path().apply {
                            moveTo(centerX, centerY - 6.dp.toPx())
                            lineTo(centerX + 2.dp.toPx(), centerY - 2.dp.toPx())
                            lineTo(centerX + 6.dp.toPx(), centerY)
                            lineTo(centerX + 2.dp.toPx(), centerY + 2.dp.toPx())
                            lineTo(centerX, centerY + 6.dp.toPx())
                            lineTo(centerX - 2.dp.toPx(), centerY + 2.dp.toPx())
                            lineTo(centerX - 6.dp.toPx(), centerY)
                            lineTo(centerX - 2.dp.toPx(), centerY - 2.dp.toPx())
                            close()
                        }
                        drawPath(path = starPath, color = Color(0xFFFDE047).copy(alpha = 0.75f))
                    }
                    "GRID_GOLDEN_FILIGREE" -> {
                        drawRoundRect(
                            color = Color(0xFFF59E0B).copy(alpha = 0.14f),
                            topLeft = Offset(left, top),
                            size = Size(w, h),
                            cornerRadius = CornerRadius(12f, 12f)
                        )
                        drawCircle(
                            color = Color(0xFFFBBF24),
                            radius = 3.dp.toPx(),
                            center = Offset(centerX, centerY)
                        )
                    }
                    "GRID_MINIMAL_CLEAN" -> {
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.08f),
                            topLeft = Offset(left, top),
                            size = Size(w, h),
                            cornerRadius = CornerRadius(16f, 16f)
                        )
                    }
                    else -> {
                        // Default Neon Lattice
                        drawRoundRect(
                            color = themeDotColor.copy(alpha = 0.25f),
                            topLeft = Offset(left, top),
                            size = Size(w, h),
                            cornerRadius = CornerRadius(16f, 16f)
                        )
                        drawCircle(
                            color = themeDotColor.copy(alpha = 0.6f),
                            radius = 2.dp.toPx(),
                            center = Offset(centerX, centerY)
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// FRAMES & BORDERS PROCEDURAL DRAWING
// =========================================================================

fun DrawScope.drawCosmeticFrameBorder(frameId: String, size: Size, accentColor: Color) {
    val w = size.width
    val h = size.height
    val cornerR = 24.dp.toPx()

    when (frameId) {
        "FRAME_NEON_PULSE_TUBE" -> {
            drawRoundRect(
                color = Color(0xFFFF007F).copy(alpha = 0.4f),
                size = size,
                cornerRadius = CornerRadius(cornerR, cornerR),
                style = Stroke(width = 8.dp.toPx())
            )
            drawRoundRect(
                color = Color.White,
                size = size,
                cornerRadius = CornerRadius(cornerR, cornerR),
                style = Stroke(width = 2.5.dp.toPx())
            )
        }
        "FRAME_GOLDEN_ROYAL_CHOP" -> {
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFDE047), Color(0xFFB45309), Color(0xFFFDE047)),
                    start = Offset(0f, 0f),
                    end = Offset(w, h)
                ),
                size = size,
                cornerRadius = CornerRadius(cornerR, cornerR),
                style = Stroke(width = 6.dp.toPx())
            )
            // Corner heraldic diamond gemstone
            drawCircle(color = Color(0xFFEF4444), radius = 6.dp.toPx(), center = Offset(cornerR * 0.7f, cornerR * 0.7f))
            drawCircle(color = Color(0xFFEF4444), radius = 6.dp.toPx(), center = Offset(w - cornerR * 0.7f, cornerR * 0.7f))
            drawCircle(color = Color(0xFFEF4444), radius = 6.dp.toPx(), center = Offset(cornerR * 0.7f, h - cornerR * 0.7f))
            drawCircle(color = Color(0xFFEF4444), radius = 6.dp.toPx(), center = Offset(w - cornerR * 0.7f, h - cornerR * 0.7f))
        }
        "FRAME_HOLO_SHIELD" -> {
            drawRoundRect(
                color = Color(0xFF00E5FF).copy(alpha = 0.5f),
                size = size,
                cornerRadius = CornerRadius(cornerR, cornerR),
                style = Stroke(width = 5.dp.toPx())
            )
        }
        "FRAME_STEAMPUNK_BRONZE" -> {
            drawRoundRect(
                color = Color(0xFFB45309),
                size = size,
                cornerRadius = CornerRadius(cornerR, cornerR),
                style = Stroke(width = 7.dp.toPx())
            )
        }
        "FRAME_QUANTUM_CONTAINMENT" -> {
            drawRoundRect(
                brush = Brush.sweepGradient(
                    colors = listOf(Color(0xFFA855F7), Color(0xFF6366F1), Color(0xFF00E5FF), Color(0xFFA855F7)),
                    center = Offset(w * 0.5f, h * 0.5f)
                ),
                size = size,
                cornerRadius = CornerRadius(cornerR, cornerR),
                style = Stroke(width = 6.dp.toPx())
            )
        }
        "FRAME_CARBON_CHROME" -> {
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFE2E8F0), Color(0xFF64748B), Color(0xFFE2E8F0)),
                    start = Offset(0f, 0f),
                    end = Offset(w, h)
                ),
                size = size,
                cornerRadius = CornerRadius(cornerR, cornerR),
                style = Stroke(width = 5.dp.toPx())
            )
        }
        "FRAME_RETRO_CABINET" -> {
            drawRoundRect(
                color = Color(0xFFFF3366),
                size = size,
                cornerRadius = CornerRadius(cornerR, cornerR),
                style = Stroke(width = 5.dp.toPx())
            )
        }
        "FRAME_VOID_ACCRETION" -> {
            drawRoundRect(
                color = Color(0xFF9333EA).copy(alpha = 0.6f),
                size = size,
                cornerRadius = CornerRadius(cornerR, cornerR),
                style = Stroke(width = 7.dp.toPx())
            )
        }
        "FRAME_PRISMATIC_GLOW" -> {
            drawRoundRect(
                brush = Brush.sweepGradient(
                    colors = listOf(Color(0xFFFF007F), Color(0xFFFFB800), Color(0xFF00E676), Color(0xFF00E5FF), Color(0xFFFF007F)),
                    center = Offset(w * 0.5f, h * 0.5f)
                ),
                size = size,
                cornerRadius = CornerRadius(cornerR, cornerR),
                style = Stroke(width = 6.dp.toPx())
            )
        }
        else -> {
            // Default FRAME_CYBER_BRACKETS
            val bracketLen = 36.dp.toPx()
            val strokeW = 4.dp.toPx()
            val bColor = Color(0xFF00E5FF)

            // Top-left
            drawLine(bColor, Offset(0f, bracketLen), Offset(0f, 0f), strokeW)
            drawLine(bColor, Offset(0f, 0f), Offset(bracketLen, 0f), strokeW)
            // Top-right
            drawLine(bColor, Offset(w - bracketLen, 0f), Offset(w, 0f), strokeW)
            drawLine(bColor, Offset(w, 0f), Offset(w, bracketLen), strokeW)
            // Bottom-left
            drawLine(bColor, Offset(0f, h - bracketLen), Offset(0f, h), strokeW)
            drawLine(bColor, Offset(0f, h), Offset(bracketLen, h), strokeW)
            // Bottom-right
            drawLine(bColor, Offset(w - bracketLen, h), Offset(w, h), strokeW)
            drawLine(bColor, Offset(w, h), Offset(w, h - bracketLen), strokeW)
        }
    }
}
