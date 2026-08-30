package com.mitsara.arrowescape

import com.mitsara.arrowescape.engine.EscapePathEngine
import com.mitsara.arrowescape.engine.LevelFingerprint
import com.mitsara.arrowescape.engine.LevelGenerator
import com.mitsara.arrowescape.engine.ObstacleFormationGenerator
import com.mitsara.arrowescape.engine.PuzzleSolver
import com.mitsara.arrowescape.model.Arrow
import com.mitsara.arrowescape.model.Difficulty
import com.mitsara.arrowescape.model.Direction
import com.mitsara.arrowescape.model.GridPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ArrowEscapeUnitTest {

    @Test
    fun testEscapePathEngineDurationCalculation() {
        val shortArrow = Arrow(id = 1, startX = 0, startY = 0, length = 1, direction = Direction.UP)
        val shortDuration = EscapePathEngine.calculateEscapeDurationMs(shortArrow, 8, 8)

        val longArrow = Arrow(id = 2, startX = 0, startY = 7, length = 1, direction = Direction.UP)
        val longDuration = EscapePathEngine.calculateEscapeDurationMs(longArrow, 8, 8)

        assertTrue("Short path duration should be smaller than long path duration", shortDuration < longDuration)
        assertTrue("Duration should be >= 320ms", shortDuration >= 320)
        assertTrue("Duration should be <= 750ms", longDuration <= 750)
    }

    @Test
    fun testEscapePathEngineLShapedRouteAndCornerFillet() {
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
        val waypoints = EscapePathEngine.buildEscapeWaypoints(bentArrow, 8, 8, cellWidth, cellHeight)

        assertTrue("Waypoints should have at least 3 key vertices", waypoints.size >= 3)

        val path = EscapePathEngine.ParameterizedPath(
            keyVertices = waypoints,
            cornerRadiusPx = 40f,
            boardBoundsSize = 800f
        )

        assertTrue("Path total length should be positive", path.totalLength > 500f)

        val sampleStart = path.sampleAt(0f)
        assertEquals("Initial segment moving up should have 0° angle", 0f, sampleStart.angleDegrees, 1.0f)

        val sampleMidCorner = path.sampleAt(200f)
        assertTrue("Mid corner angle should be between 20° and 70° (actual: ${sampleMidCorner.angleDegrees})",
            sampleMidCorner.angleDegrees in 20f..70f)
    }

    @Test
    fun testUnobstructedArrowDetection() {
        val freeArrow = Arrow(id = 1, startX = 0, startY = 0, length = 1, direction = Direction.UP)
        val activeArrows = listOf(freeArrow)

        val isFree = PuzzleSolver.isArrowUnobstructed(freeArrow, activeArrows, 8, 8)
        assertTrue("Arrow pointing UP at (0,0) should be unobstructed", isFree)
    }

    @Test
    fun testComplexChainedBlockingArrangement() {
        // Chain: A -> B -> C -> D
        val arrowA = Arrow(id = 1, startX = 1, startY = 3, length = 1, direction = Direction.UP)
        val arrowB = Arrow(id = 2, startX = 1, startY = 2, length = 1, direction = Direction.RIGHT)
        val arrowC = Arrow(id = 3, startX = 3, startY = 2, length = 1, direction = Direction.DOWN)
        val arrowD = Arrow(id = 4, startX = 3, startY = 4, length = 1, direction = Direction.RIGHT)

        val chain = listOf(arrowA, arrowB, arrowC, arrowD)

        assertFalse(PuzzleSolver.isArrowUnobstructed(arrowA, chain, 8, 8))
        assertFalse(PuzzleSolver.isArrowUnobstructed(arrowB, chain, 8, 8))
        assertFalse(PuzzleSolver.isArrowUnobstructed(arrowC, chain, 8, 8))
        assertTrue(PuzzleSolver.isArrowUnobstructed(arrowD, chain, 8, 8))

        val analysis = PuzzleSolver.analyzePuzzle(chain, 8, 8)
        assertTrue("Chain puzzle must be solvable", analysis.isSolvable)
        assertEquals("Dependency depth must be 4", 4, analysis.dependencyDepth)
        assertEquals(listOf(4, 3, 2, 1), analysis.solutionSequence)
    }

    @Test
    fun testOnlyHardHarderHardcoreDifficulties() {
        for (levelId in listOf(1, 10, 50, 60, 61, 100, 150, 180, 181, 250, 350, 500)) {
            val level = LevelGenerator.getLevel(levelId)
            assertTrue(
                "Level $levelId difficulty must be HARD, HARDER, or HARDCORE (was ${level.difficulty})",
                level.difficulty in listOf(Difficulty.HARD, Difficulty.HARDER, Difficulty.HARDCORE)
            )
        }
    }

    @Test
    fun testGridSizeProgression() {
        val hardLevel = LevelGenerator.getLevel(1)
        assertTrue("Hard level 1 grid size should be >= 8x8 (actual ${hardLevel.gridWidth})", hardLevel.gridWidth >= 8)

        val harderLevel = LevelGenerator.getLevel(100)
        assertTrue("Harder level 100 grid size should be >= 10x10 (actual ${harderLevel.gridWidth})", harderLevel.gridWidth >= 10)

        val hardcoreLevel = LevelGenerator.getLevel(450)
        assertTrue("Hardcore level 450 grid size should be >= 12x12 (actual ${hardcoreLevel.gridWidth})", hardcoreLevel.gridWidth >= 12)
    }

    @Test
    fun testSmartHintPrioritization() {
        // Two free arrows: Arrow 1 unblocks another arrow, Arrow 2 unblocks nothing
        val arrow1 = Arrow(id = 1, startX = 2, startY = 1, length = 1, direction = Direction.UP)
        val blockedBy1 = Arrow(id = 3, startX = 2, startY = 3, length = 1, direction = Direction.UP)
        val arrow2 = Arrow(id = 2, startX = 5, startY = 0, length = 1, direction = Direction.UP)

        val arrows = listOf(arrow1, arrow2, blockedBy1)
        val hintId = PuzzleSolver.getHintArrowId(arrows, 8, 8)
        assertNotNull("Hint must not be null", hintId)
        assertTrue("Hint should prioritize free arrows", hintId == 1 || hintId == 2)
    }

    @Test
    fun testObstacleFormationDiversity() {
        val validCells = mutableSetOf<GridPoint>()
        for (x in 0 until 12) {
            for (y in 0 until 12) {
                validCells.add(GridPoint(x, y))
            }
        }
        for (formation in ObstacleFormationGenerator.FormationType.entries) {
            val obs = ObstacleFormationGenerator.generateObstacles(
                gridSize = 12,
                targetObstacleCount = 10,
                formationType = formation,
                random = Random(42),
                validCells = validCells
            )
            assertTrue("Formation $formation should generate obstacles", obs.isNotEmpty())
        }
    }

    @Test
    fun testLevelSolvabilityAcrossLargeSample() {
        val sampleLevels = (1..60).toList() + (100..130).toList() + listOf(200, 250, 300, 350, 400, 450, 500)
        for (levelId in sampleLevels) {
            val level = LevelGenerator.getLevel(levelId)
            assertNotNull("Level $levelId must exist", level)
            val analysis = PuzzleSolver.analyzePuzzle(
                level.arrows,
                level.gridWidth,
                level.gridHeight,
                level.obstacles,
                level.validCells
            )
            assertTrue(
                "Level $levelId (Grid ${level.gridWidth}x${level.gridHeight}, Arrows: ${level.arrows.size}, Diff: ${level.difficulty}) MUST be 100% solvable",
                analysis.isSolvable && analysis.solutionSequence.size == level.arrows.size
            )
            assertTrue(
                "Level $levelId must have meaningful dependency depth >= 2 (actual: ${analysis.dependencyDepth})",
                analysis.dependencyDepth >= 2
            )
        }
    }
}
