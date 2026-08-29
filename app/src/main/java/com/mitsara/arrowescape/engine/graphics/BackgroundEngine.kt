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

    // Particles that float around procedurally
    val particles = remember {
        List(40) {
            Particle(
                xOff = Random.nextFloat(),
                yOff = Random.nextFloat(),
                speed = Random.nextFloat() * 0.5f + 0.5f,
                size = Random.nextFloat() * 4f + 2f,
                phaseX = Random.nextFloat() * 10f,
                phaseY = Random.nextFloat() * 10f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Slow gradient background
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    theme.surfaceBackgroundColor,
                    theme.boardCanvasColor
                ),
                startY = (sin(time * 0.1f) * 0.2f + 0.2f) * h,
                endY = h
            )
        )

        // 2. Grid dots procedural wave
        val dotColor = theme.gridDotColor.copy(alpha = 0.1f)
        val spacing = w * 0.15f
        var cx = spacing / 2
        while (cx < w) {
            var cy = spacing / 2
            while (cy < h) {
                val wave = sin(time * 0.5f + (cx * 0.01f) + (cy * 0.01f))
                drawCircle(
                    color = dotColor,
                    radius = 2f + wave,
                    center = Offset(cx, cy)
                )
                cy += spacing
            }
            cx += spacing
        }

        // 3. Floating particles (increases with flow state)
        val activeParticles = (15 + flowState * 8).coerceAtMost(particles.size)
        val particleColor = theme.arrowHighlightColor.copy(alpha = 0.15f + (flowState * 0.05f))
        
        for (i in 0 until activeParticles) {
            val p = particles[i]
            val px = (p.xOff * w + sin(time * p.speed + p.phaseX) * w * 0.1f) % w
            val py = (p.yOff * h - (time * 10f * (p.speed + flowState * 0.2f))) % h
            
            // Handle wrapping properly
            val finalX = if (px < 0) px + w else px
            val finalY = if (py < 0) py + h else py
            
            val pulse = (sin(time * 2f + p.phaseY) + 1f) * 0.5f
            
            drawCircle(
                color = particleColor,
                radius = p.size * (0.8f + pulse * 0.4f),
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
