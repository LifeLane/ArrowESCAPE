package com.mitsara.arrowescape.engine

import com.mitsara.arrowescape.model.GridPoint
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * High-definition silhouette masks for recognizable real-world object shapes,
 * animals, geometric mandalas, and intricate labyrinths (matching the user reference screenshots).
 */
object SilhouetteShapeRegistry {

    enum class ShapeType(val displayName: String, val baseGridSize: Int) {
        TROPHY("Trophy Cup", 14),           // Screenshot 1 (Level 10)
        ANCHOR("Nautical Anchor", 14),      // Screenshot 2 (Level 27)
        DOG("Puppy Companion", 14),         // Screenshot 3 (Level 40)
        BUTTERFLY("Monarch Butterfly", 14),  // Screenshot 4 (Level 99)
        SQUARE_MAZE("Labyrinth Matrix", 13),// Screenshot 5 (Level 199)
        HEART("Sacred Heart", 13),          // Screenshot 6 (Level 42)
        KNIGHT("Chess Knight", 14),         // Screenshot 7
        MANDALA("Geometric Mandala", 13),   // Screenshot 8
        TALL_CORRIDOR("Tall Corridor", 12), // Screenshot 9
        CAT("Curled Cat", 13),
        BIRD("Soaring Eagle", 14),
        TREE("Evergreen Pine", 13),
        CROWN("Royal Crown", 13),
        STAR("Starburst", 13),
        AIRPLANE("Supersonic Jet", 14),
        GUITAR("Acoustic Guitar", 14),
        DIAMOND("Faceted Gem", 13),
        FLOWER("Blooming Blossom", 13),
        HOUSE("Cozy Cabin", 13),
        FISH("Angelfish", 13)
    }

    /**
     * Maps any level number to an appropriate silhouette shape.
     */
    fun getShapeForLevel(levelNumber: Int): ShapeType {
        return when (levelNumber) {
            10 -> ShapeType.TROPHY
            27 -> ShapeType.ANCHOR
            40 -> ShapeType.DOG
            42 -> ShapeType.HEART
            99 -> ShapeType.BUTTERFLY
            199 -> ShapeType.SQUARE_MAZE
            else -> {
                val allShapes = ShapeType.entries.toTypedArray()
                // Deterministic mapping cycling through shapes, with milestones giving iconic shapes
                val hash = (levelNumber * 7919 + 1013) % allShapes.size
                allShapes[abs(hash)]
            }
        }
    }

    /**
     * Generates a 2D boolean mask of dimensions [gridWidth] x [gridHeight].
     * `true` indicates a cell is inside the shape boundary.
     */
    fun generateShapeMask(shape: ShapeType, gridWidth: Int, gridHeight: Int): Array<BooleanArray> {
        val mask = Array(gridWidth) { BooleanArray(gridHeight) { false } }
        val cx = (gridWidth - 1) / 2.0
        val cy = (gridHeight - 1) / 2.0

        for (x in 0 until gridWidth) {
            for (y in 0 until gridHeight) {
                // Normalized coordinates: nx in [-1, 1], ny in [-1, 1]
                val nx = (x - cx) / (gridWidth / 2.0)
                val ny = (y - cy) / (gridHeight / 2.0)

                mask[x][y] = isInsideShape(shape, nx, ny, x, y, gridWidth, gridHeight)
            }
        }

        return mask
    }

