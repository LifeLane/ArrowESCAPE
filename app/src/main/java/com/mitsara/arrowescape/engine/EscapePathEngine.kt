package com.mitsara.arrowescape.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import com.mitsara.arrowescape.model.Arrow
import com.mitsara.arrowescape.model.Direction
import com.mitsara.arrowescape.model.GridPoint
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * EscapePathEngine provides vector mathematics, polyline path-following,
 * corner fillet arc transitions, and exit fly-away physics for escaping arrows.
 */
object EscapePathEngine {

    /**
     * Animation state phases for the escape sequence.
     */
    enum class EscapePhase {
        FOLLOWING_PATH,
        TURNING,
        EXIT_ALIGN,
        FLYING_AWAY,
        COMPLETE
    }

    data class PathSample(
        val position: Offset,
        val tangent: Offset,
        val angleDegrees: Float,
        val phase: EscapePhase
    )

    /**
     * Calculates the total escape duration in milliseconds based on path length.
     */
    fun calculateEscapeDurationMs(arrow: Arrow, gridWidth: Int, gridHeight: Int): Int {
        val occupiedCount = arrow.getOccupiedCells().size
        val exitRayCount = arrow.getExitRay(gridWidth, gridHeight).size
        val totalGridDistance = occupiedCount + exitRayCount + 4.0f
        // ~65ms per grid cell travel with a reasonable bound for crisp game feel
        return (totalGridDistance * 65f).toInt().coerceIn(320, 750)
    }

    /**
     * Builds the complete ordered list of grid points and offscreen extension for the arrow escape route.
     */
    fun buildEscapeWaypoints(
        arrow: Arrow,
        gridWidth: Int,
        gridHeight: Int,
        cellWidthPx: Float,
        cellHeightPx: Float
    ): List<Offset> {
        val occupied = arrow.getOccupiedCells()
        val exitRay = arrow.getExitRay(gridWidth, gridHeight)
        val tipDir = arrow.getTipDirection()

        val allGridPoints = mutableListOf<GridPoint>()
        allGridPoints.addAll(occupied)
        allGridPoints.addAll(exitRay)

        // Convert grid centers to pixel offsets
        val pixelWaypoints = mutableListOf<Offset>()
        for (gp in allGridPoints) {
            val px = gp.x * cellWidthPx + cellWidthPx / 2f
            val py = gp.y * cellHeightPx + cellHeightPx / 2f
            pixelWaypoints.add(Offset(px, py))
        }

        // Add off-screen extension beyond the last point in the exit direction
        val lastPoint = pixelWaypoints.lastOrNull() ?: Offset(
            arrow.startX * cellWidthPx + cellWidthPx / 2f,
            arrow.startY * cellHeightPx + cellHeightPx / 2f
        )
        // Extra distance to ensure arrow tail flies completely past canvas boundaries
        val offscreenDist = max(cellWidthPx * gridWidth, cellHeightPx * gridHeight) * 1.5f + (occupied.size * max(cellWidthPx, cellHeightPx))
        val offscreenTarget = Offset(
            lastPoint.x + tipDir.dx * offscreenDist,
            lastPoint.y + tipDir.dy * offscreenDist
        )
        pixelWaypoints.add(offscreenTarget)

        return simplifyCollinearWaypoints(pixelWaypoints)
    }

    /**
     * Simplifies collinear consecutive segments into key corner vertices and endpoints.
     */
    private fun simplifyCollinearWaypoints(waypoints: List<Offset>): List<Offset> {
        if (waypoints.size <= 2) return waypoints

        val simplified = mutableListOf<Offset>()
        simplified.add(waypoints.first())

        for (i in 1 until waypoints.size - 1) {
            val prev = simplified.last()
            val curr = waypoints[i]
            val next = waypoints[i + 1]

            val d1x = curr.x - prev.x
            val d1y = curr.y - prev.y
            val d2x = next.x - curr.x
            val d2y = next.y - curr.y

            val crossProduct = d1x * d2y - d1y * d2x
            val dotProduct = d1x * d2x + d1y * d2y

            // If not strictly collinear or direction changes, it is a corner vertex
            if (kotlin.math.abs(crossProduct) > 0.01f || dotProduct < 0f) {
                simplified.add(curr)
            }
        }

        simplified.add(waypoints.last())
        return simplified
    }

