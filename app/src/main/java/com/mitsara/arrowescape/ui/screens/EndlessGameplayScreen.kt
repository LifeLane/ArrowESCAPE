package com.mitsara.arrowescape.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mitsara.arrowescape.data.UserSettingsEntity
import com.mitsara.arrowescape.ui.components.LevelFailedDialog
import com.mitsara.arrowescape.ui.components.PuzzleBoardView
import com.mitsara.arrowescape.model.ThemeManager
import com.mitsara.arrowescape.ui.viewmodel.EndlessGameViewModel

@Composable
fun EndlessGameplayScreen(
    userSettings: UserSettingsEntity,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: EndlessGameViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val activeTheme = ThemeManager.getTheme(userSettings.selectedTheme)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(activeTheme.boardCanvasColor)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Back",
                        tint = activeTheme.textPrimaryColor
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "WAVE ${state.wave}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = activeTheme.textPrimaryColor
                    )
                    Text(
                        text = "SCORE: ${state.score}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = activeTheme.arrowHighlightColor
                    )
                }

                IconButton(onClick = onSettingsClick, modifier = Modifier.testTag("settings_button").size(36.dp)) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = activeTheme.textPrimaryColor, modifier = Modifier.size(20.dp))
                }
            }

            // Game Board
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
                    inspectedArrowId = null,
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
            }

            // Bottom Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(activeTheme.surfaceBackgroundColor.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, contentDescription = "Lives", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "x${state.remainingLives}", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = activeTheme.textPrimaryColor)
                }
                
                Button(
                    onClick = { viewModel.requestHint() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = activeTheme.arrowHighlightColor),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hint (${state.hintsAvailable})", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // Game Over Dialog
        if (state.isGameOver) {
            LevelFailedDialog(
                levelNumber = state.wave,
                onRetry = { viewModel.retryEndless() },
                onWatchAdForReward = { /* Basic implementation, just retry for now */ viewModel.retryEndless() },
                onMainMenu = onBackClick
            )
        }
    }
}
