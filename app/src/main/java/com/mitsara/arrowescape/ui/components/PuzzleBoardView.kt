package com.mitsara.arrowescape.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.remember
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mitsara.arrowescape.engine.PuzzleSolver
import com.mitsara.arrowescape.model.Arrow
import com.mitsara.arrowescape.model.Direction
import com.mitsara.arrowescape.model.GameTheme
import com.mitsara.arrowescape.model.GridPoint

@Composable
fun PuzzleBoardView(
    gridWidth: Int,
    gridHeight: Int,
    activeArrows: List<Arrow>,
    animatingArrowId: Int?,
    animatingDirection: Direction?,
    hintArrowId: Int?,
    isMistakeShake: Boolean,
    onArrowClick: (Int) -> Unit,
    theme: GameTheme = GameTheme.LIGHT,
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

    // Escape progress animation
    val escapeProgress = remember(animatingArrowId) { Animatable(0f) }
    LaunchedEffect(animatingArrowId) {
        if (animatingArrowId != null) {
            escapeProgress.snapTo(0f)
            escapeProgress.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 220, easing = LinearEasing)
            )
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
                        onArrowClick(tappedArrow.id)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cW = size.width / gridWidth
            val cH = size.height / gridHeight

            // Grid guidance dots background
            for (x in 0 until gridWidth) {
                for (y in 0 until gridHeight) {
                    val centerX = x * cW + cW / 2
                    val centerY = y * cH + cH / 2
                    drawCircle(
                        color = theme.gridDotColor,
                        radius = 2.5.dp.toPx(),
                        center = Offset(centerX, centerY)
                    )
                }
            }

            // Render active arrows
            for (arrow in activeArrows) {
                val isAnimatingThis = arrow.id == animatingArrowId
                val isHintedThis = arrow.id == hintArrowId
                val isUnobstructed = PuzzleSolver.isArrowUnobstructed(arrow, activeArrows, gridWidth, gridHeight)

                val animProgressVal = if (isAnimatingThis) escapeProgress.value else 0f
                val animTravel = animProgressVal * minOf(size.width, size.height) * 1.5f
                val escapeDir = arrow.getTipDirection()
                val offsetX = escapeDir.dx * animTravel
                val offsetY = escapeDir.dy * animTravel
                val animAlpha = if (isAnimatingThis) (1.0f - animProgressVal).coerceIn(0f, 1f) else 1.0f

                translate(left = offsetX, top = offsetY) {
                    drawArrowGraphics(
                        arrow = arrow,
                        cellWidthPx = cW,
                        cellHeightPx = cH,
                        isUnobstructed = isUnobstructed,
                        isHinted = isHintedThis,
                        hintScale = hintPulseScale,
                        alpha = animAlpha,
                        theme = theme
                    )
                }
            }
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
    theme: GameTheme
) {
    val occupiedCells = arrow.getOccupiedCells()
    if (occupiedCells.isEmpty()) return

    val strokeWidth = minOf(cellWidthPx, cellHeightPx) * 0.22f
    val headLength = minOf(cellWidthPx, cellHeightPx) * 0.40f
    val headWidth = minOf(cellWidthPx, cellHeightPx) * 0.45f

    val pathColor = if (isHinted) theme.arrowHighlightColor else theme.arrowNormalColor

    // Build path connecting cell centers
    val linePath = Path()
    val firstPt = occupiedCells.first()
    linePath.moveTo(firstPt.x * cellWidthPx + cellWidthPx / 2, firstPt.y * cellHeightPx + cellHeightPx / 2)

    for (i in 1 until occupiedCells.size) {
        val pt = occupiedCells[i]
        linePath.lineTo(pt.x * cellWidthPx + cellWidthPx / 2, pt.y * cellHeightPx + cellHeightPx / 2)
    }

    // Draw main arrow body path with rounded corners and caps
    drawPath(
        path = linePath,
        color = pathColor.copy(alpha = alpha),
        style = Stroke(
            width = strokeWidth,
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

    // Draw solid Arrowhead at tip
    rotate(degrees = tipDirection.rotationDegrees, pivot = tipCenter) {
        val headPath = Path().apply {
            moveTo(tipCenter.x, tipCenter.y - headLength * 0.65f) // Apex
            lineTo(tipCenter.x - headWidth / 2, tipCenter.y + headLength * 0.35f)
            lineTo(tipCenter.x, tipCenter.y + headLength * 0.1f)
            lineTo(tipCenter.x + headWidth / 2, tipCenter.y + headLength * 0.35f)
            close()
        }

        drawPath(
            path = headPath,
            color = pathColor.copy(alpha = alpha)
        )
    }

    // Concentric Ripple Target on Hinted/Selected Arrow (Screenshot 1 & 4)
    if (isHinted) {
        val baseRadius = minOf(cellWidthPx, cellHeightPx) * 0.30f
        drawCircle(
            color = theme.arrowHighlightColor.copy(alpha = 0.25f * alpha),
            radius = baseRadius * hintScale,
            center = tipCenter
        )
        drawCircle(
            color = theme.arrowHighlightColor.copy(alpha = 0.40f * alpha),
            radius = baseRadius * 0.65f * hintScale,
            center = tipCenter
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.90f * alpha),
            radius = baseRadius * 0.35f,
            center = tipCenter
        )
    }
}
