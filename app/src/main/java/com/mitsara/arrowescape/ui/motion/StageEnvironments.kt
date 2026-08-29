package com.mitsara.arrowescape.ui.motion

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.*
import kotlin.random.Random

/**
 * Stage Visual Identity Metadata
 */
data class StageVisualProfile(
    val stageIndex: Int,
    val name: String,
    val subtitle: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentGlow: Color,
    val backgroundBase: Color,
    val iconSymbol: String
)

object StageProfiles {
    val profiles = listOf(
        StageVisualProfile(1, "The Awakening", "Mysterious Neon Awakening", Color(0xFF3B82F6), Color(0xFF8B5CF6), Color(0xFF60A5FA), Color(0xFF090D1A), "✧"),
        StageVisualProfile(2, "Neon Lattice", "Cyber Matrix Grid", Color(0xFF06B6D4), Color(0xFFEC4899), Color(0xFF22D3EE), Color(0xFF06131D), "⬡"),
        StageVisualProfile(3, "Cyber Labyrinth", "Futuristic Maze Barriers", Color(0xFF8B5CF6), Color(0xFF10B981), Color(0xFFA78BFA), Color(0xFF0F0B1E), "☲"),
        StageVisualProfile(4, "Cosmic Drift", "Nebula Orbital Drift", Color(0xFFF43F5E), Color(0xFFF59E0B), Color(0xFFFB7185), Color(0xFF190914), "★"),
        StageVisualProfile(5, "Plasma Circuit", "Electric Pulse Paths", Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFFFBBF24), Color(0xFF181005), "⚡"),
        StageVisualProfile(6, "Quantum Shift", "Spatial Ripple Flashes", Color(0xFF10B981), Color(0xFF06B6D4), Color(0xFF34D399), Color(0xFF051713), "◈"),
        StageVisualProfile(7, "Gravity Core", "Gravitational Singularity", Color(0xFF6366F1), Color(0xFF3B82F6), Color(0xFF818CF8), Color(0xFF0A0C22), "◎"),
        StageVisualProfile(8, "Laser Grid", "Security Defense Matrix", Color(0xFFEF4444), Color(0xFFEC4899), Color(0xFFF87171), Color(0xFF1C080B), "⌖"),
        StageVisualProfile(9, "Void Escape", "Dark Dimensional Void", Color(0xFF9333EA), Color(0xFF312E81), Color(0xFFC084FC), Color(0xFF080410), "◆"),
        StageVisualProfile(10, "Infinity Core", "Grand Singularity Finale", Color(0xFFFFB800), Color(0xFFFF007F), Color(0xFFFFEE00), Color(0xFF130A24), "☯")
    )

    fun getProfile(stageNum: Int): StageVisualProfile {
        val idx = (stageNum - 1).coerceIn(0, profiles.size - 1)
        return profiles[idx]
    }
}

/**
 * Animated 10-Stage Environmental Canvas
 */
