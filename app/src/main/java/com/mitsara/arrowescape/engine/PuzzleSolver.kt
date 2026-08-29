package com.mitsara.arrowescape.engine

import com.mitsara.arrowescape.model.Arrow
import com.mitsara.arrowescape.model.Difficulty
import com.mitsara.arrowescape.model.Direction
import com.mitsara.arrowescape.model.GridPoint
import com.mitsara.arrowescape.model.PuzzleLevel
import java.util.ArrayDeque
import kotlin.random.Random

object PuzzleSolver {

    /**
     * Checks if a given arrow can escape without colliding with any remaining active arrows or obstacles.
     */
    fun isArrowUnobstructed(
        arrow: Arrow,
        activeArrows: List<Arrow>,
        gridWidth: Int,
        gridHeight: Int,
        obstacles: Set<GridPoint> = emptySet()
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
        obstacles: Set<GridPoint> = emptySet()
    ): List<Arrow> {
        return activeArrows.filter { isArrowUnobstructed(it, activeArrows, gridWidth, gridHeight, obstacles) }
    }

    /**
     * Solves the puzzle using monotonic greedy elimination.
     */
    fun solvePuzzle(
        initialArrows: List<Arrow>,
        gridWidth: Int,
        gridHeight: Int,
        obstacles: Set<GridPoint> = emptySet()
    ): List<Int> {
        if (initialArrows.isEmpty()) return emptyList()

        val remaining = initialArrows.toMutableList()
        val path = mutableListOf<Int>()

        while (remaining.isNotEmpty()) {
            val free = getUnobstructedArrows(remaining, gridWidth, gridHeight, obstacles).firstOrNull()
                ?: return emptyList()

            remaining.remove(free)
            path.add(free.id)
        }

        return path
    }

    /**
     * Validates whether a puzzle level is 100% solvable.
     */
    fun isSolvable(level: PuzzleLevel): Boolean {
        return solvePuzzle(level.arrows, level.gridWidth, level.gridHeight, level.obstacles).isNotEmpty()
    }

    /**
     * Returns a valid next arrow ID hint that leads to puzzle completion.
     */
    fun getHintArrowId(
        activeArrows: List<Arrow>,
        gridWidth: Int,
        gridHeight: Int,
        obstacles: Set<GridPoint> = emptySet()
    ): Int? {
        val freeArrows = getUnobstructedArrows(activeArrows, gridWidth, gridHeight, obstacles)
        if (freeArrows.isEmpty()) return null

        for (free in freeArrows) {
            val remaining = activeArrows.filter { it.id != free.id }
            if (remaining.isEmpty() || solvePuzzle(remaining, gridWidth, gridHeight, obstacles).isNotEmpty()) {
                return free.id
            }
        }
        return freeArrows.firstOrNull()?.id
    }
}
