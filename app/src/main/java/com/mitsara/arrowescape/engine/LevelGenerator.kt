package com.mitsara.arrowescape.engine

import com.mitsara.arrowescape.model.Arrow
import com.mitsara.arrowescape.model.Difficulty
import com.mitsara.arrowescape.model.Direction
import com.mitsara.arrowescape.model.GridPoint
import com.mitsara.arrowescape.model.PuzzleLevel
import kotlin.random.Random

object LevelGenerator {

    enum class BoardShape {
        SQUARE, CROSS, DIAMOND, DONUT, PLUS
    }

    /**
     * Generates a deterministic puzzle level for any level number (1..500+).
     */
    fun getLevel(levelNumber: Int): PuzzleLevel {
        val seed = levelNumber * 10007L + 8831L
        val random = Random(seed)

        val isMilestone = levelNumber % 10 == 0
        val baseConfig = when {
            levelNumber <= 10 -> LevelConfig(6, 8 + levelNumber / 2, Difficulty.EASY, 0.3f, BoardShape.SQUARE)
            levelNumber <= 25 -> LevelConfig(7, 12 + (levelNumber - 10) / 2, Difficulty.EASY, 0.4f, BoardShape.CROSS)
            levelNumber <= 50 -> LevelConfig(8, 16 + (levelNumber - 25) / 3, Difficulty.NORMAL, 0.5f, BoardShape.DIAMOND)
            levelNumber <= 100 -> LevelConfig(9, 22 + (levelNumber - 50) / 4, Difficulty.NORMAL, 0.6f, BoardShape.values()[random.nextInt(5)])
            levelNumber <= 200 -> LevelConfig(10, 28 + (levelNumber - 100) / 5, Difficulty.HARD, 0.65f, BoardShape.values()[random.nextInt(5)])
            levelNumber <= 350 -> LevelConfig(11, 35 + (levelNumber - 200) / 6, Difficulty.HARD, 0.7f, BoardShape.values()[random.nextInt(5)])
            else -> LevelConfig(12, 42 + minOf((levelNumber - 350) / 8, 12), Difficulty.EXPERT, 0.75f, BoardShape.values()[random.nextInt(5)])
        }

        val milestoneShapes = listOf(BoardShape.CROSS, BoardShape.DIAMOND, BoardShape.DONUT, BoardShape.PLUS)
        val shape = if (isMilestone) milestoneShapes[((levelNumber / 10) - 1) % milestoneShapes.size] else baseConfig.shape
        val gridSize = if (isMilestone) maxOf(baseConfig.gridSize, 8) else baseConfig.gridSize
        val arrowCount = if (isMilestone) baseConfig.arrowCount + 5 else baseConfig.arrowCount
        val difficulty = if (isMilestone) Difficulty.EXPERT else baseConfig.difficulty
        val multiCellProb = baseConfig.multiCellProb

        var level: PuzzleLevel? = null
        var attempts = 0

        val mask = generateMask(gridSize, shape)

        val validCells = mutableSetOf<GridPoint>()
        for (x in 0 until gridSize) {
            for (y in 0 until gridSize) {
                if (mask[x][y]) {
                    validCells.add(GridPoint(x, y))
                }
            }
        }

        val obstacles = mutableSetOf<GridPoint>()
        val baseObstacleCount = when {
            levelNumber <= 10 -> 2
            levelNumber <= 25 -> 4
            levelNumber <= 50 -> 6
            levelNumber <= 100 -> 8
            levelNumber <= 200 -> 10
            levelNumber <= 350 -> 12
            else -> 14
        }
        val obstacleCount = baseObstacleCount + if (isMilestone) 6 else 0
        
        // Generate geometric maze/barrier formations on higher levels
        val center = gridSize / 2
        if (levelNumber > 10) {
            // Add central geometric barrier or cross/diamond walls
            obstacles.add(GridPoint(center, center))
            if (gridSize >= 7) {
                obstacles.add(GridPoint(center - 1, center))
                obstacles.add(GridPoint(center + 1, center))
                obstacles.add(GridPoint(center, center - 1))
                obstacles.add(GridPoint(center, center + 1))
            }
        }

        val candidateObstacleCells = validCells.filter { it.x in 1 until gridSize - 1 && it.y in 1 until gridSize - 1 && !obstacles.contains(it) }.shuffled(random)
        for (cell in candidateObstacleCells) {
            if (obstacles.size < obstacleCount) {
                obstacles.add(cell)
            }
        }

        val levelTitle = if (isMilestone) "Milestone Lv $levelNumber" else "Level $levelNumber"

        while (level == null && attempts < 40) {
            attempts++
            val generatedArrows = generateGuaranteedReverseConstruction(
                gridSize = gridSize,
                targetCount = arrowCount,
                multiCellProb = multiCellProb,
                mask = mask,
                obstacles = obstacles,
                random = Random(seed + attempts * 997L)
            )

            val candidate = PuzzleLevel(
                id = levelNumber,
                title = levelTitle,
                difficulty = difficulty,
                gridWidth = gridSize,
                gridHeight = gridSize,
                arrows = generatedArrows,
                startingLives = 3,
                maxHints = 3,
                validCells = validCells,
                obstacles = obstacles
            )

            if (PuzzleSolver.isSolvable(candidate)) {
                level = candidate
            }
        }

        return level ?: createFallbackLevel(levelNumber, gridSize, difficulty, obstacles)
    }

