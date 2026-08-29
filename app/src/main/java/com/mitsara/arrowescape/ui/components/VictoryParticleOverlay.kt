package com.mitsara.arrowescape.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun VictoryParticleOverlay(modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2500, easing = LinearEasing)
            )
        )
    }

    val particleList = remember {
        val random = Random(42)
        List(60) {
            Particle(
                x = random.nextFloat(),
                y = random.nextFloat(),
                vx = (random.nextFloat() - 0.5f) * 0.008f,
                vy = -random.nextFloat() * 0.015f - 0.005f,
                color = listOf(
                    Color(0xFFF59E0B),
                    Color(0xFF10B981),
                    Color(0xFF3B82F6),
                    Color(0xFFEC4899),
                    Color(0xFF8B5CF6)
                ).random(random),
                size = random.nextFloat() * 12f + 6f,
                angle = random.nextFloat() * 360f,
                spin = (random.nextFloat() - 0.5f) * 10f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val t = progress.value
        val canvasWidth = size.width
        val canvasHeight = size.height

        for (p in particleList) {
            val cx = (p.x + p.vx * t * 200f) % 1f * canvasWidth
            val cy = (p.y + p.vy * t * 200f + t * 0.3f) % 1f * canvasHeight
            val alpha = (1f - (t * 1.2f % 1f)).coerceIn(0f, 1f)

            drawCircle(
                color = p.color.copy(alpha = alpha),
                radius = p.size,
                center = Offset(cx, cy)
            )
        }
    }
}

data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val angle: Float,
    val spin: Float
)
