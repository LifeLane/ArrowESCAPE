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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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

import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.border

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
        // Procedural Cosmetic Animated Background Canvas
        com.mitsara.arrowescape.ui.components.CosmeticBackgroundCanvas(
            backgroundId = userSettings.selectedBackground
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Always Visible Top Header Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(activeTheme.surfaceBackgroundColor.copy(alpha = 0.95f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBackClick, modifier = Modifier.testTag("back_button").size(40.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = activeTheme.textPrimaryColor, modifier = Modifier.size(24.dp))
                        }
                        IconButton(
                            onClick = {
                                val allThemes = ThemeManager.allThemes
                                val currentIdx = allThemes.indexOfFirst { it.id == activeTheme.id }
                                val nextTheme = allThemes[(currentIdx + 1) % allThemes.size]
                                viewModel.selectTheme(nextTheme.id)
                            },
                            modifier = Modifier.testTag("theme_toggle_button").size(40.dp)
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = "Toggle Theme", tint = activeTheme.arrowHighlightColor, modifier = Modifier.size(22.dp))
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Level ${state.level.id}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 0.5.sp),
                            color = activeTheme.textPrimaryColor
                        )
                        if (state.level.title.isNotBlank() && !state.level.title.startsWith("Level")) {
                            Text(
                                text = state.level.title.uppercase(),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                color = activeTheme.arrowHighlightColor
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Currency Pill (Coins & Diamonds)
                        Row(
                            modifier = Modifier
                                .background(Color(0xFF0F172A).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🪙", fontSize = 12.sp)
                            Text(
                                text = "${userSettings.coins}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                color = Color.White
                            )
                        }
                        IconButton(onClick = onSettingsClick, modifier = Modifier.testTag("settings_button").size(36.dp)) {
                            Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = activeTheme.textPrimaryColor, modifier = Modifier.size(22.dp))
                        }
                    }
                }

                // Smooth Animated Lives / Hearts & Combo Progress Indicator
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 4.dp, end = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val maxLives = state.level.startingLives.coerceAtLeast(3)
                        for (i in 1..maxLives) {
                            val isAlive = i <= state.remainingLives
                            Icon(
                                imageVector = if (isAlive) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Life $i",
                                tint = if (isAlive) Color(0xFFEF4444) else Color.Gray.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(horizontal = 1.dp)
                            )
                        }
                        if (state.activeShieldTaps > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0EA5E9).copy(alpha = 0.25f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("${state.activeShieldTaps} Shield", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Combo Multiplier Badge
                    if (state.comboMultiplier > 1) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF59E0B),
                            shadowElevation = 2.dp
                        ) {
                            Text(
                                text = "${state.comboMultiplier}x COMBO",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 10.sp),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "${state.activeArrows.size} left",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = activeTheme.textPrimaryColor.copy(alpha = 0.85f)
                    )
                }
            }

            // Game Board with Floating Action Buttons (Grid toggle & Hint bulb)
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
                    selectedArrowId = userSettings.selectedArrow,
                    selectedBoardId = userSettings.selectedBoard,
                    selectedGridId = userSettings.selectedGrid,
                    selectedFrameId = userSettings.selectedFrame,
                    modifier = Modifier.fillMaxSize()
                )

                // Floating Left Action: Grid / Dot Matrix Toggle
                IconButton(
                    onClick = {
                        val currentGrid = userSettings.selectedGrid
                        val newGrid = if (currentGrid == "GRID_DOT_MATRIX" || currentGrid == "GRID_MINIMAL_CLEAN") "GRID_NEON_LATTICE" else "GRID_DOT_MATRIX"
                        viewModel.updateCosmetic("GRID", newGrid)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 8.dp, bottom = 8.dp)
                        .size(44.dp)
                        .background(activeTheme.surfaceBackgroundColor.copy(alpha = 0.9f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.GridOn,
                        contentDescription = "Toggle Grid",
                        tint = activeTheme.textPrimaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Floating Right Action: Lightbulb Hint Button with Count Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 8.dp, bottom = 8.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.requestHint() },
                        modifier = Modifier
                            .size(50.dp)
                            .background(activeTheme.arrowHighlightColor, CircleShape)
                            .testTag("hint_button")
                    ) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = "Hint",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    if (state.hintsAvailable > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                                .size(20.dp)
                                .background(Color(0xFFEF4444), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${state.hintsAvailable}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ==========================================
            // POWER-UP ACTION DOCK
            // ==========================================
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = activeTheme.surfaceBackgroundColor.copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Laser Vaporizer
                    val isLaserActive = state.isPowerupActive && state.selectedPowerUp == com.mitsara.arrowescape.model.PowerUpType.LASER_VAPORIZER
                    PowerUpDockButton(
                        title = "Laser Sweep",
                        icon = Icons.Default.FlashOn,
                        count = state.laserCharges,
                        isActive = isLaserActive,
                        activeColor = Color(0xFFFF3366),
                        onClick = { viewModel.activatePowerUp(com.mitsara.arrowescape.model.PowerUpType.LASER_VAPORIZER) }
                    )

                    // 2. Zen Shield
                    PowerUpDockButton(
                        title = "Zen Shield",
                        icon = Icons.Default.Security,
                        count = state.shieldCharges,
                        isActive = state.activeShieldTaps > 0,
                        activeColor = Color(0xFF0EA5E9),
                        onClick = { viewModel.activatePowerUp(com.mitsara.arrowescape.model.PowerUpType.ZEN_SHIELD) }
                    )

                    // 3. Sonar Burst
                    PowerUpDockButton(
                        title = "Sonar Burst",
                        icon = Icons.Default.NearMe,
                        count = state.magnetCharges,
                        isActive = false,
                        activeColor = Color(0xFF10B981),
                        onClick = { viewModel.activatePowerUp(com.mitsara.arrowescape.model.PowerUpType.SONAR_MAGNET) }
                    )
                }
            }

            // Bottom Controls Strip (Undo & Retry)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(activeTheme.surfaceBackgroundColor.copy(alpha = 0.95f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.undoMove() },
                        enabled = state.canUndo,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = activeTheme.textPrimaryColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Undo", color = activeTheme.textPrimaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Calming Center Mantra Badge
                    Text(
                        text = "TAP TO ESCAPE",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = activeTheme.textPrimaryColor.copy(alpha = 0.7f)
                    )

                    OutlinedButton(
                        onClick = { viewModel.retryLevel() },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Retry", color = activeTheme.textPrimaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
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
                coinsEarned = if (state.earnedCoins > 0) state.earnedCoins else (50 + state.level.id * 2 + stars * 15),
                diamondsEarned = if (state.earnedDiamonds > 0) state.earnedDiamonds else (if (state.level.id % 5 == 0) 5 else (2 + if (stars == 3) 1 else 0)),
                theme = activeTheme,
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

        // Floating Combo Banner Overlay
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
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        ),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Floating Shield Absorption Message Overlay
        if (state.shieldTriggeredMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 160.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                androidx.compose.material3.Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0284C7),
                    shadowElevation = 8.dp
                ) {
                    Text(
                        text = state.shieldTriggeredMessage!!,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
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

@Composable
private fun PowerUpDockButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isActive) activeColor.copy(alpha = 0.25f) else Color(0xFF1E293B).copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isActive) 2.dp else 1.dp,
            color = if (isActive) activeColor else Color(0xFF475569).copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isActive) activeColor else Color.White,
                modifier = Modifier.size(16.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = if (isActive) activeColor else Color.White
                )
                Text(
                    text = if (count > 0) "x$count" else "Empty",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 9.sp
                    ),
                    color = if (count > 0) Color(0xFF94A3B8) else Color(0xFFEF4444)
                )
            }
        }
    }
}