    private data class LevelConfig(
        val gridSize: Int,
        val arrowCount: Int,
        val difficulty: Difficulty,
        val multiCellProb: Float,
        val shape: BoardShape
    )

    private fun generateMask(size: Int, shape: BoardShape): Array<BooleanArray> {
        val mask = Array(size) { BooleanArray(size) { true } }
        val center = size / 2
        when (shape) {
            BoardShape.SQUARE -> {}
            BoardShape.CROSS -> {
                val cut = size / 3
                for (x in 0 until size) {
                    for (y in 0 until size) {
                        if ((x < cut || x >= size - cut) && (y < cut || y >= size - cut)) {
                            mask[x][y] = false
                        }
                    }
                }
            }
            BoardShape.DIAMOND -> {
                for (x in 0 until size) {
                    for (y in 0 until size) {
                        if (Math.abs(x - center) + Math.abs(y - center) > center + 1) {
                            mask[x][y] = false
                        }
                    }
                }
            }
            BoardShape.DONUT -> {
                val holeRadius = size / 4
                for (x in 0 until size) {
                    for (y in 0 until size) {
                        if (Math.abs(x - center) <= holeRadius && Math.abs(y - center) <= holeRadius) {
                            mask[x][y] = false
                        }
                    }
                }
            }
            BoardShape.PLUS -> {
                val thick = size / 5
                for (x in 0 until size) {
                    for (y in 0 until size) {
                        if (Math.abs(x - center) > thick && Math.abs(y - center) > thick) {
                            mask[x][y] = false
                        }
                    }
                }
            }
        }
        return mask
    }

    private val paletteHex = listOf(
        "#3B82F6", "#F97316", "#10B981", "#8B5CF6", "#EC4899",
        "#F59E0B", "#06B6D4", "#6366F1", "#14B8A6", "#EF4444"
    )

