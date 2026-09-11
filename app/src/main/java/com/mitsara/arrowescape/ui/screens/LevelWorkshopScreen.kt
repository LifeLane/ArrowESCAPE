package com.mitsara.arrowescape.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mitsara.arrowescape.data.UserSettingsEntity
import com.mitsara.arrowescape.engine.EscapePathEngine
import com.mitsara.arrowescape.engine.PuzzleSolver
import com.mitsara.arrowescape.model.Arrow
import com.mitsara.arrowescape.model.Difficulty
import com.mitsara.arrowescape.model.Direction
import com.mitsara.arrowescape.model.GridPoint
import com.mitsara.arrowescape.model.PuzzleLevel
import com.mitsara.arrowescape.model.ThemeManager
import com.mitsara.arrowescape.ui.components.PuzzleBoardView
import com.mitsara.arrowescape.ui.motion.AnimatedAtmosphericBackground
import com.mitsara.arrowescape.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay

enum class WorkshopTool {
    ARROW_UP, ARROW_RIGHT, ARROW_DOWN, ARROW_LEFT, OBSTACLE, ERASER
}

@Composable
fun LevelWorkshopScreen(
    viewModel: GameViewModel,
    userSettings: UserSettingsEntity,
    onBackClick: () -> Unit
) {
    var gridSize by remember { mutableIntStateOf(8) }
    var selectedTool by remember { mutableStateOf(WorkshopTool.ARROW_RIGHT) }
    var isTestPlaying by remember { mutableStateOf(false) }

    val customArrows = remember { mutableStateListOf<Arrow>() }
    val customObstacles = remember { mutableStateListOf<GridPoint>() }

    // Test Play State
    var testActiveArrows by remember { mutableStateOf<List<Arrow>>(emptyList()) }
    var testAnimatingArrowId by remember { mutableStateOf<Int?>(null) }
    var testIsMistakeShake by remember { mutableStateOf(false) }

    // Seed Import / Export dialog
    var showShareDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var shareSeedCode by remember { mutableStateOf("") }
    var importCodeText by remember { mutableStateOf("") }

    // PuzzleSolver Analysis
    val solverAnalysis = remember(customArrows.size, customObstacles.size, gridSize) {
        if (customArrows.isEmpty()) null
        else PuzzleSolver.analyzePuzzle(
            initialArrows = customArrows.toList(),
            gridWidth = gridSize,
            gridHeight = gridSize,
            obstacles = customObstacles.toSet()
        )
    }
    val isSolvable = solverAnalysis?.isSolvable == true
    val dependencyDepth = solverAnalysis?.dependencyDepth ?: 0
    val initialFreeCount = solverAnalysis?.initialFreeCount ?: 0

    fun startTestPlay() {
        if (customArrows.isEmpty()) return
        testActiveArrows = customArrows.toList()
        testAnimatingArrowId = null
        testIsMistakeShake = false
        isTestPlaying = true
        viewModel.soundManager.playTapSound()
    }

    fun handleTestArrowClick(arrowId: Int) {
        val arrow = testActiveArrows.find { it.id == arrowId } ?: return
        val isClear = PuzzleSolver.isArrowUnobstructed(
            arrow = arrow,
            activeArrows = testActiveArrows,
            gridWidth = gridSize,
            gridHeight = gridSize,
            obstacles = customObstacles.toSet()
        )

        if (isClear) {
            testAnimatingArrowId = arrow.id
            viewModel.soundManager.playEscapeSound("RETRO_ARCADE")

            val animDuration = EscapePathEngine.calculateEscapeDurationMs(arrow, gridSize, gridSize)
            coroutineScope.launch {
                delay(animDuration.toLong())
                testActiveArrows = testActiveArrows.filter { it.id != arrowId }
                testAnimatingArrowId = null
                if (testActiveArrows.isEmpty()) {
                    viewModel.soundManager.playVictorySound()
                }
            }
        } else {
            testIsMistakeShake = true
            viewModel.soundManager.playMistakeSound()
            coroutineScope.launch {
                delay(250)
                testIsMistakeShake = false
            }
        }
    }

    fun handleCellClick(gx: Int, gy: Int) {
        if (gx !in 0 until gridSize || gy !in 0 until gridSize) return

        // Remove any existing arrow or obstacle on cell
        val existingArrowIndex = customArrows.indexOfFirst { it.startX == gx && it.startY == gy }
        if (existingArrowIndex >= 0) {
            customArrows.removeAt(existingArrowIndex)
        }
        val existingObstacleIndex = customObstacles.indexOfFirst { it.x == gx && it.y == gy }
        if (existingObstacleIndex >= 0) {
            customObstacles.removeAt(existingObstacleIndex)
        }

        when (selectedTool) {
            WorkshopTool.ARROW_UP -> {
                customArrows.add(Arrow(id = customArrows.size + 1, startX = gx, startY = gy, length = 1, direction = Direction.UP))
                viewModel.soundManager.playTapSound()
            }
            WorkshopTool.ARROW_RIGHT -> {
                customArrows.add(Arrow(id = customArrows.size + 1, startX = gx, startY = gy, length = 1, direction = Direction.RIGHT))
                viewModel.soundManager.playTapSound()
            }
            WorkshopTool.ARROW_DOWN -> {
                customArrows.add(Arrow(id = customArrows.size + 1, startX = gx, startY = gy, length = 1, direction = Direction.DOWN))
                viewModel.soundManager.playTapSound()
            }
            WorkshopTool.ARROW_LEFT -> {
                customArrows.add(Arrow(id = customArrows.size + 1, startX = gx, startY = gy, length = 1, direction = Direction.LEFT))
                viewModel.soundManager.playTapSound()
            }
            WorkshopTool.OBSTACLE -> {
                customObstacles.add(GridPoint(gx, gy))
                viewModel.soundManager.playTapSound()
            }
            WorkshopTool.ERASER -> {
                viewModel.soundManager.playTapSound()
            }
        }
    }

    fun generateShareCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val seed = (1..6).map { chars.random() }.joinToString("")
        return "#$seed"
    }

    AnimatedAtmosphericBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B).copy(alpha = 0.8f))
                        .testTag("workshop_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = if (isTestPlaying) "TEST PLAYING" else "LEVEL WORKSHOP",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = {
                            shareSeedCode = generateShareCode()
                            showShareDialog = true
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .testTag("workshop_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            customArrows.clear()
                            customObstacles.clear()
                            viewModel.soundManager.playTapSound()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .testTag("workshop_clear_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Real-Time Validation Status Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSolvable) Color(0xFF10B981) else Color(0xFFEF4444).copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isSolvable) Icons.Default.CheckCircle else Icons.Default.Block,
                            contentDescription = null,
                            tint = if (isSolvable) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSolvable) "100% Solvable" else "Deadlock / Incomplete",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSolvable) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }

                    Text(
                        text = "Depth: $dependencyDepth • Free: $initialFreeCount",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFFCBD5E1)
                    )
                }
            }

            // Interactive Editor or Test Play Board
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isTestPlaying) {
                    PuzzleBoardView(
                        gridWidth = gridSize,
                        gridHeight = gridSize,
                        activeArrows = testActiveArrows,
                        animatingArrowId = testAnimatingArrowId,
                        animatingDirection = null,
                        hintArrowId = null,
                        isMistakeShake = testIsMistakeShake,
                        onArrowClick = { handleTestArrowClick(it) },
                        theme = ThemeManager.RETRO_ARCADE,
                        selectedArrowId = userSettings.selectedArrow,
                        selectedBoardId = userSettings.selectedBoard,
                        selectedGridId = userSettings.selectedGrid,
                        selectedFrameId = userSettings.selectedFrame
                    )
                } else {
                    // Visual Interactive Grid Painter
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.5.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                            .pointerInput(gridSize, selectedTool) {
                                detectTapGestures { offset ->
                                    val cellW = size.width / gridSize
                                    val cellH = size.height / gridSize
                                    val gx = (offset.x / cellW).toInt().coerceIn(0, gridSize - 1)
                                    val gy = (offset.y / cellH).toInt().coerceIn(0, gridSize - 1)
                                    handleCellClick(gx, gy)
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cW = size.width / gridSize
                            val cH = size.height / gridSize

                            // Draw dot matrix grid
                            for (x in 0 until gridSize) {
                                for (y in 0 until gridSize) {
                                    drawCircle(
                                        color = Color(0xFF334155).copy(alpha = 0.6f),
                                        radius = 2.5f,
                                        center = Offset(x * cW + cW / 2, y * cH + cH / 2)
                                    )
                                }
                            }

                            // Draw Obstacles
                            customObstacles.forEach { obs ->
                                drawRoundRect(
                                    color = Color(0xFFEF4444).copy(alpha = 0.8f),
                                    topLeft = Offset(obs.x * cW + 4f, obs.y * cH + 4f),
                                    size = Size(cW - 8f, cH - 8f),
                                    cornerRadius = CornerRadius(6f, 6f)
                                )
                            }

                            // Draw Arrows
                            customArrows.forEach { arrow ->
                                val cx = arrow.startX * cW + cW / 2
                                val cy = arrow.startY * cH + cH / 2

                                drawCircle(
                                    color = Color(0xFF38BDF8),
                                    radius = cW * 0.35f,
                                    center = Offset(cx, cy)
                                )

                                // Tip Direction Line
                                val tipX = cx + arrow.direction.dx * (cW * 0.32f)
                                val tipY = cy + arrow.direction.dy * (cH * 0.32f)

                                drawLine(
                                    color = Color.White,
                                    start = Offset(cx, cy),
                                    end = Offset(tipX, tipY),
                                    strokeWidth = 4f
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Tools Palette or Exit Test Mode Button
            if (isTestPlaying) {
                Button(
                    onClick = { isTestPlaying = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("exit_test_play_button")
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Return to Editor", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Tool Palette Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WorkshopToolButton(
                            tool = WorkshopTool.ARROW_UP,
                            selected = selectedTool == WorkshopTool.ARROW_UP,
                            icon = Icons.Default.ArrowUpward,
                            onClick = { selectedTool = WorkshopTool.ARROW_UP }
                        )
                        WorkshopToolButton(
                            tool = WorkshopTool.ARROW_RIGHT,
                            selected = selectedTool == WorkshopTool.ARROW_RIGHT,
                            icon = Icons.Default.ArrowForward,
                            onClick = { selectedTool = WorkshopTool.ARROW_RIGHT }
                        )
                        WorkshopToolButton(
                            tool = WorkshopTool.ARROW_DOWN,
                            selected = selectedTool == WorkshopTool.ARROW_DOWN,
                            icon = Icons.Default.ArrowDownward,
                            onClick = { selectedTool = WorkshopTool.ARROW_DOWN }
                        )
                        WorkshopToolButton(
                            tool = WorkshopTool.ARROW_LEFT,
                            selected = selectedTool == WorkshopTool.ARROW_LEFT,
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            onClick = { selectedTool = WorkshopTool.ARROW_LEFT }
                        )
                        WorkshopToolButton(
                            tool = WorkshopTool.OBSTACLE,
                            selected = selectedTool == WorkshopTool.OBSTACLE,
                            icon = Icons.Default.Block,
                            onClick = { selectedTool = WorkshopTool.OBSTACLE }
                        )
                        WorkshopToolButton(
                            tool = WorkshopTool.ERASER,
                            selected = selectedTool == WorkshopTool.ERASER,
                            icon = Icons.Default.Clear,
                            onClick = { selectedTool = WorkshopTool.ERASER }
                        )
                    }

                    // Test Play Button
                    Button(
                        onClick = { startTestPlay() },
                        enabled = customArrows.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("test_play_button")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Test Play Puzzle (${customArrows.size} Arrows)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }

    // Share / Import Modal Dialog
    if (showShareDialog) {
        Dialog(onDismissRequest = { showShareDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF38BDF8)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "COMMUNITY WORKSHOP CODE",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = shareSeedCode,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 4.sp
                            ),
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                        )
                    }

                    Text(
                        text = "Share this 6-character code with friends to challenge them to your custom puzzle layout!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showShareDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@Composable
fun WorkshopToolButton(
    tool: WorkshopTool,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) Color(0xFF2563EB) else Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) Color(0xFF60A5FA) else Color(0xFF334155)
        ),
        modifier = Modifier.size(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = tool.name,
                tint = if (selected) Color.White else Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
