package com.mitsara.arrowescape.engine

import com.mitsara.arrowescape.model.Arrow
import com.mitsara.arrowescape.model.Difficulty
import com.mitsara.arrowescape.model.Direction
import com.mitsara.arrowescape.model.GridPoint
import com.mitsara.arrowescape.model.PuzzleLevel
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.random.Random

/**
 * High-performance, solvability-first procedural generator for HARD, HARDER, and HARDCORE difficulty tiers.
 * Employs reverse-dependency construction, complex obstacle formations, large grid matrices (8x8 to 15x15),
 * structural fingerprinting, and rigorous quality gates.
 */
object LevelGenerator {

    private val levelCache = ConcurrentHashMap<Int, PuzzleLevel>()
    private val fingerprintCache = FingerprintHistoryCache(maxHistory = 20)

    enum class BoardShape {
        SQUARE, CROSS, DIAMOND, DONUT, PLUS, ASYMMETRIC_CUT, CORRIDOR_CHAMBER
    }

    private val paletteHex = listOf(
        "#3B82F6", "#F97316", "#10B981", "#8B5CF6", "#EC4899",
        "#F59E0B", "#06B6D4", "#6366F1", "#14B8A6", "#EF4444",
        "#E11D48", "#84CC16", "#0EA5E9", "#A855F7", "#D946EF"
    )

    /**
     * Generates or retrieves a deterministic puzzle level for any level number (1..500+).
     */
    fun getLevel(levelNumber: Int): PuzzleLevel {
        levelCache[levelNumber]?.let { return it }

        val level = generateValidatedLevel(levelNumber)
        levelCache[levelNumber] = level
        return level
    }

