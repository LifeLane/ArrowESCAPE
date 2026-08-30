package com.mitsara.arrowescape.engine

import com.mitsara.arrowescape.model.Arrow
import com.mitsara.arrowescape.model.Difficulty
import com.mitsara.arrowescape.model.Direction
import com.mitsara.arrowescape.model.GridPoint
import kotlin.math.abs
import kotlin.math.ln

/**
 * Multi-factor Difficulty Evaluation and Quality Gate System.
 * Evaluates spatial complexity, directional diversity, dependency depth,
 * bottleneck density, and branching to ensure every level meets HARD / HARDER / HARDCORE standards.
 */
object LevelDifficultyScorer {

    data class DifficultyReport(
        val difficulty: Difficulty,
        val compositeScore: Int,
        val dependencyDepth: Int,
        val arrowCount: Int,
        val arrowDensity: Float,
        val obstacleCount: Int,
        val initialFreeCount: Int,
        val initialFreeRatio: Float,
        val directionalEntropy: Float,
        val avgArrowLength: Float,
        val passesQualityGate: Boolean,
        val rejectionReason: String? = null
    )

    /**
     * Calculates the multi-factor difficulty score and evaluates against quality criteria.
     */
    fun evaluateLevel(
        gridSize: Int,
        arrows: List<Arrow>,
        obstacles: Set<GridPoint>,
        validCells: Set<GridPoint>?,
        targetDifficulty: Difficulty,
        levelNumber: Int,
        analysis: PuzzleSolver.SolveAnalysis
    ): DifficultyReport {
        if (!analysis.isSolvable) {
            return DifficultyReport(
                difficulty = targetDifficulty,
                compositeScore = 0,
                dependencyDepth = 0,
                arrowCount = arrows.size,
                arrowDensity = 0f,
                obstacleCount = obstacles.size,
                initialFreeCount = 0,
                initialFreeRatio = 0f,
                directionalEntropy = 0f,
                avgArrowLength = 0f,
                passesQualityGate = false,
                rejectionReason = "Unsolvable puzzle"
            )
        }

        val totalUsableCells = validCells?.size ?: (gridSize * gridSize)
        var totalOccupiedBodyCells = 0
        val dirCounts = mutableMapOf<Direction, Int>()

        for (a in arrows) {
            val body = a.getOccupiedCells()
            totalOccupiedBodyCells += body.size
            val tipDir = a.getTipDirection()
            dirCounts[tipDir] = (dirCounts[tipDir] ?: 0) + 1
        }

        val arrowDensity = if (totalUsableCells > 0) totalOccupiedBodyCells.toFloat() / totalUsableCells else 0f
        val avgArrowLength = if (arrows.isNotEmpty()) totalOccupiedBodyCells.toFloat() / arrows.size else 1f

        // Directional entropy calculation: sum(-p * ln(p))
        var entropy = 0f
        val totalDir = arrows.size.toFloat()
        if (totalDir > 0) {
            for ((_, count) in dirCounts) {
                if (count > 0) {
                    val p = count / totalDir
                    entropy += (-p * ln(p).toFloat())
                }
            }
        }
        val maxEntropy = ln(4f).toFloat() // ~1.386
        val normalizedEntropy = (entropy / maxEntropy).coerceIn(0f, 1f)

        val initialFreeCount = analysis.initialFreeCount
        val initialFreeRatio = if (arrows.isNotEmpty()) initialFreeCount.toFloat() / arrows.size else 1f
        val dependencyDepth = analysis.dependencyDepth

        // Multi-Factor Difficulty Formula
        // 1. Grid Scale Points: 8 -> 20pts, 15 -> 65pts
        val gridPoints = (gridSize - 7) * 7.5f

        // 2. Arrow Count Points: 15 arrows -> 25pts, 50 arrows -> 80pts
        val arrowPoints = arrows.size * 1.6f

        // 3. Dependency Depth Points: 3 waves -> 15pts, 12 waves -> 75pts
        val depPoints = dependencyDepth * 6.5f

        // 4. Density & Obstacle Points
        val densityPoints = arrowDensity * 40f
        val obstaclePoints = obstacles.size * 1.5f

        // 5. Constrained Start Bonus (Fewer initial free arrows = higher challenge)
        val constraintBonus = ((1f - initialFreeRatio) * 30f).coerceAtLeast(0f)

        // 6. Directional Diversity Bonus
        val diversityBonus = normalizedEntropy * 20f

        val rawScore = (gridPoints + arrowPoints + depPoints + densityPoints + obstaclePoints + constraintBonus + diversityBonus).toInt()

        // Minimum Standards per Difficulty Tier
        val (minDepth, minGrid, minArrows, minScore) = when (targetDifficulty) {
            Difficulty.HARD -> Tuple4(3, 8, 12, 100)
            Difficulty.HARDER -> Tuple4(5, 10, 20, 160)
            Difficulty.HARDCORE -> Tuple4(8, 12, 30, 230)
        }

        var pass = true
        var reason: String? = null

        if (gridSize < minGrid) {
            pass = false
            reason = "Grid size $gridSize below minimum $minGrid for $targetDifficulty"
        } else if (arrows.size < minArrows) {
            pass = false
            reason = "Arrow count ${arrows.size} below minimum $minArrows for $targetDifficulty"
        } else if (dependencyDepth < minDepth) {
            pass = false
            reason = "Dependency depth $dependencyDepth below minimum $minDepth for $targetDifficulty"
        } else if (rawScore < minScore) {
            pass = false
            reason = "Composite difficulty score $rawScore below minimum $minScore"
        } else if (initialFreeRatio > 0.55f && arrows.size >= 15) {
            pass = false
            reason = "Too many obvious first moves (${initialFreeCount}/${arrows.size})"
        }

        return DifficultyReport(
            difficulty = targetDifficulty,
            compositeScore = rawScore,
            dependencyDepth = dependencyDepth,
            arrowCount = arrows.size,
            arrowDensity = arrowDensity,
            obstacleCount = obstacles.size,
            initialFreeCount = initialFreeCount,
            initialFreeRatio = initialFreeRatio,
            directionalEntropy = normalizedEntropy,
            avgArrowLength = avgArrowLength,
            passesQualityGate = pass,
            rejectionReason = reason
        )
    }

    private data class Tuple4(val minDepth: Int, val minGrid: Int, val minArrows: Int, val minScore: Int)
}
