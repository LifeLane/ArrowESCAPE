package com.mitsara.arrowescape.ui.motion

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitsara.arrowescape.ui.theme.GoldStar
import kotlin.math.*
import kotlin.random.Random

/**
 * Visual Theme Tokens & Multi-Hue Colors for Googly & Default Mode
 */
object AppThemeTokens {
    val GooglyRainbowPalette = listOf(
        Color(0xFFFF0055), // Neon Red/Pink
        Color(0xFFFF7700), // Electric Orange
        Color(0xFFFFDD00), // Neon Yellow
        Color(0xFF00FF66), // Spring Green
        Color(0xFF00E5FF), // Electric Cyan
        Color(0xFF7000FF), // Deep Violet
        Color(0xFFFF00CC)  // Magenta
    )

    fun getGooglyBrush(phase: Float = 0f): Brush {
        val colors = listOf(
            Color(0xFFFF3366),
            Color(0xFFFFB800),
            Color(0xFF00E676),
            Color(0xFF00E5FF),
            Color(0xFFAA00FF),
            Color(0xFFFF3366)
        )
        return Brush.sweepGradient(
            colors = colors,
            center = Offset.Unspecified
        )
    }

    fun getAnimatedRainbowGradient(progress: Float): Brush {
        val baseColors = GooglyRainbowPalette
        val offset = (progress * baseColors.size).toInt() % baseColors.size
        val shifted = List(baseColors.size) { i ->
            baseColors[(i + offset) % baseColors.size]
        }
        return Brush.linearGradient(
            colors = shifted,
            start = Offset(0f, 0f),
            end = Offset(1000f, 1000f)
        )
    }
}

/**
 * Animated Atmospheric Background
 * Features:
 * - Ambient gradient canvas
 * - Subtle geometric neon grid
 * - Floating glowing energy motes
 * - Curved arrow light escape trails sweeping smoothly
 * - Translucent floating arrow silhouettes
 * - Full multi-color animation in Googly mode
 */
