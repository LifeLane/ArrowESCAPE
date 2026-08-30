package com.mitsara.arrowescape.engine

import com.mitsara.arrowescape.model.Arrow
import com.mitsara.arrowescape.model.GridPoint
import com.mitsara.arrowescape.model.PuzzleLevel
import java.util.ArrayDeque

object PuzzleSolver {

    data class SolveAnalysis(
        val isSolvable: Boolean,
        val solutionSequence: List<Int>,
        val dependencyDepth: Int,
        val initialFreeCount: Int,
        val avgBranchingFactor: Float,
        val waveSizes: List<Int>
    )

    /**
     * Checks if a given arrow can escape without colliding with any remaining active arrows or obstacles.
     */
    fun isArrowUnobstructed(
        arrow: Arrow,
        activeArrows: List<Arrow>,
        gridWidth: Int,
        gridHeight: Int,
        obstacles: Set<GridPoint> = emptySet(),
        validCells: Set<GridPoint>? = null
    ): Boolean {
        val occupiedCells = HashSet<GridPoint>()
        for (other in activeArrows) {
            if (other.id != arrow.id) {
                occupiedCells.addAll(other.getOccupiedCells())
            }
        }
        occupiedCells.addAll(obstacles)

        val ray = arrow.getExitRay(gridWidth, gridHeight)
        for (point in ray) {
            if (validCells != null && validCells.isNotEmpty() && !validCells.contains(point)) {
                return false
            }
            if (occupiedCells.contains(point)) {
                return false
            }
        }
        return true
    }

    /**
     * Returns all currently unobstructed arrows on the board.
     */
    fun getUnobstructedArrows(
        activeArrows: List<Arrow>,
        gridWidth: Int,
        gridHeight: Int,
        obstacles: Set<GridPoint> = emptySet(),
        validCells: Set<GridPoint>? = null
    ): List<Arrow> {
        return activeArrows.filter { isArrowUnobstructed(it, activeArrows, gridWidth, gridHeight, obstacles, validCells) }
    }

    /**
     * Comprehensive topological wave analysis to compute exact solution, dependency depth, and branching factor.
     */
    fun analyzePuzzle(
        initialArrows: List<Arrow>,
        gridWidth: Int,
        gridHeight: Int,
        obstacles: Set<GridPoint> = emptySet(),
        validCells: Set<GridPoint>? = null
    ): SolveAnalysis {
        if (initialArrows.isEmpty()) {
            return SolveAnalysis(true, emptyList(), 0, 0, 0f, emptyList())
        }

        val remaining = initialArrows.toMutableList()
        val solution = mutableListOf<Int>()
        val waveSizes = mutableListOf<Int>()
        var totalBranches = 0
        var branchDecisions = 0

        var initialFree = 0
        var isFirstWave = true

        while (remaining.isNotEmpty()) {
            val freeArrows = getUnobstructedArrows(remaining, gridWidth, gridHeight, obstacles, validCells)
            if (freeArrows.isEmpty()) {
                return SolveAnalysis(
                    isSolvable = false,
                    solutionSequence = emptyList(),
                    dependencyDepth = waveSizes.size,
                    initialFreeCount = initialFree,
                    avgBranchingFactor = if (branchDecisions > 0) totalBranches.toFloat() / branchDecisions else 1f,
                    waveSizes = waveSizes
                )
            }

            if (isFirstWave) {
                initialFree = freeArrows.size
                isFirstWave = false
            }

            waveSizes.add(freeArrows.size)
            totalBranches += freeArrows.size
            branchDecisions++

            // For deterministic sequencing, remove one by one (or strategic best)
            // Order: prioritize arrows that unblock the most other arrows
            val chosen = freeArrows.maxByOrNull { free ->
                val nextRemaining = remaining.filter { it.id != free.id }
                getUnobstructedArrows(nextRemaining, gridWidth, gridHeight, obstacles, validCells).size
            } ?: freeArrows.first()

            remaining.remove(chosen)
            solution.add(chosen.id)
        }

        val avgBranching = if (branchDecisions > 0) totalBranches.toFloat() / branchDecisions else 1f
        return SolveAnalysis(
            isSolvable = true,
            solutionSequence = solution,
            dependencyDepth = waveSizes.size,
            initialFreeCount = initialFree,
            avgBranchingFactor = avgBranching,
            waveSizes = waveSizes
        )
    }

    /**
     * Solves the puzzle using monotonic greedy elimination.
     */
    fun solvePuzzle(
        initialArrows: List<Arrow>,
        gridWidth: Int,
        gridHeight: Int,
        obstacles: Set<GridPoint> = emptySet(),
        validCells: Set<GridPoint>? = null
    ): List<Int> {
        return analyzePuzzle(initialArrows, gridWidth, gridHeight, obstacles, validCells).solutionSequence
    }

    /**
     * Validates whether a puzzle level is 100% solvable.
     */
    fun isSolvable(level: PuzzleLevel): Boolean {
        return analyzePuzzle(level.arrows, level.gridWidth, level.gridHeight, level.obstacles, level.validCells).isSolvable
    }

    /**
     * Intelligent Hint System:
     * Analyzes all currently unobstructed arrows and selects the move that
     * unlocks the deepest downstream dependency chain and leads directly to victory.
     */
    fun getHintArrowId(
        activeArrows: List<Arrow>,
        gridWidth: Int,
        gridHeight: Int,
        obstacles: Set<GridPoint> = emptySet(),
        validCells: Set<GridPoint>? = null
    ): Int? {
        val freeArrows = getUnobstructedArrows(activeArrows, gridWidth, gridHeight, obstacles, validCells)
        if (freeArrows.isEmpty()) return null
        if (freeArrows.size == 1) return freeArrows.first().id

        // Score each free arrow by downstream dependency depth & unlock potential
        var bestArrow: Arrow? = null
        var maxDownstreamDepth = -1

        for (candidate in freeArrows) {
            val remaining = activeArrows.filter { it.id != candidate.id }
            val analysis = analyzePuzzle(remaining, gridWidth, gridHeight, obstacles, validCells)
            if (analysis.isSolvable) {
                // The candidate that keeps the puzzle solvable and has the highest downstream unlock potential
                val score = analysis.dependencyDepth * 10 + (activeArrows.size - remaining.size)
                if (score > maxDownstreamDepth) {
                    maxDownstreamDepth = score
                    bestArrow = candidate
                }
            }
        }

        return bestArrow?.id ?: freeArrows.first().id
    }
}
