package com.mitsara.arrowescape.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mitsara.arrowescape.engine.LevelTextEngine
import com.mitsara.arrowescape.model.Direction
import com.mitsara.arrowescape.model.GameTheme
import com.mitsara.arrowescape.model.PuzzleLevel
import com.mitsara.arrowescape.ui.motion.AppThemeTokens
import com.mitsara.arrowescape.ui.motion.StageProfiles
import com.mitsara.arrowescape.ui.theme.GoldStar
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun LevelCompleteDialog(
    level: PuzzleLevel,
    stars: Int,
    moveCount: Int,
    score: Int,
    elapsedSeconds: Int,
    theme: GameTheme? = null,
    isGooglyMode: Boolean = theme?.id?.equals("GOOGLY", ignoreCase = true) ?: false,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onMainMenu: () -> Unit
) {
    val stageNumber = ((level.id - 1) / 50 + 1).coerceIn(1, 10)
    val stageProfile = StageProfiles.getProfile(stageNumber)

    // Staged Animation Choreography
    var animationStage by remember { mutableIntStateOf(0) }
    var displayedScore by remember { mutableIntStateOf(0) }
    var displayedStars by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        // Stage 0: Initial flash & particle burst
        delay(150)
        animationStage = 1 // Title enters
        delay(250)
        animationStage = 2 // Preview card pops in
        delay(200)

        // Count up score smoothly
        val targetScore = score
        val steps = 20
        for (i in 1..steps) {
            displayedScore = (targetScore * (i.toFloat() / steps)).toInt()
            delay(15)
        }
        displayedScore = targetScore

        // Pop stars sequentially
        for (s in 1..stars) {
            delay(220)
            displayedStars = s
        }

        animationStage = 3 // CTA button ready
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isGooglyMode) {
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF26123D), Color(0xFF0F081D), Color(0xFF040208)),
                            radius = 1200f
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(stageProfile.secondaryColor.copy(alpha = 0.6f), Color(0xFF0B1120), Color(0xFF030712)),
                            radius = 1200f
                        )
                    }
                )
                .testTag("level_complete_dialog"),
            contentAlignment = Alignment.Center
        ) {
            // Particle & Confetti Celebration Overlay
            CelebrationParticleField(
                isGooglyMode = isGooglyMode,
                stageProfile = stageProfile,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // ==========================================
                // 1. HEADER & CELEBRATION TITLE
                // ==========================================
                Surface(
                    color = if (isGooglyMode) Color(0xFFFF007F).copy(alpha = 0.25f) else stageProfile.primaryColor.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, stageProfile.accentGlow.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (isGooglyMode) Color(0xFFFF00CC) else stageProfile.accentGlow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "STAGE $stageNumber • ${stageProfile.name.uppercase()}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                fontSize = 11.sp
                            ),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // "LEVEL COMPLETED!" Title
                val titleScale by animateFloatAsState(
                    targetValue = if (animationStage >= 1) 1f else 0.4f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "TitleScale"
                )

                Text(
                    text = "LEVEL ${level.id} CLEAR!",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.scale(titleScale)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Stars Display (Pop & Bounce)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..3) {
                        val isStarEarned = i <= displayedStars
                        val starScale by animateFloatAsState(
                            targetValue = if (isStarEarned) 1.2f else 0.85f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium),
                            label = "StarScale_$i"
                        )

                        Box(
                            modifier = Modifier
                                .scale(starScale)
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isStarEarned) GoldStar.copy(alpha = 0.25f) else Color(0xFF1E293B).copy(alpha = 0.6f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star $i",
                                tint = if (isStarEarned) GoldStar else Color(0xFF475569),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Score and Time Telemetry Bar
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "SCORE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "$displayedScore",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 18.sp),
                                color = Color.White
                            )
                        }

                        Box(modifier = Modifier.size(1.dp, 28.dp).background(Color(0xFF334155)))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "TIME",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "${elapsedSeconds}s",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 18.sp),
                                color = Color(0xFF38BDF8)
                            )
                        }

                        Box(modifier = Modifier.size(1.dp, 28.dp).background(Color(0xFF334155)))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "MOVES",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "$moveCount",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 18.sp),
                                color = Color(0xFF00E676)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // 2. COMPLETED PUZZLE PREVIEW CARD
                // ==========================================
                val cardScale by animateFloatAsState(
                    targetValue = if (animationStage >= 2) 1f else 0.8f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                    label = "CardScale"
                )

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.9f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, stageProfile.accentGlow.copy(alpha = 0.6f)),
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(1.1f)
                        .scale(cardScale)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CompletedPuzzleBoardCanvas(
                            level = level,
                            stageProfile = stageProfile,
                            isGooglyMode = isGooglyMode,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ==========================================
                // 3. FUN FACT / TRIVIA CARD
                // ==========================================
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.65f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.8f)),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = GoldStar,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = LevelTextEngine.getFunFactForLevel(level.id),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontStyle = FontStyle.Italic,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            ),
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ==========================================
                // 4. PRIMARY CTA: NEXT LEVEL BUTTON
                // ==========================================
                val ctaScale by animateFloatAsState(
                    targetValue = if (animationStage >= 3) 1f else 0.8f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    label = "CtaScale"
                )

                AnimatedVictoryCTAButton(
                    text = "NEXT LEVEL",
                    onClick = onNextLevel,
                    isGooglyMode = isGooglyMode,
                    stageProfile = stageProfile,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(58.dp)
                        .scale(ctaScale)
                        .testTag("next_level_button")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ==========================================
                // 5. SECONDARY BUTTONS (Main Menu & Replay)
                // ==========================================
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onMainMenu,
                        modifier = Modifier.testTag("main_menu_button")
                    ) {
                        Text(
                            text = "Main Menu",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Text(text = "•", color = Color(0xFF475569))

                    TextButton(
                        onClick = onReplay,
                        modifier = Modifier.testTag("replay_button")
                    ) {
                        Text(
                            text = "Replay Level",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Procedural Completed Puzzle Board Canvas with glowing solved routes and subtle animated trace sparkles
 */
@Composable
private fun CompletedPuzzleBoardCanvas(
    level: PuzzleLevel,
    stageProfile: com.mitsara.arrowescape.ui.motion.StageVisualProfile,
    isGooglyMode: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BoardTraces")
    val tracePulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "TracePulse"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val gw = level.gridWidth
        val gh = level.gridHeight
        val cellW = w / gw
        val cellH = h / gh

        // Grid Background Dots
        for (ix in 1 until gw) {
            for (iy in 1 until gh) {
                drawCircle(
                    color = Color(0xFF334155).copy(alpha = 0.5f),
                    radius = 1.5.dp.toPx(),
                    center = Offset(ix * cellW, iy * cellH)
                )
            }
        }

        // Draw escaped arrows with glowing routes and solved success indicators
        for ((idx, arrow) in level.arrows.withIndex()) {
            val cx = arrow.startX * cellW + cellW / 2
            val cy = arrow.startY * cellH + cellH / 2
            val arrowSize = minOf(cellW, cellH) * 0.38f

            val arrowColor = if (isGooglyMode) {
                AppThemeTokens.GooglyRainbowPalette[idx % AppThemeTokens.GooglyRainbowPalette.size]
            } else {
                stageProfile.primaryColor
            }

            val path = Path()
            when (arrow.direction) {
                Direction.UP -> {
                    path.moveTo(cx, cy + arrowSize)
                    path.lineTo(cx, cy - arrowSize)
                    path.moveTo(cx - arrowSize * 0.5f, cy)
                    path.lineTo(cx, cy - arrowSize)
                    path.lineTo(cx + arrowSize * 0.5f, cy)
                }
                Direction.DOWN -> {
                    path.moveTo(cx, cy - arrowSize)
                    path.lineTo(cx, cy + arrowSize)
                    path.moveTo(cx - arrowSize * 0.5f, cy)
                    path.lineTo(cx, cy + arrowSize)
                    path.lineTo(cx + arrowSize * 0.5f, cy)
                }
                Direction.LEFT -> {
                    path.moveTo(cx + arrowSize, cy)
                    path.lineTo(cx - arrowSize, cy)
                    path.moveTo(cx, cy - arrowSize * 0.5f)
                    path.lineTo(cx - arrowSize, cy)
                    path.lineTo(cx, cy + arrowSize * 0.5f)
                }
                Direction.RIGHT -> {
                    path.moveTo(cx - arrowSize, cy)
                    path.lineTo(cx + arrowSize, cy)
                    path.moveTo(cx, cy - arrowSize * 0.5f)
                    path.lineTo(cx + arrowSize, cy)
                    path.lineTo(cx, cy + arrowSize * 0.5f)
                }
            }

            // Glow Stroke
            drawPath(
                path = path,
                color = arrowColor.copy(alpha = 0.4f),
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Core Clean Stroke
            drawPath(
                path = path,
                color = Color.White,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Green Check / Star Sparkle Badge at Arrow Start
            drawCircle(
                color = Color(0xFF10B981),
                radius = 3.5.dp.toPx(),
                center = Offset(cx, cy)
            )
        }
    }
}

/**
 * Primary Victory CTA Button with Moving Sheen & Forward Pulse
 */
@Composable
private fun AnimatedVictoryCTAButton(
    text: String,
    onClick: () -> Unit,
    isGooglyMode: Boolean,
    stageProfile: com.mitsara.arrowescape.ui.motion.StageVisualProfile,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "VictoryCTA")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val shimmerX by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerX"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "PressScale"
    )

    val buttonGradient = if (isGooglyMode) {
        Brush.horizontalGradient(listOf(Color(0xFFFF0055), Color(0xFFFF7700), Color(0xFF00E5FF)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF0284C7), stageProfile.accentGlow))
    }

    Box(
        modifier = modifier
            .scale(pulseScale * pressScale)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = stageProfile.accentGlow,
                spotColor = Color.White
            )
            .clip(CircleShape)
            .background(buttonGradient)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Sweeping Sheen Highlight
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val sx = shimmerX * (w + 200f) - 100f
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.35f), Color.Transparent),
                    start = Offset(sx - 40f, 0f),
                    end = Offset(sx + 40f, h)
                )
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    fontSize = 18.sp
                ),
                color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Celebration Confetti & Particle Overlay
 */
