package com.mitsara.arrowescape.engine

import com.mitsara.arrowescape.model.Arrow
import com.mitsara.arrowescape.model.Difficulty
import com.mitsara.arrowescape.model.Direction
import com.mitsara.arrowescape.model.GridPoint
import kotlin.math.abs
import kotlin.math.ln

/**
 * Multi-Factor Difficulty Scorer and Quality Gate Engine.
 *
 * Evaluates:
 * 1. Grid matrix scale
 * 2. Total arrow count & spatial packing density
 * 3. Topological dependency depth & wave count
 * 4. Branching factor & strategic decoys
 * 5. 4-Way directional entropy (balanced UP/DOWN/LEFT/RIGHT distribution)
 * 6. Long-range blocker relationships
 * 7. Obstacle formation complexity
 *
 * Enforces strict HARD, HARDER, and HARDCORE standards.
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
        val longRangeBlockerCount: Int,
        val avgBranchingFactor: Float,
        val passesQualityGate: Boolean,
        val rejectionReason: String? = null
    )

    /**
     * Evaluates a generated puzzle configuration against multi-factor difficulty criteria.
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
        if (!analysis.isSolvable || analysis.solutionSequence.size != arrows.size) {
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
                longRangeBlockerCount = 0,
                avgBranchingFactor = 0f,
                passesQualityGate = false,
                rejectionReason = "Unsolvable puzzle or incomplete solution sequence"
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

        // Directional entropy: sum(-p * ln(p)) / ln(4)
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
        val maxEntropy = ln(4f).toFloat()
        val normalizedEntropy = (entropy / maxEntropy).coerceIn(0f, 1f)

        // Count long-range blockers (where an arrow blocks another arrow > 2 cells away)
        var longRangeBlockers = 0
        for (i in arrows.indices) {
            val ray = arrows[i].getExitRay(gridSize, gridSize)
            for (j in arrows.indices) {
                if (i != j) {
                    val otherBody = arrows[j].getOccupiedCells()
                    for (pt in otherBody) {
                        val rayIdx = ray.indexOf(pt)
                        if (rayIdx >= 2) {
                            longRangeBlockers++
                        }
                    }
                }
            }
        }

        val initialFreeCount = analysis.initialFreeCount
        val initialFreeRatio = if (arrows.isNotEmpty()) initialFreeCount.toFloat() / arrows.size else 1f
        val dependencyDepth = analysis.dependencyDepth
        val branching = analysis.avgBranchingFactor

        // Composite Multi-Factor Score Calculation
        val gridPoints = (gridSize - 7) * 8f
        val arrowPoints = arrows.size * 1.5f
        val depPoints = dependencyDepth * 7f
        val densityPoints = arrowDensity * 50f
        val obstaclePoints = obstacles.size * 1.8f
        val diversityBonus = normalizedEntropy * 25f
        val longRangeBonus = minOf(30f, longRangeBlockers * 1.2f)
        val branchingPoints = minOf(20f, branching * 4f)

        val rawScore = (gridPoints + arrowPoints + depPoints + densityPoints + obstaclePoints +
                diversityBonus + longRangeBonus + branchingPoints).toInt()

        // Minimum standards per difficulty tier
        val (minDepth, minGrid, minArrows, minScore) = when (targetDifficulty) {
            Difficulty.HARD -> Tuple4(3, 8, 16, 95)
            Difficulty.HARDER -> Tuple4(5, 10, 28, 150)
            Difficulty.HARDCORE -> Tuple4(8, 12, 45, 220)
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
            reason = "Difficulty score $rawScore below minimum $minScore for $targetDifficulty"
        } else if (initialFreeRatio > 0.60f && arrows.size >= 16) {
            pass = false
            reason = "Too many obvious initial moves ($initialFreeCount / ${arrows.size})"
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
            longRangeBlockerCount = longRangeBlockers,
            avgBranchingFactor = branching,
            passesQualityGate = pass,
            rejectionReason = reason
        )
    }

    private data class Tuple4(val minDepth: Int, val minGrid: Int, val minArrows: Int, val minScore: Int)
}
