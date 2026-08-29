package com.mitsara.arrowescape.model

import androidx.compose.runtime.Immutable

enum class Direction(val dx: Int, val dy: Int, val rotationDegrees: Float) {
    UP(0, -1, 0f),
    RIGHT(1, 0, 90f),
    DOWN(0, 1, 180f),
    LEFT(-1, 0, 270f);

    fun opposite(): Direction = when (this) {
        UP -> DOWN
        DOWN -> UP
        LEFT -> RIGHT
        RIGHT -> LEFT
    }
}

@Immutable
data class GridPoint(val x: Int, val y: Int)

enum class Difficulty(val displayName: String) {
    EASY("Easy"),
    NORMAL("Normal"),
    HARD("Hard"),
    EXPERT("Expert")
}

@Immutable
data class Arrow(
    val id: Int,
    val startX: Int,
    val startY: Int,
    val length: Int = 1,
    val direction: Direction,
    val pathPoints: List<GridPoint>? = null,
    val customColorHex: String? = null
) {
    /**
     * Returns all grid points occupied by this arrow's body.
     */
    fun getOccupiedCells(): List<GridPoint> {
        if (!pathPoints.isNullOrEmpty()) {
            return pathPoints
        }
        val points = mutableListOf<GridPoint>()
        for (i in 0 until length) {
            points.add(GridPoint(startX + direction.dx * i, startY + direction.dy * i))
        }
        return points
    }

    /**
     * Returns the tip direction (direction of the last step).
     */
    fun getTipDirection(): Direction {
        if (!pathPoints.isNullOrEmpty() && pathPoints.size >= 2) {
            val pPrev = pathPoints[pathPoints.size - 2]
            val pLast = pathPoints.last()
            val dx = pLast.x - pPrev.x
            val dy = pLast.y - pPrev.y
            return Direction.entries.find { it.dx == dx && it.dy == dy } ?: direction
        }
        return direction
    }

    /**
     * Returns the tip cell of the arrow where it points outward.
     */
    fun getTipCell(): GridPoint {
        if (!pathPoints.isNullOrEmpty()) {
            return pathPoints.last()
        }
        return GridPoint(
            startX + direction.dx * (length - 1),
            startY + direction.dy * (length - 1)
        )
    }

    /**
     * Returns the ray of grid cells beyond the arrow's tip extending to the edge of the board.
     */
    fun getExitRay(gridWidth: Int, gridHeight: Int): List<GridPoint> {
        val ray = mutableListOf<GridPoint>()
        val tip = getTipCell()
        val tipDir = getTipDirection()
        var currentX = tip.x + tipDir.dx
        var currentY = tip.y + tipDir.dy

        while (currentX in 0 until gridWidth && currentY in 0 until gridHeight) {
            ray.add(GridPoint(currentX, currentY))
            currentX += tipDir.dx
            currentY += tipDir.dy
        }
        return ray
    }
}

@Immutable
data class PuzzleLevel(
    val id: Int,
    val title: String,
    val difficulty: Difficulty,
    val gridWidth: Int,
    val gridHeight: Int,
    val arrows: List<Arrow>,
    val startingLives: Int = 3,
    val maxHints: Int = 3,
    val validCells: Set<GridPoint>? = null,
    val obstacles: Set<GridPoint> = emptySet()
)

@Immutable
data class MoveHistoryEntry(
    val activeArrows: List<Arrow>,
    val escapedArrowIds: Set<Int>,
    val moveCount: Int
)

@Immutable
data class GamePlayState(
    val level: PuzzleLevel,
    val activeArrows: List<Arrow>,
    val escapedArrowIds: Set<Int> = emptySet(),
    val moveHistory: List<MoveHistoryEntry> = emptyList(),
    val animatingArrowId: Int? = null,
    val animatingDirection: Direction? = null,
    val hintArrowId: Int? = null,
    val remainingLives: Int = 3,
    val hintsAvailable: Int = 3,
    val moveCount: Int = 0,
    val flowCount: Int = 0,
    val isCompleted: Boolean = false,
    val isFailed: Boolean = false,
    val isMistakeShake: Boolean = false,
    val inspectedArrowId: Int? = null,
    val score: Int = 0,
    val elapsedSeconds: Int = 0,
    val hintCooldown: Int = 0,
    val comboMultiplier: Int = 1,
    val lastEscapeTimestamp: Long = 0L,
    val activeComboMessage: String? = null
) {
    val flowState: Int get() = (flowCount / 3).coerceIn(0, 3)
    val canUndo: Boolean get() = moveHistory.isNotEmpty() && !isCompleted && !isFailed
}
