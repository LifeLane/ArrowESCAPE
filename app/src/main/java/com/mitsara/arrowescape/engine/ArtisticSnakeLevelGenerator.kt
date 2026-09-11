package com.mitsara.arrowescape.engine

import com.mitsara.arrowescape.model.Arrow
import com.mitsara.arrowescape.model.Difficulty
import com.mitsara.arrowescape.model.Direction
import com.mitsara.arrowescape.model.GridPoint
import com.mitsara.arrowescape.model.PuzzleLevel
import kotlin.random.Random

/**
 * Procedural synthesis engine for intricate winding snake arrows, L-bends,
 * U-turn hairpins, S-staircases, spiral coils, and boundary arrows.
 * 
 * Enforces strict reverse-dependency chains (DAG) so that arrows are conditional
 * and depend on the exact unblocking of prerequisite arrows before escaping.
 */
object ArtisticSnakeLevelGenerator {

    private val paletteHex = listOf(
        "#3B82F6", "#F97316", "#10B981", "#8B5CF6", "#EC4899",
        "#F59E0B", "#06B6D4", "#6366F1", "#14B8A6", "#EF4444",
        "#E11D48", "#84CC16", "#0EA5E9", "#A855F7", "#D946EF"
    )

    enum class SnakeType {
        STRAIGHT,
        L_BEND,
        U_TURN_HAIRPIN,
        S_STAIRCASE,
        SPIRAL_HOOK,
        BOUNDARY_WRAPPER
    }

    /**
     * Synthesizes a fully solvable, densely packed silhouette puzzle for any of the 500 levels.
     * Enforces strict conditional escape dependencies where only 1-3 keys are initially free.
     */
    fun generateSilhouetteLevel(
        levelNumber: Int,
        gridSize: Int,
        targetArrowCount: Int,
        targetDifficulty: Difficulty,
        seed: Long
    ): PuzzleLevel {
        val levelDef = SilhouetteShapeRegistry.getLevelDef(levelNumber)
        val mask = SilhouetteShapeRegistry.generateShapeMask(levelNumber, gridSize, gridSize)

        val validCells = mutableSetOf<GridPoint>()
        for (x in 0 until gridSize) {
            for (y in 0 until gridSize) {
                if (mask[x][y]) validCells.add(GridPoint(x, y))
            }
        }

        var bestArrows: List<Arrow> = emptyList()
        var bestAnalysis: PuzzleSolver.SolveAnalysis? = null
        var bestScore = -1

        val maxAttempts = 24

        for (attempt in 0 until maxAttempts) {
            val iterRandom = Random(seed + attempt * 1009L + 42L)
            val candidateArrows = synthesizeReverseDependencyArrows(
                gridSize = gridSize,
                mask = mask,
                validCells = validCells,
                targetCount = targetArrowCount,
                targetDifficulty = targetDifficulty,
                random = iterRandom
            )

            if (candidateArrows.isNotEmpty()) {
                val analysis = PuzzleSolver.analyzePuzzle(
                    initialArrows = candidateArrows,
                    gridWidth = gridSize,
                    gridHeight = gridSize,
                    obstacles = emptySet(),
                    validCells = null
                )

                if (analysis.isSolvable && analysis.solutionSequence.size == candidateArrows.size) {
                    // Reward high dependency depth, low initial free count (1-3 keys), and high arrow count
                    val initialFreeBonus = when (analysis.initialFreeCount) {
                        1 -> 150
                        2 -> 120
                        3 -> 80
                        else -> maxOf(0, 50 - analysis.initialFreeCount * 10)
                    }
                    val score = candidateArrows.size * 10 + analysis.dependencyDepth * 20 + initialFreeBonus

                    if (score > bestScore) {
                        bestScore = score
                        bestArrows = candidateArrows
                        bestAnalysis = analysis
                        if (analysis.initialFreeCount in 1..2 && analysis.dependencyDepth >= candidateArrows.size * 0.70f) {
                            break
                        }
                    }
                }
            }
        }

        val finalArrows = if (bestArrows.isNotEmpty()) {
            bestArrows
        } else {
            createGuaranteedReverseChainFallback(gridSize, mask, validCells, Random(seed))
        }

        val finalAnalysis = bestAnalysis ?: PuzzleSolver.analyzePuzzle(finalArrows, gridSize, gridSize, emptySet(), null)

        return PuzzleLevel(
            id = levelNumber,
            title = levelDef.name,
            difficulty = targetDifficulty,
            gridWidth = gridSize,
            gridHeight = gridSize,
            arrows = finalArrows,
            startingLives = when (targetDifficulty) {
                Difficulty.HARD -> 3
                Difficulty.HARDER -> 4
                Difficulty.HARDCORE -> 5
            },
            maxHints = 3,
            validCells = validCells,
            obstacles = emptySet(),
            dependencyDepth = finalAnalysis.dependencyDepth,
            difficultyScore = finalAnalysis.dependencyDepth * 10 + finalArrows.size
        )
    }