@Composable
private fun CelebrationParticleField(
    isGooglyMode: Boolean,
    stageProfile: com.mitsara.arrowescape.ui.motion.StageVisualProfile,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(3500, easing = LinearEasing)
            )
        )
    }

    val particleList = remember(isGooglyMode) {
        val rand = Random(99999)
        List(55) {
            CelebrationParticle(
                x = rand.nextFloat(),
                y = rand.nextFloat(),
                vx = (rand.nextFloat() - 0.5f) * 0.015f,
                vy = -rand.nextFloat() * 0.02f - 0.005f,
                size = rand.nextFloat() * 8f + 4f,
                color = if (isGooglyMode) {
                    AppThemeTokens.GooglyRainbowPalette.random(rand)
                } else {
                    listOf(stageProfile.primaryColor, stageProfile.accentGlow, GoldStar, Color(0xFF00E676), Color.White).random(rand)
                },
                isSquare = rand.nextBoolean()
            )
        }
    }

    Canvas(modifier = modifier) {
        val t = progress.value
        val w = size.width
        val h = size.height

        for (p in particleList) {
            val cx = (p.x + p.vx * t * 150f + 1f) % 1f * w
            val cy = (p.y + p.vy * t * 150f + t * 0.2f + 1f) % 1f * h
            val alpha = (1f - (t * 1.3f % 1f)).coerceIn(0.1f, 0.9f)

            if (p.isSquare) {
                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(cx - p.size / 2, cy - p.size / 2),
                    size = androidx.compose.ui.geometry.Size(p.size, p.size)
                )
            } else {
                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = p.size / 2,
                    center = Offset(cx, cy)
                )
            }
        }
    }
}

private data class CelebrationParticle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    val color: Color,
    val isSquare: Boolean
)