@Composable
fun StageEnvironmentCanvas(
    stageNumber: Int,
    isGooglyMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val profile = StageProfiles.getProfile(stageNumber)

    val infiniteTransition = rememberInfiniteTransition(label = "StageEnv_$stageNumber")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "EnvTime"
    )

    val fastPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FastPulse"
    )

    val particles = remember(stageNumber) {
        val rand = Random(stageNumber * 31337)
        List(28) {
            StageParticle(
                x = rand.nextFloat(),
                y = rand.nextFloat(),
                size = rand.nextFloat() * 4f + 2f,
                speedX = (rand.nextFloat() - 0.5f) * 0.05f,
                speedY = (rand.nextFloat() - 0.5f) * 0.05f,
                orbitRadius = rand.nextFloat() * 120f + 30f,
                phase = rand.nextFloat() * PI.toFloat() * 2f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Stage Base Gradient Backdrop
        val bgPrimary = if (isGooglyMode) Color(0xFF100B22) else profile.backgroundBase
        val bgSecondary = if (isGooglyMode) Color(0xFF220D35) else profile.primaryColor.copy(alpha = 0.25f)
        val bgRadial = Brush.radialGradient(
            colors = listOf(bgSecondary, bgPrimary, Color(0xFF030308)),
            center = Offset(w * 0.5f, h * 0.35f),
            radius = max(w, h) * 0.85f
        )
        drawRect(brush = bgRadial)

        // 2. Stage-Specific Animated Environmental Geometry & Effects
        when (stageNumber) {
            1 -> drawStage1TheAwakening(w, h, time, fastPulse, profile, isGooglyMode)
            2 -> drawStage2NeonLattice(w, h, time, fastPulse, profile, isGooglyMode)
            3 -> drawStage3CyberLabyrinth(w, h, time, fastPulse, profile, isGooglyMode)
            4 -> drawStage4CosmicDrift(w, h, time, fastPulse, profile, isGooglyMode)
            5 -> drawStage5PlasmaCircuit(w, h, time, fastPulse, profile, isGooglyMode)
            6 -> drawStage6QuantumShift(w, h, time, fastPulse, profile, isGooglyMode)
            7 -> drawStage7GravityCore(w, h, time, fastPulse, profile, isGooglyMode)
            8 -> drawStage8LaserGrid(w, h, time, fastPulse, profile, isGooglyMode)
            9 -> drawStage9VoidEscape(w, h, time, fastPulse, profile, isGooglyMode)
            10 -> drawStage10InfinityCore(w, h, time, fastPulse, profile, isGooglyMode)
            else -> drawStage1TheAwakening(w, h, time, fastPulse, profile, isGooglyMode)
        }

        // 3. Stage Floating Particle Motifs
        for (p in particles) {
            val px = (p.x + p.speedX * time * 10f + 1f) % 1f * w
            val py = (p.y + p.speedY * time * 10f + 1f) % 1f * h
            val pColor = if (isGooglyMode) {
                AppThemeTokens.GooglyRainbowPalette[((p.phase * 5).toInt()) % AppThemeTokens.GooglyRainbowPalette.size]
            } else {
                profile.accentGlow
            }
            val alpha = (0.3f + 0.3f * sin(time * 2 * PI.toFloat() + p.phase)).coerceIn(0.1f, 0.8f)

            drawCircle(
                color = pColor.copy(alpha = alpha * 0.4f),
                radius = p.size * 2f,
                center = Offset(px, py)
            )
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = p.size * 0.7f,
                center = Offset(px, py)
            )
        }
    }
}

// Stage 1 — The Awakening: Deep space stardust and emerging arrow constellations
private fun DrawScope.drawStage1TheAwakening(w: Float, h: Float, t: Float, pulse: Float, p: StageVisualProfile, isGoogly: Boolean) {
    val constellationColor = if (isGoogly) Color(0xFF00E5FF) else p.accentGlow
    val nodes = listOf(
        Offset(w * 0.2f, h * 0.2f),
        Offset(w * 0.8f, h * 0.25f),
        Offset(w * 0.5f, h * 0.5f),
        Offset(w * 0.3f, h * 0.75f),
        Offset(w * 0.7f, h * 0.8f)
    )

    for (i in 0 until nodes.size - 1) {
        val n1 = nodes[i]
        val n2 = nodes[i + 1]
        drawLine(
            color = constellationColor.copy(alpha = 0.15f + 0.1f * pulse),
            start = n1,
            end = n2,
            strokeWidth = 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), t * 50f)
        )
    }

    for (node in nodes) {
        drawCircle(
            color = constellationColor.copy(alpha = 0.3f),
            radius = 12f * (1f + 0.2f * pulse),
            center = node
        )
        drawCircle(
            color = Color.White,
            radius = 4f,
            center = node
        )
    }
}

// Stage 2 — Neon Lattice: Cyan/Magenta isometric matrix with pulsing nodes
private fun DrawScope.drawStage2NeonLattice(w: Float, h: Float, t: Float, pulse: Float, p: StageVisualProfile, isGoogly: Boolean) {
    val c1 = if (isGoogly) Color(0xFFFF007F) else p.primaryColor
    val c2 = if (isGoogly) Color(0xFF00E5FF) else p.secondaryColor
    val step = 60f

    var x = 0f
    while (x < w + h) {
        drawLine(
            color = c1.copy(alpha = 0.08f),
            start = Offset(x, 0f),
            end = Offset(x - h * 0.5f, h),
            strokeWidth = 1.5f
        )
        drawLine(
            color = c2.copy(alpha = 0.08f),
            start = Offset(x - h, 0f),
            end = Offset(x + h * 0.5f, h),
            strokeWidth = 1.5f
        )
        x += step
    }

    // Moving laser scan line
    val scanY = (t * h * 1.2f) % h
    drawLine(
        brush = Brush.horizontalGradient(listOf(Color.Transparent, c1.copy(alpha = 0.4f), Color.Transparent)),
        start = Offset(0f, scanY),
        end = Offset(w, scanY),
        strokeWidth = 2.5f
    )
}

