package com.mitsara.arrowescape.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.mitsara.arrowescape.engine.LevelTextEngine
import com.mitsara.arrowescape.model.GamePlayState
import com.mitsara.arrowescape.model.GameTheme
import com.mitsara.arrowescape.monetization.AdsManager
import com.mitsara.arrowescape.ui.components.GameBottomBar

import com.mitsara.arrowescape.ui.components.GameTopBar
import com.mitsara.arrowescape.ui.components.LevelCompleteDialog
import com.mitsara.arrowescape.ui.components.LevelFailedDialog
import com.mitsara.arrowescape.ui.components.PuzzleBoardView
import com.mitsara.arrowescape.ui.theme.SurfaceLight
import com.mitsara.arrowescape.ui.viewmodel.GameViewModel

@Composable
fun GameplayScreen(
    levelId: Int,
    viewModel: GameViewModel,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onMainMenuClick: () -> Unit,
    onPremiumUpgradeClick: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()

    val wipeProgress = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(levelId) {
        if (gameState?.level?.id != levelId) {
            viewModel.startLevel(levelId)
        }
        wipeProgress.snapTo(0f)
        wipeProgress.animateTo(1f, animationSpec = androidx.compose.animation.core.tween(500))
    }

    val state = gameState ?: return
    val context = LocalContext.current
    val activeTheme = GameTheme.fromId(userSettings.selectedTheme)

    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) {
            AdsManager.incrementLevelCompleted()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = activeTheme.surfaceBackgroundColor,
            topBar = {
                GameTopBar(
                    levelNumber = state.level.id,
                    difficulty = state.level.difficulty,
                    remainingLives = state.remainingLives,
                    remainingArrowsCount = state.activeArrows.size,
                    onBackClick = onBackClick,
                    onSettingsClick = onSettingsClick
                )
            },
            bottomBar = {
                GameBottomBar(
                    hintsAvailable = state.hintsAvailable,
                    isPremium = userSettings.isPremium,
                    canUndo = state.canUndo,
                    onUndoClick = { viewModel.undoMove() },
                    onHintClick = { viewModel.requestHint() },
                    onRetryClick = { viewModel.retryLevel() },
                    onGetMoreHintsClick = { viewModel.addRewardHints(3) }
                )
            }
        ) { paddingValues ->
            // Procedural Background
            com.mitsara.arrowescape.engine.graphics.BackgroundEngine(
                theme = activeTheme,
                flowState = state.flowState
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // One-liner hook or milestone indicator
                    Text(
                        text = if (state.level.id % 10 == 0) "★ MILESTONE STAGE ★" else LevelTextEngine.getHookForLevel(state.level.id),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = if (state.level.id % 10 == 0) Color(0xFFF59E0B) else activeTheme.textPrimaryColor.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    com.mitsara.arrowescape.ui.components.DifficultyMeter(level = state.level)

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    PuzzleBoardView(
                        gridWidth = state.level.gridWidth,
                        gridHeight = state.level.gridHeight,
                        activeArrows = state.activeArrows,
                        animatingArrowId = state.animatingArrowId,
                        animatingDirection = state.animatingDirection,
                        hintArrowId = state.hintArrowId,
                        isMistakeShake = state.isMistakeShake,
                        inspectedArrowId = state.inspectedArrowId,
                        onArrowClick = { arrowId -> viewModel.onArrowTapped(arrowId) },
                        theme = activeTheme,
                        validCells = state.level.validCells,
                        obstacles = state.level.obstacles
                    )
                }
            }

            // Modals
            if (state.isCompleted) {
                val stars = when {
                    state.remainingLives >= 3 -> 3
                    state.remainingLives == 2 -> 2
                    else -> 1
                }
                LevelCompleteDialog(
                    level = state.level,
                    stars = stars,
                    moveCount = state.moveCount,
                    score = state.score,
                    elapsedSeconds = state.elapsedSeconds,
                    onNextLevel = {
                        AdsManager.showInterstitial(context, userSettings.isPremium) {
                            viewModel.nextLevel()
                        }
                    },
                    onReplay = { viewModel.retryLevel() },
                    onMainMenu = {
                        AdsManager.showInterstitial(context, userSettings.isPremium) {
                            onMainMenuClick()
                        }
                    }
                )
            }

            if (state.isFailed) {
                LevelFailedDialog(
                    levelNumber = state.level.id,
                    onRetry = { viewModel.retryLevel() },
                    onWatchAdForReward = {
                        AdsManager.showRewardedAd(
                            context = context,
                            isPremium = userSettings.isPremium,
                            onRewardGranted = { viewModel.addRewardHints(3) },
                            onAdFailed = {}
                        )
                    },
                    onMainMenu = onMainMenuClick
                )
            }
        }

        // Combo Banner Overlay
        if (state.activeComboMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 110.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                androidx.compose.material3.Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF59E0B),
                    shadowElevation = 8.dp
                ) {
                    Text(
                        text = state.activeComboMessage!!,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        ),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Screen Wipe Transition Overlay
        if (wipeProgress.value < 1f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.maxDimension * (1f - wipeProgress.value)
                drawCircle(
                    color = activeTheme.surfaceBackgroundColor,
                    radius = radius,
                    center = center
                )
            }
        }
    }
}

