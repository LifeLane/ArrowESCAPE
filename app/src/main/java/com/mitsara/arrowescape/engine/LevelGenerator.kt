package com.mitsara.arrowescape.engine

import com.mitsara.arrowescape.model.Arrow
import com.mitsara.arrowescape.model.Difficulty
import com.mitsara.arrowescape.model.Direction
import com.mitsara.arrowescape.model.GridPoint
import com.mitsara.arrowescape.model.PuzzleLevel
import kotlin.random.Random

object LevelGenerator {

    /**
     * Generates a deterministic puzzle level for any level number (1..500+).
     */
    fun getLevel(levelNumber: Int): PuzzleLevel {
        val seed = levelNumber * 10007L + 8831L
        val random = Random(seed)

        val (gridSize, arrowCount, difficulty, multiCellProb) = when {
            levelNumber <= 10 -> LevelConfig(4, 4 + (levelNumber / 3), Difficulty.EASY, 0.0f)
            levelNumber <= 25 -> LevelConfig(4, 5 + (levelNumber - 10) / 4, Difficulty.EASY, 0.1f)
            levelNumber <= 50 -> LevelConfig(5, 7 + (levelNumber - 25) / 5, Difficulty.EASY, 0.15f)
            levelNumber <= 100 -> LevelConfig(5, 9 + (levelNumber - 50) / 8, Difficulty.NORMAL, 0.20f)
            levelNumber <= 150 -> LevelConfig(6, 12 + (levelNumber - 100) / 10, Difficulty.NORMAL, 0.25f)
            levelNumber <= 225 -> LevelConfig(6, 15 + (levelNumber - 150) / 10, Difficulty.HARD, 0.30f)
            levelNumber <= 300 -> LevelConfig(7, 18 + (levelNumber - 225) / 12, Difficulty.HARD, 0.35f)
            levelNumber <= 400 -> LevelConfig(7, 22 + (levelNumber - 300) / 12, Difficulty.EXPERT, 0.40f)
            else -> LevelConfig(8, 25 + minOf((levelNumber - 400) / 10, 8), Difficulty.EXPERT, 0.45f)
        }

        var level: PuzzleLevel? = null
        var attempts = 0

        while (level == null && attempts < 40) {
            attempts++
            val generatedArrows = generateGuaranteedReverseConstruction(
                gridSize = gridSize,
                targetCount = arrowCount,
                multiCellProb = multiCellProb,
                random = Random(seed + attempts * 997L)
            )

            val candidate = PuzzleLevel(
                id = levelNumber,
                title = "Level $levelNumber",
                difficulty = difficulty,
                gridWidth = gridSize,
                gridHeight = gridSize,
                arrows = generatedArrows,
                startingLives = 3,
                maxHints = 3
            )

            if (PuzzleSolver.isSolvable(candidate)) {
                level = candidate
            }
        }

        return level ?: createFallbackLevel(levelNumber, gridSize, difficulty)
    }

    private data class LevelConfig(
        val gridSize: Int,
        val arrowCount: Int,
        val difficulty: Difficulty,
        val multiCellProb: Float
    )

    /**
     * Mathematical Reverse Construction:
     * Places arrows sequentially such that each newly placed arrow A_new has a clear exit ray
     * relative to all existing arrows {A_1 ... A_new-1}.
     * This guarantees that A_new can escape first, unblocking older arrows, forming a 100% solvable puzzle.
     */
    private fun generateGuaranteedReverseConstruction(
        gridSize: Int,
        targetCount: Int,
        multiCellProb: Float,
        random: Random
    ): List<Arrow> {
        val arrows = mutableListOf<Arrow>()
        val occupied = HashSet<GridPoint>()

        var currentId = 1
        val directions = Direction.entries.toTypedArray()
        val maxAttemptsPerArrow = 100

        while (arrows.size < targetCount) {
            var placed = false
            var bestCandidate: Arrow? = null
            var maxBlockingScore = -1

            for (tryCount in 0 until maxAttemptsPerArrow) {
                val dir = directions[random.nextInt(directions.size)]
                val length = if (random.nextFloat() < multiCellProb && gridSize >= 5) 2 else 1
                val startX = random.nextInt(gridSize)
                val startY = random.nextInt(gridSize)

                val cand = Arrow(
                    id = currentId,
                    startX = startX,
                    startY = startY,
                    length = length,
                    direction = dir
                )

                val bodyCells = cand.getOccupiedCells()

                // Rule 1: All body cells must be inside grid
                val inBounds = bodyCells.all { it.x in 0 until gridSize && it.y in 0 until gridSize }
                if (!inBounds) continue

                // Rule 2: Body cells must not overlap any already placed arrow bodies
                val overlaps = bodyCells.any { occupied.contains(it) }
                if (overlaps) continue

                // Rule 3: Exit ray of cand must NOT hit any body cell of existing arrows
                val exitRay = cand.getExitRay(gridSize, gridSize)
                val exitRayBlockedByExisting = exitRay.any { occupied.contains(it) }
                if (exitRayBlockedByExisting) continue

                // Calculate how many existing arrows' exit rays this candidate blocks (creates fun dependencies!)
                var blockingScore = 0
                for (existing in arrows) {
                    val ray = existing.getExitRay(gridSize, gridSize)
                    if (bodyCells.any { ray.contains(it) }) {
                        blockingScore++
                    }
                }

                if (blockingScore > maxBlockingScore) {
                    maxBlockingScore = blockingScore
                    bestCandidate = cand
                    if (blockingScore > 0) break // Prioritize interlocking arrows
                }
            }

            if (bestCandidate != null) {
                arrows.add(bestCandidate)
                occupied.addAll(bestCandidate.getOccupiedCells())
                currentId++
                placed = true
            }

            // If space is too tight to place more arrows without violating exit ray rules, break early
            if (!placed) break
        }

        return arrows
    }

    private fun createFallbackLevel(
        levelNumber: Int,
        gridSize: Int,
        difficulty: Difficulty
    ): PuzzleLevel {
        val arrows = mutableListOf<Arrow>()
        var id = 1

        // Simple outward edge ring guaranteed solvable layout for grid of size `gridSize`
        for (i in 0 until gridSize) {
            arrows.add(Arrow(id++, startX = i, startY = 0, length = 1, direction = Direction.UP))
            arrows.add(Arrow(id++, startX = i, startY = gridSize - 1, length = 1, direction = Direction.DOWN))
        }

        return PuzzleLevel(
            id = levelNumber,
            title = "Level $levelNumber",
            difficulty = difficulty,
            gridWidth = gridSize,
            gridHeight = gridSize,
            arrows = arrows,
            startingLives = 3,
            maxHints = 3
        )
    }
}


