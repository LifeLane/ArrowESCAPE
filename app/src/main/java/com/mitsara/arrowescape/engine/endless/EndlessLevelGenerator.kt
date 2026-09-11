package com.mitsara.arrowescape.engine.endless

import com.mitsara.arrowescape.model.Arrow
import com.mitsara.arrowescape.model.Difficulty
import com.mitsara.arrowescape.model.Direction
import com.mitsara.arrowescape.model.GridPoint
import kotlin.random.Random

object EndlessLevelGenerator {
    private val paletteHex = listOf(
        "#3B82F6", "#F97316", "#10B981", "#8B5CF6", "#EC4899",
        "#F59E0B", "#06B6D4", "#6366F1", "#14B8A6", "#EF4444",
        "#E11D48", "#84CC16", "#0EA5E9", "#A855F7", "#D946EF"
    )

    /**
     * Generates a new wave of arrows on top of an existing board.
     * existingArrows are treated as strict obstacles so the new arrows 
     * don't intersect them and can cleanly escape.
     */
    fun generateRestockWave(
        gridSize: Int,
        existingArrows: List<Arrow>,
        targetNewArrows: Int,
        startId: Int
    ): List<Arrow> {
        val random = Random(System.currentTimeMillis())
        val newArrows = mutableListOf<Arrow>()
        val occupied = HashSet<GridPoint>()

        // Treat all existing arrows as solid obstacles
        for (arr in existingArrows) {
            occupied.addAll(arr.getOccupiedCells())
        }

        var currentId = startId
        val directions = Direction.entries.toTypedArray()
        val maxAttemptsPerArrow = 100

        val dirUsage = mutableMapOf(
            Direction.UP to 0,
            Direction.DOWN to 0,
            Direction.LEFT to 0,
            Direction.RIGHT to 0
        )

        while (newArrows.size < targetNewArrows) {
            var bestCandidate: Arrow? = null
            var bestScore = -100

            val minUsage = dirUsage.values.minOrNull() ?: 0
            val preferredDirs = directions.filter { (dirUsage[it] ?: 0) <= minUsage + 2 }.ifEmpty { directions.toList() }

            for (tryCount in 0 until maxAttemptsPerArrow) {
                val dir = preferredDirs[random.nextInt(preferredDirs.size)]
                
                val isBent = random.nextFloat() < 0.35f
                val length = if (random.nextFloat() < 0.5f) random.nextInt(2, 5) else 1
                
                val startX = random.nextInt(gridSize)
                val startY = random.nextInt(gridSize)

                val pathPoints = if (isBent && length >= 2) {
                    val d2 = when (dir) {
                        Direction.UP, Direction.DOWN -> if (random.nextBoolean()) Direction.LEFT else Direction.RIGHT
                        Direction.LEFT, Direction.RIGHT -> if (random.nextBoolean()) Direction.UP else Direction.DOWN
                    }
                    val pts = mutableListOf<GridPoint>()
                    var cx = startX
                    var cy = startY
                    pts.add(GridPoint(cx, cy))
                    var valid = true
                    for (i in 1 until minOf(length, 3)) {
                        cx += dir.dx
                        cy += dir.dy
                        if (cx !in 0 until gridSize || cy !in 0 until gridSize || occupied.contains(GridPoint(cx, cy))) {
                            valid = false
                            break
                        }
                        pts.add(GridPoint(cx, cy))
                    }
                    if (valid) {
                        for (i in 1 until minOf(length, 3)) {
                            cx += d2.dx
                            cy += d2.dy
                            if (cx !in 0 until gridSize || cy !in 0 until gridSize || occupied.contains(GridPoint(cx, cy))) {
                                valid = false
                                break
                            }
                            pts.add(GridPoint(cx, cy))
                        }
                    }
                    if (valid && pts.size >= 2) pts else null
                } else null

                val cand = Arrow(
                    id = currentId,
                    startX = startX,
                    startY = startY,
                    length = pathPoints?.size ?: length,
                    direction = dir,
                    pathPoints = pathPoints,
                    customColorHex = paletteHex[(currentId - 1) % paletteHex.size]
                )

                val bodyCells = cand.getOccupiedCells()

                // Check 1: In bounds and no overlap with ANY existing/new arrows
                if (!bodyCells.all { it.x in 0 until gridSize && it.y in 0 until gridSize && !occupied.contains(it) }) {
                    continue
                }

                // Check 2: Exit ray must be free to the board perimeter 
                // This guarantees the new arrow can escape eventually!
                val exitRay = cand.getExitRay(gridSize, gridSize)
                if (exitRay.any { occupied.contains(it) }) {
                    continue
                }

                // Dependency Scoring: Reward candidates whose body blocks previously placed NEW arrows
                var blockedNewCount = 0
                for (existing in newArrows) {
                    val exRay = existing.getExitRay(gridSize, gridSize)
                    for (cell in bodyCells) {
                        if (exRay.contains(cell)) {
                            blockedNewCount++
                        }
                    }
                }

                var score = blockedNewCount * 20
                score += (bodyCells.size * 5) // Reward longer arrows

                if (score > bestScore) {
                    bestScore = score
                    bestCandidate = cand
                    if (blockedNewCount >= 1) break
                }
            }

            if (bestCandidate != null) {
                newArrows.add(bestCandidate)
                occupied.addAll(bestCandidate.getOccupiedCells())
                dirUsage[bestCandidate.getTipDirection()] = (dirUsage[bestCandidate.getTipDirection()] ?: 0) + 1
                currentId++
            } else {
                // Board is likely too full to add more arrows cleanly
                break
            }
        }

        return newArrows
    }
}
