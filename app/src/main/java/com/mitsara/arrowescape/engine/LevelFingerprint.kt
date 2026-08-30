package com.mitsara.arrowescape.engine

import com.mitsara.arrowescape.model.Arrow
import com.mitsara.arrowescape.model.Direction
import com.mitsara.arrowescape.model.GridPoint
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Structural Fingerprinting to detect and reject puzzles that are structurally too similar
 * or repetitive compared to recently generated levels.
 */
data class LevelFingerprint(
    val gridWidth: Int,
    val gridHeight: Int,
    val arrowCount: Int,
    val obstacleCount: Int,
    val upCount: Int,
    val downCount: Int,
    val leftCount: Int,
    val rightCount: Int,
    val avgArrowLength: Float,
    val obstacleCenterOfMassX: Float,
    val obstacleCenterOfMassY: Float,
    val dependencyDepth: Int
) {
    companion object {
        fun from(
            gridWidth: Int,
            gridHeight: Int,
            arrows: List<Arrow>,
            obstacles: Set<GridPoint>,
            dependencyDepth: Int
        ): LevelFingerprint {
            var up = 0
            var down = 0
            var left = 0
            var right = 0
            var totalLen = 0

            for (a in arrows) {
                when (a.getTipDirection()) {
                    Direction.UP -> up++
                    Direction.DOWN -> down++
                    Direction.LEFT -> left++
                    Direction.RIGHT -> right++
                }
                totalLen += a.getOccupiedCells().size
            }

            val avgLen = if (arrows.isNotEmpty()) totalLen.toFloat() / arrows.size else 1f
            val comX = if (obstacles.isNotEmpty()) obstacles.map { it.x }.average().toFloat() else gridWidth / 2f
            val comY = if (obstacles.isNotEmpty()) obstacles.map { it.y }.average().toFloat() else gridHeight / 2f

            return LevelFingerprint(
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                arrowCount = arrows.size,
                obstacleCount = obstacles.size,
                upCount = up,
                downCount = down,
                leftCount = left,
                rightCount = right,
                avgArrowLength = avgLen,
                obstacleCenterOfMassX = comX,
                obstacleCenterOfMassY = comY,
                dependencyDepth = dependencyDepth
            )
        }
    }

    /**
     * Computes similarity score in [0.0..1.0] with another fingerprint.
     */
    fun similarityWith(other: LevelFingerprint): Float {
        if (gridWidth != other.gridWidth || gridHeight != other.gridHeight) return 0.0f

        val arrowDiff = abs(arrowCount - other.arrowCount).toFloat() / maxOf(1, arrowCount)
        val obsDiff = abs(obstacleCount - other.obstacleCount).toFloat() / maxOf(1, obstacleCount)
        val dirDiff = (abs(upCount - other.upCount) + abs(downCount - other.downCount) +
                abs(leftCount - other.leftCount) + abs(rightCount - other.rightCount)).toFloat() / maxOf(1, arrowCount * 2)
        val depDiff = abs(dependencyDepth - other.dependencyDepth).toFloat() / maxOf(1, dependencyDepth)
        val comDist = sqrt(
            (obstacleCenterOfMassX - other.obstacleCenterOfMassX) * (obstacleCenterOfMassX - other.obstacleCenterOfMassX) +
                    (obstacleCenterOfMassY - other.obstacleCenterOfMassY) * (obstacleCenterOfMassY - other.obstacleCenterOfMassY)
        ) / maxOf(1f, gridWidth.toFloat())

        val totalDiff = (arrowDiff * 0.25f + obsDiff * 0.15f + dirDiff * 0.3f + depDiff * 0.15f + comDist * 0.15f)
        return (1f - totalDiff).coerceIn(0f, 1f)
    }
}

/**
 * Thread-safe LRU history cache to track recent level structural fingerprints.
 */
class FingerprintHistoryCache(private val maxHistory: Int = 10) {
    private val history = mutableListOf<LevelFingerprint>()

    @Synchronized
    fun isTooSimilar(candidate: LevelFingerprint, similarityThreshold: Float = 0.88f): Boolean {
        for (fp in history) {
            if (candidate.similarityWith(fp) > similarityThreshold) {
                return true
            }
        }
        return false
    }

    @Synchronized
    fun record(fingerprint: LevelFingerprint) {
        if (history.size >= maxHistory) {
            history.removeAt(0)
        }
        history.add(fingerprint)
    }

    @Synchronized
    fun clear() {
        history.clear()
    }
}