// Stage 3 — Cyber Labyrinth: Scanning maze corridors & glowing barriers
private fun DrawScope.drawStage3CyberLabyrinth(w: Float, h: Float, t: Float, pulse: Float, p: StageVisualProfile, isGoogly: Boolean) {
    val mazeColor = if (isGoogly) Color(0xFF00FF66) else p.primaryColor
    val barSize = 80f

    for (row in 1..8) {
        val y = row * (h / 9f)
        val offset = if (row % 2 == 0) sin(t * PI.toFloat() * 2f) * 20f else -sin(t * PI.toFloat() * 2f) * 20f
        drawLine(
            color = mazeColor.copy(alpha = 0.12f),
            start = Offset(w * 0.15f + offset, y),
            end = Offset(w * 0.85f + offset, y),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(40f, 25f), t * 100f)
        )
    }

    // Floating barrier posts
    val postColor = if (isGoogly) Color(0xFFFFDD00) else p.accentGlow
    for (i in 0..4) {
        val cx = w * (0.2f + i * 0.15f)
        val cy = h * (0.3f + (i % 2) * 0.35f)
        drawRect(
            color = postColor.copy(alpha = 0.2f + 0.15f * pulse),
            topLeft = Offset(cx - 8f, cy - 8f),
            size = Size(16f, 16f),
            style = Stroke(width = 2f)
        )
    }
}

// Stage 4 — Cosmic Drift: Nebula clouds, rotating orbital arcs, star clusters
private fun DrawScope.drawStage4CosmicDrift(w: Float, h: Float, t: Float, pulse: Float, p: StageVisualProfile, isGoogly: Boolean) {
    val starColor = if (isGoogly) Color(0xFFFF7700) else p.primaryColor
    val center = Offset(w * 0.5f, h * 0.45f)

    for (ring in 1..3) {
        val radius = ring * 90f + pulse * 10f
        rotate(degrees = (t * 360f * (if (ring % 2 == 0) 1 else -1) / ring), pivot = center) {
            drawCircle(
                color = starColor.copy(alpha = 0.1f / ring),
                radius = radius,
                center = center,
                style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 20f)))
            )
            // Orbital node
            drawCircle(
                color = Color.White,
                radius = 3.5f,
                center = Offset(center.x + radius, center.y)
            )
        }
    }
}

// Stage 5 — Plasma Circuit: Electric circuit board traces with traveling spark dots
private fun DrawScope.drawStage5PlasmaCircuit(w: Float, h: Float, t: Float, pulse: Float, p: StageVisualProfile, isGoogly: Boolean) {
    val electricColor = if (isGoogly) Color(0xFFFFB800) else p.primaryColor

    val circuits = listOf(
        listOf(Offset(w * 0.1f, h * 0.1f), Offset(w * 0.4f, h * 0.1f), Offset(w * 0.4f, h * 0.4f), Offset(w * 0.8f, h * 0.4f)),
        listOf(Offset(w * 0.9f, h * 0.9f), Offset(w * 0.6f, h * 0.9f), Offset(w * 0.6f, h * 0.6f), Offset(w * 0.2f, h * 0.6f)),
        listOf(Offset(w * 0.2f, h * 0.3f), Offset(w * 0.2f, h * 0.8f), Offset(w * 0.5f, h * 0.8f))
    )

    for (circuit in circuits) {
        for (i in 0 until circuit.size - 1) {
            drawLine(
                color = electricColor.copy(alpha = 0.18f),
                start = circuit[i],
                end = circuit[i + 1],
                strokeWidth = 2f
            )
        }
        // Pulse spark
        val segIdx = (t * (circuit.size - 1)).toInt().coerceIn(0, circuit.size - 2)
        val segProgress = (t * (circuit.size - 1)) - segIdx
        val p1 = circuit[segIdx]
        val p2 = circuit[segIdx + 1]
        val sparkPos = Offset(p1.x + (p2.x - p1.x) * segProgress, p1.y + (p2.y - p1.y) * segProgress)

        drawCircle(color = Color.White, radius = 4f, center = sparkPos)
        drawCircle(color = electricColor.copy(alpha = 0.6f), radius = 10f, center = sparkPos)
    }
}

