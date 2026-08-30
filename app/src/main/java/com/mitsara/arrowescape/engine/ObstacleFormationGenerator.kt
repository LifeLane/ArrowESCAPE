package com.mitsara.arrowescape.engine

import com.mitsara.arrowescape.model.GridPoint
import kotlin.math.abs
import kotlin.random.Random

/**
 * Procedural generator for complex, asymmetrical, and varied obstacle formations.
 * Creates interesting maze walls, choke points, and corridors while leaving viable routes for arrows.
 */
object ObstacleFormationGenerator {

    enum class FormationType {
        CROSS,
        DENSE_BLOCKS,
        CONCENTRIC_RINGS,
        BROKEN_RINGS,
        ZIG_ZAG_BARRIERS,
        STAIRCASE,
        OFFSET_CLUSTERS,
        MULTI_LAYER_WALLS,
        ASYMMETRIC_MAZE,
        CORRIDOR_CHOKES
    }

    /**
     * Generates a set of obstacle cells within [1 until size - 1, 1 until size - 1] to avoid closing the entire outer boundary.
     */
    fun generateObstacles(
        gridSize: Int,
        targetObstacleCount: Int,
        formationType: FormationType,
        random: Random,
        validCells: Set<GridPoint>
    ): Set<GridPoint> {
        val obstacles = mutableSetOf<GridPoint>()
        val center = gridSize / 2

        when (formationType) {
            FormationType.CROSS -> {
                val armOffset = random.nextInt(-1, 2)
                val crossCenter = GridPoint((center + armOffset).coerceIn(2, gridSize - 3), center)
                val armLength = (gridSize / 3).coerceAtLeast(2)

                for (d in -armLength..armLength) {
                    if (d != 0 || random.nextBoolean()) {
                        // Horizontal bar with possible gaps
                        if (random.nextFloat() > 0.15f) {
                            obstacles.add(GridPoint(crossCenter.x + d, crossCenter.y))
                        }
                        // Vertical bar with possible gaps
                        if (random.nextFloat() > 0.15f) {
                            obstacles.add(GridPoint(crossCenter.x, crossCenter.y + d))
                        }
                    }
                }
            }

            FormationType.DENSE_BLOCKS -> {
                val blockCount = random.nextInt(2, 5)
                for (b in 0 until blockCount) {
                    val bx = random.nextInt(2, (gridSize - 3).coerceAtLeast(3))
                    val by = random.nextInt(2, (gridSize - 3).coerceAtLeast(3))
                    val bw = random.nextInt(2, 3)
                    val bh = random.nextInt(2, 3)
                    for (dx in 0 until bw) {
                        for (dy in 0 until bh) {
                            obstacles.add(GridPoint(bx + dx, by + dy))
                        }
                    }
                }
            }

            FormationType.CONCENTRIC_RINGS, FormationType.BROKEN_RINGS -> {
                val radius = (gridSize / 3).coerceIn(2, 4)
                val isBroken = formationType == FormationType.BROKEN_RINGS || random.nextBoolean()
                val gapDirs = setOf(random.nextInt(4), (random.nextInt(4) + 2) % 4)

                for (dx in -radius..radius) {
                    for (dy in -radius..radius) {
                        val dist = maxOf(abs(dx), abs(dy))
                        if (dist == radius) {
                            val dirIdx = when {
                                dy == -radius -> 0 // Top
                                dx == radius -> 1  // Right
                                dy == radius -> 2  // Bottom
                                else -> 3          // Left
                            }
                            if (!isBroken || !gapDirs.contains(dirIdx) || abs(dx) > 1 && abs(dy) > 1) {
                                obstacles.add(GridPoint(center + dx, center + dy))
                            }
                        }
                    }
                }
            }

            FormationType.ZIG_ZAG_BARRIERS -> {
                val numZigs = (gridSize / 4).coerceAtLeast(2)
                var currentX = random.nextInt(2, 4)
                var currentY = 2
                var goingDown = true

                while (currentY < gridSize - 2 && obstacles.size < targetObstacleCount) {
                    val len = random.nextInt(2, 4)
                    for (i in 0 until len) {
                        val pt = if (goingDown) GridPoint(currentX, currentY + i) else GridPoint(currentX + i, currentY)
                        obstacles.add(pt)
                    }
                    if (goingDown) {
                        currentY += len
                        currentX = (currentX + random.nextInt(2, 4)).coerceAtMost(gridSize - 3)
                    } else {
                        currentX += len
                        currentY = (currentY + random.nextInt(2, 4)).coerceAtMost(gridSize - 3)
                    }
                    goingDown = !goingDown
                }
            }

            FormationType.STAIRCASE -> {
                val startX = 2
                val startY = 2
                val steps = (gridSize - 4).coerceAtLeast(3)
                for (s in 0 until steps) {
                    obstacles.add(GridPoint(startX + s, startY + s))
                    if (s % 2 == 0) {
                        obstacles.add(GridPoint(startX + s + 1, startY + s))
                    }
                }
            }

            FormationType.OFFSET_CLUSTERS -> {
                val clusterCenters = listOf(
                    GridPoint(center - 2, center - 2),
                    GridPoint(center + 2, center + 2),
                    GridPoint(center - 2, center + 2),
                    GridPoint(center + 2, center - 2)
                ).shuffled(random).take(random.nextInt(2, 4))

                for (c in clusterCenters) {
                    val cSize = random.nextInt(3, 6)
                    for (i in 0 until cSize) {
                        val ox = c.x + random.nextInt(-1, 2)
                        val oy = c.y + random.nextInt(-1, 2)
                        obstacles.add(GridPoint(ox, oy))
                    }
                }
            }

            FormationType.MULTI_LAYER_WALLS -> {
                val layerSpacing = 2
                val startOffset = 2
                for (layer in 0 until 2) {
                    val row = startOffset + layer * layerSpacing
                    val gap = random.nextInt(2, gridSize - 3)
                    for (x in 2 until gridSize - 2) {
                        if (abs(x - gap) > 1) {
                            obstacles.add(GridPoint(x, row))
                        }
                    }
                }
            }

            FormationType.CORRIDOR_CHOKES -> {
                // Creates narrow 1-cell or 2-cell corridors in the grid
                val chokeCol1 = center - 1
                val chokeCol2 = center + 1
                for (y in 2 until gridSize - 2) {
                    if (y % 3 != 0) {
                        obstacles.add(GridPoint(chokeCol1, y))
                    }
                    if ((y + 1) % 3 != 0) {
                        obstacles.add(GridPoint(chokeCol2, y))
                    }
                }
            }

            FormationType.ASYMMETRIC_MAZE -> {
                // Scatter asymmetric L-shaped and T-shaped barrier walls
                val barrierCount = (gridSize / 3).coerceAtLeast(3)
                for (b in 0 until barrierCount) {
                    val bx = random.nextInt(2, gridSize - 3)
                    val by = random.nextInt(2, gridSize - 3)
                    val bDir = if (random.nextBoolean()) 1 else 0
                    val len = random.nextInt(2, 4)
                    for (i in 0 until len) {
                        val pt = if (bDir == 1) GridPoint(bx + i, by) else GridPoint(bx, by + i)
                        obstacles.add(pt)
                    }
                }
            }
        }

        // Filter to ensure obstacles stay strictly inside board bounds (away from direct perimeter), inside valid mask
        val filtered = obstacles.filter { pt ->
            pt.x in 1 until gridSize - 1 &&
            pt.y in 1 until gridSize - 1 &&
            validCells.contains(pt)
        }.toMutableSet()

        // Fill or trim to target obstacle count
        if (filtered.size > targetObstacleCount) {
            val trimmed = filtered.shuffled(random).take(targetObstacleCount).toSet()
            return trimmed
        } else if (filtered.size < targetObstacleCount) {
            val candidateCells = validCells.filter {
                it.x in 1 until gridSize - 1 &&
                it.y in 1 until gridSize - 1 &&
                !filtered.contains(it)
            }.shuffled(random)

            for (cell in candidateCells) {
                if (filtered.size >= targetObstacleCount) break
                filtered.add(cell)
            }
        }

        return filtered
    }
}
