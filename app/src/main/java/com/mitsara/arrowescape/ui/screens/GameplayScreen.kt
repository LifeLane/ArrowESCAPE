package com.mitsara.arrowescape.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.platform.testTag
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import com.mitsara.arrowescape.engine.LevelTextEngine
import com.mitsara.arrowescape.model.GamePlayState
import com.mitsara.arrowescape.model.GameTheme
import com.mitsara.arrowescape.model.ThemeManager
import com.mitsara.arrowescape.monetization.AdsManager
import com.mitsara.arrowescape.ui.components.GameBottomBar

import com.mitsara.arrowescape.ui.components.GameTopBar
import com.mitsara.arrowescape.ui.components.LevelCompleteDialog
import com.mitsara.arrowescape.ui.components.LevelFailedDialog
import com.mitsara.arrowescape.ui.components.PuzzleBoardView
import com.mitsara.arrowescape.ui.theme.SurfaceLight
import com.mitsara.arrowescape.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay

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
    val activeTheme = ThemeManager.getTheme(userSettings.selectedTheme)

    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) {
            AdsManager.incrementLevelCompleted()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .background(activeTheme.boardCanvasColor)
    ) {
        // Procedural Immersive Background Engine
        com.mitsara.arrowescape.engine.graphics.BackgroundEngine(
            theme = activeTheme,
            flowState = state.flowState
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Always Visible Single-Row Top Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(activeTheme.surfaceBackgroundColor.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("back_button").size(36.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = activeTheme.textPrimaryColor, modifier = Modifier.size(20.dp))
                    }
                    IconButton(
                        onClick = {
                            val allThemes = ThemeManager.allThemes
                            val currentIdx = allThemes.indexOfFirst { it.id == activeTheme.id }
                            val nextTheme = allThemes[(currentIdx + 1) % allThemes.size]
                            viewModel.selectTheme(nextTheme.id)
                        },
                        modifier = Modifier.testTag("theme_toggle_button").size(36.dp)
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = "Toggle Theme", tint = activeTheme.arrowHighlightColor, modifier = Modifier.size(20.dp))
                    }
                    // Powerup Button (Fire/Crusher Arrow)
                    if (state.powerupCharges > 0 || state.isPowerupActive) {
                        Button(
                            onClick = { viewModel.togglePowerup() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.isPowerupActive) Color(0xFFFF3366) else activeTheme.arrowHighlightColor
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp).testTag("powerup_button")
                        ) {
                            Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("x${state.powerupCharges}", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "LVL ${state.level.id}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = activeTheme.textPrimaryColor
                    )
                    Text(
                        text = "${state.activeArrows.size} LEFT",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                        color = activeTheme.arrowHighlightColor
                    )
                }

                IconButton(onClick = onSettingsClick, modifier = Modifier.testTag("settings_button").size(36.dp)) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = activeTheme.textPrimaryColor, modifier = Modifier.size(20.dp))
                }
            }

            // Game Board filling entire central area as grids
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
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
                    obstacles = state.level.obstacles,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Always Visible Single-Row Bottom Footer Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(activeTheme.surfaceBackgroundColor.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.undoMove() },
                    enabled = state.canUndo,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = activeTheme.textPrimaryColor, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Undo", color = activeTheme.textPrimaryColor, fontSize = 11.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, contentDescription = "Lives", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "x${state.remainingLives}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = activeTheme.textPrimaryColor)
                }

                Button(
                    onClick = { viewModel.requestHint() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = activeTheme.arrowHighlightColor),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Hint (${state.hintsAvailable})", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.retryLevel() },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Text("Retry", color = activeTheme.textPrimaryColor, fontSize = 11.sp)
                }
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

