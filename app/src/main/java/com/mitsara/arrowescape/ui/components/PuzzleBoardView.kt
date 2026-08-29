package com.mitsara.arrowescape.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateListOf
import kotlin.math.max
import kotlin.math.sqrt
import com.mitsara.arrowescape.engine.EscapePathEngine
import com.mitsara.arrowescape.engine.PuzzleSolver
import com.mitsara.arrowescape.model.Arrow
import com.mitsara.arrowescape.model.Direction
import com.mitsara.arrowescape.model.GameTheme
import com.mitsara.arrowescape.model.ThemeManager
import com.mitsara.arrowescape.model.GridPoint

class EscapeParticle(var x: Float, var y: Float, var vx: Float, var vy: Float, var life: Float)

@Composable
fun PuzzleBoardView(
    gridWidth: Int,
    gridHeight: Int,
    activeArrows: List<Arrow>,
    animatingArrowId: Int?,
    animatingDirection: Direction?,
    hintArrowId: Int?,
    isMistakeShake: Boolean,
    inspectedArrowId: Int? = null,
    onArrowClick: (Int) -> Unit,
    theme: GameTheme = ThemeManager.RETRO_ARCADE,
    validCells: Set<GridPoint>? = null,
    obstacles: Set<GridPoint> = emptySet(),
    selectedArrowId: String = "ARROW_CYBER_NEON",
    selectedBoardId: String = "BOARD_OBSIDIAN",
    selectedGridId: String = "GRID_NEON_LATTICE",
    selectedFrameId: String = "FRAME_CYBER_BRACKETS",
    modifier: Modifier = Modifier
) {
    // Shake animation offset
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(isMistakeShake) {
        if (isMistakeShake) {
            val keyframes = listOf(-14f, 14f, -9f, 9f, -4f, 4f, 0f)
            for (valOffset in keyframes) {
                shakeOffset.animateTo(valOffset, tween(durationMillis = 35, easing = LinearEasing))
            }
        }
    }

    // Pulse animation for hint arrow
    val infiniteTransition = rememberInfiniteTransition(label = "hintPulse")
    val hintPulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hintPulseScale"
    )

    // Escape progress animation driven by path length
    val escapeProgress = remember(animatingArrowId) { Animatable(0f) }
    
    // Tactile press scale feedback for tapped arrow
    val pressScale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    var pressedArrowId by remember { mutableStateOf<Int?>(null) }
    
    // Store recent escapes for particle effects
    val particles = remember { mutableStateListOf<EscapeParticle>() }
    
    LaunchedEffect(animatingArrowId) {
        if (animatingArrowId != null) {
            val arrow = activeArrows.find { it.id == animatingArrowId }
            val animDurationMs = if (arrow != null) {
                EscapePathEngine.calculateEscapeDurationMs(arrow, gridWidth, gridHeight)
            } else 400

            escapeProgress.snapTo(0f)
            escapeProgress.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(
                    durationMillis = animDurationMs,
                    easing = LinearEasing
                )
            )
            
            // Generate particles at tip when animation finishes
            if (arrow != null) {
                val tip = arrow.getTipCell()
                for (i in 0..12) {
                    particles.add(
                        EscapeParticle(
                            x = tip.x.toFloat(),
                            y = tip.y.toFloat(),
                            vx = (Math.random() - 0.5).toFloat() * 4f,
                            vy = (Math.random() - 0.5).toFloat() * 4f,
                            life = 1f
                        )
                    )
                }
            }
        }
    }
    
    // Update particles
    LaunchedEffect(Unit) {
        while(true) {
            if (particles.isNotEmpty()) {
                val iter = particles.iterator()
                while(iter.hasNext()) {
                    val p = iter.next()
                    p.x += p.vx * 0.12f
                    p.y += p.vy * 0.12f
                    p.life -= 0.04f
                    if (p.life <= 0) iter.remove()
                }
            }
            kotlinx.coroutines.delay(16)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .offset { IntOffset(shakeOffset.value.toInt(), 0) }
            .shadow(12.dp, shape = RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(theme.boardCanvasColor)
            .padding(10.dp)
            .testTag("puzzle_board")
            .pointerInput(gridWidth, gridHeight, activeArrows) {
                detectTapGestures { offset ->
                    val cW = size.width.toFloat() / gridWidth
                    val cH = size.height.toFloat() / gridHeight
                    val cellX = (offset.x / cW).toInt().coerceIn(0, gridWidth - 1)
                    val cellY = (offset.y / cH).toInt().coerceIn(0, gridHeight - 1)
                    val tappedPoint = GridPoint(cellX, cellY)

                    val tappedArrow = activeArrows.find { arrow ->
                        arrow.getOccupiedCells().contains(tappedPoint)
                    }
                    if (tappedArrow != null) {
                        pressedArrowId = tappedArrow.id
                        scope.launch {
                            pressScale.snapTo(0.88f)
                            pressScale.animateTo(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = 300f
                                )
                            )
                        }
                        onArrowClick(tappedArrow.id)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cW = size.width / gridWidth
            val cH = size.height / gridHeight

            // 1. Draw Cosmetic Board Surface
            drawCosmeticBoardSurface(selectedBoardId, size, theme.boardCanvasColor)

            // 2. Draw Cosmetic Grid Matrix Slots
            drawCosmeticGridSlots(
                gridId = selectedGridId,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                cellW = cW,
                cellH = cH,
                validCells = validCells,
                themeDotColor = theme.gridDotColor
            )

            // Render obstacles as glowing geometric shapes
            for (obs in obstacles) {
                val obsCenter = Offset(obs.x * cW + cW / 2, obs.y * cH + cH / 2)
                val shapeRadius = minOf(cW, cH) * 0.38f
                
                // Outer neon glow
                drawCircle(
                    color = Color(0xFFFF5722).copy(alpha = 0.3f),
                    radius = shapeRadius * 1.2f,
                    center = obsCenter
                )
                
                // Geometric Diamond / Crystal obstacle shape
                val diamondPath = Path().apply {
                    moveTo(obsCenter.x, obsCenter.y - shapeRadius)
                    lineTo(obsCenter.x + shapeRadius, obsCenter.y)
                    lineTo(obsCenter.x, obsCenter.y + shapeRadius)
                    lineTo(obsCenter.x - shapeRadius, obsCenter.y)
                    close()
                }
                drawPath(
                    path = diamondPath,
                    color = Color(0xFF1E293B)
                )
                drawPath(
                    path = diamondPath,
                    color = Color(0xFFFF5722),
                    style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                // Inner core dot
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = 4f,
                    center = obsCenter
                )
            }

            // Render active arrows
            for (arrow in activeArrows) {
                val isAnimatingThis = arrow.id == animatingArrowId
                val isHintedThis = arrow.id == hintArrowId
                val isInspectedThis = arrow.id == inspectedArrowId
                val isUnobstructed = PuzzleSolver.isArrowUnobstructed(arrow, activeArrows, gridWidth, gridHeight, obstacles)

                if (isAnimatingThis) {
                    // Physical route-following escape animation
                    drawEscapingArrow(
                        arrow = arrow,
                        gridWidth = gridWidth,
                        gridHeight = gridHeight,
                        cellWidthPx = cW,
                        cellHeightPx = cH,
                        progress = escapeProgress.value,
                        theme = theme,
                        selectedArrowId = selectedArrowId
                    )
                } else {
                    // Laser inspection ray when tapped while blocked
                    if (isInspectedThis) {
                        val tip = arrow.getTipCell()
                        val tipDir = arrow.getTipDirection()
                        val startOffset = Offset(tip.x * cW + cW / 2, tip.y * cH + cH / 2)
                        
                        val occupiedCells = HashSet<GridPoint>()
                        for (other in activeArrows) {
                            if (other.id != arrow.id) {
                                occupiedCells.addAll(other.getOccupiedCells())
                            }
                        }
                        occupiedCells.addAll(obstacles)
                        
                        val ray = arrow.getExitRay(gridWidth, gridHeight)
                        var hitPoint: GridPoint? = null
                        for (pt in ray) {
                            if (occupiedCells.contains(pt)) {
                                hitPoint = pt
                                break
                            }
                        }
                        
                        val endOffset = if (hitPoint != null) {
                            Offset(hitPoint.x * cW + cW / 2, hitPoint.y * cH + cH / 2)
                        } else {
                            val lastRayPt = ray.lastOrNull() ?: tip
                            Offset(lastRayPt.x * cW + cW / 2, lastRayPt.y * cH + cH / 2) + Offset(tipDir.dx * cW, tipDir.dy * cH)
                        }

                        // Outer red laser glow
                        drawLine(
                            color = Color(0xFFEF4444).copy(alpha = 0.7f),
                            start = startOffset,
                            end = endOffset,
                            strokeWidth = minOf(cW, cH) * 0.25f,
                            cap = StrokeCap.Round
                        )
                        // Inner brilliant core laser
                        drawLine(
                            color = Color.White.copy(alpha = 0.95f),
                            start = startOffset,
                            end = endOffset,
                            strokeWidth = minOf(cW, cH) * 0.1f,
                            cap = StrokeCap.Round
                        )

                        if (hitPoint != null) {
                            drawCircle(
                                color = Color(0xFFEF4444),
                                radius = minOf(cW, cH) * 0.35f,
                                center = endOffset
                            )
                            drawCircle(
                                color = Color.White,
                                radius = minOf(cW, cH) * 0.15f,
                                center = endOffset
                            )
                        }
                    }

                    val arrowScale = if (arrow.id == pressedArrowId) pressScale.value else 1.0f

                    drawArrowGraphics(
                        arrow = arrow,
                        cellWidthPx = cW,
                        cellHeightPx = cH,
                        isUnobstructed = isUnobstructed,
                        isHinted = isHintedThis,
                        hintScale = hintPulseScale,
                        alpha = 1.0f,
                        scale = arrowScale,
                        theme = theme,
                        selectedArrowId = selectedArrowId
                    )
                }
            }
            
            // Draw particles
            for (p in particles) {
                drawCircle(
                    color = theme.arrowHighlightColor.copy(alpha = p.life),
                    radius = (p.life * 10f),
                    center = Offset(p.x * cW + cW/2, p.y * cH + cH/2)
                )
            }

            // Draw Frame Border
            drawCosmeticFrameBorder(selectedFrameId, size, theme.arrowHighlightColor)
        }
    }
}

private val gamePalette = listOf(
    Color(0xFF3B82F6), // Blue
    Color(0xFFF97316), // Orange
    Color(0xFF10B981), // Emerald
    Color(0xFF8B5CF6), // Purple
    Color(0xFFEC4899), // Pink
    Color(0xFFF59E0B), // Amber
    Color(0xFF06B6D4), // Cyan
    Color(0xFF6366F1), // Indigo
    Color(0xFF14B8A6), // Teal
    Color(0xFFEF4444)  // Red
)

private fun getArrowColor(arrow: Arrow, isHinted: Boolean, theme: GameTheme): Color {
    if (isHinted) return theme.arrowHighlightColor
    if (arrow.customColorHex != null) {
        try {
            return Color(android.graphics.Color.parseColor(arrow.customColorHex))
        } catch (e: Exception) {
            // fallback
        }
    }
    return gamePalette[(arrow.id - 1) % gamePalette.size]
}

private fun DrawScope.drawEscapingArrow(
    arrow: Arrow,
    gridWidth: Int,
    gridHeight: Int,
    cellWidthPx: Float,
    cellHeightPx: Float,
    progress: Float,
    theme: GameTheme,
    selectedArrowId: String = "ARROW_CYBER_NEON"
) {
    val occupiedCells = arrow.getOccupiedCells()
    if (occupiedCells.isEmpty()) return

    val arrowColor = getArrowColor(arrow, false, theme)
    val strokeWidth = minOf(cellWidthPx, cellHeightPx) * 0.12f
    val headLength = minOf(cellWidthPx, cellHeightPx) * 0.35f
    val headWidth = minOf(cellWidthPx, cellHeightPx) * 0.40f

    // 1. Build escape route waypoints and continuous parameterized path
    val waypoints = EscapePathEngine.buildEscapeWaypoints(arrow, gridWidth, gridHeight, cellWidthPx, cellHeightPx)
    val cornerRadiusPx = minOf(cellWidthPx, cellHeightPx) * 0.40f
    val boardSize = max(cellWidthPx * gridWidth, cellHeightPx * gridHeight)
    val path = EscapePathEngine.ParameterizedPath(
        keyVertices = waypoints,
        cornerRadiusPx = cornerRadiusPx,
        boardBoundsSize = boardSize
    )

    // 2. Calculate arrow rest body length
    var bodyRestLength = 0f
    for (i in 0 until occupiedCells.size - 1) {
        val p1 = occupiedCells[i]
        val p2 = occupiedCells[i + 1]
        val dx = (p2.x - p1.x) * cellWidthPx
        val dy = (p2.y - p1.y) * cellHeightPx
        bodyRestLength += sqrt(dx * dx + dy * dy)
    }

    val totalTravelDistance = path.totalLength + cellWidthPx
    val distanceTraveled = EscapePathEngine.calculateTravelDistance(progress, totalTravelDistance)

    val uTail = distanceTraveled
    val uTip = distanceTraveled + bodyRestLength

    if (uTail >= path.totalLength && progress >= 0.99f) return

    val tipSample = path.sampleAt(uTip)
    val tipPos = tipSample.position
    val tipAngleDeg = tipSample.angleDegrees

    // 3. Draw body if arrow has length > 1
    if (bodyRestLength > 0.01f) {
        val bodyPath = path.buildBodyPath(uTail, uTip)

        val glowColor = when (selectedArrowId) {
            "ARROW_DRAGON_FLAME" -> Color(0xFFFF5722)
            "ARROW_PLASMA_BOLT" -> Color(0xFFC084FC)
            "ARROW_CRYSTAL_PRISM" -> Color(0xFF38BDF8)
            "ARROW_STEAMPUNK_BRASS" -> Color(0xFFF59E0B)
            "ARROW_HOLOGRAM_AURA" -> Color(0xFF10B981)
            "ARROW_GOOGLY_RAINBOW" -> Color(0xFFFF007F)
            "ARROW_GOLDEN_ROYAL" -> Color(0xFFFBBF24)
            "ARROW_VOID_SINGULARITY" -> Color(0xFFA855F7)
            else -> arrowColor
        }

        // Outer translucent neon glow aura along shaft
        drawPath(
            path = bodyPath,
            color = glowColor.copy(alpha = 0.45f),
            style = Stroke(
                width = strokeWidth * 2.4f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Parallel fiber laser lines
        drawPath(
            path = bodyPath,
            color = arrowColor.copy(alpha = 0.65f),
            style = Stroke(
                width = strokeWidth * 0.6f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Crisp inner core line shaft
        drawPath(
            path = bodyPath,
            color = Color.White.copy(alpha = 0.95f),
            style = Stroke(
                width = strokeWidth * 0.8f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }

    // 4. Draw glowing Arrowhead at tip oriented with the path tangent angle
    rotate(degrees = tipAngleDeg, pivot = tipPos) {
        drawCosmeticArrowhead(
            selectedArrowId = selectedArrowId,
            tipPos = tipPos,
            headLength = headLength,
            headWidth = headWidth,
            arrowColor = arrowColor,
            alpha = 1.0f
        )
    }

    // 5. Draw delicate glowing particle spark dots trailing along the wake behind tip
    for (i in 1..4) {
        val sparkDist = i * (minOf(cellWidthPx, cellHeightPx) * 0.22f)
        val sparkSample = path.sampleAt(max(0f, uTip - sparkDist))
        val sparkColor = when (selectedArrowId) {
            "ARROW_DRAGON_FLAME" -> Color(0xFFFF9100)
            "ARROW_PLASMA_BOLT" -> Color(0xFFE879F9)
            "ARROW_CRYSTAL_PRISM" -> Color(0xFFA5B4FC)
            "ARROW_GOLDEN_ROYAL" -> Color(0xFFFDE047)
            else -> theme.arrowHighlightColor
        }
        drawCircle(
            color = sparkColor.copy(alpha = (0.7f / i).coerceIn(0f, 1f)),
            radius = (minOf(cellWidthPx, cellHeightPx) * 0.16f / i),
            center = sparkSample.position
        )
    }
}

fun DrawScope.drawCosmeticArrowhead(
    selectedArrowId: String,
    tipPos: Offset,
    headLength: Float,
    headWidth: Float,
    arrowColor: Color,
    alpha: Float
) {
    when (selectedArrowId) {
        "ARROW_CRYSTAL_PRISM" -> {
            // Faceted Crystal Diamond Head
            val diamondPath = Path().apply {
                moveTo(tipPos.x, tipPos.y - headLength * 0.85f)
                lineTo(tipPos.x + headWidth * 0.55f, tipPos.y + headLength * 0.1f)
                lineTo(tipPos.x, tipPos.y + headLength * 0.45f)
                lineTo(tipPos.x - headWidth * 0.55f, tipPos.y + headLength * 0.1f)
                close()
            }
            drawPath(path = diamondPath, color = Color(0xFF818CF8).copy(alpha = alpha * 0.4f))
            drawPath(path = diamondPath, color = Color.White.copy(alpha = alpha * 0.95f), style = Stroke(width = 2.dp.toPx()))
            drawLine(Color(0xFF38BDF8), Offset(tipPos.x, tipPos.y - headLength * 0.85f), Offset(tipPos.x, tipPos.y + headLength * 0.45f), 2.dp.toPx())
        }
        "ARROW_DRAGON_FLAME" -> {
            // Dragon Flame Spearhead
            val flamePath = Path().apply {
                moveTo(tipPos.x, tipPos.y - headLength * 0.85f)
                cubicTo(tipPos.x + headWidth * 0.6f, tipPos.y - headLength * 0.2f, tipPos.x + headWidth * 0.6f, tipPos.y + headLength * 0.2f, tipPos.x, tipPos.y + headLength * 0.4f)
                cubicTo(tipPos.x - headWidth * 0.6f, tipPos.y + headLength * 0.2f, tipPos.x - headWidth * 0.6f, tipPos.y - headLength * 0.2f, tipPos.x, tipPos.y - headLength * 0.85f)
                close()
            }
            drawPath(path = flamePath, color = Color(0xFFFF3D00).copy(alpha = alpha * 0.6f))
            drawPath(path = flamePath, color = Color(0xFFFFD600).copy(alpha = alpha * 0.9f), style = Stroke(width = 2.dp.toPx()))
        }
        "ARROW_PLASMA_BOLT" -> {
            // Jagged Plasma Bolt
            val boltPath = Path().apply {
                moveTo(tipPos.x, tipPos.y - headLength * 0.8f)
                lineTo(tipPos.x + headWidth * 0.5f, tipPos.y)
                lineTo(tipPos.x + headWidth * 0.15f, tipPos.y + headLength * 0.1f)
                lineTo(tipPos.x + headWidth * 0.45f, tipPos.y + headLength * 0.4f)
                lineTo(tipPos.x, tipPos.y + headLength * 0.2f)
                lineTo(tipPos.x - headWidth * 0.45f, tipPos.y + headLength * 0.4f)
                lineTo(tipPos.x - headWidth * 0.15f, tipPos.y + headLength * 0.1f)
                lineTo(tipPos.x - headWidth * 0.5f, tipPos.y)
                close()
            }
            drawPath(path = boltPath, color = Color(0xFFD946EF).copy(alpha = alpha * 0.7f))
            drawPath(path = boltPath, color = Color.White.copy(alpha = alpha * 0.95f))
        }
        "ARROW_STEAMPUNK_BRASS" -> {
            // Steampunk Gear-Notched Pointer
            val gearPath = Path().apply {
                moveTo(tipPos.x, tipPos.y - headLength * 0.75f)
                lineTo(tipPos.x + headWidth * 0.45f, tipPos.y + headLength * 0.25f)
                lineTo(tipPos.x + headWidth * 0.2f, tipPos.y + headLength * 0.25f)
                lineTo(tipPos.x, tipPos.y + headLength * 0.1f)
                lineTo(tipPos.x - headWidth * 0.2f, tipPos.y + headLength * 0.25f)
                lineTo(tipPos.x - headWidth * 0.45f, tipPos.y + headLength * 0.25f)
                close()
            }
            drawPath(path = gearPath, color = Color(0xFFD97706).copy(alpha = alpha * 0.8f))
            drawPath(path = gearPath, color = Color(0xFFFEF3C7).copy(alpha = alpha * 0.95f), style = Stroke(width = 2.dp.toPx()))
        }
        "ARROW_RETRO_PIXEL" -> {
            // 8-Bit Pixelated Arrow
            drawRect(Color(0xFFFF0055), Offset(tipPos.x - 3.dp.toPx(), tipPos.y - headLength * 0.7f), Size(6.dp.toPx(), 6.dp.toPx()))
            drawRect(Color(0xFFFFEE00), Offset(tipPos.x - 7.dp.toPx(), tipPos.y - headLength * 0.35f), Size(14.dp.toPx(), 6.dp.toPx()))
            drawRect(Color(0xFF00FF99), Offset(tipPos.x - 11.dp.toPx(), tipPos.y), Size(22.dp.toPx(), 6.dp.toPx()))
        }
        "ARROW_HOLOGRAM_AURA" -> {
            // Wireframe Hologram Arrow
            val holoPath = Path().apply {
                moveTo(tipPos.x, tipPos.y - headLength * 0.75f)
                lineTo(tipPos.x - headWidth / 2, tipPos.y + headLength * 0.35f)
                lineTo(tipPos.x, tipPos.y + headLength * 0.15f)
                lineTo(tipPos.x + headWidth / 2, tipPos.y + headLength * 0.35f)
                close()
            }
            drawPath(path = holoPath, color = Color(0xFF10B981).copy(alpha = alpha * 0.25f))
            drawPath(path = holoPath, color = Color(0xFF00FF99).copy(alpha = alpha * 0.95f), style = Stroke(width = 2.5.dp.toPx()))
        }
        "ARROW_GOOGLY_RAINBOW" -> {
            // Playful Rainbow Comet Head with Cute Eyes
            val roundPath = Path().apply {
                moveTo(tipPos.x, tipPos.y - headLength * 0.7f)
                lineTo(tipPos.x - headWidth / 2, tipPos.y + headLength * 0.35f)
                lineTo(tipPos.x, tipPos.y + headLength * 0.15f)
                lineTo(tipPos.x + headWidth / 2, tipPos.y + headLength * 0.35f)
                close()
            }
            drawPath(path = roundPath, color = Color(0xFFFF007F).copy(alpha = alpha * 0.9f))
            // Googly cartoon eyes
            drawCircle(Color.White, radius = 4.dp.toPx(), center = Offset(tipPos.x - 5.dp.toPx(), tipPos.y))
            drawCircle(Color.Black, radius = 2.dp.toPx(), center = Offset(tipPos.x - 5.dp.toPx(), tipPos.y - 1.dp.toPx()))
            drawCircle(Color.White, radius = 4.dp.toPx(), center = Offset(tipPos.x + 5.dp.toPx(), tipPos.y))
            drawCircle(Color.Black, radius = 2.dp.toPx(), center = Offset(tipPos.x + 5.dp.toPx(), tipPos.y - 1.dp.toPx()))
        }
        "ARROW_GOLDEN_ROYAL" -> {
            // 24K Royal Gilded Spearhead
            val royalPath = Path().apply {
                moveTo(tipPos.x, tipPos.y - headLength * 0.85f)
                lineTo(tipPos.x + headWidth * 0.5f, tipPos.y + headLength * 0.25f)
                lineTo(tipPos.x + headWidth * 0.25f, tipPos.y + headLength * 0.15f)
                lineTo(tipPos.x, tipPos.y + headLength * 0.35f)
                lineTo(tipPos.x - headWidth * 0.25f, tipPos.y + headLength * 0.15f)
                lineTo(tipPos.x - headWidth * 0.5f, tipPos.y + headLength * 0.25f)
                close()
            }
            drawPath(path = royalPath, color = Color(0xFFF59E0B).copy(alpha = alpha * 0.85f))
            drawPath(path = royalPath, color = Color(0xFFFEF3C7).copy(alpha = alpha * 0.95f), style = Stroke(width = 2.5.dp.toPx()))
            drawCircle(Color(0xFFDC2626), radius = 2.5.dp.toPx(), center = Offset(tipPos.x, tipPos.y - headLength * 0.2f))
        }
        "ARROW_VOID_SINGULARITY" -> {
            // Dark matter void blade
            val voidPath = Path().apply {
                moveTo(tipPos.x, tipPos.y - headLength * 0.8f)
                lineTo(tipPos.x - headWidth / 2, tipPos.y + headLength * 0.35f)
                lineTo(tipPos.x, tipPos.y + headLength * 0.15f)
                lineTo(tipPos.x + headWidth / 2, tipPos.y + headLength * 0.35f)
                close()
            }
            drawCircle(Color(0xFFA855F7).copy(alpha = alpha * 0.45f), radius = headWidth * 0.7f, center = tipPos)
            drawPath(path = voidPath, color = Color(0xFF0F172A).copy(alpha = alpha * 0.95f))
            drawPath(path = voidPath, color = Color(0xFFA855F7).copy(alpha = alpha * 0.9f), style = Stroke(width = 2.dp.toPx()))
        }
        else -> {
            // Default ARROW_CYBER_NEON
            val headPath = Path().apply {
                moveTo(tipPos.x, tipPos.y - headLength * 0.7f) // Apex
                lineTo(tipPos.x - headWidth / 2, tipPos.y + headLength * 0.35f)
                lineTo(tipPos.x, tipPos.y + headLength * 0.15f)
                lineTo(tipPos.x + headWidth / 2, tipPos.y + headLength * 0.35f)
                close()
            }
            drawPath(path = headPath, color = arrowColor.copy(alpha = alpha * 0.6f))
            drawPath(path = headPath, color = Color.White.copy(alpha = alpha * 0.95f))
        }
    }
}

private fun DrawScope.drawArrowGraphics(
    arrow: Arrow,
    cellWidthPx: Float,
    cellHeightPx: Float,
    isUnobstructed: Boolean,
    isHinted: Boolean,
    hintScale: Float,
    alpha: Float,
    scale: Float = 1.0f,
    theme: GameTheme,
    selectedArrowId: String = "ARROW_CYBER_NEON"
) {
    val occupiedCells = arrow.getOccupiedCells()
    if (occupiedCells.isEmpty()) return

    val sumX = occupiedCells.sumOf { it.x.toDouble() } / occupiedCells.size
    val sumY = occupiedCells.sumOf { it.y.toDouble() } / occupiedCells.size
    val arrowCenter = Offset(
        (sumX * cellWidthPx + cellWidthPx / 2).toFloat(),
        (sumY * cellHeightPx + cellHeightPx / 2).toFloat()
    )

    scale(scale = scale, pivot = arrowCenter) {
        val arrowColor = getArrowColor(arrow, isHinted, theme)
        val strokeWidth = minOf(cellWidthPx, cellHeightPx) * 0.12f
        val headLength = minOf(cellWidthPx, cellHeightPx) * 0.35f
        val headWidth = minOf(cellWidthPx, cellHeightPx) * 0.40f

        // Build path connecting cell centers
        val linePath = Path()
        val firstPt = occupiedCells.first()
        linePath.moveTo(firstPt.x * cellWidthPx + cellWidthPx / 2, firstPt.y * cellHeightPx + cellHeightPx / 2)

        for (i in 1 until occupiedCells.size) {
            val pt = occupiedCells[i]
            linePath.lineTo(pt.x * cellWidthPx + cellWidthPx / 2, pt.y * cellHeightPx + cellHeightPx / 2)
        }

        val glowColor = when (selectedArrowId) {
            "ARROW_DRAGON_FLAME" -> Color(0xFFFF5722)
            "ARROW_PLASMA_BOLT" -> Color(0xFFC084FC)
            "ARROW_CRYSTAL_PRISM" -> Color(0xFF38BDF8)
            "ARROW_STEAMPUNK_BRASS" -> Color(0xFFF59E0B)
            "ARROW_HOLOGRAM_AURA" -> Color(0xFF10B981)
            "ARROW_GOOGLY_RAINBOW" -> Color(0xFFFF007F)
            "ARROW_GOLDEN_ROYAL" -> Color(0xFFFBBF24)
            "ARROW_VOID_SINGULARITY" -> Color(0xFFA855F7)
            else -> arrowColor
        }

        // Draw outer translucent neon glow aura along shaft
        drawPath(
            path = linePath,
            color = glowColor.copy(alpha = alpha * 0.45f),
            style = Stroke(
                width = strokeWidth * 2.4f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw parallel fiber lines
        drawPath(
            path = linePath,
            color = arrowColor.copy(alpha = alpha * 0.65f),
            style = Stroke(
                width = strokeWidth * 0.6f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw crisp inner core line shaft
        drawPath(
            path = linePath,
            color = Color.White.copy(alpha = alpha * 0.95f),
            style = Stroke(
                width = strokeWidth * 0.8f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Tip cell & Direction
        val tipCell = arrow.getTipCell()
        val tipCenter = Offset(
            tipCell.x * cellWidthPx + cellWidthPx / 2,
            tipCell.y * cellHeightPx + cellHeightPx / 2
        )
        val tipDirection = arrow.getTipDirection()

        // Draw translucent glowing Arrowhead at tip
        rotate(degrees = tipDirection.rotationDegrees, pivot = tipCenter) {
            drawCosmeticArrowhead(
                selectedArrowId = selectedArrowId,
                tipPos = tipCenter,
                headLength = headLength,
                headWidth = headWidth,
                arrowColor = arrowColor,
                alpha = alpha
            )
        }

        // Concentric Ripple Target on Hinted/Selected Arrow
        if (isHinted) {
            val baseRadius = minOf(cellWidthPx, cellHeightPx) * 0.32f
            drawCircle(
                color = theme.arrowHighlightColor.copy(alpha = 0.25f * alpha),
                radius = baseRadius * hintScale,
                center = tipCenter
            )
            drawCircle(
                color = theme.arrowHighlightColor.copy(alpha = 0.45f * alpha),
                radius = baseRadius * 0.65f * hintScale,
                center = tipCenter
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.95f * alpha),
                radius = baseRadius * 0.35f,
                center = tipCenter
            )
        }
    }
}