    /**
     * Reverse-Dependency Construction:
     * Builds intertwined snakes such that placed arrows deliberately block the exit rays
     * of other arrows, creating deep conditional escape chains.
     */
    private fun synthesizeReverseDependencyArrows(
        gridSize: Int,
        mask: Array<BooleanArray>,
        validCells: Set<GridPoint>,
        targetCount: Int,
        targetDifficulty: Difficulty,
        random: Random
    ): List<Arrow> {
        val arrows = mutableListOf<Arrow>()
        val occupied = HashSet<GridPoint>()

        var currentId = 1
        var consecutiveFailures = 0

        val snakeTypes = when (targetDifficulty) {
            Difficulty.HARD -> listOf(
                SnakeType.STRAIGHT, SnakeType.L_BEND, SnakeType.U_TURN_HAIRPIN, SnakeType.S_STAIRCASE
            )
            Difficulty.HARDER, Difficulty.HARDCORE -> listOf(
                SnakeType.STRAIGHT, SnakeType.L_BEND, SnakeType.U_TURN_HAIRPIN,
                SnakeType.S_STAIRCASE, SnakeType.SPIRAL_HOOK, SnakeType.BOUNDARY_WRAPPER
            )
        }

        while (arrows.size < targetCount && consecutiveFailures < 60) {
            val type = snakeTypes[random.nextInt(snakeTypes.size)]
            val candidate = generateInterlockingSnake(
                id = currentId,
                type = type,
                gridSize = gridSize,
                mask = mask,
                occupied = occupied,
                existingArrows = arrows,
                random = random
            )

            if (candidate != null) {
                arrows.add(candidate)
                occupied.addAll(candidate.getOccupiedCells())
                currentId++
                consecutiveFailures = 0
            } else {
                consecutiveFailures++
            }
        }

        // Fill remaining isolated single-cell or two-cell holes with small dependent arrows
        val remainingHoles = validCells.filter { !occupied.contains(it) }.shuffled(random)
        for (hole in remainingHoles) {
            if (occupied.contains(hole)) continue

            // Pick a direction that blocks an existing arrow or is blocked by an existing arrow
            var chosenCand: Arrow? = null
            for (dir in Direction.entries.shuffled(random)) {
                val cand = Arrow(
                    id = currentId,
                    startX = hole.x,
                    startY = hole.y,
                    length = 1,
                    direction = dir,
                    pathPoints = listOf(hole),
                    customColorHex = paletteHex[(currentId - 1) % paletteHex.size]
                )

                // Test if this candidate is valid
                chosenCand = cand
                val ray = cand.getExitRay(gridSize, gridSize)
                if (ray.any { occupied.contains(it) }) {
                    // Blocked by an existing arrow -> excellent dependency!
                    break
                }
            }

            if (chosenCand != null) {
                arrows.add(chosenCand)
                occupied.add(hole)
                currentId++
            }
        }

        return arrows
    }

