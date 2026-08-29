package com.mitsara.arrowescape.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitsara.arrowescape.ui.theme.GoldStar
import com.mitsara.arrowescape.ui.theme.HintGlowColor
import com.mitsara.arrowescape.ui.theme.PrimaryBlue
import com.mitsara.arrowescape.ui.theme.SurfaceCard
import com.mitsara.arrowescape.ui.theme.SurfaceLight
import com.mitsara.arrowescape.ui.theme.TextPrimary
import com.mitsara.arrowescape.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyChallengeScreen(
    onStartDailyPuzzle: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    var isSpeedrunMode by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Challenges & Speedrun", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
            )
        },
        containerColor = SurfaceLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Mode Switcher Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceCard, RoundedCornerShape(16.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { isSpeedrunMode = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isSpeedrunMode) PrimaryBlue else Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Daily Puzzle", color = if (!isSpeedrunMode) Color.White else TextSecondary, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { isSpeedrunMode = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSpeedrunMode) PrimaryBlue else Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Speedrun Time Trial", color = if (isSpeedrunMode) Color.White else TextSecondary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = SurfaceCard,
                    shadowElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (isSpeedrunMode) Icons.Default.PlayArrow else Icons.Default.DateRange,
                            contentDescription = null,
                            tint = if (isSpeedrunMode) Color(0xFFFF5722) else HintGlowColor,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isSpeedrunMode) "60-SECOND SPEEDRUN TRIAL" else "TODAY'S SPECIAL PUZZLE",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (isSpeedrunMode) 
                                "Race against the clock! Clear the grid within 60 seconds to earn the Elite Speedrunner Badge & 2x Star Multiplier."
                            else 
                                "Solve today's custom layout to keep your streak alive and unlock bonus hints!",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = GoldStar)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSpeedrunMode) "Reward: 2x Stars + Speed Badge" else "Reward: +3 Bonus Hints", 
                                fontWeight = FontWeight.Bold, 
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { onStartDailyPuzzle(if (isSpeedrunMode) 999 else 42) },
                colors = ButtonDefaults.buttonColors(containerColor = if (isSpeedrunMode) Color(0xFFFF5722) else PrimaryBlue),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSpeedrunMode) "START SPEEDRUN TRIAL" else "START DAILY PUZZLE",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
