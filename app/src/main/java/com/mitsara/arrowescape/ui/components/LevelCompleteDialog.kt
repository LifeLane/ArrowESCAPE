package com.mitsara.arrowescape.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mitsara.arrowescape.engine.LevelTextEngine
import com.mitsara.arrowescape.model.Direction
import com.mitsara.arrowescape.model.PuzzleLevel
import com.mitsara.arrowescape.ui.theme.ArrowNavyDark
import com.mitsara.arrowescape.ui.theme.PrimaryBlue

@Composable
fun LevelCompleteDialog(
    level: PuzzleLevel,
    stars: Int,
    moveCount: Int,
    score: Int,
    elapsedSeconds: Int,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onMainMenu: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // Sky Blue Fullscreen Container matching Screenshot 5
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0088FF))
                .padding(24.dp)
                .testTag("level_complete_dialog"),
            contentAlignment = Alignment.Center
        ) {
            VictoryParticleOverlay(modifier = Modifier.fillMaxSize())

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Header Text Banner (Screenshot 5)
                Text(
                    text = "Score: $score | Time: ${elapsedSeconds}s",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Level Completed!",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = LevelTextEngine.getFunFactForLevel(level.id),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Center White Card with Vector Maze Graphics
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(1f)
                        .padding(bottom = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing vector puzzle graphic inside white card
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            
                            val gw = level.gridWidth
                            val gh = level.gridHeight
                            val cellW = w / gw
                            val cellH = h / gh
                            
                            // Grid dots
                            for (ix in 1 until gw) {
                                for (iy in 1 until gh) {
                                    drawCircle(
                                        color = Color(0xFFE2E8F0),
                                        radius = 2.dp.toPx(),
                                        center = Offset(ix * cellW, iy * cellH)
                                    )
                                }
                            }

                            // Draw the initial level arrows statically
                            for (arrow in level.arrows) {
                                val cx = arrow.startX * cellW + cellW / 2
                                val cy = arrow.startY * cellH + cellH / 2
                                
                                val path = Path()
                                val arrowSize = minOf(cellW, cellH) * 0.4f
                                
                                when (arrow.direction) {
                                    Direction.UP -> {
                                        path.moveTo(cx, cy + arrowSize)
                                        path.lineTo(cx, cy - arrowSize)
                                        path.moveTo(cx - arrowSize * 0.5f, cy)
                                        path.lineTo(cx, cy - arrowSize)
                                        path.lineTo(cx + arrowSize * 0.5f, cy)
                                    }
                                    Direction.DOWN -> {
                                        path.moveTo(cx, cy - arrowSize)
                                        path.lineTo(cx, cy + arrowSize)
                                        path.moveTo(cx - arrowSize * 0.5f, cy)
                                        path.lineTo(cx, cy + arrowSize)
                                        path.lineTo(cx + arrowSize * 0.5f, cy)
                                    }
                                    Direction.LEFT -> {
                                        path.moveTo(cx + arrowSize, cy)
                                        path.lineTo(cx - arrowSize, cy)
                                        path.moveTo(cx, cy - arrowSize * 0.5f)
                                        path.lineTo(cx - arrowSize, cy)
                                        path.lineTo(cx, cy + arrowSize * 0.5f)
                                    }
                                    Direction.RIGHT -> {
                                        path.moveTo(cx - arrowSize, cy)
                                        path.lineTo(cx + arrowSize, cy)
                                        path.moveTo(cx, cy - arrowSize * 0.5f)
                                        path.lineTo(cx + arrowSize, cy)
                                        path.lineTo(cx, cy + arrowSize * 0.5f)
                                    }
                                }
                                
                                drawPath(
                                    path = path,
                                    color = ArrowNavyDark,
                                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Large White Pill Button: "New Game" / "Next Level"
                Button(
                    onClick = onNextLevel,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF0088FF)
                    ),
                    shape = CircleShape,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(60.dp)
                        .testTag("next_level_button")
                ) {
                    Text(
                        text = "Next Level",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF0088FF)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Secondary Button: "Main"
                Text(
                    text = "Main",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White.copy(alpha = 0.95f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onMainMenu() }
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .testTag("main_menu_button")
                )
            }
        }
    }
}
