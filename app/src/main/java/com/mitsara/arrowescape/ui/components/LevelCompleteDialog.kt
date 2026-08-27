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
import com.mitsara.arrowescape.ui.theme.ArrowNavyDark
import com.mitsara.arrowescape.ui.theme.PrimaryBlue

@Composable
fun LevelCompleteDialog(
    levelNumber: Int,
    stars: Int,
    moveCount: Int,
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Header Text Banner (Screenshot 5)
                Text(
                    text = "Train Your Brain",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
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

                Spacer(modifier = Modifier.height(28.dp))

                // Center White Card with Vector Maze Graphics (Screenshot 5)
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

                            // Grid dots
                            for (ix in 0..4) {
                                for (iy in 0..4) {
                                    drawCircle(
                                        color = Color(0xFFE2E8F0),
                                        radius = 3.dp.toPx(),
                                        center = Offset(w * (ix + 1) / 6f, h * (iy + 1) / 6f)
                                    )
                                }
                            }

                            // Sample celebratory maze path graphic
                            val path1 = Path().apply {
                                moveTo(w * 2 / 6f, h * 4 / 6f)
                                lineTo(w * 2 / 6f, h * 2 / 6f)
                                lineTo(w * 4 / 6f, h * 2 / 6f)
                            }
                            drawPath(
                                path = path1,
                                color = ArrowNavyDark,
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )

                            val path2 = Path().apply {
                                moveTo(w * 4 / 6f, h * 4 / 6f)
                                lineTo(w * 5 / 6f, h * 4 / 6f)
                            }
                            drawPath(
                                path = path2,
                                color = PrimaryBlue,
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Large White Pill Button: "New Game" / "Next Level" (Screenshot 5)
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
