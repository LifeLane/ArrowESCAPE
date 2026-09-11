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
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mitsara.arrowescape.data.UserSettingsEntity
import com.mitsara.arrowescape.engine.ArtisticSnakeLevelGenerator
import com.mitsara.arrowescape.engine.EscapePathEngine
import com.mitsara.arrowescape.engine.PuzzleSolver
import com.mitsara.arrowescape.model.Arrow
import com.mitsara.arrowescape.model.Difficulty
import com.mitsara.arrowescape.model.Direction
import com.mitsara.arrowescape.model.PuzzleLevel
import com.mitsara.arrowescape.model.ThemeManager
import com.mitsara.arrowescape.ui.components.PuzzleBoardView
import com.mitsara.arrowescape.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun ChronoRushScreen(
    viewModel: GameViewModel,
    userSettings: UserSettingsEntity,
    onBackClick: () -> Unit
) {
    var timeLeftSeconds by remember { mutableIntStateOf(60) }
    var isRunning by remember { mutableStateOf(true) }
    var isGameOver by remember { mutableStateOf(false) }

    var rushScore by remember { mutableIntStateOf(0) }
    var comboCount by remember { mutableIntStateOf(0) }
    var lastEscapeTime by remember { mutableStateOf(0L) }
    var stageClearCount by remember { mutableIntStateOf(0) }

    var stageSeed by remember { mutableStateOf(System.currentTimeMillis()) }
    var currentLevel by remember(stageSeed) {
        mutableStateOf(
            ArtisticSnakeLevelGenerator.generateSilhouetteLevel(
                levelNumber = (1..100).random(Random(stageSeed)),
                gridSize = 7,
                targetArrowCount = 8 + (stageClearCount * 2).coerceAtMost(14),
                targetDifficulty = Difficulty.HARD,
                seed = stageSeed
            )
        )
    }

    var activeArrows by remember(currentLevel) { mutableStateOf(currentLevel.arrows) }
    var animatingArrowId by remember { mutableStateOf<Int?>(null) }
    var isMistakeShake by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Multiplier calculation (1x, 2x, 3x, 5x, 8x, 10x Frenzy)
    val multiplier = when {
        comboCount >= 15 -> 10
        comboCount >= 10 -> 8
        comboCount >= 6 -> 5
        comboCount >= 3 -> 3
        comboCount >= 1 -> 2
        else -> 1
    }

    val isFrenzy = multiplier >= 8

    // 1-second countdown ticker
    LaunchedEffect(isRunning, isGameOver) {
        while (isRunning && !isGameOver) {
            delay(1000)
            if (timeLeftSeconds > 0) {
                timeLeftSeconds--
            } else {
                isGameOver = true
                isRunning = false

                // Calculate rewards
                val earnedCoins = rushScore * 3
                val earnedDiamonds = (rushScore / 20).coerceAtLeast(1)
                viewModel.saveChronoResult(rushScore, earnedCoins, earnedDiamonds)
                viewModel.soundManager.playVictorySound()
            }
        }
    }

    fun handleArrowClick(arrowId: Int) {
        if (isGameOver) return
        val arrow = activeArrows.find { it.id == arrowId } ?: return
        val isClear = PuzzleSolver.isArrowUnobstructed(
            arrow = arrow,
            activeArrows = activeArrows,
            gridWidth = currentLevel.gridWidth,
            gridHeight = currentLevel.gridHeight,
            obstacles = emptySet()
        )

        val now = System.currentTimeMillis()

        if (isClear) {
            animatingArrowId = arrow.id

            // Combo window: 2.2 seconds
            if (now - lastEscapeTime < 2200) {
                comboCount++
            } else {
                comboCount = 1
            }
            lastEscapeTime = now

            if (isFrenzy) {
                viewModel.soundManager.playFrenzySweep()
            } else {
                viewModel.soundManager.playEscapeSound("CYBER_TERMINAL")
            }

            // Score addition and time replenish (+2 seconds)
            rushScore += 10 * multiplier
            timeLeftSeconds = (timeLeftSeconds + 2).coerceAtMost(99)

            val animDuration = EscapePathEngine.calculateEscapeDurationMs(arrow, currentLevel.gridWidth, currentLevel.gridHeight)

            coroutineScope.launch {
                delay(animDuration.toLong())
                activeArrows = activeArrows.filter { it.id != arrowId }
                animatingArrowId = null

                // If board cleared, award massive bonus and spawn next rush stage
                if (activeArrows.isEmpty()) {
                    rushScore += 100 * multiplier
                    timeLeftSeconds = (timeLeftSeconds + 8).coerceAtMost(99)
                    stageClearCount++
                    viewModel.soundManager.playVictorySound()
                    delay(200)
                    stageSeed = System.currentTimeMillis()
                }
            }
        } else {
            // Mistake penalty: loses combo and drops 2 seconds
            isMistakeShake = true
            comboCount = 0
            timeLeftSeconds = maxOf(0, timeLeftSeconds - 2)
            viewModel.soundManager.playMistakeSound()
            coroutineScope.launch {
                delay(250)
                isMistakeShake = false
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ChronoFrenzy")
    val frenzyGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "frenzyGlow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        if (isFrenzy) Color(0xFFE11D48).copy(alpha = frenzyGlow * 0.4f) else Color(0xFF1E1B4B),
                        Color(0xFF030712)
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
            // Header Bar
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
                        .testTag("chrono_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Digital Glowing Timer Ring
                Surface(
                    color = if (timeLeftSeconds <= 10) Color(0xFFEF4444) else Color(0xFF1E293B),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (timeLeftSeconds <= 10) Color(0xFFFCA5A5) else Color(0xFF38BDF8)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${timeLeftSeconds}s",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White
                        )
                    }
                }

                // Multiplier Pill
                Surface(
                    color = if (isFrenzy) Color(0xFFFF0055) else Color(0xFF2563EB),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color(0xFFFFE600),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${multiplier}x",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                            color = Color.White
                        )
                    }
                }
            }

            // Score Banner & High Score
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "CURRENT SCORE", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                    Text(
                        text = "$rushScore",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "PERSONAL BEST", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                    Text(
                        text = "${userSettings.chronoHighScore}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFFFD700)
                    )
                }
            }

            // Puzzle Arena
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
                    hintArrowId = null,
                    isMistakeShake = isMistakeShake,
                    onArrowClick = { handleArrowClick(it) },
                    theme = ThemeManager.RETRO_ARCADE,
                    selectedArrowId = userSettings.selectedArrow,
                    selectedBoardId = userSettings.selectedBoard,
                    selectedGridId = userSettings.selectedGrid,
                    selectedFrameId = userSettings.selectedFrame
                )
            }

            // Bottom Streak / Stages Cleared bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0F172A).copy(alpha = 0.8f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Combo Streak: $comboCount",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isFrenzy) Color(0xFFFF0055) else Color(0xFF38BDF8)
                    )
                    Text(
                        text = "Stages Wiped: $stageClearCount",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF10B981)
                    )
                }
            }
        }
    }

    // Game Over Results Dialog
    if (isGameOver) {
        val earnedCoins = rushScore * 3
        val earnedDiamonds = (rushScore / 20).coerceAtLeast(1)
        val isNewRecord = rushScore > userSettings.chronoHighScore

        Dialog(onDismissRequest = { /* Modal */ }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(2.dp, if (isNewRecord) Color(0xFFFFD700) else Color(0xFF38BDF8)),
                shadowElevation = 20.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isNewRecord) "🎉 NEW HIGH SCORE!" else "⏱️ TIME'S UP!",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = if (isNewRecord) Color(0xFFFFD700) else Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "$rushScore PTS",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 36.sp
                        ),
                        color = Color(0xFF38BDF8)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Rewards Summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                text = "🪙 +$earnedCoins Coins",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFFFD700),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                text = "💎 +$earnedDiamonds Gems",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF00E5FF),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onBackClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("Main Menu", style = MaterialTheme.typography.labelLarge)
                        }

                        Button(
                            onClick = {
                                rushScore = 0
                                comboCount = 0
                                stageClearCount = 0
                                timeLeftSeconds = 60
                                isGameOver = false
                                isRunning = true
                                stageSeed = System.currentTimeMillis()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("Play Again", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}
