package com.mitsara.arrowescape

import com.mitsara.arrowescape.engine.EscapePathEngine
import com.mitsara.arrowescape.engine.LevelGenerator
import com.mitsara.arrowescape.engine.PuzzleSolver
import com.mitsara.arrowescape.model.Arrow
import com.mitsara.arrowescape.model.Direction
import com.mitsara.arrowescape.model.GridPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ArrowEscapeUnitTest {

    @Test
    fun testEscapePathEngineDurationCalculation() {
        val shortArrow = Arrow(id = 1, startX = 0, startY = 0, length = 1, direction = Direction.UP)
        val shortDuration = EscapePathEngine.calculateEscapeDurationMs(shortArrow, 6, 6)

        val longArrow = Arrow(id = 2, startX = 0, startY = 5, length = 1, direction = Direction.UP)
        val longDuration = EscapePathEngine.calculateEscapeDurationMs(longArrow, 6, 6)

        assertTrue("Short path duration should be smaller than long path duration", shortDuration < longDuration)
        assertTrue("Duration should be >= 320ms", shortDuration >= 320)
        assertTrue("Duration should be <= 750ms", longDuration <= 750)
    }

    @Test
    fun testEscapePathEngineLShapedRouteAndCornerFillet() {
        // L-shaped arrow: path from (1, 4) -> (1, 2) -> (3, 2)
        val bentArrow = Arrow(
            id = 1,
            startX = 1,
            startY = 4,
            length = 5,
            direction = Direction.RIGHT,
            pathPoints = listOf(
                GridPoint(1, 4),
                GridPoint(1, 3),
                GridPoint(1, 2),
                GridPoint(2, 2),
                GridPoint(3, 2)
            )
        )

        val cellWidth = 100f
        val cellHeight = 100f
        val waypoints = EscapePathEngine.buildEscapeWaypoints(bentArrow, 6, 6, cellWidth, cellHeight)

        // Verify waypoints include starting tail, corner at (1,2), exit ray, and offscreen
        assertTrue("Waypoints should have at least 3 key vertices", waypoints.size >= 3)

        val path = EscapePathEngine.ParameterizedPath(
            keyVertices = waypoints,
            cornerRadiusPx = 40f,
            boardBoundsSize = 600f
        )

        assertTrue("Path total length should be positive", path.totalLength > 500f)

        // Sample at start (progress = 0): angle should be pointing UP (0°)
        val sampleStart = path.sampleAt(0f)
        assertEquals("Initial segment moving up should have 0° angle", 0f, sampleStart.angleDegrees, 1.0f)

        // Sample along corner: angle should smoothly transition between 0° and 90°
        val cornerDist = 200f // distance to corner vertex
        val sampleBeforeCorner = path.sampleAt(140f) // before fillet start (160f)
        val sampleMidCorner = path.sampleAt(200f) // midpoint of fillet
        val sampleAfterCorner = path.sampleAt(260f) // after fillet end (240f)

        assertEquals("Before corner fillet should be ~0°", 0f, sampleBeforeCorner.angleDegrees, 1.0f)
        assertTrue("Mid corner angle should be between 20° and 70° (actual: ${sampleMidCorner.angleDegrees})",
            sampleMidCorner.angleDegrees in 20f..70f)
        assertEquals("After corner fillet should be ~90° (RIGHT)", 90f, sampleAfterCorner.angleDegrees, 1.0f)

        // Sample near offscreen end: angle should stay locked to 90° (exit direction)
        val sampleExit = path.sampleAt(path.totalLength - 100f)
        assertEquals("Exit trajectory must be aligned with 90°", 90f, sampleExit.angleDegrees, 1.0f)
    }

    @Test
    fun testEscapePathEngineTravelDistanceEasing() {
        val totalDist = 1000f
        val dist0 = EscapePathEngine.calculateTravelDistance(0f, totalDist)
        val distHalf = EscapePathEngine.calculateTravelDistance(0.5f, totalDist)
        val distEnd = EscapePathEngine.calculateTravelDistance(1.0f, totalDist)

        assertEquals("Start distance should be 0", 0f, dist0, 0.01f)
        assertEquals("End distance should equal total length", totalDist, distEnd, 0.01f)
        assertTrue("Distance at midpoint should be positive and less than total", distHalf in 100f..900f)

        // Verify speed acceleration: slope in second half > slope in first half
        val deltaFirstHalf = distHalf - dist0
        val deltaSecondHalf = distEnd - distHalf
        assertTrue("Second half of travel should have higher speed than first half for fly-away", deltaSecondHalf > deltaFirstHalf)
    }

    @Test
    fun testUnobstructedArrowDetection() {
        val freeArrow = Arrow(id = 1, startX = 0, startY = 0, length = 1, direction = Direction.UP)
        val activeArrows = listOf(freeArrow)

        val isFree = PuzzleSolver.isArrowUnobstructed(freeArrow, activeArrows, 4, 4)
        assertTrue("Arrow pointing UP at (0,0) should be unobstructed", isFree)
    }

    @Test
    fun testEdgePositionsOutwardAndInward() {
        // Outward facing edge arrows
        val topEdgeUp = Arrow(id = 1, startX = 2, startY = 0, length = 1, direction = Direction.UP)
        val leftEdgeLeft = Arrow(id = 2, startX = 0, startY = 3, length = 1, direction = Direction.LEFT)
        val bottomEdgeDown = Arrow(id = 3, startX = 1, startY = 5, length = 1, direction = Direction.DOWN)
        val rightEdgeRight = Arrow(id = 4, startX = 5, startY = 2, length = 1, direction = Direction.RIGHT)

        val edgeArrows = listOf(topEdgeUp, leftEdgeLeft, bottomEdgeDown, rightEdgeRight)

        for (arrow in edgeArrows) {
            assertTrue("Outward-facing edge arrow ${arrow.id} should be free",
                PuzzleSolver.isArrowUnobstructed(arrow, edgeArrows, 6, 6))
        }

        // Inward facing edge arrow blocked by center arrow
        val leftEdgeRight = Arrow(id = 5, startX = 0, startY = 3, length = 1, direction = Direction.RIGHT)
        val centerArrow = Arrow(id = 6, startX = 3, startY = 3, length = 1, direction = Direction.UP)
        val inwardSet = listOf(leftEdgeRight, centerArrow)

        assertFalse("Inward edge arrow pointing RIGHT should be blocked by center arrow",
            PuzzleSolver.isArrowUnobstructed(leftEdgeRight, inwardSet, 6, 6))
    }

    @Test
    fun testBlockedArrowDetection() {
        // Arrow 1 at (1,2) pointing UP (ray goes through 1,1 and 1,0)
        val arrow1 = Arrow(id = 1, startX = 1, startY = 2, length = 1, direction = Direction.UP)
        // Arrow 2 blocking arrow 1 at (1,1)
        val arrow2 = Arrow(id = 2, startX = 1, startY = 1, length = 1, direction = Direction.RIGHT)

        val activeArrows = listOf(arrow1, arrow2)

        val isArrow1Free = PuzzleSolver.isArrowUnobstructed(arrow1, activeArrows, 4, 4)
        assertFalse("Arrow 1 should be blocked by Arrow 2", isArrow1Free)

        val isArrow2Free = PuzzleSolver.isArrowUnobstructed(arrow2, activeArrows, 4, 4)
        assertTrue("Arrow 2 pointing RIGHT at (1,1) should be unobstructed", isArrow2Free)
    }

    @Test
    fun testMultiCellArrowOccupancyAndCollision() {
        // Arrow of length 2 starting at (1,1) pointing RIGHT => occupies (1,1) and (2,1). Tip is at (2,1).
        val longArrow = Arrow(id = 1, startX = 1, startY = 1, length = 2, direction = Direction.RIGHT)
        val occupied = longArrow.getOccupiedCells()

        assertEquals("Multi-cell arrow should occupy 2 points", 2, occupied.size)
        assertTrue("Occupied should contain (1,1)", occupied.contains(GridPoint(1, 1)))
        assertTrue("Occupied should contain (2,1)", occupied.contains(GridPoint(2, 1)))
        assertEquals("Tip cell should be at (2,1)", GridPoint(2, 1), longArrow.getTipCell())

        // Arrow 2 at (4,1) pointing UP (blocking the exit ray of longArrow which extends through (3,1), (4,1)...)
        val blocker = Arrow(id = 2, startX = 4, startY = 1, length = 1, direction = Direction.UP)
        val activeSet = listOf(longArrow, blocker)

        assertFalse("Long arrow should be blocked by arrow at (4,1)",
            PuzzleSolver.isArrowUnobstructed(longArrow, activeSet, 6, 6))
    }

    @Test
    fun testLongPathAcrossLargeGrid() {
        // 8x8 grid: Arrow at (0, 4) pointing RIGHT, blocked at far end (7, 4)
        val startArrow = Arrow(id = 1, startX = 0, startY = 4, length = 1, direction = Direction.RIGHT)
        val farBlocker = Arrow(id = 2, startX = 7, startY = 4, length = 1, direction = Direction.DOWN)

        val activeArrows = listOf(startArrow, farBlocker)
        assertFalse("Long path arrow should be blocked by far-end arrow",
            PuzzleSolver.isArrowUnobstructed(startArrow, activeArrows, 8, 8))

        // When farBlocker escapes, startArrow should become free
        val remaining = listOf(startArrow)
        assertTrue("Long path arrow should be free after far blocker escapes",
            PuzzleSolver.isArrowUnobstructed(startArrow, remaining, 8, 8))
    }

    @Test
    fun testComplexChainedBlockingArrangement() {
        // Chain: A -> B -> C -> D
        // Arrow A at (1,3) UP (blocked by B at 1,2)
        // Arrow B at (1,2) RIGHT (blocked by C at 3,2)
        // Arrow C at (3,2) DOWN (blocked by D at 3,4)
        // Arrow D at (3,4) RIGHT (free!)
        val arrowA = Arrow(id = 1, startX = 1, startY = 3, length = 1, direction = Direction.UP)
        val arrowB = Arrow(id = 2, startX = 1, startY = 2, length = 1, direction = Direction.RIGHT)
        val arrowC = Arrow(id = 3, startX = 3, startY = 2, length = 1, direction = Direction.DOWN)
        val arrowD = Arrow(id = 4, startX = 3, startY = 4, length = 1, direction = Direction.RIGHT)

        val chain = listOf(arrowA, arrowB, arrowC, arrowD)

        // Only D should be unobstructed initially
        assertFalse(PuzzleSolver.isArrowUnobstructed(arrowA, chain, 5, 5))
        assertFalse(PuzzleSolver.isArrowUnobstructed(arrowB, chain, 5, 5))
        assertFalse(PuzzleSolver.isArrowUnobstructed(arrowC, chain, 5, 5))
        assertTrue(PuzzleSolver.isArrowUnobstructed(arrowD, chain, 5, 5))

        // Solve should produce exact order [4, 3, 2, 1]
        val solution = PuzzleSolver.solvePuzzle(chain, 5, 5)
        assertEquals("Solver should unblock chain in exact sequence [4, 3, 2, 1]", listOf(4, 3, 2, 1), solution)
    }

    @Test
    fun testPuzzleSolverAndHint() {
        val arrow1 = Arrow(id = 1, startX = 1, startY = 2, length = 1, direction = Direction.UP)
        val arrow2 = Arrow(id = 2, startX = 1, startY = 1, length = 1, direction = Direction.RIGHT)
        val activeArrows = listOf(arrow1, arrow2)

        val solution = PuzzleSolver.solvePuzzle(activeArrows, 4, 4)
        assertEquals("Solution sequence should first clear Arrow 2 then Arrow 1", listOf(2, 1), solution)

        val hintId = PuzzleSolver.getHintArrowId(activeArrows, 4, 4)
        assertEquals("Hint should point to Arrow 2", 2, hintId)
    }

    @Test
    fun testLevelGeneratorSolvability() {
        val testLevels = listOf(1, 5, 10, 25, 50, 75, 100, 150, 200, 300, 400, 500)
        for (levelId in testLevels) {
            val level = LevelGenerator.getLevel(levelId)
            assertNotNull("Level $levelId should not be null", level)
            val solution = PuzzleSolver.solvePuzzle(level.arrows, level.gridWidth, level.gridHeight)
            println("Level $levelId: Grid=${level.gridWidth}x${level.gridHeight}, Arrows=${level.arrows.size}, SolvedSteps=${solution.size}")
            assertTrue("Level $levelId must be solvable (Solution size: ${solution.size}, Arrows: ${level.arrows.size})",
                solution.isNotEmpty() && solution.size == level.arrows.size)
        }
    }
}