    private fun generateGuaranteedReverseConstruction(
        gridSize: Int,
        targetCount: Int,
        multiCellProb: Float,
        mask: Array<BooleanArray>,
        obstacles: Set<GridPoint>,
        random: Random
    ): List<Arrow> {
        val arrows = mutableListOf<Arrow>()
        val occupied = HashSet<GridPoint>()
        occupied.addAll(obstacles)

        var currentId = 1
        val directions = Direction.entries.toTypedArray()
        val maxAttemptsPerArrow = 100

        while (arrows.size < targetCount) {
            var placed = false
            var bestCandidate: Arrow? = null
            var maxBlockingScore = -1

            for (tryCount in 0 until maxAttemptsPerArrow) {
                val dir = directions[random.nextInt(directions.size)]
                val length = if (random.nextFloat() < multiCellProb && gridSize >= 5) {
                    random.nextInt(2, minOf(gridSize - 1, 5))
                } else 1
                val startX = random.nextInt(gridSize)
                val startY = random.nextInt(gridSize)

                val isBent = random.nextFloat() < 0.35f && multiCellProb > 0.2f && gridSize >= 6
                val pathPoints = if (isBent) {
                    val turnLen1 = random.nextInt(2, 4)
                    val turnLen2 = random.nextInt(2, 4)
                    val pts = mutableListOf<GridPoint>()
                    var cx = startX
                    var cy = startY
                    pts.add(GridPoint(cx, cy))
                    for (i in 1 until turnLen1) {
                        cx += dir.dx
                        cy += dir.dy
                        pts.add(GridPoint(cx, cy))
                    }
                    val perpDir = if (dir == Direction.UP || dir == Direction.DOWN) {
                        if (random.nextBoolean()) Direction.LEFT else Direction.RIGHT
                    } else {
                        if (random.nextBoolean()) Direction.UP else Direction.DOWN
                    }
                    for (i in 1 until turnLen2) {
                        cx += perpDir.dx
                        cy += perpDir.dy
                        pts.add(GridPoint(cx, cy))
                    }
                    pts
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

                // Rule 1: All body cells must be inside grid, inside mask, and not on obstacles
                val inBounds = bodyCells.all { it.x in 0 until gridSize && it.y in 0 until gridSize && mask[it.x][it.y] && !obstacles.contains(it) }
                if (!inBounds) continue

                // Rule 2: Body cells must not overlap any already placed arrow bodies or obstacles
                val overlaps = bodyCells.any { occupied.contains(it) }
                if (overlaps) continue

                // Rule 3: Exit ray of cand must NOT hit any body cell of existing arrows or obstacles
                val exitRay = cand.getExitRay(gridSize, gridSize)
                val exitRayBlockedByExisting = exitRay.any { occupied.contains(it) }
                if (exitRayBlockedByExisting) continue

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
                    if (blockingScore > 0) break
                }
            }

            if (bestCandidate != null) {
                arrows.add(bestCandidate)
                occupied.addAll(bestCandidate.getOccupiedCells())
                currentId++
                placed = true
            }

            if (!placed) break
        }

        return arrows
    }

    private fun createFallbackLevel(
        levelNumber: Int,
        gridSize: Int,
        difficulty: Difficulty,
        obstacles: Set<GridPoint>
    ): PuzzleLevel {
        val isMilestone = levelNumber % 10 == 0
        val levelTitle = if (isMilestone) "Milestone Lv $levelNumber" else "Level $levelNumber"
        val arrows = mutableListOf<Arrow>()
        var id = 1

        for (i in 0 until gridSize) {
            val p1 = GridPoint(i, 0)
            val p2 = GridPoint(i, gridSize - 1)
            if (!obstacles.contains(p1)) {
                val arrowId = id++
                arrows.add(Arrow(arrowId, startX = i, startY = 0, length = 1, direction = Direction.UP, customColorHex = paletteHex[(arrowId - 1) % paletteHex.size]))
            }
            if (!obstacles.contains(p2)) {
                val arrowId = id++
                arrows.add(Arrow(arrowId, startX = i, startY = gridSize - 1, length = 1, direction = Direction.DOWN, customColorHex = paletteHex[(arrowId - 1) % paletteHex.size]))
            }
        }

        return PuzzleLevel(
            id = levelNumber,
            title = levelTitle,
            difficulty = difficulty,
            gridWidth = gridSize,
            gridHeight = gridSize,
            arrows = arrows,
            startingLives = 3,
            maxHints = 3,
            obstacles = obstacles
        )
    }
}


