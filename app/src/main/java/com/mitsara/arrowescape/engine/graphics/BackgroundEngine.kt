package com.mitsara.arrowescape.engine.graphics

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.mitsara.arrowescape.model.GameTheme
import kotlin.math.sin
import kotlin.math.cos
import kotlin.random.Random

@Composable
fun BackgroundEngine(theme: GameTheme, flowState: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(200000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val particles = remember {
        List(50) {
            Particle(
                xOff = Random.nextFloat(),
                yOff = Random.nextFloat(),
                speed = Random.nextFloat() * 0.6f + 0.4f,
                size = Random.nextFloat() * 5f + 2f,
                phaseX = Random.nextFloat() * 10f,
                phaseY = Random.nextFloat() * 10f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Console Unique Background Gradient & Shading
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    theme.surfaceBackgroundColor,
                    theme.boardCanvasColor
                ),
                startY = 0f,
                endY = h
            )
        )

        // 2. Theme-Specific Unique Grid Lines & Pattern Background
        when (theme.id) {
            "RETRO_ARCADE" -> {
                // CRT Scanlines & Pixel Grid
                val scanlineColor = Color.Black.copy(alpha = 0.25f)
                var y = 0f
                while (y < h) {
                    drawRect(color = scanlineColor, topLeft = Offset(0f, y), size = androidx.compose.ui.geometry.Size(w, 2f))
                    y += 6f
                }
                // Retro digital grid dots
                val step = w * 0.1f
                var cx = step / 2
                while (cx < w) {
                    var cy = step / 2
                    while (cy < h) {
                        drawCircle(color = theme.gridDotColor.copy(alpha = 0.2f), radius = 3f, center = Offset(cx, cy))
                        cy += step
                    }
                    cx += step
                }
            }
            "CYBER_TERMINAL" -> {
                // Holographic Matrix Laser Grid
                val laserColor = theme.gridDotColor.copy(alpha = 0.15f)
                val spacing = w * 0.12f
                var x = 0f
                while (x < w) {
                    drawLine(color = laserColor, start = Offset(x, 0f), end = Offset(x, h), strokeWidth = 1f)
                    x += spacing
                }
                var y = 0f
                while (y < h) {
                    drawLine(color = laserColor, start = Offset(0f, y), end = Offset(w, y), strokeWidth = 1f)
                    y += spacing
                }
            }
            "ZEN_WOOD" -> {
                // Bamboo grain & Zen circles
                val zenColor = theme.gridDotColor.copy(alpha = 0.3f)
                drawCircle(color = zenColor, radius = w * 0.4f, center = Offset(w * 0.5f, h * 0.5f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f))
                drawCircle(color = zenColor, radius = w * 0.65f, center = Offset(w * 0.5f, h * 0.5f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f))
            }
            "VAPORWAVE" -> {
                // Synthwave Horizon Sun & Grid
                val horizonY = h * 0.5f
                val sunColor = Color(0xFFFF71CE).copy(alpha = 0.3f)
                drawCircle(color = sunColor, radius = w * 0.25f, center = Offset(w * 0.5f, horizonY))
                // Perspective grid lines
                val gridCol = theme.gridDotColor.copy(alpha = 0.2f)
                for (i in -5..5) {
                    drawLine(
                        color = gridCol,
                        start = Offset(w * 0.5f, horizonY),
                        end = Offset(w * 0.5f + i * w * 0.25f, h),
                        strokeWidth = 1.5f
                    )
                }
            }
            "QUANTUM_NEBULA" -> {
                // Starlight constellation nebula dust
                val starCol = theme.gridDotColor.copy(alpha = 0.3f)
                for (i in 0..30) {
                    val sx = (i * 137.5f) % w
                    val sy = (i * 243.1f) % h
                    drawCircle(color = starCol, radius = (i % 3 + 1).toFloat(), center = Offset(sx, sy))
                }
            }
        }

        // 3. Floating Console Particles
        val activeParticles = (20 + flowState * 6).coerceAtMost(particles.size)
        val particleColor = theme.arrowHighlightColor.copy(alpha = 0.2f + (flowState * 0.05f))

        for (i in 0 until activeParticles) {
            val p = particles[i]
            val px = (p.xOff * w + sin(time * p.speed + p.phaseX) * w * 0.15f) % w
            val py = (p.yOff * h - (time * 15f * p.speed)) % h
            val finalX = if (px < 0) px + w else px
            val finalY = if (py < 0) py + h else py

            drawCircle(
                color = particleColor,
                radius = p.size,
                center = Offset(finalX, finalY)
            )
        }
    }
}

private data class Particle(
    val xOff: Float,
    val yOff: Float,
    val speed: Float,
    val size: Float,
    val phaseX: Float,
    val phaseY: Float
)