// Stage 6 — Quantum Shift: Distortion wave rings and teleport flashes
private fun DrawScope.drawStage6QuantumShift(w: Float, h: Float, t: Float, pulse: Float, p: StageVisualProfile, isGoogly: Boolean) {
    val qColor = if (isGoogly) Color(0xFF00FF66) else p.primaryColor
    val center = Offset(w * 0.5f, h * 0.4f)

    for (i in 1..4) {
        val waveT = (t + i * 0.25f) % 1f
        val radius = waveT * max(w, h) * 0.6f
        val alpha = (1f - waveT).coerceIn(0f, 1f) * 0.25f

        drawCircle(
            color = qColor.copy(alpha = alpha),
            radius = radius,
            center = center,
            style = Stroke(width = 2f)
        )
    }

    // Geometric teleport diamonds
    for (d in 1..4) {
        val angle = d * 90f + t * 180f
        val rad = 100f
        val cx = center.x + cos(angle * PI.toFloat() / 180f) * rad
        val cy = center.y + sin(angle * PI.toFloat() / 180f) * rad

        rotate(degrees = angle, pivot = Offset(cx, cy)) {
            drawRect(
                color = qColor.copy(alpha = 0.3f),
                topLeft = Offset(cx - 10f, cy - 10f),
                size = Size(20f, 20f),
                style = Stroke(width = 1.5f)
            )
        }
    }
}

// Stage 7 — Gravity Core: Pulsing singularity and curved gravitational field lines
private fun DrawScope.drawStage7GravityCore(w: Float, h: Float, t: Float, pulse: Float, p: StageVisualProfile, isGoogly: Boolean) {
    val gColor = if (isGoogly) Color(0xFF7000FF) else p.primaryColor
    val center = Offset(w * 0.5f, h * 0.42f)

    // Singularity Core
    drawCircle(
        brush = Brush.radialGradient(listOf(Color.White, gColor, Color.Transparent), center = center, radius = 70f * (1f + 0.15f * pulse)),
        radius = 70f * (1f + 0.15f * pulse),
        center = center
    )

    // Curved Gravity Spirals
    for (arm in 0..2) {
        val spiralPath = Path()
        val baseAngle = arm * 120f + t * 360f
        var first = true
        for (step in 10..180 step 10) {
            val rad = step * 1.5f
            val a = (baseAngle + step * 1.5f) * PI.toFloat() / 180f
            val x = center.x + cos(a) * rad
            val y = center.y + sin(a) * rad
            if (first) {
                spiralPath.moveTo(x, y)
                first = false
            } else {
                spiralPath.lineTo(x, y)
            }
        }
        drawPath(
            path = spiralPath,
            color = gColor.copy(alpha = 0.2f),
            style = Stroke(width = 2f)
        )
    }
}

// Stage 8 — Laser Grid: Futuristic scanning security beams and reactive sensors
private fun DrawScope.drawStage8LaserGrid(w: Float, h: Float, t: Float, pulse: Float, p: StageVisualProfile, isGoogly: Boolean) {
    val laserColor = if (isGoogly) Color(0xFFFF0055) else p.primaryColor

    // Horizontal laser scan
    val beamY = (sin(t * PI.toFloat() * 2f) * 0.5f + 0.5f) * h
    drawLine(
        brush = Brush.horizontalGradient(listOf(Color.Transparent, laserColor.copy(alpha = 0.6f), Color.White, laserColor.copy(alpha = 0.6f), Color.Transparent)),
        start = Offset(0f, beamY),
        end = Offset(w, beamY),
        strokeWidth = 3f
    )

    // Diagonal laser cross
    val beamX = (cos(t * PI.toFloat() * 2f) * 0.5f + 0.5f) * w
    drawLine(
        brush = Brush.verticalGradient(listOf(Color.Transparent, laserColor.copy(alpha = 0.4f), Color.Transparent)),
        start = Offset(beamX, 0f),
        end = Offset(beamX, h),
        strokeWidth = 2f
    )

    // Scanline texture
    var slY = 0f
    while (slY < h) {
        drawLine(
            color = Color.Black.copy(alpha = 0.15f),
            start = Offset(0f, slY),
            end = Offset(w, slY),
            strokeWidth = 1f
        )
        slY += 6f
    }
}

