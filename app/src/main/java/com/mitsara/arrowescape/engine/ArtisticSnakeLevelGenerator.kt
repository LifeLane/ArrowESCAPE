package com.mitsara.arrowescape.engine

import com.mitsara.arrowescape.model.Arrow
import com.mitsara.arrowescape.model.Difficulty
import com.mitsara.arrowescape.model.Direction
import com.mitsara.arrowescape.model.GridPoint
import com.mitsara.arrowescape.model.PuzzleLevel
import kotlin.random.Random

/**
 * Procedural synthesis engine for intricate winding snake arrows, L-bends,
 * U-turn hairpins, S-staircases, spiral coils, and silhouette-framing boundary arrows.
 * Faithfully reproduces the aesthetic and logic from reference game screenshots.
 */
object ArtisticSnakeLevelGenerator {

    private val paletteHex = listOf(
        "#3E2E23", "#2C3E50", "#1E293B", "#374151", "#4A3525",
        "#1F2937", "#334155", "#475569", "#292524", "#18181B"
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
     */
    fun generateSilhouetteLevel(
        levelNumber: Int,
        gridSize: Int,
        targetArrowCount: Int,
        targetDifficulty: Difficulty,
        seed: Long
    ): PuzzleLevel {
        val random = Random(seed)
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

        val maxAttempts = 18

        for (attempt in 0 until maxAttempts) {
            val iterRandom = Random(seed + attempt * 1009L + 42L)
            val candidateArrows = synthesizeArrows(
                gridSize = gridSize,
                mask = mask,
                validCells = validCells,
                targetCount = targetArrowCount,
                targetDifficulty = targetDifficulty,
                random = iterRandom
            )

            if (candidateArrows.size >= 10) {
                val analysis = PuzzleSolver.analyzePuzzle(
                    initialArrows = candidateArrows,
                    gridWidth = gridSize,
                    gridHeight = gridSize,
                    obstacles = emptySet(),
                    validCells = null // null because exiting into open canvas is unobstructed
                )

                if (analysis.isSolvable && analysis.solutionSequence.size == candidateArrows.size) {
                    val score = candidateArrows.size * 10 + analysis.dependencyDepth * 15
                    if (score > bestScore) {
                        bestScore = score
                        bestArrows = candidateArrows
                        bestAnalysis = analysis
                        if (analysis.dependencyDepth >= 8 && candidateArrows.size >= targetArrowCount * 0.85f) {
                            break
                        }
                    }
                }
            }
        }

        val finalArrows = if (bestArrows.isNotEmpty()) bestArrows else createGuaranteedFallback(gridSize, mask, validCells, random)
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

    private fun synthesizeArrows(
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
        val maxPlacements = targetCount * 3
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
            val candidate = generateCandidateSnake(
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

        // Fill remaining isolated single-cell or two-cell holes with small straight arrows
        val remainingHoles = validCells.filter { !occupied.contains(it) }.shuffled(random)
        for (hole in remainingHoles) {
            if (occupied.contains(hole)) continue

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

                val ray = cand.getExitRay(gridSize, gridSize)
                if (!ray.any { occupied.contains(it) }) {
                    arrows.add(cand)
                    occupied.add(hole)
                    currentId++
                    break
                }
            }
        }

        return arrows
    }

    private fun generateCandidateSnake(
        id: Int,
        type: SnakeType,
        gridSize: Int,
        mask: Array<BooleanArray>,
        occupied: Set<GridPoint>,
        existingArrows: List<Arrow>,
        random: Random
    ): Arrow? {
        val attempts = 40
        var bestCandidate: Arrow? = null
        var bestScore = -100

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

            // Verify that the candidate's exit ray is clear to the perimeter of the canvas
            val exitRay = cand.getExitRay(gridSize, gridSize)
            if (exitRay.any { occupied.contains(it) }) {
                continue
            }

            // Dependency scoring: Reward candidate for blocking already placed arrows' exit rays
            var blockedEarlierCount = 0
            val bodyCells = cand.getOccupiedCells()
            for (earlier in existingArrows) {
                val exRay = earlier.getExitRay(gridSize, gridSize)
                if (exRay.any { bodyCells.contains(it) }) {
                    blockedEarlierCount++
                }
            }

            var score = blockedEarlierCount * 35 + path.size * 6
            if (type == SnakeType.U_TURN_HAIRPIN || type == SnakeType.SPIRAL_HOOK) {
                score += 15
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
                // Hairpin turn: forward -> perpendicular 1 step -> reverse
                val dForward = dirs[random.nextInt(dirs.size)]
                val dPerp = getPerpendicular(dForward, random)
                val dBack = dForward.opposite()

                val len1 = random.nextInt(2, 5)
                val len2 = random.nextInt(2, 5)

                val pts = mutableListOf<GridPoint>()
                var cx = startX
                var cy = startY
                pts.add(GridPoint(cx, cy))

                // Leg 1: forward
                for (i in 1 until len1) {
                    cx += dForward.dx
                    cy += dForward.dy
                    if (!isValidCell(cx, cy, gridSize, mask, occupied, pts)) return null
                    pts.add(GridPoint(cx, cy))
                }
                // Step 2: 1 perpendicular step (the bend)
                cx += dPerp.dx
                cy += dPerp.dy
                if (!isValidCell(cx, cy, gridSize, mask, occupied, pts)) return null
                pts.add(GridPoint(cx, cy))

                // Leg 3: reverse parallel
                for (i in 1 until len2) {
                    cx += dBack.dx
                    cy += dBack.dy
                    if (!isValidCell(cx, cy, gridSize, mask, occupied, pts)) return null
                    pts.add(GridPoint(cx, cy))
                }
                pts
            }

            SnakeType.S_STAIRCASE -> {
                // Staircase zig-zag: Leg1 -> Step Perp -> Leg2 (same direction)
                val dMain = dirs[random.nextInt(dirs.size)]
                val dPerp = getPerpendicular(dMain, random)
                val len1 = random.nextInt(2, 4)
                val len2 = random.nextInt(2, 4)

                val pts = mutableListOf<GridPoint>()
                var cx = startX
                var cy = startY
                pts.add(GridPoint(cx, cy))

                for (i in 1 until len1) {
                    cx += dMain.dx
                    cy += dMain.dy
                    if (!isValidCell(cx, cy, gridSize, mask, occupied, pts)) return null
                    pts.add(GridPoint(cx, cy))
                }

                cx += dPerp.dx
                cy += dPerp.dy
                if (!isValidCell(cx, cy, gridSize, mask, occupied, pts)) return null
                pts.add(GridPoint(cx, cy))

                for (i in 1 until len2) {
                    cx += dMain.dx
                    cy += dMain.dy
                    if (!isValidCell(cx, cy, gridSize, mask, occupied, pts)) return null
                    pts.add(GridPoint(cx, cy))
                }
                pts
            }

            SnakeType.SPIRAL_HOOK -> {
                // G-hook or 3-turn spiral coil
                val d1 = dirs[random.nextInt(dirs.size)]
                val clockwise = random.nextBoolean()
                val d2 = rotateDir(d1, clockwise)
                val d3 = rotateDir(d2, clockwise)
                val d4 = rotateDir(d3, clockwise)

                val pts = mutableListOf<GridPoint>()
                var cx = startX
                var cy = startY
                pts.add(GridPoint(cx, cy))

                val segLengths = listOf(3, 3, 2, 2)
                val segDirs = listOf(d1, d2, d3, d4)

                for (s in segDirs.indices) {
                    val sDir = segDirs[s]
                    val sLen = segLengths[s]
                    for (i in 1 until sLen) {
                        cx += sDir.dx
                        cy += sDir.dy
                        if (!isValidCell(cx, cy, gridSize, mask, occupied, pts)) {
                            // If partial spiral is valid and long enough, accept it
                            return if (pts.size >= 4) pts else null
                        }
                        pts.add(GridPoint(cx, cy))
                    }
                }
                pts
            }

            SnakeType.BOUNDARY_WRAPPER -> {
                // Long contour arrow tracing mask edges
                val d1 = dirs[random.nextInt(dirs.size)]
                val d2 = getPerpendicular(d1, random)
                val len1 = random.nextInt(4, 7)
                val len2 = random.nextInt(3, 6)

                val pts = mutableListOf<GridPoint>()
                var cx = startX
                var cy = startY
                pts.add(GridPoint(cx, cy))

                for (i in 1 until len1) {
                    cx += d1.dx
                    cy += d1.dy
                    if (!isValidCell(cx, cy, gridSize, mask, occupied, pts)) break
                    pts.add(GridPoint(cx, cy))
                }
                for (i in 1 until len2) {
                    cx += d2.dx
                    cy += d2.dy
                    if (!isValidCell(cx, cy, gridSize, mask, occupied, pts)) break
                    pts.add(GridPoint(cx, cy))
                }
                if (pts.size >= 3) pts else null
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

    private fun createGuaranteedFallback(
        gridSize: Int,
        mask: Array<BooleanArray>,
        validCells: Set<GridPoint>,
        random: Random
    ): List<Arrow> {
        val arrows = mutableListOf<Arrow>()
        val occupied = HashSet<GridPoint>()
        var currentId = 1

        val cells = validCells.shuffled(random)
        for (cell in cells) {
            if (occupied.contains(cell)) continue

            for (dir in Direction.entries.shuffled(random)) {
                val cand = Arrow(
                    id = currentId,
                    startX = cell.x,
                    startY = cell.y,
                    length = 1,
                    direction = dir,
                    pathPoints = listOf(cell),
                    customColorHex = paletteHex[(currentId - 1) % paletteHex.size]
                )

                val ray = cand.getExitRay(gridSize, gridSize)
                if (!ray.any { occupied.contains(it) }) {
                    arrows.add(cand)
                    occupied.add(cell)
                    currentId++
                    break
                }
            }
        }
        return arrows
    }
}