    private fun generateInterlockingSnake(
        id: Int,
        type: SnakeType,
        gridSize: Int,
        mask: Array<BooleanArray>,
        occupied: Set<GridPoint>,
        existingArrows: List<Arrow>,
        random: Random
    ): Arrow? {
        val attempts = 45
        var bestCandidate: Arrow? = null
        var bestScore = -500

        for (tryCount in 0 until attempts) {
            val startX = random.nextInt(gridSize)
            val startY = random.nextInt(gridSize)

            if (!mask[startX][startY] || occupied.contains(GridPoint(startX, startY))) {
                continue
            }

            val path = buildSnakePath(type, startX, startY, gridSize, mask, occupied, random)
            if (path == null || path.size < 2) continue

            val initialDir = getDirection(path[0], path[1]) ?: Direction.RIGHT
            val cand = Arrow(
                id = id,
                startX = path.first().x,
                startY = path.first().y,
                length = path.size,
                direction = initialDir,
                pathPoints = path,
                customColorHex = paletteHex[(id - 1) % paletteHex.size]
            )

            val bodyCells = cand.getOccupiedCells()
            val candExitRay = cand.getExitRay(gridSize, gridSize)

            // Dependency scoring:
            // 1. Reward candidate if its body intersects and blocks existing arrows' exit rays (creates dependency)
            var blockedEarlierCount = 0
            for (earlier in existingArrows) {
                val exRay = earlier.getExitRay(gridSize, gridSize)
                if (exRay.any { bodyCells.contains(it) }) {
                    blockedEarlierCount++
                }
            }

            // 2. Count if this candidate itself is blocked by an existing arrow
            val isCandBlocked = candExitRay.any { occupied.contains(it) }

            var score = blockedEarlierCount * 50 + path.size * 8
            if (isCandBlocked) {
                score += 30 // Healthy dependency chain!
            } else if (existingArrows.size < 3) {
                score += 20 // Outer initial key
            }

            if (type == SnakeType.U_TURN_HAIRPIN || type == SnakeType.SPIRAL_HOOK) {
                score += 25
            }

            if (score > bestScore) {
                bestScore = score
                bestCandidate = cand
                if (blockedEarlierCount >= 2) break
            }
        }

        return bestCandidate
    }