@Composable
fun AnimatedAtmosphericBackground(
    modifier: Modifier = Modifier,
    isGooglyMode: Boolean = false,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AtmosphereAnim")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Pulse"
    )

    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Shimmer"
    )

    val particles = remember {
        val rand = Random(12345)
        List(35) {
            AtmosphericParticle(
                x = rand.nextFloat(),
                y = rand.nextFloat(),
                radius = rand.nextFloat() * 3.5f + 1.5f,
                speedX = (rand.nextFloat() - 0.5f) * 0.04f,
                speedY = -rand.nextFloat() * 0.06f - 0.015f,
                baseAlpha = rand.nextFloat() * 0.5f + 0.2f,
                colorIndex = rand.nextInt(7),
                pulseOffset = rand.nextFloat() * PI.toFloat() * 2f
            )
        }
    }

    val arrowSilhouettes = remember {
        val rand = Random(54321)
        List(6) {
            BackgroundArrowSilhouette(
                startX = rand.nextFloat(),
                startY = rand.nextFloat(),
                angle = rand.nextFloat() * 360f,
                speed = rand.nextFloat() * 0.02f + 0.01f,
                scale = rand.nextFloat() * 0.6f + 0.5f,
                colorIndex = rand.nextInt(7)
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Base Gradient Canvas
            val bgGradient = if (isGooglyMode) {
                val c1 = Color(0xFF0F0C20)
                val c2 = Color(0xFF1E1035)
                val c3 = Color(0xFF0D1B2A)
                Brush.radialGradient(
                    colors = listOf(c2, c1, c3),
                    center = Offset(w * 0.5f, h * 0.4f),
                    radius = max(w, h) * 0.9f
                )
            } else {
                val c1 = Color(0xFF0B1120)
                val c2 = Color(0xFF0F172A)
                val c3 = Color(0xFF030712)
                Brush.radialGradient(
                    colors = listOf(c2, c1, c3),
                    center = Offset(w * 0.5f, h * 0.35f),
                    radius = max(w, h) * 0.85f
                )
            }
            drawRect(brush = bgGradient)

            // 2. Subtle Geometric Grid / Lattice Lines
            val gridSpacing = 48.dp.toPx()
            val gridAlpha = if (isGooglyMode) 0.08f else 0.05f
            val gridColor = if (isGooglyMode) Color(0xFF00E5FF) else Color(0xFF38BDF8)
            var gx = 0f
            while (gx < w) {
                drawLine(
                    color = gridColor.copy(alpha = gridAlpha),
                    start = Offset(gx, 0f),
                    end = Offset(gx, h),
                    strokeWidth = 1f
                )
                gx += gridSpacing
            }
            var gy = 0f
            while (gy < h) {
                drawLine(
                    color = gridColor.copy(alpha = gridAlpha),
                    start = Offset(0f, gy),
                    end = Offset(w, gy),
                    strokeWidth = 1f
                )
                gy += gridSpacing
            }

            // 3. Animated Neon Light Trails (Arrow Escape Trails)
            drawEscapeLightTrails(w, h, pulse, isGooglyMode)

            // 4. Floating Translucent Arrow Silhouettes
            for (arrow in arrowSilhouettes) {
                val curX = (arrow.startX + cos(arrow.angle * PI.toFloat() / 180f) * arrow.speed * shimmer * 5f) % 1f * w
                val curY = (arrow.startY + sin(arrow.angle * PI.toFloat() / 180f) * arrow.speed * shimmer * 5f) % 1f * h
                val arrowColor = if (isGooglyMode) {
                    AppThemeTokens.GooglyRainbowPalette[arrow.colorIndex % AppThemeTokens.GooglyRainbowPalette.size]
                } else {
                    Color(0xFF38BDF8)
                }

                rotate(degrees = arrow.angle, pivot = Offset(curX, curY)) {
                    val s = 24.dp.toPx() * arrow.scale
                    val arrowPath = Path().apply {
                        moveTo(curX, curY - s)
                        lineTo(curX - s * 0.6f, curY + s * 0.6f)
                        lineTo(curX, curY + s * 0.2f)
                        lineTo(curX + s * 0.6f, curY + s * 0.6f)
                        close()
                    }
                    drawPath(
                        path = arrowPath,
                        color = arrowColor.copy(alpha = 0.06f)
                    )
                    drawPath(
                        path = arrowPath,
                        color = arrowColor.copy(alpha = 0.12f),
                        style = Stroke(width = 1.5f)
                    )
                }
            }

            // 5. Ambient Glowing Energy Particles & Spark Motes
            for (p in particles) {
                val cx = (p.x + p.speedX * pulse * 20f + 1f) % 1f * w
                val cy = (p.y + p.speedY * pulse * 20f + 1f) % 1f * h
                val particleColor = if (isGooglyMode) {
                    AppThemeTokens.GooglyRainbowPalette[p.colorIndex % AppThemeTokens.GooglyRainbowPalette.size]
                } else {
                    if (p.colorIndex % 2 == 0) Color(0xFF38BDF8) else Color(0xFF818CF8)
                }

                val dynamicAlpha = (p.baseAlpha * (0.6f + 0.4f * sin(pulse * 2 * PI.toFloat() + p.pulseOffset))).coerceIn(0.1f, 0.9f)

                // Outer soft halo
                drawCircle(
                    color = particleColor.copy(alpha = dynamicAlpha * 0.35f),
                    radius = p.radius * 2.8f,
                    center = Offset(cx, cy)
                )
                // Crisp glowing core
                drawCircle(
                    color = Color.White.copy(alpha = dynamicAlpha),
                    radius = p.radius * 0.8f,
                    center = Offset(cx, cy)
                )
            }
        }

        // Foreground Screen Content
        content()
    }
}

private fun DrawScope.drawEscapeLightTrails(
    w: Float,
    h: Float,
    time: Float,
    isGooglyMode: Boolean
) {
    val trails = listOf(
        Triple(Offset(w * 0.1f, h * 0.85f), Offset(w * 0.9f, h * 0.25f), 0f),
        Triple(Offset(w * 0.85f, h * 0.9f), Offset(w * 0.15f, h * 0.35f), 0.5f),
        Triple(Offset(w * 0.5f, h * 0.95f), Offset(w * 0.5f, h * 0.05f), 0.25f)
    )

    trails.forEachIndexed { idx, (start, end, phaseOffset) ->
        val t = (time + phaseOffset) % 1f
        val currentHead = Offset(
            start.x + (end.x - start.x) * t,
            start.y + (end.y - start.y) * t
        )
        val tailLength = 120.dp.toPx()
        val dir = (end - start)
        val dirLen = sqrt(dir.x * dir.x + dir.y * dir.y)
        if (dirLen > 0.01f) {
            val normDir = Offset(dir.x / dirLen, dir.y / dirLen)
            val currentTail = currentHead - normDir * tailLength

            val trailColor = if (isGooglyMode) {
                AppThemeTokens.GooglyRainbowPalette[(idx * 2) % AppThemeTokens.GooglyRainbowPalette.size]
            } else {
                if (idx == 0) Color(0xFF0EA5E9) else if (idx == 1) Color(0xFF8B5CF6) else Color(0xFF06B6D4)
            }

            // Draw glowing beam line
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, trailColor.copy(alpha = 0.45f), Color.White.copy(alpha = 0.85f)),
                    start = currentTail,
                    end = currentHead
                ),
                start = currentTail,
                end = currentHead,
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Sparkle dot at the head
            drawCircle(
                color = Color.White,
                radius = 3.5.dp.toPx(),
                center = currentHead
            )
            drawCircle(
                color = trailColor.copy(alpha = 0.5f),
                radius = 8.dp.toPx(),
                center = currentHead
            )
        }
    }
}