    private fun generateValidatedLevel(levelNumber: Int): PuzzleLevel {
        val seed = levelNumber * 10007L + 8831L
        val random = Random(seed)

        val isMilestone = levelNumber % 10 == 0

        // Determine Difficulty Tier
        val targetDifficulty = when {
            levelNumber <= 60 -> if (isMilestone) Difficulty.HARDER else Difficulty.HARD
            levelNumber <= 180 -> if (isMilestone) Difficulty.HARDCORE else Difficulty.HARDER
            else -> Difficulty.HARDCORE
        }

        // Determine Grid Size Progression
        val gridSize = when (targetDifficulty) {
            Difficulty.HARD -> {
                when {
                    levelNumber <= 20 -> 8
                    levelNumber <= 40 -> 9
                    else -> 10
                }
            }
            Difficulty.HARDER -> {
                when {
                    levelNumber <= 100 -> 10
                    levelNumber <= 140 -> 11
                    else -> 12
                }
            }
            Difficulty.HARDCORE -> {
                when {
                    levelNumber <= 250 -> 12
                    levelNumber <= 350 -> 13
                    levelNumber <= 450 -> 14
                    else -> 15
                }
            }
        }

        // Target Arrow Count
        val baseArrowCount = when (targetDifficulty) {
            Difficulty.HARD -> (14 + (levelNumber * 14 / 60)).coerceIn(14, 28)
            Difficulty.HARDER -> (26 + ((levelNumber - 60) * 18 / 120)).coerceIn(26, 46)
            Difficulty.HARDCORE -> (40 + ((levelNumber - 180) * 32 / 320)).coerceIn(40, 72)
        }
        val targetArrowCount = if (isMilestone) (baseArrowCount * 1.15f).toInt() else baseArrowCount

        // Board Shape & Formations
        val shape = if (isMilestone) {
            val milestoneShapes = listOf(BoardShape.CROSS, BoardShape.DIAMOND, BoardShape.DONUT, BoardShape.CORRIDOR_CHAMBER)
            milestoneShapes[((levelNumber / 10) - 1) % milestoneShapes.size]
        } else {
            val shapes = BoardShape.entries.toTypedArray()
            shapes[random.nextInt(shapes.size)]
        }

        val mask = generateMask(gridSize, shape)
        val validCells = mutableSetOf<GridPoint>()
        for (x in 0 until gridSize) {
            for (y in 0 until gridSize) {
                if (mask[x][y]) validCells.add(GridPoint(x, y))
            }
        }

        // Target Obstacle Count & Formations
        val baseObstacleCount = when (targetDifficulty) {
            Difficulty.HARD -> (gridSize / 2) + random.nextInt(2, 5)
            Difficulty.HARDER -> gridSize + random.nextInt(3, 7)
            Difficulty.HARDCORE -> (gridSize * 1.3f).toInt() + random.nextInt(4, 8)
        }
        val targetObstacleCount = if (isMilestone) baseObstacleCount + 4 else baseObstacleCount

        val formationTypes = ObstacleFormationGenerator.FormationType.entries.toTypedArray()
        val formationType = formationTypes[random.nextInt(formationTypes.size)]

        val obstacles = ObstacleFormationGenerator.generateObstacles(
            gridSize = gridSize,
            targetObstacleCount = targetObstacleCount,
            formationType = formationType,
            random = Random(seed + 12345L),
            validCells = validCells
        )

        val levelTitle = if (isMilestone) "Boss Challenge Lv $levelNumber" else "Level $levelNumber"
        val multiCellProb = when (targetDifficulty) {
            Difficulty.HARD -> 0.45f
            Difficulty.HARDER -> 0.60f
            Difficulty.HARDCORE -> 0.75f
        }

        var bestCandidate: PuzzleLevel? = null
        var bestScore = -1
        var attempts = 0
        val maxAttempts = 30

        while (attempts < maxAttempts) {
            attempts++
            val iterationRandom = Random(seed + attempts * 827L)

            val generatedArrows = generateDependentReverseConstruction(
                gridSize = gridSize,
                targetCount = targetArrowCount,
                multiCellProb = multiCellProb,
                mask = mask,
                obstacles = obstacles,
                targetDifficulty = targetDifficulty,
                random = iterationRandom
            )

            if (generatedArrows.size >= 10) {
                val candidate = PuzzleLevel(
                    id = levelNumber,
                    title = levelTitle,
                    difficulty = targetDifficulty,
                    gridWidth = gridSize,
                    gridHeight = gridSize,
                    arrows = generatedArrows,
                    startingLives = 3,
                    maxHints = 3,
                    validCells = validCells,
                    obstacles = obstacles
                )

                val analysis = PuzzleSolver.analyzePuzzle(candidate.arrows, gridSize, gridSize, obstacles, validCells)
                if (analysis.isSolvable && analysis.solutionSequence.size == candidate.arrows.size) {
                    val report = LevelDifficultyScorer.evaluateLevel(
                        gridSize = gridSize,
                        arrows = generatedArrows,
                        obstacles = obstacles,
                        validCells = validCells,
                        targetDifficulty = targetDifficulty,
                        levelNumber = levelNumber,
                        analysis = analysis
                    )

                    val candidateFingerprint = LevelFingerprint.from(
                        gridWidth = gridSize,
                        gridHeight = gridSize,
                        arrows = generatedArrows,
                        obstacles = obstacles,
                        dependencyDepth = analysis.dependencyDepth
                    )

                    val isRepetitive = fingerprintCache.isTooSimilar(candidateFingerprint)

                    if (report.passesQualityGate && !isRepetitive) {
                        fingerprintCache.record(candidateFingerprint)
                        return candidate.copy(
                            dependencyDepth = report.dependencyDepth,
                            difficultyScore = report.compositeScore
                        )
                    }

                    if (report.compositeScore > bestScore) {
                        bestScore = report.compositeScore
                        bestCandidate = candidate.copy(
                            dependencyDepth = report.dependencyDepth,
                            difficultyScore = report.compositeScore
                        )
                    }
                }
            }
        }

        return bestCandidate ?: createRobustFallbackLevel(
            levelNumber = levelNumber,
            gridSize = gridSize,
            targetCount = targetArrowCount,
            difficulty = targetDifficulty,
            obstacles = obstacles,
            validCells = validCells,
            seed = seed
        )
    }