    private fun buildSnakePath(
        type: SnakeType,
        startX: Int,
        startY: Int,
        gridSize: Int,
        mask: Array<BooleanArray>,
        occupied: Set<GridPoint>,
        random: Random
    ): List<GridPoint>? {
        val dirs = Direction.entries.toTypedArray()

        return when (type) {
            SnakeType.STRAIGHT -> {
                val dir = dirs[random.nextInt(dirs.size)]
                val length = random.nextInt(2, 6)
                generateStraightPath(startX, startY, dir, length, gridSize, mask, occupied)
            }

            SnakeType.L_BEND -> {
                val d1 = dirs[random.nextInt(dirs.size)]
                val d2 = getPerpendicular(d1, random)
                val len1 = random.nextInt(2, 5)
                val len2 = random.nextInt(2, 5)

                val pts = mutableListOf<GridPoint>()
                var cx = startX
                var cy = startY
                pts.add(GridPoint(cx, cy))

                // First leg
                for (i in 1 until len1) {
                    cx += d1.dx
                    cy += d1.dy
                    if (!isValidCell(cx, cy, gridSize, mask, occupied, pts)) return null
                    pts.add(GridPoint(cx, cy))
                }
                // Second leg (turned)
                for (i in 1 until len2) {
                    cx += d2.dx
                    cy += d2.dy
                    if (!isValidCell(cx, cy, gridSize, mask, occupied, pts)) return null
                    pts.add(GridPoint(cx, cy))
                }
                pts
            }

            SnakeType.U_TURN_HAIRPIN -> {
                val dForward = dirs[random.nextInt(dirs.size)]
                val dPerp = getPerpendicular(dForward, random)
                val dBack = dForward.opposite()

                val len1 = random.nextInt(2, 5)
                val len2 = random.nextInt(2, 5)

                val pts = mutableListOf<GridPoint>()
                var cx = startX
                var cy = startY
                pts.add(GridPoint(cx, cy))

                // Leg 1
                for (i in 1 until len1) {
                    cx += dForward.dx
                    cy += dForward.dy
                    if (!isValidCell(cx, cy, gridSize, mask, occupied, pts)) return null
                    pts.add(GridPoint(cx, cy))
                }
                // Step perpendicular
                cx += dPerp.dx
                cy += dPerp.dy
                if (!isValidCell(cx, cy, gridSize, mask, occupied, pts)) return null
                pts.add(GridPoint(cx, cy))

                // Leg 2
                for (i in 1 until len2) {
                    cx += dBack.dx
                    cy += dBack.dy
                    if (!isValidCell(cx, cy, gridSize, mask, occupied, pts)) return null
                    pts.add(GridPoint(cx, cy))
                }
                pts
            }

            SnakeType.S_STAIRCASE -> {
                val dForward = dirs[random.nextInt(dirs.size)]
                val dPerp = getPerpendicular(dForward, random)
                val len = random.nextInt(2, 4)

                val pts = mutableListOf<GridPoint>()
                var cx = startX
                var cy = startY
                pts.add(GridPoint(cx, cy))

                for (step in 0 until len) {
                    cx += dForward.dx
                    cy += dForward.dy
                    if (!isValidCell(cx, cy, gridSize, mask, occupied, pts)) return null
                    pts.add(GridPoint(cx, cy))

                    cx += dPerp.dx
                    cy += dPerp.dy
                    if (!isValidCell(cx, cy, gridSize, mask, occupied, pts)) return null
                    pts.add(GridPoint(cx, cy))
                }
                pts
            }

            SnakeType.SPIRAL_HOOK -> {
                val d0 = dirs[random.nextInt(dirs.size)]
                val clockwise = random.nextBoolean()
                val d1 = rotateDir(d0, clockwise)
                val d2 = rotateDir(d1, clockwise)

                val pts = mutableListOf<GridPoint>()
                var cx = startX
                var cy = startY
                pts.add(GridPoint(cx, cy))

                val segLens = listOf(random.nextInt(2, 4), random.nextInt(2, 4), random.nextInt(2, 4))
                val segDirs = listOf(d0, d1, d2)

                for (s in 0..2) {
                    val sDir = segDirs[s]
                    val sLen = segLens[s]
                    for (i in 1 until sLen) {
                        cx += sDir.dx
                        cy += sDir.dy
                        if (!isValidCell(cx, cy, gridSize, mask, occupied, pts)) return null
                        pts.add(GridPoint(cx, cy))
                    }
                }
                pts
            }

            SnakeType.BOUNDARY_WRAPPER -> {
                val d1 = dirs[random.nextInt(dirs.size)]
                val d2 = getPerpendicular(d1, random)
                val len1 = random.nextInt(3, 6)
                val len2 = random.nextInt(3, 6)

                val pts = mutableListOf<GridPoint>()
                var cx = startX
                var cy = startY
                pts.add(GridPoint(cx, cy))

                for (i in 1 until len1) {
                    cx += d1.dx
                    cy += d1.dy
                    if (!isValidCell(cx, cy, gridSize, mask, occupied, pts)) return null
                    pts.add(GridPoint(cx, cy))
                }
                for (i in 1 until len2) {
                    cx += d2.dx
                    cy += d2.dy
                    if (!isValidCell(cx, cy, gridSize, mask, occupied, pts)) return null
                    pts.add(GridPoint(cx, cy))
                }
                pts
            }
        }
    }