/**
 * Animated Primary Hero CTA Button (e.g., Continue / Play Game / Next Level)
 */
@Composable
fun AnimatedHeroButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isGooglyMode: Boolean = false,
    accentColor: Color = Color(0xFF2563EB),
    leadingIcon: ImageVector = Icons.Default.PlayArrow,
    testTag: String = "animated_hero_button"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "HeroBtnTransition")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerProgress"
    )

    val iconHop by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IconHop"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "PressScale"
    )

    val buttonGradient = if (isGooglyMode) {
        Brush.horizontalGradient(
            listOf(
                Color(0xFFFF0055),
                Color(0xFFFF7700),
                Color(0xFF00E5FF),
                Color(0xFFAA00FF)
            )
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                accentColor,
                Color(0xFF0284C7),
                Color(0xFF4F46E5)
            )
        )
    }

    Box(
        modifier = modifier
            .scale(pulseScale * pressScale)
            .shadow(
                elevation = if (isPressed) 6.dp else 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = if (isGooglyMode) Color(0xFFFF007F) else accentColor,
                spotColor = if (isGooglyMode) Color(0xFF00E5FF) else accentColor
            )
            .clip(RoundedCornerShape(24.dp))
            .background(buttonGradient)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .testTag(testTag)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Sweeping Highlight Sheen
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val sheenX = shimmerProgress * (w + 200f) - 100f
            val sheenBrush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.35f),
                    Color.Transparent
                ),
                start = Offset(sheenX - 60f, 0f),
                end = Offset(sheenX + 60f, h)
            )
            drawRect(brush = sheenBrush)

            // Crisp inner border
            drawRoundRect(
                color = Color.White.copy(alpha = 0.3f),
                style = Stroke(width = 1.5.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx())
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .offset(x = iconHop.dp)
                    .size(34.dp)
                    .background(Color.White.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                ),
                color = Color.White
            )
        }
    }
}

/**
 * Animated Interactive Menu Card (for Main Menu options like Level Selection, Daily Challenge, etc.)
 */
@Composable
fun AnimatedMenuCard(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isGooglyMode: Boolean = false,
    badgeText: String? = null,
    testTag: String = "menu_card"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "CardScale"
    )

    val cardBg = if (isGooglyMode) {
        Color(0xFF181829).copy(alpha = 0.85f)
    } else {
        Color(0xFF1E293B).copy(alpha = 0.75f)
    }

    val borderColor = if (isGooglyMode) {
        iconTint.copy(alpha = 0.5f)
    } else {
        Color(0xFF334155).copy(alpha = 0.8f)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shadowElevation = if (isPressed) 2.dp else 6.dp,
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Glow Icon Badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(iconTint.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.3.sp
                        ),
                        color = Color.White
                    )
                    if (!subtitle.isNullOrEmpty()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            if (badgeText != null) {
                Surface(
                    color = iconTint.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                        color = iconTint,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Data structures for background particle physics
 */
private data class AtmosphericParticle(
    val x: Float,
    val y: Float,
    val radius: Float,
    val speedX: Float,
    val speedY: Float,
    val baseAlpha: Float,
    val colorIndex: Int,
    val pulseOffset: Float
)

private data class BackgroundArrowSilhouette(
    val startX: Float,
    val startY: Float,
    val angle: Float,
    val speed: Float,
    val scale: Float,
    val colorIndex: Int
)