// Stage 9 — Void Escape: Dark void abyss with floating shards and energy cracks
private fun DrawScope.drawStage9VoidEscape(w: Float, h: Float, t: Float, pulse: Float, p: StageVisualProfile, isGoogly: Boolean) {
    val voidColor = if (isGoogly) Color(0xFFFF00CC) else p.accentGlow
    val center = Offset(w * 0.5f, h * 0.45f)

    // Dimensional rift crack
    val crackPath = Path().apply {
        moveTo(w * 0.3f, h * 0.2f)
        lineTo(w * 0.48f, h * 0.38f)
        lineTo(w * 0.52f, h * 0.45f)
        lineTo(w * 0.42f, h * 0.58f)
        lineTo(w * 0.7f, h * 0.75f)
    }

    drawPath(
        path = crackPath,
        color = voidColor.copy(alpha = 0.4f + 0.2f * pulse),
        style = Stroke(width = 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    drawPath(
        path = crackPath,
        color = Color.White.copy(alpha = 0.8f),
        style = Stroke(width = 1.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Floating shards drifting into void
    for (s in 0..5) {
        val dist = (s * 40f + t * 80f) % 240f
        val angle = s * 60f + t * 45f
        val sx = center.x + cos(angle * PI.toFloat() / 180f) * dist
        val sy = center.y + sin(angle * PI.toFloat() / 180f) * dist

        rotate(degrees = angle * 2f, pivot = Offset(sx, sy)) {
            val shardPath = Path().apply {
                moveTo(sx, sy - 12f)
                lineTo(sx + 8f, sy + 6f)
                lineTo(sx - 8f, sy + 6f)
                close()
            }
            drawPath(path = shardPath, color = voidColor.copy(alpha = (1f - dist / 240f) * 0.35f))
        }
    }
}

// Stage 10 — Infinity Core: Grand radiant core with multi-layer orbiting rings
private fun DrawScope.drawStage10InfinityCore(w: Float, h: Float, t: Float, pulse: Float, p: StageVisualProfile, isGoogly: Boolean) {
    val coreColor = if (isGoogly) Color(0xFFFFDD00) else Color(0xFFFFB800)
    val ringColor = if (isGoogly) Color(0xFFFF007F) else Color(0xFFEC4899)
    val center = Offset(w * 0.5f, h * 0.38f)

    // Grand Radiant Core Glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, coreColor.copy(alpha = 0.8f), ringColor.copy(alpha = 0.3f), Color.Transparent),
            center = center,
            radius = 120f * (1f + 0.1f * pulse)
        ),
        radius = 120f * (1f + 0.1f * pulse),
        center = center
    )

    // Concentric orbiting infinity rings with rotating arrow diamonds
    for (r in 1..4) {
        val rad = r * 45f + pulse * 8f
        rotate(degrees = (t * 360f * (if (r % 2 == 0) 1 else -1) / r), pivot = center) {
            drawCircle(
                color = ringColor.copy(alpha = 0.2f),
                radius = rad,
                center = center,
                style = Stroke(width = 1.8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f)))
            )
            // Orbiting core arrows
            val ax = center.x + rad
            val ay = center.y
            val arrowP = Path().apply {
                moveTo(ax, ay - 8f)
                lineTo(ax + 8f, ay + 6f)
                lineTo(ax, ay + 3f)
                lineTo(ax - 8f, ay + 6f)
                close()
            }
            drawPath(path = arrowP, color = Color.White)
        }
    }
}

private data class StageParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speedX: Float,
    val speedY: Float,
    val orbitRadius: Float,
    val phase: Float
)
