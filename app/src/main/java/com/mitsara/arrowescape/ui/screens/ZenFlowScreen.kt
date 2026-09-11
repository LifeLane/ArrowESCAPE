package com.mitsara.arrowescape.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitsara.arrowescape.data.UserSettingsEntity
import com.mitsara.arrowescape.engine.ArtisticSnakeLevelGenerator
import com.mitsara.arrowescape.engine.EscapePathEngine
import com.mitsara.arrowescape.engine.PuzzleSolver
import com.mitsara.arrowescape.model.Arrow
import com.mitsara.arrowescape.model.Difficulty
import com.mitsara.arrowescape.model.Direction
import com.mitsara.arrowescape.model.GameTheme
import com.mitsara.arrowescape.model.GridPoint
import com.mitsara.arrowescape.model.PuzzleLevel
import com.mitsara.arrowescape.model.ThemeManager
import com.mitsara.arrowescape.ui.components.PuzzleBoardView
import com.mitsara.arrowescape.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlin.random.Random

val FLOW_STATES = listOf(
    "1. Soft Breeze" to Color(0xFF38BDF8),
    "2. Rippling Waters" to Color(0xFF34D399),
    "3. Deep Focus" to Color(0xFFA78BFA),
    "4. Transcendence" to Color(0xFFF472B6)
)

@Composable
fun ZenFlowScreen(
    viewModel: GameViewModel,
    userSettings: UserSettingsEntity,
    onBackClick: () -> Unit
) {
    var zenSeed by remember { mutableStateOf(System.currentTimeMillis()) }
    var currentLevel by remember(zenSeed) {
        mutableStateOf(
            ArtisticSnakeLevelGenerator.generateSilhouetteLevel(
                levelNumber = (1..50).random(Random(zenSeed)),
                gridSize = 8,
                targetArrowCount = 14,
                targetDifficulty = Difficulty.HARD,
                seed = zenSeed
            )
        )
    }

    var activeArrows by remember(currentLevel) { mutableStateOf(currentLevel.arrows) }
    var animatingArrowId by remember { mutableStateOf<Int?>(null) }
    var hintArrowId by remember { mutableStateOf<Int?>(null) }
    var isMistakeShake by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var zenEscapeCount by remember { mutableIntStateOf(0) }
    var flowStreak by remember { mutableIntStateOf(0) }
    val moveHistory = remember { mutableStateListOf<List<Arrow>>() }

    val flowTier = (flowStreak / 4).coerceIn(0, 3)
    val (flowTitle, flowColor) = FLOW_STATES[flowTier]

    // Infinite breathing atmosphere
    val infiniteTransition = rememberInfiniteTransition(label = "ZenBreathe")
    val breatheAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breatheAlpha"
    )

    fun handleArrowClick(arrowId: Int) {
        val arrow = activeArrows.find { it.id == arrowId } ?: return
        val isClear = PuzzleSolver.isArrowUnobstructed(
            arrow = arrow,
            activeArrows = activeArrows,
            gridWidth = currentLevel.gridWidth,
            gridHeight = currentLevel.gridHeight,
            obstacles = emptySet()
        )

        if (isClear) {
            moveHistory.add(activeArrows.toList())
            animatingArrowId = arrow.id

            // Play Harmonic Pentatonic Chime
            viewModel.soundManager.playPentatonicChime(zenEscapeCount)

            zenEscapeCount++
            flowStreak++
            viewModel.addZenEscapes(1)

            val animDuration = EscapePathEngine.calculateEscapeDurationMs(arrow, currentLevel.gridWidth, currentLevel.gridHeight)

            // Remove arrow after animation
            coroutineScope.launch {
                delay(animDuration.toLong())
                activeArrows = activeArrows.filter { it.id != arrowId }
                animatingArrowId = null

                // If puzzle cleared, seamlessly spawn next harmonious cluster
                if (activeArrows.isEmpty()) {
                    delay(300)
                    zenSeed = System.currentTimeMillis()
                }
            }
        } else {
            // Gentle non-punitive feedback
            isMistakeShake = true
            viewModel.soundManager.playTapSound("ZEN_WOOD")
            flowStreak = maxOf(0, flowStreak - 1)
            coroutineScope.launch {
                delay(250)
                isMistakeShake = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        flowColor.copy(alpha = breatheAlpha * 0.4f),
                        Color(0xFF090D16)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top HUD
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
                        .testTag("zen_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.85f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, flowColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = null,
                            tint = flowColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = flowTitle,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                IconButton(
                    onClick = {
                        zenSeed = System.currentTimeMillis()
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B).copy(alpha = 0.8f))
                        .testTag("zen_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "New Garden",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Flow Stats Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Total Escapes", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                    Text(
                        text = "$zenEscapeCount",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Flow Streak", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                    Text(
                        text = "$flowStreak",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = flowColor
                    )
                }
            }

            // Infinite Canvas Puzzle Board
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                PuzzleBoardView(
                    gridWidth = currentLevel.gridWidth,
                    gridHeight = currentLevel.gridHeight,
                    activeArrows = activeArrows,
                    animatingArrowId = animatingArrowId,
                    animatingDirection = null,
                    hintArrowId = hintArrowId,
                    isMistakeShake = isMistakeShake,
                    onArrowClick = { handleArrowClick(it) },
                    theme = ThemeManager.EYE_COMFORT,
                    selectedArrowId = userSettings.selectedArrow,
                    selectedBoardId = userSettings.selectedBoard,
                    selectedGridId = userSettings.selectedGrid,
                    selectedFrameId = userSettings.selectedFrame
                )
            }

            // Bottom Controls (Infinite Undo & Peaceful Hint)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = {
                        if (moveHistory.isNotEmpty()) {
                            activeArrows = moveHistory.removeLast()
                            flowStreak = maxOf(0, flowStreak - 1)
                            viewModel.soundManager.playTapSound("ZEN_WOOD")
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.testTag("zen_undo_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Infinite Undo",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
