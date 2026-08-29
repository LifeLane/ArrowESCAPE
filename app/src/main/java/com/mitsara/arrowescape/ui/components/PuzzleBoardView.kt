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

    // Escape progress animation with spring-based momentum physics
    val escapeProgress = remember(animatingArrowId) { Animatable(0f) }
    
    // Tactile press scale feedback for tapped arrow
    val pressScale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    var pressedArrowId by remember { mutableStateOf<Int?>(null) }
    
    // Store recent escapes for particle effects
    val particles = remember { mutableStateListOf<EscapeParticle>() }
    
    LaunchedEffect(animatingArrowId) {
        if (animatingArrowId != null) {
            escapeProgress.snapTo(0f)
            escapeProgress.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = 150f
                )
            )
            
            // Generate particles at tip when animation finishes
            val arrow = activeArrows.find { it.id == animatingArrowId }
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
            .shadow(8.dp, shape = RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(theme.boardCanvasColor)
            .padding(12.dp)
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

            // Grid cell slots background
            for (x in 0 until gridWidth) {
                for (y in 0 until gridHeight) {
                    val p = GridPoint(x, y)
                    if (validCells == null || validCells.contains(p)) {
                        val left = x * cW + 4f
                        val top = y * cH + 4f
                        val w = cW - 8f
                        val h = cH - 8f
                        drawRoundRect(
                            color = theme.gridDotColor.copy(alpha = 0.25f),
                            topLeft = Offset(left, top),
                            size = androidx.compose.ui.geometry.Size(w, h),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                        )
                        val centerX = x * cW + cW / 2
                        val centerY = y * cH + cH / 2
                        drawCircle(
                            color = theme.gridDotColor.copy(alpha = 0.6f),
                            radius = 2.dp.toPx(),
                            center = Offset(centerX, centerY)
                        )
                    }
                }
            }

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

                val animProgressVal = if (isAnimatingThis) escapeProgress.value else 0f
                val escapeDir = arrow.getTipDirection()
                
                // Smooth snake-like sliding along grid axes without fading or side curving
                val boardSpan = minOf(size.width, size.height) * 2.5f
                val travelDist = animProgressVal * boardSpan
                
                val offsetX = escapeDir.dx * travelDist
                val offsetY = escapeDir.dy * travelDist
                val animAlpha = 1.0f // Fully opaque as it slides off the board like a snake
                val escapeScale = 1.0f

                // Laser inspection ray when tapped while blocked
                if (isInspectedThis) {
                    val tip = arrow.getTipCell()
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
                        Offset(lastRayPt.x * cW + cW / 2, lastRayPt.y * cH + cH / 2) + Offset(escapeDir.dx * cW, escapeDir.dy * cH)
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

                // Lightweight glowing neon sparkle trail when escaping
                if (isAnimatingThis) {
                    val tip = arrow.getTipCell()
                    val startCenter = Offset(tip.x * cW + cW / 2, tip.y * cH + cH / 2)
                    val currentCenter = Offset(startCenter.x + offsetX, startCenter.y + offsetY)
                    
                    // Draw delicate fading particle spark dots along the wake
                    for (i in 1..3) {
                        val sparkOffset = i * 15f
                        val sparkCenter = Offset(
                            currentCenter.x - escapeDir.dx * sparkOffset,
                            currentCenter.y - escapeDir.dy * sparkOffset
                        )
                        drawCircle(
                            color = theme.arrowHighlightColor.copy(alpha = (animAlpha * (0.6f / i)).coerceIn(0f, 1f)),
                            radius = (minOf(cW, cH) * 0.15f / i),
                            center = sparkCenter
                        )
                    }
                }

                val arrowScale = (if (arrow.id == pressedArrowId) pressScale.value else 1.0f) * escapeScale

                translate(left = offsetX, top = offsetY) {
                    drawArrowGraphics(
                        arrow = arrow,
                        cellWidthPx = cW,
                        cellHeightPx = cH,
                        isUnobstructed = isUnobstructed,
                        isHinted = isHintedThis,
                        hintScale = hintPulseScale,
                        alpha = animAlpha,
                        scale = arrowScale,
                        theme = theme
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

private fun DrawScope.drawArrowGraphics(
    arrow: Arrow,
    cellWidthPx: Float,
    cellHeightPx: Float,
    isUnobstructed: Boolean,
    isHinted: Boolean,
    hintScale: Float,
    alpha: Float,
    scale: Float = 1.0f,
    theme: GameTheme
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

        // Build path connecting cell centers for single-line transparent neon arrow with long tail
        val linePath = Path()
        val firstPt = occupiedCells.first()
        linePath.moveTo(firstPt.x * cellWidthPx + cellWidthPx / 2, firstPt.y * cellHeightPx + cellHeightPx / 2)

        for (i in 1 until occupiedCells.size) {
            val pt = occupiedCells[i]
            linePath.lineTo(pt.x * cellWidthPx + cellWidthPx / 2, pt.y * cellHeightPx + cellHeightPx / 2)
        }

        // Draw outer translucent neon glow aura along shaft
        drawPath(
            path = linePath,
            color = arrowColor.copy(alpha = alpha * 0.4f),
            style = Stroke(
                width = strokeWidth * 2.2f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw parallel neon laser lines along the tail for rich multi-line fiber aesthetic
        drawPath(
            path = linePath,
            color = arrowColor.copy(alpha = alpha * 0.6f),
            style = Stroke(
                width = strokeWidth * 0.5f,
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

        // Unobstructed "Ready to escape" glowing pulse indicator
        if (isUnobstructed && !isHinted) {
            drawCircle(
                color = theme.arrowHighlightColor.copy(alpha = 0.4f * alpha),
                radius = minOf(cellWidthPx, cellHeightPx) * 0.42f,
                center = tipCenter
            )
        }

        // Draw translucent glowing Arrowhead at tip
        rotate(degrees = tipDirection.rotationDegrees, pivot = tipCenter) {
            val headPath = Path().apply {
                moveTo(tipCenter.x, tipCenter.y - headLength * 0.7f) // Apex
                lineTo(tipCenter.x - headWidth / 2, tipCenter.y + headLength * 0.35f)
                lineTo(tipCenter.x, tipCenter.y + headLength * 0.15f)
                lineTo(tipCenter.x + headWidth / 2, tipCenter.y + headLength * 0.35f)
                close()
            }

            // Outer arrowhead glow
            drawPath(
                path = headPath,
                color = arrowColor.copy(alpha = alpha * 0.6f)
            )
            // Inner bright arrowhead face
            drawPath(
                path = headPath,
                color = Color.White.copy(alpha = alpha * 0.95f)
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
