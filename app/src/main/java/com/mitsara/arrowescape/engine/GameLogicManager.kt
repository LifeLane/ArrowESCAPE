package com.mitsara.arrowescape.engine

import com.mitsara.arrowescape.model.Arrow
import com.mitsara.arrowescape.model.GridPoint
import com.mitsara.arrowescape.model.PuzzleLevel

/**
 * GameLogicManager centralizes BFS solver logic for move validation, state management
 * for heart/life counts, and level completion conditions based on board geometry and obstacles.
 */
class GameLogicManager(val level: PuzzleLevel) {

    private var activeArrowsList: MutableList<Arrow> = level.arrows.toMutableList()
    private var escapedIds: MutableSet<Int> = mutableSetOf()
    private var livesCount: Int = level.startingLives
    private var moveHistoryList: MutableList<MoveSnapshot> = mutableListOf()

    data class MoveSnapshot(
        val activeArrows: List<Arrow>,
        val escapedArrowIds: Set<Int>,
        val moveCount: Int,
        val lives: Int
    )

    val currentActiveArrows: List<Arrow> get() = activeArrowsList
    val escapedArrowIds: Set<Int> get() = escapedIds
    val remainingLives: Int get() = livesCount
    val isCompleted: Boolean get() = activeArrowsList.isEmpty()
    val isFailed: Boolean get() = livesCount <= 0

    /**
     * Validates and executes an arrow tap attempt based on current board geometry and obstacles.
     */
    fun processArrowTap(arrowId: Int): TapResult {
        if (isCompleted || isFailed) return TapResult.Ignored

        val arrow = activeArrowsList.find { it.id == arrowId } ?: return TapResult.NotFound
        val unobstructed = isArrowUnobstructed(arrow)

        if (unobstructed) {
            // Save state snapshot for undo
            moveHistoryList.add(
                MoveSnapshot(
                    activeArrows = activeArrowsList.toList(),
                    escapedArrowIds = escapedIds.toSet(),
                    moveCount = escapedIds.size,
                    lives = livesCount
                )
            )

            activeArrowsList.removeAll { it.id == arrowId }
            escapedIds.add(arrowId)

            return if (activeArrowsList.isEmpty()) {
                TapResult.LevelCompleted
            } else {
                TapResult.Success(arrowId)
            }
        } else {
            // Mistake: lose a heart
            livesCount = maxOf(0, livesCount - 1)
            return if (livesCount <= 0) {
                TapResult.LevelFailed
            } else {
                TapResult.Mistake(arrowId, livesCount)
            }
        }
    }

    /**
     * Checks if an arrow can escape without colliding with other active arrows, obstacles, or out-of-bounds (validCells geometry).
     */
    fun isArrowUnobstructed(arrow: Arrow): Boolean {
        val occupiedCells = HashSet<GridPoint>()
        for (other in activeArrowsList) {
            if (other.id != arrow.id) {
                occupiedCells.addAll(other.getOccupiedCells())
            }
        }
        occupiedCells.addAll(level.obstacles)

        val ray = arrow.getExitRay(level.gridWidth, level.gridHeight)
        for (point in ray) {
            val vc = level.validCells
            if (vc != null && vc.isNotEmpty() && !vc.contains(point)) {
                return false
            }
            if (occupiedCells.contains(point)) {
                return false
            }
        }
        return true
    }

    /**
     * Returns the next optimal hint arrow ID using BFS solver / monotonic greedy search.
     */
    fun getNextHintArrowId(): Int? {
        val freeArrows = activeArrowsList.filter { isArrowUnobstructed(it) }
        if (freeArrows.isEmpty()) return null

        for (free in freeArrows) {
            val remaining = activeArrowsList.filter { it.id != free.id }
            if (remaining.isEmpty() || PuzzleSolver.solvePuzzle(remaining, level.gridWidth, level.gridHeight, level.obstacles).isNotEmpty()) {
                return free.id
            }
        }
        return freeArrows.firstOrNull()?.id
    }

    fun undo(): Boolean {
        if (moveHistoryList.isEmpty()) return false
        val last = moveHistoryList.removeAt(moveHistoryList.size - 1)
        activeArrowsList = last.activeArrows.toMutableList()
        escapedIds = last.escapedArrowIds.toMutableSet()
        livesCount = last.lives
        return true
    }

    sealed class TapResult {
        object Ignored : TapResult()
        object NotFound : TapResult()
        data class Success(val arrowId: Int) : TapResult()
        data class Mistake(val arrowId: Int, val remainingLives: Int) : TapResult()
        object LevelCompleted : TapResult()
        object LevelFailed : TapResult()
    }
}
