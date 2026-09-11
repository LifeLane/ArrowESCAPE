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
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt
import com.mitsara.arrowescape.engine.EscapePathEngine
import com.mitsara.arrowescape.engine.PuzzleSolver
import com.mitsara.arrowescape.model.Arrow
import com.mitsara.arrowescape.model.Direction
import com.mitsara.arrowescape.model.GameTheme
import com.mitsara.arrowescape.model.GridPoint
import com.mitsara.arrowescape.model.ThemeManager

class EscapeParticle(var x: Float, var y: Float, var vx: Float, var vy: Float, var life: Float)

/**
 * Infinite Notion-Like Canvas with pinch-to-zoom, pan, infinite matrix dot grid,
 * and high-precision conditional escape dependency visualization.
 */
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

    // Pulse animation for hint and blocking warning arrows
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

    val warningPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "warningPulseAlpha"
    )

    // Escape progress animation driven by path length
    val escapeProgress = remember(animatingArrowId) { Animatable(0f) }
    
    // Tactile press scale feedback for tapped arrow
    val pressScale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    var pressedArrowId by remember { mutableStateOf<Int?>(null) }
    
    // Store recent escapes for particle effects
    val particles = remember { mutableStateListOf<EscapeParticle>() }

    // Notion Canvas Pan & Zoom State
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    
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
                for (i in 0..14) {
                    particles.add(
                        EscapeParticle(
                            x = tip.x.toFloat(),
                            y = tip.y.toFloat(),
                            vx = (Math.random() - 0.5).toFloat() * 4.5f,
                            vy = (Math.random() - 0.5).toFloat() * 4.5f,
                            life = 1f
                        )
                    )
                }
            }
        }
    }
    
    // Update particles loop
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

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0B0F17))
            .testTag("puzzle_board")
    ) {
        val canvasWidthPx = constraints.maxWidth.toFloat()
        val canvasHeightPx = constraints.maxHeight.toFloat()
        val boardSidePx = min(canvasWidthPx, canvasHeightPx) * 0.90f

        val boardLeft = (canvasWidthPx - boardSidePx) / 2f + panOffset.x
        val boardTop = (canvasHeightPx - boardSidePx) / 2f + panOffset.y
        val boardCenter = Offset(boardLeft + boardSidePx / 2f, boardTop + boardSidePx / 2f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(gridWidth, gridHeight, activeArrows, zoomScale, panOffset) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.55f, 3.5f)
                        panOffset += pan
                    }
                }
                .pointerInput(gridWidth, gridHeight, activeArrows, zoomScale, panOffset) {
                    detectTapGestures { tapOffset ->
                        // Transform tap point from screen to board coordinates
                        val relX = (tapOffset.x - boardCenter.x) / zoomScale + boardSidePx / 2f
                        val relY = (tapOffset.y - boardCenter.y) / zoomScale + boardSidePx / 2f

                        if (relX in 0f..boardSidePx && relY in 0f..boardSidePx) {
                            val cW = boardSidePx / gridWidth
                            val cH = boardSidePx / gridHeight
                            val cellX = (relX / cW).toInt().coerceIn(0, gridWidth - 1)
                            val cellY = (relY / cH).toInt().coerceIn(0, gridHeight - 1)
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
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // 1. Draw Infinite Notion Canvas Dot Grid Background
                val dotSpacing = 28.dp.toPx() * zoomScale
                val startX = (panOffset.x % dotSpacing) - dotSpacing
                val startY = (panOffset.y % dotSpacing) - dotSpacing

                var xPos = startX
                while (xPos < size.width + dotSpacing) {
                    var yPos = startY
                    while (yPos < size.height + dotSpacing) {
                        drawCircle(
                            color = Color(0xFF334155).copy(alpha = 0.35f),
                            radius = 1.4f * zoomScale.coerceIn(0.8f, 1.8f),
                            center = Offset(xPos, yPos)
                        )
                        yPos += dotSpacing
                    }
                    xPos += dotSpacing
                }

                // 2. Draw Zoomed & Panned Board Content
                translate(left = boardLeft + shakeOffset.value, top = boardTop) {
                    scale(scale = zoomScale, pivot = Offset(boardSidePx / 2f, boardSidePx / 2f)) {
                        val cW = boardSidePx / gridWidth
                        val cH = boardSidePx / gridHeight
                        val boardSize = Size(boardSidePx, boardSidePx)

                        // Ambient board shadow & backdrop
                        drawRoundRect(
                            color = Color(0xFF020617).copy(alpha = 0.85f),
                            size = boardSize,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
                        )

                        // Board Surface
                        drawCosmeticBoardSurface(selectedBoardId, boardSize, theme.boardCanvasColor)

                        // Grid Matrix Slots
                        drawCosmeticGridSlots(
                            gridId = selectedGridId,
                            gridWidth = gridWidth,
                            gridHeight = gridHeight,
                            cellW = cW,
                            cellH = cH,
                            validCells = validCells,
                            themeDotColor = theme.gridDotColor
                        )

                        // Render Obstacles
                        for (obs in obstacles) {
                            val obsCenter = Offset(obs.x * cW + cW / 2, obs.y * cH + cH / 2)
                            val shapeRadius = minOf(cW, cH) * 0.38f
                            
                            drawCircle(
                                color = Color(0xFFFF5722).copy(alpha = 0.3f),
                                radius = shapeRadius * 1.2f,
                                center = obsCenter
                            )
                            
                            val diamondPath = Path().apply {
                                moveTo(obsCenter.x, obsCenter.y - shapeRadius)
                                lineTo(obsCenter.x + shapeRadius, obsCenter.y)
                                lineTo(obsCenter.x, obsCenter.y + shapeRadius)
                                lineTo(obsCenter.x - shapeRadius, obsCenter.y)
                                close()
                            }
                            drawPath(path = diamondPath, color = Color(0xFF1E293B))
                            drawPath(
                                path = diamondPath,
                                color = Color(0xFFFF5722),
                                style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.9f),
                                radius = 4f,
                                center = obsCenter
                            )
                        }

                        // Calculate collision details for inspected (blocked) arrow
                        var blockingArrow: Arrow? = null
                        var collisionHitPoint: GridPoint? = null

                        if (inspectedArrowId != null) {
                            val inspected = activeArrows.find { it.id == inspectedArrowId }
                            if (inspected != null) {
                                val ray = inspected.getExitRay(gridWidth, gridHeight)
                                for (pt in ray) {
                                    val blocker = activeArrows.find { it.id != inspected.id && it.getOccupiedCells().contains(pt) }
                                    if (blocker != null) {
                                        blockingArrow = blocker
                                        collisionHitPoint = pt
                                        break
                                    }
                                }
                            }
                        }

                        // Render Active Arrows
                        for (arrow in activeArrows) {
                            val isAnimatingThis = arrow.id == animatingArrowId
                            val isHintedThis = arrow.id == hintArrowId
                            val isInspectedThis = arrow.id == inspectedArrowId
                            val isBlockingThis = arrow.id == blockingArrow?.id
                            val isUnobstructed = PuzzleSolver.isArrowUnobstructed(arrow, activeArrows, gridWidth, gridHeight, obstacles)

                            if (isAnimatingThis) {
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
                                val arrowScale = if (arrow.id == pressedArrowId) pressScale.value else 1.0f

                                drawArrowGraphics(
                                    arrow = arrow,
                                    cellWidthPx = cW,
                                    cellHeightPx = cH,
                                    isUnobstructed = isUnobstructed,
                                    isHinted = isHintedThis,
                                    isBlockingWarning = isBlockingThis,
                                    warningAlpha = warningPulseAlpha,
                                    hintScale = hintPulseScale,
                                    alpha = 1.0f,
                                    scale = arrowScale,
                                    theme = theme,
                                    selectedArrowId = selectedArrowId
                                )

                                // Draw laser collision line from inspected arrow to blocking arrow
                                if (isInspectedThis) {
                                    val tip = arrow.getTipCell()
                                    val tipDir = arrow.getTipDirection()
                                    val startOffset = Offset(tip.x * cW + cW / 2, tip.y * cH + cH / 2)
                                    val endOffset = if (collisionHitPoint != null) {
                                        Offset(collisionHitPoint.x * cW + cW / 2, collisionHitPoint.y * cH + cH / 2)
                                    } else {
                                        Offset(tip.x * cW + cW / 2 + tipDir.dx * cW * 2f, tip.y * cH + cH / 2 + tipDir.dy * cH * 2f)
                                    }

                                    // Outer red laser glow
                                    drawLine(
                                        color = Color(0xFFEF4444).copy(alpha = 0.85f),
                                        start = startOffset,
                                        end = endOffset,
                                        strokeWidth = minOf(cW, cH) * 0.28f,
                                        cap = StrokeCap.Round
                                    )
                                    // Inner laser beam
                                    drawLine(
                                        color = Color.White.copy(alpha = 0.95f),
                                        start = startOffset,
                                        end = endOffset,
                                        strokeWidth = minOf(cW, cH) * 0.12f,
                                        cap = StrokeCap.Round
                                    )

                                    if (collisionHitPoint != null) {
                                        drawCircle(
                                            color = Color(0xFFEF4444),
                                            radius = minOf(cW, cH) * 0.40f,
                                            center = endOffset
                                        )
                                        drawCircle(
                                            color = Color.White,
                                            radius = minOf(cW, cH) * 0.18f,
                                            center = endOffset
                                        )
                                    }
                                }
                            }
                        }

                        // Draw Particles
                        for (p in particles) {
                            drawCircle(
                                color = theme.arrowHighlightColor.copy(alpha = p.life),
                                radius = (p.life * 10f),
                                center = Offset(p.x * cW + cW / 2, p.y * cH + cH / 2)
                            )
                        }

                        // Frame Border
                        drawCosmeticFrameBorder(selectedFrameId, boardSize, theme.arrowHighlightColor)
                    }
                }
            }
        }

        // Floating Notion Canvas Controls (Zoom In, Zoom Indicator, Zoom Out, Recenter)
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E293B).copy(alpha = 0.92f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569).copy(alpha = 0.45f)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Zoom In (+)
                IconButton(
                    onClick = {
                        zoomScale = (zoomScale * 1.25f).coerceAtMost(3.5f)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Zoom Level Pill
                Text(
                    text = "${(zoomScale * 100).roundToInt()}%",
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                // Zoom Out (-)
                IconButton(
                    onClick = {
                        zoomScale = (zoomScale / 1.25f).coerceAtLeast(0.55f)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Recenter Canvas
                IconButton(
                    onClick = {
                        zoomScale = 1.0f
                        panOffset = Offset.Zero
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CenterFocusStrong,
                        contentDescription = "Center View",
                        tint = theme.arrowHighlightColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
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
    if (theme.id == "EYE_COMFORT") {
        return theme.arrowNormalColor
    }
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

    val waypoints = EscapePathEngine.buildEscapeWaypoints(arrow, gridWidth, gridHeight, cellWidthPx, cellHeightPx)
    val cornerRadiusPx = minOf(cellWidthPx, cellHeightPx) * 0.40f
    val boardSize = max(cellWidthPx * gridWidth, cellHeightPx * gridHeight)
    val path = EscapePathEngine.ParameterizedPath(
        keyVertices = waypoints,
        cornerRadiusPx = cornerRadiusPx,
        boardBoundsSize = boardSize
    )

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

        drawPath(
            path = bodyPath,
            color = glowColor.copy(alpha = 0.45f),
            style = Stroke(
                width = strokeWidth * 2.4f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        drawPath(
            path = bodyPath,
            color = arrowColor.copy(alpha = 0.65f),
            style = Stroke(
                width = strokeWidth * 0.6f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

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
            drawRect(Color(0xFFFF0055), Offset(tipPos.x - 3.dp.toPx(), tipPos.y - headLength * 0.7f), Size(6.dp.toPx(), 6.dp.toPx()))
            drawRect(Color(0xFFFFEE00), Offset(tipPos.x - 7.dp.toPx(), tipPos.y - headLength * 0.35f), Size(14.dp.toPx(), 6.dp.toPx()))
            drawRect(Color(0xFF00FF99), Offset(tipPos.x - 11.dp.toPx(), tipPos.y), Size(22.dp.toPx(), 6.dp.toPx()))
        }
        "ARROW_HOLOGRAM_AURA" -> {
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
            val roundPath = Path().apply {
                moveTo(tipPos.x, tipPos.y - headLength * 0.7f)
                lineTo(tipPos.x - headWidth / 2, tipPos.y + headLength * 0.35f)
                lineTo(tipPos.x, tipPos.y + headLength * 0.15f)
                lineTo(tipPos.x + headWidth / 2, tipPos.y + headLength * 0.35f)
                close()
            }
            drawPath(path = roundPath, color = Color(0xFFFF007F).copy(alpha = alpha * 0.9f))
            drawCircle(Color.White, radius = 4.dp.toPx(), center = Offset(tipPos.x - 5.dp.toPx(), tipPos.y))
            drawCircle(Color.Black, radius = 2.dp.toPx(), center = Offset(tipPos.x - 5.dp.toPx(), tipPos.y - 1.dp.toPx()))
            drawCircle(Color.White, radius = 4.dp.toPx(), center = Offset(tipPos.x + 5.dp.toPx(), tipPos.y))
            drawCircle(Color.Black, radius = 2.dp.toPx(), center = Offset(tipPos.x + 5.dp.toPx(), tipPos.y - 1.dp.toPx()))
        }
        "ARROW_GOLDEN_ROYAL" -> {
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
            val headPath = Path().apply {
                moveTo(tipPos.x, tipPos.y - headLength * 0.7f)
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
    isBlockingWarning: Boolean = false,
    warningAlpha: Float = 0.5f,
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
        val baseColor = getArrowColor(arrow, isHinted, theme)
        val arrowColor = if (isBlockingWarning) Color(0xFFEF4444) else baseColor
        val strokeWidth = minOf(cellWidthPx, cellHeightPx) * 0.12f
        val headLength = minOf(cellWidthPx, cellHeightPx) * 0.35f
        val headWidth = minOf(cellWidthPx, cellHeightPx) * 0.40f

        val linePath = Path()
        val firstPt = occupiedCells.first()
        linePath.moveTo(firstPt.x * cellWidthPx + cellWidthPx / 2, firstPt.y * cellHeightPx + cellHeightPx / 2)

        for (i in 1 until occupiedCells.size) {
            val pt = occupiedCells[i]
            linePath.lineTo(pt.x * cellWidthPx + cellWidthPx / 2, pt.y * cellHeightPx + cellHeightPx / 2)
        }

        val glowColor = if (isBlockingWarning) {
            Color(0xFFEF4444)
        } else {
            when (selectedArrowId) {
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
        }

        // Draw outer translucent glow aura along shaft
        drawPath(
            path = linePath,
            color = glowColor.copy(alpha = if (isBlockingWarning) warningAlpha * 0.7f else alpha * 0.45f),
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

        val tipCell = arrow.getTipCell()
        val tipCenter = Offset(
            tipCell.x * cellWidthPx + cellWidthPx / 2,
            tipCell.y * cellHeightPx + cellHeightPx / 2
        )
        val tipDirection = arrow.getTipDirection()

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

        // Concentric Ripple Target on Hinted Arrow
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

        // Concentric Warning Halo on Blocking Arrow
        if (isBlockingWarning) {
            val baseRadius = minOf(cellWidthPx, cellHeightPx) * 0.40f
            drawCircle(
                color = Color(0xFFEF4444).copy(alpha = warningAlpha * 0.5f),
                radius = baseRadius * 1.3f,
                center = tipCenter
            )
        }
    }
}
