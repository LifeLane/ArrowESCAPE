package com.mitsara.arrowescape.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
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

    LaunchedEffect(levelId) {
        if (gameState?.level?.id != levelId) {
            viewModel.startLevel(levelId)
        }
    }

    val state = gameState ?: return
    val context = LocalContext.current
    val activeTheme = GameTheme.fromId(userSettings.selectedTheme)

    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) {
            AdsManager.incrementLevelCompleted()
        }
    }

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
                PuzzleBoardView(
                    gridWidth = state.level.gridWidth,
                    gridHeight = state.level.gridHeight,
                    activeArrows = state.activeArrows,
                    animatingArrowId = state.animatingArrowId,
                    animatingDirection = state.animatingDirection,
                    hintArrowId = state.hintArrowId,
                    isMistakeShake = state.isMistakeShake,
                    onArrowClick = { arrowId -> viewModel.onArrowTapped(arrowId) },
                    theme = activeTheme
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
                levelNumber = state.level.id,
                stars = stars,
                moveCount = state.moveCount,
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
}