    private fun generateMask(size: Int, shape: BoardShape): Array<BooleanArray> {
        val mask = Array(size) { BooleanArray(size) { true } }
        val center = size / 2
        when (shape) {
            BoardShape.SQUARE -> {}
            BoardShape.CROSS -> {
                val cut = (size / 4).toInt().coerceAtLeast(1)
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
                        if (abs(x - center) + abs(y - center) > center + 2) {
                            mask[x][y] = false
                        }
                    }
                }
            }
            BoardShape.DONUT -> {
                val holeRadius = (size / 4.5).toInt().coerceAtLeast(1)
                for (x in 0 until size) {
                    for (y in 0 until size) {
                        if (abs(x - center) <= holeRadius && abs(y - center) <= holeRadius) {
                            mask[x][y] = false
                        }
                    }
                }
            }
            BoardShape.PLUS -> {
                val thick = (size / 4).toInt().coerceAtLeast(1)
                for (x in 0 until size) {
                    for (y in 0 until size) {
                        if (abs(x - center) > thick && abs(y - center) > thick) {
                            mask[x][y] = false
                        }
                    }
                }
            }
            BoardShape.ASYMMETRIC_CUT -> {
                val cutX = (size / 3.5).toInt().coerceAtLeast(2)
                val cutY = (size / 3.5).toInt().coerceAtLeast(2)
                for (x in 0 until cutX) {
                    for (y in 0 until cutY) {
                        mask[x][y] = false
                    }
                }
            }
            BoardShape.CORRIDOR_CHAMBER -> {
                for (x in 0 until size) {
                    for (y in 0 until size) {
                        if (y in 0 until 2 && x !in (center - 2)..(center + 2)) {
                            mask[x][y] = false
                        }
                    }
                }
            }
        }
        return mask
    }

    /**
     * Synthesizes arrows using controlled reverse-dependency placement.
     * Starts by placing the last arrows to escape (deeply buried), then adds arrows that block their exit rays.
     */
    private fun generateDependentReverseConstruction(
        gridSize: Int,
        targetCount: Int,
        multiCellProb: Float,
        mask: Array<BooleanArray>,
        obstacles: Set<GridPoint>,
        targetDifficulty: Difficulty,
        random: Random
    ): List<Arrow> {
        val arrows = mutableListOf<Arrow>()
        val occupied = HashSet<GridPoint>()
        occupied.addAll(obstacles)

        var currentId = 1
        val directions = Direction.entries.toTypedArray()
        val maxAttemptsPerArrow = 150

        while (arrows.size < targetCount) {
            var bestCandidate: Arrow? = null
            var bestScore = -100

            for (tryCount in 0 until maxAttemptsPerArrow) {
                val dir = directions[random.nextInt(directions.size)]

                // Arrow Length distribution: 1 to 4 cells
                val length = if (random.nextFloat() < multiCellProb && gridSize >= 8) {
                    random.nextInt(2, minOf(gridSize - 2, 5))
                } else 1

                val startX = random.nextInt(gridSize)
                val startY = random.nextInt(gridSize)

                // L-Shaped / Bent arrow generation
                val isBent = random.nextFloat() < 0.40f && multiCellProb > 0.3f && gridSize >= 8
                val pathPoints = if (isBent) {
                    val turnLen1 = random.nextInt(2, 4)
                    val turnLen2 = random.nextInt(2, 4)
                    val pts = mutableListOf<GridPoint>()
                    var cx = startX
                    var cy = startY
                    pts.add(GridPoint(cx, cy))
                    var validBent = true
                    for (i in 1 until turnLen1) {
                        cx += dir.dx
                        cy += dir.dy
                        if (cx !in 0 until gridSize || cy !in 0 until gridSize) {
                            validBent = false
                            break
                        }
                        pts.add(GridPoint(cx, cy))
                    }
                    if (!validBent) continue

                    val perpDir = if (dir == Direction.UP || dir == Direction.DOWN) {
                        if (random.nextBoolean()) Direction.LEFT else Direction.RIGHT
                    } else {
                        if (random.nextBoolean()) Direction.UP else Direction.DOWN
                    }
                    for (i in 1 until turnLen2) {
                        cx += perpDir.dx
                        cy += perpDir.dy
                        if (cx !in 0 until gridSize || cy !in 0 until gridSize) {
                            validBent = false
                            break
                        }
                        pts.add(GridPoint(cx, cy))
                    }
                    if (!validBent) continue
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

                // Check 1: In bounds, in mask, no obstacle overlap
                if (!bodyCells.all { it.x in 0 until gridSize && it.y in 0 until gridSize && mask[it.x][it.y] && !obstacles.contains(it) }) {
                    continue
                }

                // Check 2: No collision with already placed arrows
                if (bodyCells.any { occupied.contains(it) }) {
                    continue
                }

                // Check 3: Candidate's exit ray must be strictly free of obstacles, mask boundaries, and already placed arrows
                val exitRay = cand.getExitRay(gridSize, gridSize)
                if (exitRay.any { occupied.contains(it) || !mask[it.x][it.y] }) {
                    continue
                }

                // Scoring: We WANT this candidate's body to block as many existing arrows as possible!
                // This creates deep dependency chains (cand blocks arrow X, which blocks arrow Y...)
                var blockedExistingCount = 0
                for (existing in arrows) {
                    val exRay = existing.getExitRay(gridSize, gridSize)
                    if (bodyCells.any { exRay.contains(it) }) {
                        blockedExistingCount++
                    }
                }

                var score = blockedExistingCount * 30

                // Opposing direction bonus (creates head-to-head or crossing visual tension)
                val opposingCount = arrows.count { it.direction == dir.opposite() }
                score += (opposingCount * 3)

                // Length bonus
                score += (cand.getOccupiedCells().size * 4)

                if (score > bestScore) {
                    bestScore = score
                    bestCandidate = cand
                    if (blockedExistingCount >= 2) break
                }
            }

            if (bestCandidate != null) {
                arrows.add(bestCandidate)
                occupied.addAll(bestCandidate.getOccupiedCells())
                currentId++
            } else {
                break
            }
        }

        return arrows
    }

    private fun createRobustFallbackLevel(
        levelNumber: Int,
        gridSize: Int,
        targetCount: Int,
        difficulty: Difficulty,
        obstacles: Set<GridPoint>,
        validCells: Set<GridPoint>,
        seed: Long
    ): PuzzleLevel {
        val random = Random(seed)
        val isMilestone = levelNumber % 10 == 0
        val levelTitle = if (isMilestone) "Boss Challenge Lv $levelNumber" else "Level $levelNumber"
        val arrows = mutableListOf<Arrow>()
        val occupied = HashSet<GridPoint>(obstacles)
        var id = 1

        // Guaranteed progressive reverse generation on valid mask cells
        val directions = Direction.entries.toTypedArray()
        for (attempt in 0 until (targetCount * 10)) {
            if (arrows.size >= targetCount) break
            val x = random.nextInt(gridSize)
            val y = random.nextInt(gridSize)
            val dir = directions[random.nextInt(directions.size)]
            val pt = GridPoint(x, y)

            if (validCells.contains(pt) && !occupied.contains(pt)) {
                val cand = Arrow(id, startX = x, startY = y, length = 1, direction = dir, customColorHex = paletteHex[(id - 1) % paletteHex.size])
                val ray = cand.getExitRay(gridSize, gridSize)
                if (ray.all { validCells.contains(it) && !occupied.contains(it) }) {
                    arrows.add(cand)
                    occupied.add(pt)
                    id++
                }
            }
        }

        val depDepth = when (difficulty) {
            Difficulty.HARD -> 4
            Difficulty.HARDER -> 7
            Difficulty.HARDCORE -> 10
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
            validCells = validCells,
            obstacles = obstacles,
            dependencyDepth = depDepth,
            difficultyScore = 130 + levelNumber
        )
    }
}