    /**
     * Parameterized continuous path with smooth corner fillets.
     */
    class ParameterizedPath(
        val keyVertices: List<Offset>,
        val cornerRadiusPx: Float,
        val boardBoundsSize: Float
    ) {
        private val samples = mutableListOf<PathPoint>()
        val totalLength: Float

        init {
            buildDenseSamples()
            totalLength = samples.lastOrNull()?.cumDist ?: 0f
        }

        private data class PathPoint(
            val pos: Offset,
            val tangent: Offset,
            val angleDeg: Float,
            val cumDist: Float,
            val isCorner: Boolean
        )

        private fun buildDenseSamples() {
            if (keyVertices.isEmpty()) return
            if (keyVertices.size == 1) {
                samples.add(PathPoint(keyVertices[0], Offset(0f, -1f), 0f, 0f, false))
                return
            }

            var currentCumDist = 0f
            val stepSize = 3.0f // High-density sub-pixel sampling for butter-smooth path following

            for (i in 0 until keyVertices.size - 1) {
                val vCurr = keyVertices[i]
                val vNext = keyVertices[i + 1]

                // Check if this segment starts after a corner fillet
                val hasCornerAtStart = (i > 0)
                val hasCornerAtEnd = (i < keyVertices.size - 2)

                val vPrev = if (i > 0) keyVertices[i - 1] else null
                val vAfter = if (i < keyVertices.size - 2) keyVertices[i + 2] else null

                val segDx = vNext.x - vCurr.x
                val segDy = vNext.y - vCurr.y
                val segLen = sqrt(segDx * segDx + segDy * segDy)
                if (segLen <= 0.001f) continue

                val uDir = Offset(segDx / segLen, segDy / segLen)

                // Calculate corner fillet bounds
                val rStart = if (hasCornerAtStart && vPrev != null) {
                    val prevDx = vCurr.x - vPrev.x
                    val prevDy = vCurr.y - vPrev.y
                    val prevLen = sqrt(prevDx * prevDx + prevDy * prevDy)
                    min(cornerRadiusPx, min(prevLen * 0.45f, segLen * 0.45f))
                } else 0f

                val rEnd = if (hasCornerAtEnd && vAfter != null) {
                    val nextDx = vAfter.x - vNext.x
                    val nextDy = vAfter.y - vNext.y
                    val nextLen = sqrt(nextDx * nextDx + nextDy * nextDy)
                    min(cornerRadiusPx, min(segLen * 0.45f, nextLen * 0.45f))
                } else 0f

                val straightStart = if (rStart > 0f) vCurr + uDir * rStart else vCurr
                val straightEnd = if (rEnd > 0f) vNext - uDir * rEnd else vNext

                val straightDx = straightEnd.x - straightStart.x
                val straightDy = straightEnd.y - straightStart.y
                val straightLen = sqrt(straightDx * straightDx + straightDy * straightDy)

                // Add start of straight segment
                if (samples.isEmpty()) {
                    val angle = calculateAngleDegrees(uDir)
                    samples.add(PathPoint(straightStart, uDir, angle, 0f, false))
                }

                // Sample straight segment
                if (straightLen > 0.01f) {
                    val steps = max(1, (straightLen / stepSize).toInt())
                    for (s in 1..steps) {
                        val t = s.toFloat() / steps
                        val pos = Offset(
                            straightStart.x + straightDx * t,
                            straightStart.y + straightDy * t
                        )
                        val angle = calculateAngleDegrees(uDir)
                        val d = straightLen * (1.0f / steps)
                        currentCumDist += d
                        samples.add(PathPoint(pos, uDir, angle, currentCumDist, false))
                    }
                }

                // If there's a corner at vNext, sample the rounded Bezier corner fillet
                if (hasCornerAtEnd && vAfter != null && rEnd > 0f) {
                    val afterDx = vAfter.x - vNext.x
                    val afterDy = vAfter.y - vNext.y
                    val afterLen = sqrt(afterDx * afterDx + afterDy * afterDy)
                    val uAfter = Offset(afterDx / afterLen, afterDy / afterLen)

                    val pCornerStart = straightEnd
                    val pCornerControl = vNext
                    val pCornerEnd = vNext + uAfter * rEnd

                    val cornerSteps = max(8, (rEnd * 2f / stepSize).toInt())
                    var lastCornerPos = pCornerStart

                    for (cs in 1..cornerSteps) {
                        val t = cs.toFloat() / cornerSteps
                        val oneMinusT = 1f - t
                        // Quadratic Bezier: (1-t)^2 * P0 + 2(1-t)t * P1 + t^2 * P2
                        val bx = oneMinusT * oneMinusT * pCornerStart.x + 2f * oneMinusT * t * pCornerControl.x + t * t * pCornerEnd.x
                        val by = oneMinusT * oneMinusT * pCornerStart.y + 2f * oneMinusT * t * pCornerControl.y + t * t * pCornerEnd.y
                        val pos = Offset(bx, by)

                        // Derivative B'(t) = 2(1-t)(P1-P0) + 2t(P2-P1)
                        val tdx = 2f * oneMinusT * (pCornerControl.x - pCornerStart.x) + 2f * t * (pCornerEnd.x - pCornerControl.x)
                        val tdy = 2f * oneMinusT * (pCornerControl.y - pCornerStart.y) + 2f * t * (pCornerEnd.y - pCornerControl.y)
                        val tLen = sqrt(tdx * tdx + tdy * tdy)
                        val cornerTangent = if (tLen > 0.001f) Offset(tdx / tLen, tdy / tLen) else uDir
                        val cornerAngle = calculateAngleDegrees(cornerTangent)

                        val delta = sqrt((pos.x - lastCornerPos.x) * (pos.x - lastCornerPos.x) + (pos.y - lastCornerPos.y) * (pos.y - lastCornerPos.y))
                        currentCumDist += delta
                        lastCornerPos = pos

                        samples.add(PathPoint(pos, cornerTangent, cornerAngle, currentCumDist, true))
                    }
                }
            }
        }

        /**
         * Converts direction vector to arrow rotation degrees where UP is 0°, RIGHT is 90°, DOWN is 180°, LEFT is 270°.
         */
        private fun calculateAngleDegrees(tangent: Offset): Float {
            val rad = atan2(tangent.y.toDouble(), tangent.x.toDouble()).toFloat()
            var deg = Math.toDegrees(rad.toDouble()).toFloat() + 90f
            while (deg < 0f) deg += 360f
            while (deg >= 360f) deg -= 360f
            return deg
        }

        /**
         * Evaluates position, tangent, and orientation at arc distance u.
         */
        fun sampleAt(distance: Float): PathSample {
            if (samples.isEmpty()) {
                return PathSample(Offset.Zero, Offset(0f, -1f), 0f, EscapePhase.COMPLETE)
            }
            if (distance <= 0f) {
                val first = samples.first()
                return PathSample(first.pos, first.tangent, first.angleDeg, EscapePhase.FOLLOWING_PATH)
            }
            if (distance >= totalLength) {
                val last = samples.last()
                return PathSample(last.pos, last.tangent, last.angleDeg, EscapePhase.COMPLETE)
            }

            // Binary search for closest segment
            var low = 0
            var high = samples.size - 1
            while (low < high - 1) {
                val mid = (low + high) / 2
                if (samples[mid].cumDist <= distance) {
                    low = mid
                } else {
                    high = mid
                }
            }

            val p0 = samples[low]
            val p1 = samples[high]
            val span = p1.cumDist - p0.cumDist
            val t = if (span > 0.0001f) ((distance - p0.cumDist) / span).coerceIn(0f, 1f) else 0f

            val interpPos = Offset(
                p0.pos.x + (p1.pos.x - p0.pos.x) * t,
                p0.pos.y + (p1.pos.y - p0.pos.y) * t
            )

            // Interpolate angle without 360° wrapping discontinuities
            var a0 = p0.angleDeg
            var a1 = p1.angleDeg
            val diff = a1 - a0
            if (diff > 180f) a1 -= 360f
            if (diff < -180f) a1 += 360f
            var interpAngle = a0 + (a1 - a0) * t
            while (interpAngle < 0f) interpAngle += 360f
            while (interpAngle >= 360f) interpAngle -= 360f

            val interpTangent = Offset(
                p0.tangent.x + (p1.tangent.x - p0.tangent.x) * t,
                p0.tangent.y + (p1.tangent.y - p0.tangent.y) * t
            )
            val tLen = sqrt(interpTangent.x * interpTangent.x + interpTangent.y * interpTangent.y)
            val normalizedTangent = if (tLen > 0.001f) Offset(interpTangent.x / tLen, interpTangent.y / tLen) else p0.tangent

            val phase = when {
                distance >= totalLength -> EscapePhase.COMPLETE
                p0.isCorner || p1.isCorner -> EscapePhase.TURNING
                interpPos.x < 0f || interpPos.x > boardBoundsSize || interpPos.y < 0f || interpPos.y > boardBoundsSize -> EscapePhase.FLYING_AWAY
                else -> EscapePhase.FOLLOWING_PATH
            }

            return PathSample(interpPos, normalizedTangent, interpAngle, phase)
        }

        /**
         * Builds a continuous Compose Path representing the arrow's body between uTail and uTip.
         */
        fun buildBodyPath(uTail: Float, uTip: Float): Path {
            val path = Path()
            val startDist = max(0f, uTail)
            val endDist = min(totalLength, uTip)

            if (endDist <= startDist) {
                val sample = sampleAt(startDist)
                path.moveTo(sample.position.x, sample.position.y)
                path.lineTo(sample.position.x, sample.position.y)
                return path
            }

            // Find start and end sample indices
            val startSample = sampleAt(startDist)
            path.moveTo(startSample.position.x, startSample.position.y)

            // Intermediate samples
            var low = 0
            var high = samples.size - 1
            while (low < high - 1) {
                val mid = (low + high) / 2
                if (samples[mid].cumDist <= startDist) low = mid else high = mid
            }

            var currIdx = high
            while (currIdx < samples.size && samples[currIdx].cumDist < endDist) {
                val pt = samples[currIdx]
                path.lineTo(pt.pos.x, pt.pos.y)
                currIdx++
            }

            val endSample = sampleAt(endDist)
            path.lineTo(endSample.position.x, endSample.position.y)

            return path
        }
    }

    /**
     * Calculates distance traveled along path from normalized progress [0..1]
     * with linear route traversal and polished fly-away acceleration.
     */
    fun calculateTravelDistance(progress: Float, totalPathLength: Float): Float {
        val p = progress.coerceIn(0f, 1f)
        // Subtly accelerated curve for satisfying off-screen fly away
        val eased = (p + 0.35f * p * p * p) / 1.35f
        return eased * totalPathLength
    }
}