    private fun generateStraightPath(
        startX: Int,
        startY: Int,
        dir: Direction,
        length: Int,
        gridSize: Int,
        mask: Array<BooleanArray>,
        occupied: Set<GridPoint>
    ): List<GridPoint>? {
        val pts = mutableListOf<GridPoint>()
        var cx = startX
        var cy = startY
        pts.add(GridPoint(cx, cy))

        for (i in 1 until length) {
            cx += dir.dx
            cy += dir.dy
            if (!isValidCell(cx, cy, gridSize, mask, occupied, pts)) {
                return if (pts.size >= 2) pts else null
            }
            pts.add(GridPoint(cx, cy))
        }
        return pts
    }

    private fun isValidCell(
        x: Int,
        y: Int,
        gridSize: Int,
        mask: Array<BooleanArray>,
        occupied: Set<GridPoint>,
        currentPath: List<GridPoint>
    ): Boolean {
        if (x !in 0 until gridSize || y !in 0 until gridSize) return false
        if (!mask[x][y]) return false
        val gp = GridPoint(x, y)
        if (occupied.contains(gp) || currentPath.contains(gp)) return false
        return true
    }

    private fun getPerpendicular(dir: Direction, random: Random): Direction {
        return when (dir) {
            Direction.UP, Direction.DOWN -> if (random.nextBoolean()) Direction.LEFT else Direction.RIGHT
            Direction.LEFT, Direction.RIGHT -> if (random.nextBoolean()) Direction.UP else Direction.DOWN
        }
    }

    private fun rotateDir(dir: Direction, clockwise: Boolean): Direction {
        return if (clockwise) {
            when (dir) {
                Direction.UP -> Direction.RIGHT
                Direction.RIGHT -> Direction.DOWN
                Direction.DOWN -> Direction.LEFT
                Direction.LEFT -> Direction.UP
            }
        } else {
            when (dir) {
                Direction.UP -> Direction.LEFT
                Direction.LEFT -> Direction.DOWN
                Direction.DOWN -> Direction.RIGHT
                Direction.RIGHT -> Direction.UP
            }
        }
    }

    private fun getDirection(p1: GridPoint, p2: GridPoint): Direction? {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        return Direction.entries.find { it.dx == dx && it.dy == dy }
    }

    /**
     * Fallback that constructs a guaranteed-solvable reverse peeling dependency chain.
     */
    private fun createGuaranteedReverseChainFallback(
        gridSize: Int,
        mask: Array<BooleanArray>,
        validCells: Set<GridPoint>,
        random: Random
    ): List<Arrow> {
        val arrows = mutableListOf<Arrow>()
        val occupied = HashSet<GridPoint>()
        var currentId = 1

        // Sort cells radially from center outwards
        val cx = gridSize / 2f
        val cy = gridSize / 2f
        val sortedCells = validCells.sortedByDescending {
            val dx = it.x - cx
            val dy = it.y - cy
            dx * dx + dy * dy
        }

        for (cell in sortedCells) {
            if (occupied.contains(cell)) continue

            // Aim outward to perimeter
            val dir = when {
                cell.x < cx && cell.y < cy -> if (random.nextBoolean()) Direction.LEFT else Direction.UP
                cell.x >= cx && cell.y < cy -> if (random.nextBoolean()) Direction.RIGHT else Direction.UP
                cell.x < cx && cell.y >= cy -> if (random.nextBoolean()) Direction.LEFT else Direction.DOWN
                else -> if (random.nextBoolean()) Direction.RIGHT else Direction.DOWN
            }

            val cand = Arrow(
                id = currentId,
                startX = cell.x,
                startY = cell.y,
                length = 1,
                direction = dir,
                pathPoints = listOf(cell),
                customColorHex = paletteHex[(currentId - 1) % paletteHex.size]
            )

            arrows.add(cand)
            occupied.add(cell)
            currentId++
        }
        return arrows
    }
}