    private fun isInsideShape(
        shape: ShapeType,
        nx: Double,
        ny: Double,
        x: Int,
        y: Int,
        w: Int,
        h: Int
    ): Boolean {
        val absNx = abs(nx)

        return when (shape) {
            ShapeType.TROPHY -> {
                // Screenshot 1: Cup bowl (-0.8 to 0.1), handles on sides, stem (0.1 to 0.5), base pedestal (0.5 to 0.9)
                when {
                    // Pedestal base
                    ny in 0.55..0.92 && absNx <= 0.65 -> {
                        if (ny > 0.82) absNx <= 0.85 else absNx <= 0.55
                    }
                    // Thin stem
                    ny in 0.15..0.55 && absNx <= 0.22 -> true
                    // Bowl
                    ny in -0.75..0.15 && absNx <= 0.62 -> true
                    // Handles
                    ny in -0.65..-0.05 && absNx in 0.62..0.92 -> {
                        // Hollow handle cutout
                        !(ny in -0.50..-0.20 && absNx in 0.70..0.82)
                    }
                    else -> false
                }
            }

            ShapeType.ANCHOR -> {
                // Screenshot 2: Top loop, horizontal crossbar, vertical shank, sweeping curved flukes with tips
                when {
                    // Top loop ring
                    ny in -0.92..-0.62 && absNx <= 0.35 -> {
                        val dist = sqrt(nx * nx + (ny + 0.77).pow(2.0))
                        dist <= 0.32 && dist >= 0.08
                    }
                    // Central shank / stem
                    ny in -0.65..0.70 && absNx <= 0.18 -> true
                    // Horizontal crossbar
                    ny in -0.42..-0.25 && absNx <= 0.75 -> true
                    // Curved lower arms & flukes
                    ny in 0.15..0.88 -> {
                        val armCurve = 0.55 + 0.35 * (absNx * absNx)
                        val inArm = abs(ny - armCurve) <= 0.18 && absNx in 0.15..0.88
                        val flukeTip = ny in 0.05..0.45 && absNx in 0.75..0.92
                        inArm || flukeTip
                    }
                    else -> false
                }
            }

            ShapeType.DOG -> {
                // Screenshot 3: Puppy silhouette - head with ears, snout, body, 4 legs, tail
                when {
                    // Head & ears (top right)
                    ny in -0.85..-0.10 && nx in 0.05..0.88 -> {
                        // Snout
                        if (nx > 0.55 && ny < -0.30) true
                        // Ears & top of head
                        else nx <= 0.65
                    }
                    // Neck & body
                    ny in -0.15..0.45 && nx in -0.75..0.55 -> true
                    // Tail (top left)
                    ny in -0.55..-0.10 && nx in -0.88..-0.60 -> true
                    // Front legs
                    ny in 0.45..0.92 && nx in 0.22..0.52 -> true
                    // Back legs
                    ny in 0.45..0.92 && nx in -0.75..-0.45 -> true
                    else -> false
                }
            }

            ShapeType.BUTTERFLY -> {
                // Screenshot 4: Butterfly with symmetrical upper & lower wings, central body
                when {
                    // Central thorax
                    absNx <= 0.14 && ny in -0.75..0.75 -> true
                    // Upper wings (large spreading lobes)
                    ny in -0.85..0.15 && absNx in 0.14..0.92 -> {
                        // Outer wing taper
                        val wingTop = -0.85 + 0.45 * (1.0 - absNx)
                        ny >= wingTop
                    }
                    // Lower wings (smaller tapering lobes)
                    ny in 0.15..0.80 && absNx in 0.14..0.78 -> {
                        val wingBottom = 0.80 - 0.40 * (1.0 - absNx)
                        ny <= wingBottom
                    }
                    else -> false
                }
            }

            ShapeType.SQUARE_MAZE -> {
                // Screenshot 5: Clean full square labyrinth
                true
            }

            ShapeType.HEART -> {
                // Screenshot 6: Classic Valentine Heart
                // Implicit equation: (x^2 + y^2 - 1)^3 - x^2 * y^3 <= 0
                val px = nx * 1.15
                val py = -ny * 1.15 + 0.25 // Invert y so point is at bottom
                val term = px * px + py * py - 1.0
                (term * term * term - px * px * py * py * py) <= 0.05
            }

            ShapeType.KNIGHT -> {
                // Screenshot 7: Chess Knight / Horse Head
                when {
                    // Base pedestal
                    ny in 0.65..0.92 && absNx <= 0.75 -> true
                    // Chest / Body
                    ny in 0.05..0.65 && nx in -0.65..0.65 -> true
                    // Arched Neck / Mane (left/back side)
                    ny in -0.75..0.05 && nx in -0.65..0.15 -> true
                    // Snout / Muzzle (pointing right/forward)
                    ny in -0.45..0.05 && nx in 0.15..0.75 -> true
                    // Pointed Ears
                    ny in -0.88..-0.65 && nx in -0.25..0.10 -> true
                    else -> false
                }
            }

            ShapeType.MANDALA -> {
                // Screenshot 8: Symmetrical Octagon / Mandala Jewel
                val dist = abs(nx) + abs(ny)
                val maxAxis = maxOf(abs(nx), abs(ny))
                dist <= 1.35 && maxAxis <= 0.88
            }

            ShapeType.TALL_CORRIDOR -> {
                // Screenshot 9: Long vertical labyrinth
                absNx <= 0.82 && abs(ny) <= 0.94
            }

            ShapeType.CAT -> {
                // Cat with pointed ears, head, body, tail
                when {
                    // Ears
                    ny in -0.88..-0.55 && (absNx in 0.15..0.45) -> true
                    // Head
                    ny in -0.55..-0.10 && absNx <= 0.55 -> true
                    // Body
                    ny in -0.10..0.70 && absNx <= 0.68 -> true
                    // Tail curled to the right
                    ny in 0.20..0.85 && nx in 0.68..0.92 -> true
                    else -> false
                }
            }

            ShapeType.BIRD -> {
                // Eagle / Falcon with spread wings
                when {
                    // Body / Head / Beak
                    absNx <= 0.20 && ny in -0.85..0.75 -> true
                    // Spread Wings
                    ny in -0.50..0.35 && absNx in 0.20..0.94 -> {
                        ny >= -0.50 + 0.35 * (absNx - 0.20)
                    }
                    // Tail feathers
                    ny in 0.70..0.92 && absNx <= 0.35 -> true
                    else -> false
                }
            }

            ShapeType.TREE -> {
                // Pine Tree with tiered foliage layers and trunk
                when {
                    // Trunk
                    ny in 0.60..0.92 && absNx <= 0.22 -> true
                    // Tier 1 (top)
                    ny in -0.85..-0.35 && absNx <= (0.45 * (ny + 0.85) / 0.50) -> true
                    // Tier 2 (middle)
                    ny in -0.35..0.15 && absNx <= (0.70 * (ny + 0.35) / 0.50) -> true
                    // Tier 3 (bottom)
                    ny in 0.15..0.65 && absNx <= (0.90 * (ny - 0.15) / 0.50) -> true
                    else -> false
                }
            }

            ShapeType.CROWN -> {
                // 3-Peak Royal Crown
                when {
                    // Base band
                    ny in 0.40..0.80 && absNx <= 0.85 -> true
                    // Center high peak
                    ny in -0.85..0.40 && absNx <= 0.22 -> true
                    // Left and right side peaks
                    ny in -0.60..0.40 && absNx in 0.45..0.85 -> true
                    // Webbing between peaks
                    ny in 0.05..0.40 && absNx <= 0.85 -> true
                    else -> false
                }
            }

            ShapeType.STAR -> {
                // 5-Point Star
                val r = sqrt(nx * nx + ny * ny)
                val angle = Math.atan2(ny, nx)
                val starR = 0.55 + 0.35 * Math.cos(5 * angle)
                r <= starR
            }

            ShapeType.AIRPLANE -> {
                // Jet with fuselage, main wings, and rear stabilizers
                when {
                    // Fuselage
                    absNx <= 0.18 && ny in -0.90..0.85 -> true
                    // Main swept wings
                    ny in -0.15..0.35 && absNx in 0.18..0.92 -> {
                        ny >= -0.15 + 0.40 * (absNx - 0.18)
                    }
                    // Tail wings
                    ny in 0.60..0.85 && absNx in 0.18..0.60 -> true
                    else -> false
                }
            }

            ShapeType.GUITAR -> {
                // Acoustic guitar silhouette
                when {
                    // Headstock and neck
                    ny in -0.92..-0.15 && absNx <= 0.18 -> true
                    // Upper bout
                    ny in -0.15..0.25 && absNx <= 0.55 -> true
                    // Waist
                    ny in 0.25..0.40 && absNx <= 0.42 -> true
                    // Lower bout
                    ny in 0.40..0.90 && absNx <= 0.75 -> true
                    else -> false
                }
            }

            ShapeType.DIAMOND -> {
                // Faceted Gem
                when {
                    // Top crown table
                    ny in -0.75..-0.25 && absNx <= 0.85 -> true
                    // Bottom pavilion tapering to point
                    ny in -0.25..0.85 && absNx <= (0.85 * (0.85 - ny) / 1.10) -> true
                    else -> false
                }
            }

            ShapeType.FLOWER -> {
                // 6-Petal Blossom with center
                val r = sqrt(nx * nx + ny * ny)
                val angle = Math.atan2(ny, nx)
                val petalR = 0.50 + 0.38 * Math.cos(6 * angle)
                r <= petalR
            }

            ShapeType.HOUSE -> {
                // House with pitched roof, chimney, and body
                when {
                    // Chimney
                    ny in -0.85..-0.35 && nx in 0.35..0.60 -> true
                    // Triangular Roof
                    ny in -0.75..-0.05 && absNx <= (0.85 * (ny + 0.75) / 0.70) -> true
                    // Main walls
                    ny in -0.05..0.85 && absNx <= 0.75 -> true
                    else -> false
                }
            }

            ShapeType.FISH -> {
                // Angelfish body with fins and tail
                when {
                    // Oval body
                    (nx / 0.65).pow(2.0) + (ny / 0.45).pow(2.0) <= 1.0 -> true
                    // Top dorsal fin
                    ny in -0.85..-0.30 && nx in -0.25..0.30 -> true
                    // Bottom ventral fin
                    ny in 0.30..0.85 && nx in -0.25..0.30 -> true
                    // Tail fin (left side)
                    nx in -0.90..-0.50 && abs(ny) <= 0.55 * (abs(nx) - 0.50) / 0.40 -> true
                    else -> false
                }
            }
        }
    }
}
